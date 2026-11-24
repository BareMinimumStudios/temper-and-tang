"""Validate every shipped model, texture reference, face UV and display transform."""
from pathlib import Path
import json, math, struct

root=Path(__file__).resolve().parents[1]
assets=root/'src/main/resources/assets/temperandtang'
models={str(p.relative_to(assets/'models')).replace('\\','/').removesuffix('.json'):json.loads(p.read_text()) for p in (assets/'models').rglob('*.json')}
report=[]
def own(identifier,folder,suffix):
    if identifier.startswith('temperandtang:'):
        p=assets/folder/(identifier.split(':',1)[1]+suffix)
        assert p.is_file(),f'Missing {folder} reference: {identifier}'
def textures(name,seen=()):
    assert name not in seen,f'Parent cycle: {name}'
    m=models[name]; result={}
    parent=m.get('parent','')
    if parent.startswith('temperandtang:'):result.update(textures(parent.split(':',1)[1],seen+(name,)))
    result.update(m.get('textures',{}));return result
for name,m in models.items():
    own(m.get('parent',''),'models','.json')
    tex=textures(name)
    def resolve(value,seen=()):
        if value.startswith('#'):
            key=value[1:];assert key in tex,f'{name}: unbound texture {key}'
            assert key not in seen,f'{name}: texture alias cycle'
            return resolve(tex[key],seen+(key,))
        own(value,'textures','.png');return value
    for value in tex.values():resolve(value)
    bounds=[]
    for i,e in enumerate(m.get('elements',[])):
        lo=e['from'];hi=e['to'];assert len(lo)==len(hi)==3
        assert all(math.isfinite(v) and -16<=v<=32 for v in lo+hi),(name,i,'invalid element bounds')
        assert all(x<=y for x,y in zip(lo,hi)),(name,i,'inverted element')
        bounds.extend([lo,hi])
        rot=e.get('rotation',{})
        assert rot.get('angle',0) in (-45,-22.5,0,22.5,45),(name,i,'illegal element rotation')
        assert not rot or rot['axis'] in 'xyz'
        for direction,f in e['faces'].items():
            assert direction in ('up','down','north','south','east','west')
            resolve(f['texture'])
            uv=f.get('uv')
            assert uv is None or len(uv)==4 and all(math.isfinite(v) and 0<=v<=16 for v in uv),(name,i,'UV outside texture')
            assert f.get('rotation',0) in (0,90,180,270),(name,i,'illegal UV rotation')
    for context,display in m.get('display',{}).items():
        for key,values in display.items():
            assert len(values)==3 and all(math.isfinite(v) for v in values),(name,context,key)
        assert all(0<v<=4 for v in display.get('scale',[1,1,1])),(name,context,'invalid display scale')
    report.append({'model':name,'elements':len(m.get('elements',[])),'bounds':[[min(p[i] for p in bounds) for i in range(3)],[max(p[i] for p in bounds) for i in range(3)]] if bounds else None})
for p in (assets/'textures').rglob('*.png'):
    data=p.read_bytes();assert data[:8]==b'\x89PNG\r\n\x1a\n',p
    w,h=struct.unpack('>II',data[16:24]);assert w>0 and h>0,p
    meta=p.with_suffix('.png.mcmeta')
    if h>w:assert meta.exists(),f'Animated texture missing metadata: {p}'
out=root/'build/reports/model-audit.json';out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(report,indent=2))
print(f'Validated all {len(models)} models: parents, textures, UVs, element dimensions/rotations, and display sizing. Report: {out}')

visual_fields={'model','mesh','texture','item','block','anchor','tint','translucent','emissive','double_sided','glass','lit_as_block'}
def scene_nodes(nodes):
    for node in nodes:
        yield node
        yield from scene_nodes(node.get('children',[]))
for p in (assets/'diegesis/scenes').glob('*.json'):
    scene=json.loads(p.read_text())
    for node in scene_nodes(scene['nodes']):
        visual=node.get('visual',{})
        assert not set(visual)-visual_fields,(p.name,node['id'],'unknown visual fields',set(visual)-visual_fields)
        if 'model' in visual:
            own(visual['model'],'models','.json')
            if visual['model'].startswith('temperandtang:'):
                assert visual['model'].startswith('temperandtang:diegesis/'),(p.name,node['id'],'Scene model needs an auto-loaded diegesis alias or explicit model registration')
print('Validated every station visual schema and model reference.')

# Duplicate IDs make Diegesis reject an entire scene even when all references resolve.
for path in (assets/'diegesis/scenes').glob('*.json'):
    nodes=list(scene_nodes(json.loads(path.read_text())['nodes']))
    ids=[n['id']for n in nodes]
    assert len(ids)==len(set(ids)),f'Duplicate scene node ids: {path}'
print('Validated unique node identities in all station scenes.')
