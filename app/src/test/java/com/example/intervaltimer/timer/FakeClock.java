// FakeClock.java: 단위 테스트용 가상 경과 시간
package com.example.intervaltimer.timer;

public class FakeClock implements Clock {

    private long nowMs;

    @Override
    public long elapsedRealtime() {
        return nowMs;
    }

    public void advance(long milliseconds) {
        if (milliseconds < 0) {
            throw new IllegalArgumentException("Time cannot go backwards");
        }
        nowMs += milliseconds;
    }
}