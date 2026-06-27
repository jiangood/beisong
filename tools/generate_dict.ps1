param(
    [string]$outputFile = "app/src/main/assets/chinese_dict.json"
)

$ProgressPreference = 'SilentlyContinue'

# Download CC-CEDICT
$url = "https://www.mdbg.net/chinese/export/cedict/cedict_1_0_ts_utf-8_mdbg.txt.gz"
$gzPath = "$env:TEMP\cedict.txt.gz"
Write-Host "Downloading CC-CEDICT..."
Invoke-WebRequest -Uri $url -OutFile $gzPath

Write-Host "Decompressing..."
Add-Type -AssemblyName System.IO.Compression.FileSystem
$txtPath = "$env:TEMP\cedict.txt"
$stream = [System.IO.File]::OpenRead($gzPath)
$gzip = New-Object System.IO.Compression.GZipStream($stream, [System.IO.Compression.CompressionMode]::Decompress)
$reader = New-Object System.IO.StreamReader($gzip)
$content = $reader.ReadToEnd()
$reader.Close()
$gzip.Close()
$stream.Close()

Write-Host "Parsing..."
$charDict = @{}
$lines = $content -split "`n"

foreach ($line in $lines) {
    if ($line -eq "" -or $line.StartsWith("#")) { continue }
    
    # Format: Traditional Simplified [pinyin] /meaning/
    $parts = $line -split ' ', 3
    if ($parts.Length -lt 3) { continue }
    
    $simplified = $parts[1]
    $rest = $parts[2]
    
    # Extract pinyin
    $pinyinMatch = [regex]::Match($rest, '\[(.+?)\]')
    if (!$pinyinMatch.Success) { continue }
    $pinyin = $pinyinMatch.Groups[1].Value
    
    # Extract meaning
    $meaningMatch = [regex]::Match($rest, '/(.+?)/')
    $meaning = ""
    if ($meaningMatch.Success) {
        $meaning = $meaningMatch.Groups[1].Value
    }
    
    # Only process single character entries
    if ($simplified.Length -eq 1 -and [char]::IsLetterOrDigit($simplified[0]) -and $simplified[0] -match '[\x{4e00}-\x{9fff}]') {
        $char = $simplified
        $pinyinDisplay = ($pinyin -replace '\d', '').Trim()
        $toneNumbers = [regex]::Matches($pinyin, '\d')
        
        # Convert tone numbers to tone marks
        $vowels = @{
            'a1'='ā'; 'a2'='á'; 'a3'='ǎ'; 'a4'='à'; 'a5'='a'
            'e1'='ē'; 'e2'='é'; 'e3'='ě'; 'e4'='è'; 'e5'='e'
            'i1'='ī'; 'i2'='í'; 'i3'='ǐ'; 'i4'='ì'; 'i5'='i'
            'o1'='ō'; 'o2'='ó'; 'o3'='ǒ'; 'o4'='ò'; 'o5'='o'
            'u1'='ū'; 'u2'='ú'; 'u3'='ǔ'; 'u4'='ù'; 'u5'='u'
            'v1'='ǖ'; 'v2'='ǘ'; 'v3'='ǚ'; 'v4'='ǜ'; 'v5'='v'
        }
        
        if (!$charDict.ContainsKey($char)) {
            $charDict[$char] = @{
                p = New-Object System.Collections.ArrayList
                m = New-Object System.Collections.ArrayList
            }
        }
        
        $pinyinToned = $pinyinDisplay
        foreach ($kv in $vowels.GetEnumerator()) {
            $pinyinToned = $pinyinToned -replace $kv.Key, $kv.Value
        }
        $pinyinToned = $pinyinToned -replace '\d', ''
        
        if (!$charDict[$char].p.Contains($pinyinToned)) {
            $charDict[$char].p.Add($pinyinToned) | Out-Null
        }
        if ($meaning -ne "" -and !$charDict[$char].m.Contains($meaning)) {
            $charDict[$char].m.Add($meaning) | Out-Null
        }
    }
}

# Also process multi-character words to get pinyin for individual chars
foreach ($line in $lines) {
    if ($line -eq "" -or $line.StartsWith("#")) { continue }
    $parts = $line -split ' ', 3
    if ($parts.Length -lt 3) { continue }
    
    $simplified = $parts[1]
    if ($simplified.Length -le 1) { continue }
    
    $rest = $parts[2]
    $pinyinMatch = [regex]::Match($rest, '\[(.+?)\]')
    if (!$pinyinMatch.Success) { continue }
    $pinyinStr = $pinyinMatch.Groups[1].Value
    
    $pinyins = $pinyinStr -split ' '
    if ($pinyins.Length -ne $simplified.Length) { continue }
    
    for ($i = 0; $i -lt $simplified.Length; $i++) {
        $char = $simplified[$i]
        if ($char -match '[\x{4e00}-\x{9fff}]') {
            $py = $pinyins[$i] -replace '\d', ''
            
            # Add tone marks
            $pinyinToned = $py
            foreach ($kv in $vowels.GetEnumerator()) {
                $pinyinToned = $pinyinToned -replace $kv.Key, $kv.Value
            }
            $pinyinToned = $pinyinToned -replace '\d', ''
            
            if (!$charDict.ContainsKey($char)) {
                $charDict[$char] = @{
                    p = New-Object System.Collections.ArrayList
                    m = New-Object System.Collections.ArrayList
                }
            }
            if (!$charDict[$char].p.Contains($pinyinToned)) {
                $charDict[$char].p.Add($pinyinToned) | Out-Null
            }
        }
    }
}

Write-Host "Building JSON..."
$result = @{}
foreach ($char in $charDict.Keys | Sort-Object) {
    $entry = $charDict[$char]
    $pinyins = @($entry.p | Sort-Object)
    $meanings = @($entry.m | Select-Object -First 3)
    
    $result[$char] = @{
        p = $pinyins -join ','
        m = if ($meanings.Count -gt 0) { ($meanings | Select-Object -First 1) } else { "" }
    }
}

$json = $result | ConvertTo-Json -Compress -Depth 2
$projectDir = Split-Path -Parent $PSScriptRoot
$fullPath = Join-Path $projectDir $outputFile
$json | Set-Content -Path $fullPath -Encoding UTF8

Write-Host "Done! Generated $($charDict.Count) entries -> $fullPath"
Write-Host "File size: $((Get-Item $fullPath).Length / 1KB) KB"
