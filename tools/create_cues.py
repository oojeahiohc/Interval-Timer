# tools/create_cues.py: 앱에 포함할 짧은 원본 WAV 알림음 생성
from pathlib import Path
import math
import struct
import wave

SAMPLE_RATE = 22050
OUTPUT = Path("app/src/main/res/raw")
OUTPUT.mkdir(parents=True, exist_ok=True)


def make_tone(frequency, duration):
    samples = []
    count = round(SAMPLE_RATE * duration)
    fade = max(1, round(SAMPLE_RATE * 0.008))

    for i in range(count):
        envelope = min(1.0, i / fade, (count - 1 - i) / fade)
        value = int(12000 * max(0.0, envelope) *
                    math.sin(2 * math.pi * frequency * i / SAMPLE_RATE))
        samples.append(value)

    return samples


def save_wave(name, pitches):
    samples = []
    for index, frequency in enumerate(pitches):
        if index:
            samples.extend([0] * round(SAMPLE_RATE * 0.06))
        samples.extend(make_tone(frequency, 0.14))

    with wave.open(str(OUTPUT / name), "wb") as audio:
        audio.setnchannels(1)
        audio.setsampwidth(2)
        audio.setframerate(SAMPLE_RATE)
        audio.writeframes(struct.pack("<" + "h" * len(samples), *samples))


save_wave("work_start.wav", [660, 880])
save_wave("rest_start.wav", [420])
save_wave("workout_complete.wav", [523, 659, 784])