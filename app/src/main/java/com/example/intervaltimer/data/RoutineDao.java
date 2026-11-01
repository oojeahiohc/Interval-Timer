// RoutineDao.java: 루틴 데이터베이스 CRUD 정의
package com.example.intervaltimer.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RoutineDao {

    @Insert
    long insertRoutine(Routine routine);

    // DB 변경 시 목록을 다시 전달한다.
    @Query("SELECT * FROM routines ORDER BY id DESC")
    LiveData<List<Routine>> getAllRoutines();

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    LiveData<Routine> getRoutineById(long id);

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    Routine getRoutineByIdOnce(long id);

    @Update
    int updateRoutine(Routine routine);

    @Delete
    int deleteRoutine(Routine routine);
}