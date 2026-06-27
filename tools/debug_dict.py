#!/usr/bin/env python3
"""Debug: check file format of char_common_detail.json"""

import urllib.request

url = 'https://raw.githubusercontent.com/mapull/chinese-dictionary/main/character/common/char_common_detail.json'
data = urllib.request.urlopen(url, timeout=120).read()
text = data.decode('utf-8')

print(f'First 5 chars: {repr(text[:5])}')
print(f'Last 5 chars: {repr(text[-5:])}')
opens = text.count('{')
closes = text.count('}')
print(f'Open braces: {opens}, Close braces: {closes}')
print(f'Sample line 0 (first 300 chars): {repr(text.splitlines()[0][:300])}')
print(f'Sample line 1 (first 300 chars): {repr(text.splitlines()[1][:300])}')
