// TimerUiState.java: 타이머 화면에 표시할 상태 스냅샷
package com.example.intervaltimer.timer;

public class TimerUiState {

    public final TimerState state;
    public final TimerPhase phase;
    public final String remainingText;
    public final int currentRound;
    public final int totalRounds;

    public TimerUiState(TimerState state, TimerPhase phase,
                        String remainingText, int currentRound,
                        int totalRounds) {
        this.state = state;
        this.phase = phase;
        this.remainingText = remainingText;
        this.currentRound = currentRound;
        this.totalRounds = totalRounds;
    }
}