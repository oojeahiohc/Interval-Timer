// RoutineRepository.java: Room 조회 및 비동기 쓰기 작업
package com.example.intervaltimer.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoutineRepository {

    // 저장 요청을 보낸 화면에 성공 여부를 전달한다.
    public interface SaveCallback {
        void onResult(boolean success);
    }

    // 쓰기 작업은 하나의 DB 작업 스레드에서 순서대로 처리한다.
    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();
    // DB 작업 결과를 화면을 갱신할 수 있는 메인 스레드로 전달한다.
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private final RoutineDao dao;

    public RoutineRepository(Context context) {
        dao = AppDatabase.getInstance(context).routineDao();
    }

    public LiveData<List<Routine>> getAllRoutines() {
        return dao.getAllRoutines();
    }

    public LiveData<Routine> getRoutineById(long id) {
        return dao.getRoutineById(id);
    }

    public interface RoutineCallback {
        void onResult(Routine routine);
    }

    public void loadRoutineById(long id, RoutineCallback callback) {
        DB_EXECUTOR.execute(() -> {
            Routine routine = null;

            try {
                routine = dao.getRoutineByIdOnce(id);
            } catch (RuntimeException error) {
                Log.e("IntervalTimer", "Failed to load routine", error);
            }

            Routine result = routine;
            MAIN_HANDLER.post(() -> callback.onResult(result));
        });
    }

    public void insertRoutine(Routine routine, SaveCallback callback) {
        // 별도 스레드에서 저장하고 메인 스레드에서 결과 콜백을 호출한다.
        DB_EXECUTOR.execute(() -> {
            boolean saved = dao.insertRoutine(routine) > 0;
            MAIN_HANDLER.post(() -> callback.onResult(saved));
        });
    }

    public void updateRoutine(Routine routine, SaveCallback callback) {
        DB_EXECUTOR.execute(() -> {
            boolean saved = dao.updateRoutine(routine) > 0;
            MAIN_HANDLER.post(() -> callback.onResult(saved));
        });
    }

    public void deleteRoutine(Routine routine) {
        // 삭제로 바뀐 목록은 Room의 LiveData를 통해 화면에 전달된다.
        DB_EXECUTOR.execute(() -> dao.deleteRoutine(routine));
    }
}
