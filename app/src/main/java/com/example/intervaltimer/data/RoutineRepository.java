// RoutineRepository.java: Room 조회 및 비동기 쓰기 작업
package com.example.intervaltimer.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoutineRepository {

    public interface SaveCallback {
        void onResult(boolean success);
    }

    // 쓰기 작업은 하나의 DB 작업 스레드에서 순서대로 처리한다.
    private static final ExecutorService DB_EXECUTOR = Executors.newSingleThreadExecutor();
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

    public void insertRoutine(Routine routine, SaveCallback callback) {
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
        DB_EXECUTOR.execute(() -> dao.deleteRoutine(routine));
    }
}