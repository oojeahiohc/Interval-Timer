// AppDatabase.java: 앱에서 공유하는 Room 데이터베이스
package com.example.intervaltimer.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// 이 DB에 포함할 테이블과 스키마 버전을 지정한다.
@Database(entities = {Routine.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    // Room이 구현한 DAO를 제공한다.
    public abstract RoutineDao routineDao();

    // 여러 화면에서 요청해도 같은 DB 인스턴스를 공유한다.
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "interval_timer.db"
                    ).build();
                }
            }
        }
        return instance;
    }
}
