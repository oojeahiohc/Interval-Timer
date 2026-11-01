// AudioCuePlayer.java: 운동, 휴식, 완료 단발성 알림음
package com.example.intervaltimer.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.intervaltimer.R;
import com.example.intervaltimer.timer.TimerEvent;

import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AudioCuePlayer {

    private static final String TAG = "IntervalTimerAudio";

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<TimerEvent, Integer> samples =
            new EnumMap<>(TimerEvent.class);
    private final Set<Integer> finishedLoads = new HashSet<>();
    private final Set<Integer> successfulLoads = new HashSet<>();
    private final ArrayDeque<TimerEvent> pending = new ArrayDeque<>();

    private SoundPool soundPool;
    private boolean active;
    private boolean released;
    private int currentStream;

    public AudioCuePlayer(Context context) {
        try {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(attributes)
                    .build();

            // 리스너를 먼저 등록해 빠른 로딩 완료도 처리한다.
            soundPool.setOnLoadCompleteListener((pool, soundId, status) ->
                    mainHandler.post(() -> {
                        if (released) return;

                        finishedLoads.add(soundId);
                        if (status == 0) {
                            successfulLoads.add(soundId);
                        } else {
                            Log.w(TAG, "Failed to load sample: " + soundId);
                        }
                        flushPending();
                    })
            );

            samples.put(TimerEvent.WORK_STARTED,
                    soundPool.load(context, R.raw.work_start, 1));
            samples.put(TimerEvent.REST_STARTED,
                    soundPool.load(context, R.raw.rest_start, 1));
            samples.put(TimerEvent.WORKOUT_COMPLETED,
                    soundPool.load(context, R.raw.workout_complete, 1));
        } catch (RuntimeException error) {
            Log.w(TAG, "Audio initialization failed", error);
            if (soundPool != null) {
                soundPool.release();
                soundPool = null;
            }
        }
    }

    public void setActive(boolean shouldPlay) {
        if (released) return;

        active = shouldPlay;
        if (!active) {
            // 화면이 보이지 않는 동안 대기 소리를 폐기하고 재생을 멈춘다.
            pending.clear();
            if (soundPool != null && currentStream != 0) {
                soundPool.stop(currentStream);
                currentStream = 0;
            }
            return;
        }

        flushPending();
    }

    public void playEvent(TimerEvent event) {
        if (released || soundPool == null) return;

        // 아직 로드되지 않은 첫 운동음도 한 번만 대기한다.
        pending.addLast(event);
        flushPending();
    }

    private void flushPending() {
        if (!active || released || soundPool == null) return;

        while (!pending.isEmpty()) {
            TimerEvent event = pending.peekFirst();
            Integer soundId = samples.get(event);

            if (soundId == null || soundId == 0) {
                pending.removeFirst();
                continue;
            }
            if (!finishedLoads.contains(soundId)) {
                // 로딩 완료를 확인하기 전에는 재생하지 않는다.
                return;
            }

            pending.removeFirst();
            if (!successfulLoads.contains(soundId)) {
                continue;
            }

            try {
                currentStream = soundPool.play(soundId,
                        1.0f, 1.0f, 1, 0, 1.0f);
            } catch (RuntimeException error) {
                Log.w(TAG, "Audio playback failed", error);
            }
        }
    }

    public void release() {
        if (released) return;

        setActive(false);
        released = true;
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}