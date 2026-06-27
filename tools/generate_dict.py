#!/usr/bin/env python3
"""Generate Chinese character dictionary from mapull/chinese-dictionary."""

import json
import urllib.request
import os

OUTPUT = os.path.join(os.path.dirname(os.path.dirname(__file__)),
                      "app", "src", "main", "assets", "chinese_dict.json")

url = "https://raw.githubusercontent.com/mapull/chinese-dictionary/main/character/common/char_common_detail.json"
print("Downloading...")
data = urllib.request.urlopen(url, timeout=120).read()
text = data.decode("utf-8")

print(f"Downloaded {len(text)} chars, parsing {text.count('{')} objects...")
result = {}

# Parse each line as JSON
for line_no, line in enumerate(text.splitlines()):
    line = line.strip()
    if not line or line == '[' or line == ']':
        continue
    # Remove trailing comma if present (NDJSON sometimes has trailing commas)
    if line.endswith(','):
        line = line[:-1]
    
    try:
        entry = json.loads(line)
    except json.JSONDecodeError:
        continue
    
    c = entry.get("char", "")
    if not c or len(c) != 1:
        continue
    
    prons = entry.get("pronunciations", [])
    if not prons:
        continue
    
    pys = []
    meanings = []
    for p in prons:
        py = p.get("pinyin", "")
        if py:
            pys.append(py)
        for exp in p.get("explanations", []):
            ct = exp.get("content", "")
            if ct:
                # Remove trailing punctuation for cleaner display
                ct = ct[:80]
                meanings.append(ct)
    
    if not pys:
        continue
    
    # Pick best meaning (shortest substantive one)
    best = ""
    for m in meanings:
        if not best or (2 < len(m) < len(best)):
            best = m
    
    result[c] = {"p": ",".join(pys), "m": best}

with open(OUTPUT, "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, separators=(",", ":"))

kb = os.path.getsize(OUTPUT) / 1024
print(f"Done! {len(result)} chars -> {OUTPUT} ({kb:.0f} KB)")
