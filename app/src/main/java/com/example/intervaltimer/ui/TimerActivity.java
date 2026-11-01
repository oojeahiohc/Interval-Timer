// TimerActivity.java: 타이머 화면의 테스트 버튼 동작
package com.example.intervaltimer.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.intervaltimer.R;

public class TimerActivity extends AppCompatActivity {

    public static final String EXTRA_ROUTINE_ID = "routine_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        // 목록에서 전달한 테스트 루틴 ID 확인
        long demoRoutineId = getIntent().getLongExtra(
                EXTRA_ROUTINE_ID, -1L);
        Log.d("IntervalTimer", "Demo routine ID: " + demoRoutineId);

        TextView phaseText = findViewById(R.id.timerPhaseText);
        Button pauseButton = findViewById(R.id.pauseButton);
        Button resumeButton = findViewById(R.id.resumeButton);
        Button stopButton = findViewById(R.id.stopButton);
        Button backToListButton = findViewById(R.id.backToListButton);

        // 타이머 시간은 변경하지 않고 화면 표시만 전환
        pauseButton.setOnClickListener(view -> {
            phaseText.setText(R.string.work_paused);
            pauseButton.setVisibility(View.GONE);
            resumeButton.setVisibility(View.VISIBLE);
        });

        resumeButton.setOnClickListener(view -> {
            phaseText.setText(R.string.work_phase);
            resumeButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);
        });

        stopButton.setOnClickListener(view -> finish());
        backToListButton.setOnClickListener(view -> finish());
    }
}