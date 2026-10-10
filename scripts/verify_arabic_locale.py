#!/usr/bin/env python3
"""Audit reversible Arabic localisation and ensure original religious data remains untouched."""
import json
import re
from pathlib import Path

root=Path(__file__).resolve().parents[1]
source=root/'app/src/main/assets'
arabic=source/'ar'
java=(root/'app/src/main/java/app/alfatiha/tafsir/MainActivity.java').read_text('utf-8')
language=(root/'app/src/main/java/app/alfatiha/tafsir/CompassLanguage.java').read_text('utf-8')

assert 'app_language' in language and 'SharedPreferences' in language
assert 'LAYOUT_DIRECTION_RTL' in java
assert 'renderSettings(false)' in java
assert 'changeAppLanguage(CompassLanguage.AR)' in java
assert 'changeAppLanguage(CompassLanguage.RU)' in java
assert 'assets/ar' in java or '"ar/"+name' in java
assert 'private String sessionHeroPhrase=HERO_PHRASES[0]' in java
assert len(re.findall(r'UI\.put\(',language)) >= 200

m=re.search(r'private static final String\[\] HERO_PHRASES_AR\s*=\s*new String\[\]\s*\{(.*?)\};',java,re.S)
assert m,'Arabic hero phrase array missing'
assert len(re.findall(r'^\s*"[^"]+"',m.group(1),re.M))==27, 'Hero phrase count mismatch'

names=[p for p in source.glob('*.json') if p.name!='app_meta.json']
validated=0
for target in sorted(arabic.glob('*.json')):
    original=source/target.name
    assert original.is_file(),f'Arabic translation without Russian original: {target.name}'
    a=json.loads(original.read_text('utf-8'))
    b=json.loads(target.read_text('utf-8'))
    assert type(a) is type(b),f'Type differs: {target.name}'
    if isinstance(a,list):
        assert len(a)==len(b),f'Row count differs: {target.name}'
        for i,(src,dst) in enumerate(zip(a,b)):
            assert set(src.keys())==set(dst.keys()),f'Object keys differ in {target.name} item {i}'
            for key in ('id','num','number','correct','correctAnswer','correct_answers'):
                if key in src:
                    assert src[key]==dst[key],f'Critical ID/answer changed in {target.name} item {i}'
    else:
        assert set(a.keys())==set(b.keys()),f'Object keys differ in {target.name}'
    validated+=1

# Track partial content coverage explicitly; do not market this as a full
# translation until every content bundle has an Arabic counterpart and review.
ui_count=language.count("UI.put(")
print(f'Arabic UI vocabulary: {ui_count} entries')
print(f'Arabic lesson bundles: {validated}/{len(names)} main content JSON files')
print('Original Russian source bundles preserved; religious source verification is still required.')
