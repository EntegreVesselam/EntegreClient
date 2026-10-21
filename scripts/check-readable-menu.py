#!/usr/bin/env python3
"""Geometry and native-action regression checks; no Minecraft launch required."""
import hashlib
import json
import math
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parent.parent
SOURCE = (ROOT / 'config/fancymenu/customization/entegre_title_screen.txt').read_text()
BLOCKS = re.findall(r'(?m)^(element|vanilla_button) \{\n(.*?)^\}', SOURCE, re.S)
ELEMENTS = {}
BASELINE = json.loads((ROOT / 'tests/readable-menu/baseline-native.json').read_text())
for kind, block in BLOCKS:
    fields = dict(re.findall(r'^  ([\w]+) = (.*)$', block, re.M))
    name = fields['instance_identifier']
    ELEMENTS[name] = fields
    if kind == 'vanilla_button':
        canonical = re.sub(r'^  advanced_(posx|width) = .*\n', '', block, flags=re.M)
        assert hashlib.sha256(canonical.encode()).hexdigest() == BASELINE[name], name + ': native actions/labels/hide guards changed'
assert 'statistics' not in SOURCE
assert 'slideshow_name = community-memories' in SOURCE
assert 'source = Entegre Client 2.4.7 | 26.3' in SOURCE


def value(fields, key, width, height):
    expression = re.search(r'"expression":"(.*)","decimal"', fields['advanced_' + key])[1]
    expression = expression.replace('{"placeholder":"guiwidth"}', str(width)).replace('{"placeholder":"guiheight"}', str(height))
    assert re.fullmatch(r'[0-9+*/(). abs-]+', expression), expression
    return int(eval(expression, {'__builtins__': {}, 'abs': abs}))


def geometry(width, height):
    return {name: dict(zip(('x', 'y', 'w', 'h'), (value(fields, key, width, height) for key in ('posx', 'posy', 'width', 'height'))))
            for name, fields in ELEMENTS.items() if 'advanced_posx' in fields}


def bottom(box): return box['y'] + box['h']


def right(box): return box['x'] + box['w']


SIZES = [(320, 240), (360, 240), (427, 240), (480, 270), (640, 360), (854, 480), (1280, 720)]
for width, height in SIZES:
    boxes = geometry(width, height)
    for name, box in boxes.items():
        assert 0 <= box['x'] < right(box) <= width and 0 <= box['y'] < bottom(box) <= height, (width, height, name, box)
        fields = ELEMENTS[name]
        if fields.get('element_type') == 'text_v2':
            assert box['h'] >= math.ceil(9 * float(fields['scale'])) + 6, name + ': native inner clip'
    left, news, events = (boxes['community-' + suffix + '-panel'] for suffix in ('play', 'news', 'events'))
    assert news['x'] - right(left) >= 23 and bottom(news) + 10 <= events['y'], 'Panel gutter'
    for group, children in [('play', ['logo','entegre','vesselam','quote','links','wallpapers']), ('news',['news-title','news-feed','all-news']), ('events',['events-title','events-feed','all-events'])]:
        panel=boxes['community-'+group+'-panel']
        for child in children:
            box=boxes['community-'+child]
            assert panel['x'] <= box['x'] and right(box) <= right(panel) and panel['y'] <= box['y'] and bottom(box) <= bottom(panel), (group,child)
    for first, second in [('entegre','vesselam'),('vesselam','quote'),('links','wallpapers'),('news-title','news-feed'),('news-feed','all-news'),('events-title','events-feed'),('events-feed','all-events')]:
        assert bottom(boxes['community-'+first]) <= boxes['community-'+second]['y'], (first,second)
    assert boxes['community-news-feed']['h'] >= 60
    for name in ['community-news-feed','community-events-feed']:
        assert ELEMENTS[name]['enable_scrolling']=='true' and ELEMENTS[name]['line_spacing']=='2'
    if width >= 640:
        assert news['w']==320 and left['w']==220
        assert boxes['community-news-feed']['h']-6 >= 5*9+4*2, 'Five native lines need 53 inner GUI pixels'
        assert boxes['community-events-feed']['h']-6 >= 3*9+2*2, 'Three native lines need 31 inner GUI pixels'
print('PASS readable menu: 7 GUI geometries; native actions/labels/hide guards preserved; 5/3-line inner clip; narrow GUI scroll fallback')
(ROOT/'tests/readable-menu/geometry.json').write_text(json.dumps({f'{w}x{h}':geometry(w,h) for w,h in SIZES},indent=2)+'\n')
