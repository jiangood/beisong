#!/usr/bin/env python3
"""Download CC-CEDICT and generate a character-level dictionary JSON."""

import json
import re
import urllib.request
import gzip
import os
from collections import defaultdict

OUTPUT = os.path.join(os.path.dirname(os.path.dirname(__file__)),
                      "app", "src", "main", "assets", "chinese_dict.json")

# Download and decompress
url = "https://www.mdbg.net/chinese/export/cedict/cedict_1_0_ts_utf-8_mdbg.txt.gz"
print("Downloading CC-CEDICT...")
data = urllib.request.urlopen(url).read()
text = gzip.decompress(data).decode("utf-8")
print(f"Got {len(text)} chars")

# Tone number to tone mark mapping
TONE_MAP = {
    'a1': 'ā', 'a2': 'á', 'a3': 'ǎ', 'a4': 'à',
    'e1': 'ē', 'e2': 'é', 'e3': 'ě', 'e4': 'è',
    'i1': 'ī', 'i2': 'í', 'i3': 'ǐ', 'i4': 'ì',
    'o1': 'ō', 'o2': 'ó', 'o3': 'ǒ', 'o4': 'ò',
    'u1': 'ū', 'u2': 'ú', 'u3': 'ǔ', 'u4': 'ù',
    'ü1': 'ǖ', 'ü2': 'ǘ', 'ü3': 'ǚ', 'ü4': 'ǜ',
}

def tone_to_mark(py):
    """Convert 'hao3' to 'hǎo'."""
    result = py.lower()
    for src, dst in TONE_MAP.items():
        result = result.replace(src, dst)
    result = re.sub(r'[0-9]', '', result)
    return result

# Parse lines
char_data = defaultdict(lambda: {'p': set(), 'm': []})
line_count = 0

for line in text.splitlines():
    if not line or line.startswith('#'):
        continue
    line_count += 1
    
    # Format: Traditional Simplified [pinyin] /meaning/
    m = re.match(r'^\S+ (\S+) \[(.+?)\] /(.+?)/', line)
    if not m:
        continue
    
    simplified = m.group(1)
    pinyin_str = m.group(2)
    meaning = m.group(3)[:120]  # truncate long meanings
    
    # Skip non-Chinese
    if not all('\u4e00' <= c <= '\u9fff' for c in simplified):
        continue
    
    pinyins = pinyin_str.split()
    
    if len(simplified) == 1:
        # Single character entry
        c = simplified
        py = tone_to_mark(pinyins[0])
        char_data[c]['p'].add(py)
        if meaning:
            char_data[c]['m'].append(meaning)
    
    elif len(simplified) == len(pinyins):
        # Multi-character word: extract per-character pinyin
        for c, py in zip(simplified, pinyins):
            if '\u4e00' <= c <= '\u9fff':
                char_data[c]['p'].add(tone_to_mark(py))

print(f"Parsed {line_count} entries, found {len(char_data)} unique characters")

# Build final dictionary
result = {}
for char in sorted(char_data.keys()):
    entry = char_data[char]
    pinyins = sorted(entry['p'])
    # Pick best meaning: prefer shortest single-char entry meaning
    best_meaning = ''
    for m in entry['m'][:10]:
        if len(best_meaning) == 0 or len(m) < len(best_meaning):
            best_meaning = m
    
    result[char] = {
        'p': ','.join(pinyins),
        'm': best_meaning[:100] if best_meaning else ''
    }

with open(OUTPUT, 'w', encoding='utf-8') as f:
    json.dump(result, f, ensure_ascii=False, separators=(',', ':'))

size_kb = os.path.getsize(OUTPUT) / 1024
print(f"Done! {len(result)} characters -> {OUTPUT} ({size_kb:.0f} KB)")
