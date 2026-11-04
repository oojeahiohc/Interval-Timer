// Routine.java: Room에 저장할 운동 루틴
package com.example.intervaltimer.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

// Room에 이 클래스의 데이터를 routines 테이블에 저장
@Entity(tableName = "routines")
public class Routine {

    // 이름이 같아도 루틴을 구별할 ID를 자동 생성한다.
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;
    public int workSeconds;
    public int restSeconds;
    public int rounds;

    public Routine(@NonNull String name, int workSeconds, int restSeconds, int rounds) {
        this.name = name;
        this.workSeconds = workSeconds;
        this.restSeconds = restSeconds;
        this.rounds = rounds;
    }
}
