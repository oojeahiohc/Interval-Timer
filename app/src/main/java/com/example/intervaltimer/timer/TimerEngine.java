// TimerEngine.java: 경과 시간 기준 타이머와 단발성 전환 이벤트
package com.example.intervaltimer.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TimerEngine {

    private final Clock clock;
    private final long workDurationMs;
    private final long restDurationMs;
    private final int totalRounds;
    private final List<TimerEvent> pendingEvents = new ArrayList<>();

    private TimerState state = TimerState.READY;
    private TimerPhase phase;
    private int currentRound;
    private long deadlineMs;
    private long pausedRemainingMs;

    public TimerEngine(Clock clock, int workSeconds,
                       int restSeconds, int rounds) {
        if (clock == null) {
            throw new IllegalArgumentException("Clock is required");
        }
        if (workSeconds < 1 || workSeconds > 3600
                || restSeconds < 0 || restSeconds > 3600
                || rounds < 1 || rounds > 99) {
            throw new IllegalArgumentException("Invalid routine values");
        }

        this.clock = clock;
        this.workDurationMs = workSeconds * 1000L;
        this.restDurationMs = restSeconds * 1000L;
        this.totalRounds = rounds;
    }

    public void start() {
        // READY에서만 첫 WORK를 시작한다.
        if (state != TimerState.READY) {
            return;
        }

        state = TimerState.RUNNING;
        phase = TimerPhase.WORK;
        currentRound = 1;
        deadlineMs = clock.elapsedRealtime() + workDurationMs;
        pendingEvents.add(TimerEvent.WORK_STARTED);
    }

    public void update() {
        if (state != TimerState.RUNNING) {
            return;
        }

        long now = clock.elapsedRealtime();

        // 여러 구간이 지난 뒤 호출돼도 이전 종료 시각을 기준으로 처리한다.
        while (state == TimerState.RUNNING && now >= deadlineMs) {
            if (phase == TimerPhase.WORK) {
                if (currentRound == totalRounds) {
                    // 마지막 WORK 후에는 REST 없이 즉시 완료한다.
                    state = TimerState.COMPLETED;
                    phase = null;
                    deadlineMs = 0;
                    pendingEvents.add(TimerEvent.WORKOUT_COMPLETED);
                } else if (restDurationMs > 0) {
                    phase = TimerPhase.REST;
                    deadlineMs += restDurationMs;
                    pendingEvents.add(TimerEvent.REST_STARTED);
                } else {
                    // 휴식이 0초면 REST 이벤트 없이 다음 WORK를 시작한다.
                    currentRound++;
                    phase = TimerPhase.WORK;
                    deadlineMs += workDurationMs;
                    pendingEvents.add(TimerEvent.WORK_STARTED);
                }
            } else {
                // REST에는 방금 끝난 세트를 표시하고 다음 WORK 직전에 증가한다.
                currentRound++;
                phase = TimerPhase.WORK;
                deadlineMs += workDurationMs;
                pendingEvents.add(TimerEvent.WORK_STARTED);
            }
        }
    }

    public void pause() {
        if (state != TimerState.RUNNING) {
            return;
        }

        // 종료 경계에서 멈추면 해당 구간 전환을 먼저 확정한다.
        update();
        if (state != TimerState.RUNNING) {
            return;
        }

        pausedRemainingMs = Math.max(0L,
                deadlineMs - clock.elapsedRealtime());
        state = TimerState.PAUSED;
    }

    public void resume() {
        if (state != TimerState.PAUSED) {
            return;
        }

        // 저장된 남은 시간으로 새로운 종료 시각을 만든다.
        deadlineMs = clock.elapsedRealtime() + pausedRemainingMs;
        state = TimerState.RUNNING;
    }

    public void stop() {
        if (state != TimerState.RUNNING && state != TimerState.PAUSED) {
            return;
        }

        state = TimerState.STOPPED;
        phase = null;
        deadlineMs = 0;
        pausedRemainingMs = 0;

        // 종료 후 이전 구간 소리가 재생되지 않도록 미소비 이벤트를 폐기한다.
        pendingEvents.clear();
    }

    public TimerState getState() {
        return state;
    }

    public TimerPhase getPhase() {
        return phase;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public long getRemainingMillis() {
        if (state == TimerState.READY) {
            return workDurationMs;
        }
        if (state == TimerState.PAUSED) {
            return pausedRemainingMs;
        }
        if (state == TimerState.RUNNING) {
            return Math.max(0L, deadlineMs - clock.elapsedRealtime());
        }
        return 0L;
    }

    public long getRemainingSecondsCeil() {
        long milliseconds = getRemainingMillis();

        // 실제 종료 전에 00:00이 나오지 않도록 올림 계산한다.
        return (milliseconds + 999L) / 1000L;
    }

    public String getRemainingTimeText() {
        long seconds = getRemainingSecondsCeil();
        return String.format(Locale.US, "%02d:%02d",
                seconds / 60, seconds % 60);
    }

    public List<TimerEvent> drainEvents() {
        // 소비한 이벤트는 큐에서 제거한다. 상태 재조회는 이벤트를 만들지 않는다.
        List<TimerEvent> events = new ArrayList<>(pendingEvents);
        pendingEvents.clear();
        return events;
    }
}