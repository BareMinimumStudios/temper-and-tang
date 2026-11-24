"""Adapt the owner-supplied Blossom Studios set; requires Pillow and the original ZIP."""
from pathlib import Path
import argparse, io, zipfile, json
from PIL import Image, ImageDraw
parser=argparse.ArgumentParser();parser.add_argument('source',type=Path);parser.add_argument('--preview',type=Path);args=parser.parse_args()
root=Path(__file__).resolve().parents[1];assets=root/'src/main/resources/assets/temperandtang'
ramps={
 'tin':[(53,61,64),(98,109,113),(139,150,153),(177,188,190),(211,221,221),(246,247,242)],
 'bronze':[(61,40,25),(103,65,32),(148,94,43),(184,127,58),(218,168,90),(244,212,145)],
 'steel':[(28,33,39),(53,62,71),(79,92,102),(110,125,133),(147,160,165),(187,198,199)],
 'silver':[(45,52,67),(81,96,120),(127,147,176),(173,191,214),(212,225,239),(244,248,255)],
 'iron':[(49,47,43),(91,88,79),(132,129,118),(169,167,156),(205,203,193),(239,238,227)]}
levels=[58,113,147,183,220,255]
def recolor(im,metal,ore=False):
 im=im.convert('RGBA');out=im.copy()
 if metal=='tin':return out
 for y in range(im.height):
  for x in range(im.width):
   r,g,b,a=im.getpixel((x,y))
   if not a:out.putpixel((x,y),(0,0,0,0));continue
   # Preserve rock, timber and leather. Source metal is cool green-grey; highlights are neutral.
   if ore and max(r,g,b)-min(r,g,b)<5:continue
   if r>g*1.18 and r>b*1.25:continue
   lum=.2126*r+.7152*g+.0722*b
   i=min(range(len(levels)),key=lambda n:abs(levels[n]-lum))
   out.putpixel((x,y),(*ramps[metal][i],a))
 return out
outputs=[]
def save(im,folder,name):
 p=assets/'textures'/folder/(name+'.png');p.parent.mkdir(parents=True,exist_ok=True);im.save(p);outputs.append((folder+'/'+name,im.copy()))
def model(folder,name,parent,texture):
 p=assets/'models'/folder/(name+'.json');p.write_text(json.dumps({'parent':parent,'textures':{'all' if folder=='block' else 'layer0':'temperandtang:'+texture}},indent=2)+'\n')
with zipfile.ZipFile(args.source) as z:
 prefix='CraftEngine Configuration/CraftEngine/resources/blossom_studios/resourcepack/assets/blossom_studios/textures/'
 def read(name):return Image.open(io.BytesIO(z.read(prefix+name))).convert('RGBA')
 for metal in ('tin','silver'):
  for source,name in [('stone_platinum_ore',metal+'_ore'),('deepslate_platinum_ore','deepslate_'+metal+'_ore'),('platinum_raw_block','raw_'+metal+'_block')]:
   save(recolor(read('block/'+source+'.png'),metal,'ore' in source),'block',name);model('block',name,'minecraft:block/cube_all','block/'+name)
  name='raw_'+metal;save(recolor(read('item/raw_platinum_ore.png'),metal),'item',name);model('item',name,'minecraft:item/generated','item/'+name)
 for metal in ('tin','silver','bronze','steel'):
  save(recolor(read('block/platinum_block.png'),metal),'block',metal+'_block');model('block',metal+'_block','minecraft:block/cube_all','block/'+metal+'_block')
  for name in ('ingot','nugget'):
   item=metal+'_'+name;save(recolor(read('item/platinum_'+name+'.png'),metal),'item',item);model('item',item,'minecraft:item/generated','item/'+item)
  if metal=='silver':continue
  for name in ('sword','pickaxe','axe','shovel','hoe','helmet','chestplate','leggings','boots'):
   item=metal+'_'+name;save(recolor(read('item/platinum_'+name+'.png'),metal),'item',item);model('item',item,'minecraft:item/generated' if name in ('helmet','chestplate','leggings','boots') else 'minecraft:item/handheld','item/'+item)
  for layer in (1,2):save(recolor(read('armor/platinum/platinum_layer_'+str(layer)+'.png'),metal),'models/armor',metal+'_layer_'+str(layer))
 if args.preview:
  cols=8;cell=112;sheet=Image.new('RGB',(cols*cell,((len(outputs)+cols-1)//cols)*cell),(39,35,32));d=ImageDraw.Draw(sheet)
  for i,(name,im) in enumerate(outputs):
   x=i%cols*cell;y=i//cols*cell;im=im.resize((64,64) if im.size==(16,16) else (96,48),Image.Resampling.NEAREST);sheet.paste(im,(x+8,y+4),im);label=name.split('/')[-1];d.text((x+3,y+75),label[:18],fill=(235,226,208))
  args.preview.parent.mkdir(parents=True,exist_ok=True);sheet.save(args.preview)
print('Adapted',len(outputs),'textures; tin includes its original silver-white equipment set.')
