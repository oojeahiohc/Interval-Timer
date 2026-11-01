// TimerViewModel.java: Room 루틴을 READY로 준비하고 타이머·알림음을 관리
package com.example.intervaltimer.timer;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.intervaltimer.audio.AudioCuePlayer;
import com.example.intervaltimer.data.Routine;
import com.example.intervaltimer.data.RoutineRepository;

public class TimerViewModel extends AndroidViewModel {

    private static final long FRAME_DELAY_MS = 100L;

    private final MutableLiveData<TimerUiState> uiState =
            new MutableLiveData<>();
    private final MutableLiveData<Boolean> loadFailed =
            new MutableLiveData<>(false);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final AudioCuePlayer audio;
    private final RoutineRepository repository;

    private TimerEngine engine;
    private long requestedRoutineId = -1L;
    private boolean visible;
    private boolean cleared;

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            if (!visible || engine == null) return;

            // 구간 전환과 오디오 이벤트는 실제 시간에 맞춰 처리한다.
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
        repository = new RoutineRepository(application);
        audio = new AudioCuePlayer(application);
    }

    public LiveData<TimerUiState> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getLoadFailed() {
        return loadFailed;
    }

    public boolean hasRoutineRequest() {
        return requestedRoutineId > 0L;
    }

    public boolean isInitialized() {
        return engine != null;
    }

    public TimerState getCurrentState() {
        return engine == null ? TimerState.READY : engine.getState();
    }

    public void loadRoutine(long routineId) {
        // 화면 회전 중 비동기 조회가 진행 중이어도 다시 요청하지 않는다.
        if (hasRoutineRequest() || engine != null || cleared) {
            return;
        }
        if (routineId <= 0L) {
            loadFailed.setValue(true);
            return;
        }

        requestedRoutineId = routineId;

        repository.loadRoutineById(routineId, routine -> {
            if (cleared || engine != null) return;

            if (routine == null) {
                loadFailed.setValue(true);
                return;
            }

            try {
                // 조회 순간의 설정만 엔진에 복사해 저장값 변경과 분리한다.
                engine = new TimerEngine(
                        new ElapsedRealtimeClock(),
                        routine.workSeconds,
                        routine.restSeconds,
                        routine.rounds
                );
            } catch (IllegalArgumentException error) {
                loadFailed.setValue(true);
                return;
            }

            // 시작 버튼을 누르기 전에는 READY이며 운동음도 재생하지 않는다.
            publish();
        });
    }

    public void startByUser() {
        if (engine == null || engine.getState() != TimerState.READY) {
            return;
        }

        engine.start();
        consumeEvents();
        publish();
        scheduleTick();
    }

    public void onVisible() {
        visible = true;
        audio.setActive(true);
        scheduleTick();
        publish();
    }

    public void onNotVisible(boolean changingConfigurations) {
        visible = false;
        handler.removeCallbacks(ticker);
        audio.setActive(false);

        // 회전은 유지, 실제 백그라운드 이동은 자동 일시정지한다.
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
        scheduleTick();
    }

    public void stopByUser() {
        handler.removeCallbacks(ticker);

        if (engine != null) {
            engine.stop();
        }
        audio.setActive(false);
        publish();
    }

    private void scheduleTick() {
        handler.removeCallbacks(ticker);
        if (visible && engine != null
                && engine.getState() == TimerState.RUNNING) {
            handler.post(ticker);
        }
    }

    private void consumeEvents() {
        if (engine == null) return;

        // 같은 WORK/REST 이벤트를 화면 갱신에서 반복 소비하지 않는다.
        for (TimerEvent event : engine.drainEvents()) {
            audio.playEvent(event);
        }
    }

    private void publish() {
        if (engine == null) return;

        TimerState state = engine.getState();

        // READY는 엔진의 실제 세트가 0이므로 표시값만 1로 보정한다.
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
        cleared = true;
        handler.removeCallbacks(ticker);
        audio.release();
        super.onCleared();
    }
}