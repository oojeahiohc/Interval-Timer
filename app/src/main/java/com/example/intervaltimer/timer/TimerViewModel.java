// TimerViewModel.java: 타이머 상태, 화면 갱신, 알림음 관리
package com.example.intervaltimer.timer;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.intervaltimer.audio.AudioCuePlayer;

public class TimerViewModel extends AndroidViewModel {

    private static final long FRAME_DELAY_MS = 100L;

    private final MutableLiveData<TimerUiState> uiState =
            new MutableLiveData<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AudioCuePlayer audio;

    private TimerEngine engine;
    private boolean visible;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!visible || engine == null) return;

            // 실제 경과 시간을 기준으로 현재 구간을 갱신한다.
            engine.update();
            consumeEvents();
            publish();

            if (engine.getState() == TimerState.RUNNING) {
                handler.postDelayed(this, FRAME_DELAY_MS);
            }
        }
    };

    public TimerViewModel(@NonNull Application application) {
        super(application);
        audio = new AudioCuePlayer(application);
    }

    public LiveData<TimerUiState> getUiState() {
        return uiState;
    }

    public boolean isInitialized() {
        return engine != null;
    }

    public TimerState getCurrentState() {
        return engine == null ? TimerState.READY : engine.getState();
    }

    public void initializeDemo() {
        if (engine != null) return;

        engine = new TimerEngine(
                new ElapsedRealtimeClock(), 20, 10, 8);
        publish();
    }

    public void startByUser() {
        if (engine == null || engine.getState() != TimerState.READY) {
            return;
        }

        engine.start();
        consumeEvents();
        publish();

        if (visible) {
            handler.removeCallbacks(ticker);
            handler.post(ticker);
        }
    }

    public void onVisible() {
        visible = true;
        audio.setActive(true);

        handler.removeCallbacks(ticker);
        if (engine != null && engine.getState() == TimerState.RUNNING) {
            handler.post(ticker);
        } else {
            publish();
        }
    }

    public void onNotVisible(boolean changingConfigurations) {
        visible = false;
        handler.removeCallbacks(ticker);
        audio.setActive(false);

        // 화면 회전은 중단하지 않고 실제 백그라운드 진입만 일시정지한다.
        if (!changingConfigurations && engine != null
                && engine.getState() == TimerState.RUNNING) {
            engine.pause();
            engine.drainEvents();
            publish();
        }
    }

    public void pauseByUser() {
        if (engine == null) return;

        engine.pause();
        consumeEvents();
        publish();

        if (engine.getState() != TimerState.RUNNING) {
            handler.removeCallbacks(ticker);
        }
    }

    public void resumeByUser() {
        if (engine == null) return;

        engine.resume();
        publish();
        if (visible && engine.getState() == TimerState.RUNNING) {
            handler.removeCallbacks(ticker);
            handler.post(ticker);
        }
    }

    public void stopByUser() {
        handler.removeCallbacks(ticker);
        if (engine != null) {
            engine.stop();
        }
        audio.setActive(false);
        publish();
    }

    private void consumeEvents() {
        if (engine == null) return;

        // 큐에서 제거한 이벤트에만 소리를 연결한다.
        for (TimerEvent event : engine.drainEvents()) {
            audio.playEvent(event);
        }
    }

    private void publish() {
        if (engine == null) return;

        TimerState state = engine.getState();
        TimerPhase displayPhase = state == TimerState.READY
                ? TimerPhase.WORK : engine.getPhase();
        int displayRound = state == TimerState.READY
                ? 1 : engine.getCurrentRound();

        uiState.setValue(new TimerUiState(
                state,
                displayPhase,
                engine.getRemainingTimeText(),
                displayRound,
                engine.getTotalRounds()
        ));
    }

    @Override
    protected void onCleared() {
        handler.removeCallbacks(ticker);
        audio.release();
        super.onCleared();
    }
}