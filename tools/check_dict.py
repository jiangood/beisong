import json
with open('D:/ws/beisong/app/src/main/assets/chinese_dict.json', 'r', encoding='utf-8') as f:
    data = json.load(f)
for c in ['之', '其', '以', '也', '天', '道']:
    if c in data:
        entry = data[c]
        print(f"\n=== {c} ===")
        print(json.dumps(entry, ensure_ascii=False, indent=2)[:1500])
