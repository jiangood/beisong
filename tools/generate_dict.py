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

print(f"Downloaded {len(text)} chars, parsing...")
result = {}

for line_no, line in enumerate(text.splitlines()):
    line = line.strip()
    if not line or line == '[' or line == ']':
        continue
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

    readings = []
    for p in prons:
        py = p.get("pinyin", "")
        if not py:
            continue

        defs = []
        for exp in p.get("explanations", []):
            content = exp.get("content", "").strip()
            if content:
                details = []
                for d in exp.get("detail", []):
                    dt = d.get("text", "").strip()
                    bk = d.get("book", "").strip()
                    if dt:
                        details.append({"t": dt[:200], "b": bk[:40]})
                defs.append({"m": content[:200], "d": details})

        if defs:
            readings.append({"p": py, "defs": defs})

    if not readings:
        continue

    result[c] = {"r": readings}

with open(OUTPUT, "w", encoding="utf-8") as f:
    json.dump(result, f, ensure_ascii=False, separators=(",", ":"))

kb = os.path.getsize(OUTPUT) / 1024
print(f"Done! {len(result)} chars -> {OUTPUT} ({kb:.0f} KB)")
