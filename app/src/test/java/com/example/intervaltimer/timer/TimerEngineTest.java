// TimerEngineTest.java: FakeClock으로 TimerEngine 경계 조건 검증
package com.example.intervaltimer.timer;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TimerEngineTest {

    @Test
    public void threeRoundsTransitionInOrder() {
        FakeClock clock = new FakeClock();
        TimerEngine engine = new TimerEngine(clock, 5, 2, 3);

        engine.start();
        assertEquals(TimerState.RUNNING, engine.getState());
        assertEquals(TimerPhase.WORK, engine.getPhase());
        assertEquals(1, engine.getCurrentRound());
        assertEquals(Collections.singletonList(TimerEvent.WORK_STARTED),
                engine.drainEvents());

        clock.advance(5000);
        engine.update();
        assertEquals(TimerPhase.REST, engine.getPhase());
        assertEquals(1, engine.getCurrentRound());
        assertEquals(Collections.singletonList(TimerEvent.REST_STARTED),
                engine.drainEvents());

        clock.advance(2000);
        engine.update();
        assertEquals(TimerPhase.WORK, engine.getPhase());
        assertEquals(2, engine.getCurrentRound());
        assertEquals(Collections.singletonList(TimerEvent.WORK_STARTED),
                engine.drainEvents());

        clock.advance(5000);
        engine.update();
        assertEquals(TimerPhase.REST, engine.getPhase());
        assertEquals(2, engine.getCurrentRound());
        assertEquals(Collections.singletonList(TimerEvent.REST_STARTED),
                engine.drainEvents());

        clock.advance(2000);
        engine.update();
        assertEquals(TimerPhase.WORK, engine.getPhase());
        assertEquals(3, engine.getCurrentRound());
        assertEquals(Collections.singletonList(TimerEvent.WORK_STARTED),
                engine.drainEvents());

        clock.advance(5000);
        engine.update();
        assertEquals(TimerState.COMPLETED, engine.getState());
        assertNull(engine.getPhase());
        assertEquals(0L, engine.getRemainingMillis());
        assertEquals(Collections.singletonList(TimerEvent.WORKOUT_COMPLETED),
                engine.drainEvents());
        assertEquals(Collections.emptyList(), engine.drainEvents());
    }

    @Test
    public void lastWorkCompletesWithoutRest() {
        FakeClock clock = new FakeClock();
        TimerEngine engine = new TimerEngine(clock, 5, 2, 1);

        engine.start();
        engine.drainEvents();
        clock.advance(5000);
        engine.update();

        assertEquals(TimerState.COMPLETED, engine.getState());
        assertNull(engine.getPhase());
        assertEquals(Collections.singletonList(TimerEvent.WORKOUT_COMPLETED),
                engine.drainEvents());
    }

    @Test
    public void zeroRestSkipsRestEvents() {
        FakeClock clock = new FakeClock();
        TimerEngine engine = new TimerEngine(clock, 5, 0, 3);

        engine.start();
        engine.drainEvents();

        clock.advance(5000);
        engine.update();
        assertEquals(TimerPhase.WORK, engine.getPhase());
        assertEquals(2, engine.getCurrentRound());

        clock.advance(5000);
        engine.update();
        assertEquals(3, engine.getCurrentRound());

        clock.advance(5000);
        engine.update();
        assertEquals(TimerState.COMPLETED, engine.getState());
        assertEquals(Arrays.asList(
                TimerEvent.WORK_STARTED,
                TimerEvent.WORK_STARTED,
                TimerEvent.WORKOUT_COMPLETED
        ), engine.drainEvents());
    }

    @Test
    public void pauseAndResumePreserveWorkAndRestTime() {
        FakeClock clock = new FakeClock();
        TimerEngine engine = new TimerEngine(clock, 5, 2, 2);

        engine.start();
        engine.drainEvents();

        // WORK에서 2초가 흐른 뒤 일시정지한다.
        clock.advance(2000);
        engine.pause();
        assertEquals(TimerState.PAUSED, engine.getState());
        assertEquals(TimerPhase.WORK, engine.getPhase());
        assertEquals(3000L, engine.getRemainingMillis());

        clock.advance(20000);
        engine.update();
        assertEquals(3000L, engine.getRemainingMillis());

        engine.resume();
        clock.advance(3000);
        engine.update();
        assertEquals(TimerPhase.REST, engine.getPhase());
        assertEquals(1, engine.getCurrentRound());
        assertEquals(Collections.singletonList(TimerEvent.REST_STARTED),
                engine.drainEvents());

        // REST 중에도 남은 시간을 유지한다.
        clock.advance(700);
        engine.pause();
        assertEquals(1300L, engine.getRemainingMillis());
        clock.advance(10000);
        engine.resume();

        clock.advance(1300);
        engine.update();
        assertEquals(TimerPhase.WORK, engine.getPhase());
        assertEquals(2, engine.getCurrentRound());
        assertEquals(Collections.singletonList(TimerEvent.WORK_STARTED),
                engine.drainEvents());
    }

    @Test
    public void repeatedStartResumeAndStopDoNotRestart() {
        FakeClock clock = new FakeClock();
        TimerEngine engine = new TimerEngine(clock, 5, 2, 2);

        engine.start();
        engine.start();
        assertEquals(Collections.singletonList(TimerEvent.WORK_STARTED),
                engine.drainEvents());

        clock.advance(1000);
        engine.pause();
        engine.pause();
        assertEquals(4000L, engine.getRemainingMillis());

        engine.resume();
        engine.resume();
        assertEquals(TimerState.RUNNING, engine.getState());
        assertEquals(Collections.emptyList(), engine.drainEvents());

        engine.stop();
        engine.stop();
        engine.start();
        engine.resume();
        clock.advance(20000);
        engine.update();

        assertEquals(TimerState.STOPPED, engine.getState());
        assertNull(engine.getPhase());
        assertEquals(Collections.emptyList(), engine.drainEvents());
    }

    @Test
    public void boundaryAndCeilingDisplayAreCorrect() {
        FakeClock clock = new FakeClock();
        TimerEngine engine = new TimerEngine(clock, 5, 2, 2);

        engine.start();
        engine.drainEvents();
        assertEquals("00:05", engine.getRemainingTimeText());

        clock.advance(4999);
        engine.update();
        assertEquals(TimerPhase.WORK, engine.getPhase());
        assertEquals(1L, engine.getRemainingMillis());
        assertEquals("00:01", engine.getRemainingTimeText());

        clock.advance(1);
        engine.update();
        assertEquals(TimerPhase.REST, engine.getPhase());
        assertEquals("00:02", engine.getRemainingTimeText());
    }

    @Test
    public void delayedUpdateDoesNotAddTimeToNextWork() {
        FakeClock clock = new FakeClock();
        TimerEngine engine = new TimerEngine(clock, 5, 2, 3);

        engine.start();
        engine.drainEvents();

        // 한 번의 update에서 WORK와 REST 종료를 모두 처리한다.
        clock.advance(9600);
        engine.update();

        assertEquals(TimerState.RUNNING, engine.getState());
        assertEquals(TimerPhase.WORK, engine.getPhase());
        assertEquals(2, engine.getCurrentRound());
        assertEquals(2400L, engine.getRemainingMillis());
        assertEquals(Arrays.asList(
                TimerEvent.REST_STARTED,
                TimerEvent.WORK_STARTED
        ), engine.drainEvents());
        assertEquals(Collections.emptyList(), engine.drainEvents());
    }

    @Test
    public void lateUpdateAcrossWholeWorkoutEmitsEventsOnce() {
        FakeClock clock = new FakeClock();
        TimerEngine engine = new TimerEngine(clock, 5, 2, 3);

        engine.start();
        engine.drainEvents();
        clock.advance(19000);
        engine.update();

        assertEquals(TimerState.COMPLETED, engine.getState());
        assertEquals(Arrays.asList(
                TimerEvent.REST_STARTED,
                TimerEvent.WORK_STARTED,
                TimerEvent.REST_STARTED,
                TimerEvent.WORK_STARTED,
                TimerEvent.WORKOUT_COMPLETED
        ), engine.drainEvents());
        engine.update();
        assertEquals(Collections.emptyList(), engine.drainEvents());
    }
}