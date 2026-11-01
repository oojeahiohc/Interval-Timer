// ElapsedRealtimeClock.java: Android의 단조 증가 경과 시간 사용
package com.example.intervaltimer.timer;

import android.os.SystemClock;

public class ElapsedRealtimeClock implements Clock {

    @Override
    public long elapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }
}