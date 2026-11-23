"""Original modal steel-impact synthesis. Generated sounds use the project's license."""
from pathlib import Path
import math, random, struct, wave, subprocess, argparse

def generate(output,ffmpeg):
    output.mkdir(parents=True,exist_ok=True)
    for variant in range(4):
        randomizer=random.Random(7301+variant)
        rate=48000
        fundamental=1450+variant*91
        modes=[(.73,.15,.14),(1,.40,.24),(1.431,.24,.15),(2.137,.17,.095),(2.719,.11,.065),(3.872,.07,.045)]
        phase=[randomizer.uniform(-.1,.1) for _ in modes]
        samples=[];low=0.0
        for i in range(int(rate*.48)):
            t=i/rate
            modal=sum(amp*math.sin(2*math.pi*fundamental*ratio*t+offset)*math.exp(-t/decay) for (ratio,amp,decay),offset in zip(modes,phase))
            noise=randomizer.uniform(-1,1);low+=.15*(noise-low)
            attack=(noise-low)*.28*math.exp(-t/.009)
            envelope=min(1.0,t/.0007)*min(1.0,(.48-t)/.02)
            samples.append((modal+attack)*envelope)
        peak=max(abs(x) for x in samples)
        wav=output/f'forge_hit_{variant+1}.wav'
        with wave.open(str(wav),'wb') as f:
            f.setparams((1,2,rate,len(samples),'NONE','not compressed'))
            f.writeframes(b''.join(struct.pack('<h',round(x/peak*26000)) for x in samples))
        subprocess.run([str(ffmpeg),'-y','-hide_banner','-loglevel','error','-i',str(wav),'-c:a','libvorbis','-q:a','6',str(wav.with_suffix('.ogg'))],check=True)
        wav.unlink()

if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--ffmpeg',required=True);args=parser.parse_args()
    generate(Path(__file__).resolve().parents[1]/'src/main/resources/assets/temperandtang/sounds/forge',args.ffmpeg)
