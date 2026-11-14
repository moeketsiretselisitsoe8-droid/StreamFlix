"""Generates the small synthesised sound effects used by StreamFlix.
Owner: Lethako Khabele (Audio portion).
Run:  python3 tools/generate_sounds.py   (needs numpy)
All sounds are generated from maths, so there are no copyright problems.
"""
import wave, os
import numpy as np

SR = 44100
OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "sounds")
os.makedirs(OUT, exist_ok=True)

def t(sec): return np.linspace(0, sec, int(SR * sec), endpoint=False)

def env(n, attack=0.01, decay=6.0):
    x = np.arange(n) / SR
    return np.minimum(1, x / attack) * np.exp(-decay * x)

def sweep(f0, f1, sec):
    tt = t(sec)
    f = np.linspace(f0, f1, tt.size)
    return np.sin(2 * np.pi * np.cumsum(f) / SR)

def save(name, data, gain=0.8):
    data = data / (np.max(np.abs(data)) + 1e-9) * gain
    pcm = (data * 32767).astype("<i2")
    with wave.open(os.path.join(OUT, name), "wb") as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes(pcm.tobytes())
    print("wrote", name, round(len(pcm) / SR, 2), "s")

# hover: very short soft tick
s = np.sin(2 * np.pi * 1400 * t(0.05)); save("hover.wav", s * env(s.size, 0.002, 70), 0.5)
# click: quick falling blip
s = sweep(900, 450, 0.09); save("click.wav", s * env(s.size, 0.002, 28))
# open: rising whoosh + sparkle (opening a trailer)
a = sweep(250, 900, 0.40) * env(int(SR * .40), 0.05, 5)
b = np.sin(2 * np.pi * 1320 * t(0.40)) * env(int(SR * .40), 0.15, 9) * 0.4
save("open.wav", a + b)
# back: falling sweep
s = sweep(800, 260, 0.28); save("back.wav", s * env(s.size, 0.005, 9))
# scroll: filtered noise whoosh
rng = np.random.default_rng(7)
n = rng.standard_normal(int(SR * 0.30)); k = np.ones(40) / 40
n = np.convolve(n, k, mode="same")
x = np.arange(n.size) / SR
save("scroll.wav", n * np.sin(np.pi * x / 0.30) ** 2, 0.6)
# toggle: two quick tones
s = np.concatenate([np.sin(2*np.pi*660*t(0.07))*env(int(SR*.07),0.003,25),
                    np.sin(2*np.pi*990*t(0.09))*env(int(SR*.09),0.003,25)])
save("toggle.wav", s)
# startup: original two-hit "ta-dum" with a soft chord
def hit(freqs, sec, decay):
    tt = t(sec); w = sum(np.sin(2*np.pi*f*tt) for f in freqs) / len(freqs)
    return w * env(tt.size, 0.01, decay)
d1 = hit([110, 165, 220], 0.55, 5)
d2 = hit([164.8, 247.2, 329.6, 494.4], 1.6, 2.6)
out = np.zeros(int(SR * 2.2)); out[:d1.size] += d1; o = int(SR * 0.45); out[o:o+d2.size] += d2
save("startup.wav", out)
