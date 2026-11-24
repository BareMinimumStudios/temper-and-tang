"""Rebuild closed pixel-boundary item meshes from unchanged native 16px sprites. Requires Pillow."""
from pathlib import Path
import json,sys
from PIL import Image
r=Path(__file__).resolve().parents[1];assets=r/'src/main/resources/assets/temperandtang'
display={'thirdperson_righthand':{'rotation':[0,-90,55],'translation':[0,4,.5],'scale':[.85,.85,.85]},'thirdperson_lefthand':{'rotation':[0,90,-55],'translation':[0,4,.5],'scale':[.85,.85,.85]},'firstperson_righthand':{'rotation':[0,-90,25],'translation':[1.13,3.2,1.13],'scale':[.68,.68,.68]},'firstperson_lefthand':{'rotation':[0,90,-25],'translation':[1.13,3.2,1.13],'scale':[.68,.68,.68]},'ground':{'translation':[0,2,0],'scale':[.5,.5,.5]},'fixed':{'rotation':[0,180,0]},'gui':{'rotation':[0,0,0]}}
for name in ['tempering_compound','honing_compound','balance_weight','lightweight_grip','smithing_flux']:
 im=Image.open(assets/f'textures/item/{name}.png').convert('RGBA');assert im.size==(16,16)
 opaque=lambda x,y:0<=x<16 and 0<=y<16 and im.getpixel((x,y))[3]==255
 elements=[{'from':[0,0,7.5],'to':[16,16,8.5],'shade':False,'faces':{'north':{'uv':[16,0,0,16],'texture':'#layer0'},'south':{'uv':[0,0,16,16],'texture':'#layer0'}}}]
 edges=0
 for y in range(16):
  for x in range(16):
   if not opaque(x,y):continue
   faces={}
   for direction,dx,dy in [('west',-1,0),('east',1,0),('up',0,-1),('down',0,1)]:
    if not opaque(x+dx,y+dy):faces[direction]={'uv':[x+.49,y+.49,x+.51,y+.51],'texture':'#layer0'};edges+=1
   if faces:elements.append({'from':[x,15-y,7.5],'to':[x+1,16-y,8.5],'shade':False,'faces':faces})
 model={'parent':'minecraft:block/block','ambientocclusion':False,'gui_light':'front','textures':{'particle':f'temperandtang:item/{name}','layer0':f'temperandtang:item/{name}'},'display':display,'elements':elements}
 (assets/f'models/item/{name}.json').write_text(json.dumps(model,indent=2)+'\n');print(name,edges,'closed texel boundary faces')
