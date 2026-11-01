// Routine.java: Room에 저장할 운동 루틴
package com.example.intervaltimer.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "routines")
public class Routine {

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