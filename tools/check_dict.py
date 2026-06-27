import json, sys
sys.stdout.reconfigure(encoding='utf-8')
with open('D:/ws/beisong/app/src/main/assets/chinese_dict.json', 'r', encoding='utf-8') as f:
    data = json.load(f)

chars = ['徼', '妙', '常', '有', '欲', '以', '观', '其', '玄', '眇']
for c in chars:
    if c in data:
        readings = data[c]['r']
        print(f"YES {c}: {len(readings)} readings")
    else:
        print(f"NO  {c}: not in dictionary")
