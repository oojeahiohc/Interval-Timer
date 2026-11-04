// RoutineDao.java: 루틴 데이터베이스 CRUD 정의
package com.example.intervaltimer.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

// 아래 작업의 실제 구현 코드는 Room이 빌드할 때 생성한다.
@Dao
public interface RoutineDao {

    // 저장한 루틴의 ID를 반환한다.
    @Insert
    long insertRoutine(Routine routine);

    // 최신 ID 순으로 조회하고, 테이블 변경 시 목록을 다시 전달한다.
    @Query("SELECT * FROM routines ORDER BY id DESC")
    LiveData<List<Routine>> getAllRoutines();

    // 수정 화면에서 해당 ID의 루틴을 관찰한다.
    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    LiveData<Routine> getRoutineById(long id);

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    Routine getRoutineByIdOnce(long id);

    // 기본 키가 같은 행을 수정하고 반영된 행 수를 반환한다.
    @Update
    int updateRoutine(Routine routine);

    // 기본 키에 해당하는 행을 삭제하고 삭제된 행 수를 반환한다.
    @Delete
    int deleteRoutine(Routine routine);
}
