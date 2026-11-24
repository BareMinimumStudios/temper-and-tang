"""Regenerate shared shaping-model color variants; no extra equipment or recipes are registered."""
from pathlib import Path
import json, struct, zlib
ROOT=Path(__file__).resolve().parents[1]/'src/main/resources/assets/temperandtang'
# Middle metal shades from the inventory palettes. Neutral is the integration fallback.
PALETTES={'neutral':(174,174,168),'iron':(186,185,174),'bronze':(184,127,58),'steel':(110,125,133),'gold':(232,191,79),'copper':(209,126,89),'tin':(200,210,207),'silver':(203,210,220),'diamond':(74,199,199),'netherite':(85,73,81)}
FORMS=('shaping_billet','drawn_stock','blade_blank','armor_plate','helmet_blank','chestplate_blank','leggings_blank','boots_blank','shield_blank','crossbow_blank','pickaxe_blank','axe_blank','shovel_blank','hoe_blank','scythe_blank','spear_blank','hammer_blank','refining_billet')
def chunk(kind,data):return struct.pack('>I',len(data))+kind+data+struct.pack('>I',zlib.crc32(kind+data)&0xffffffff)
def swatch(color):
 rows=bytearray()
 for y in range(16):
  rows.append(0)
  for x in range(16):
   grain=(-5,-2,0,2,4)[(x*7+y*11+(x*y)%3)%5]
   rows.extend((*[max(0,min(255,c+grain))for c in color],255))
 return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(rows))+chunk(b'IEND',b'')
if __name__=='__main__':
 for metal,color in PALETTES.items():
  path=ROOT/f'textures/block/workpiece/{metal}.png';path.parent.mkdir(parents=True,exist_ok=True);path.write_bytes(swatch(color))
  for form in FORMS:
   if not(ROOT/f'models/diegesis/{form}.json').exists():continue
   path=ROOT/f'models/diegesis/materials/{metal}/{form}.json';path.parent.mkdir(parents=True,exist_ok=True)
   path.write_text(json.dumps({'parent':'temperandtang:diegesis/'+form,'textures':{key:'temperandtang:block/workpiece/'+metal for key in ('metal','face','particle')}},indent=2)+'\n')
 print('Generated workpiece palettes. Geometry and non-metal textures are inherited; crafting remains recipe-controlled.')
