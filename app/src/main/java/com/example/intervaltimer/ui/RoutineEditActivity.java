// RoutineEditActivity.java: 루틴 편집 화면과 버튼 동작
package com.example.intervaltimer.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.intervaltimer.R;

public class RoutineEditActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "demo_name";
    public static final String EXTRA_WORK = "demo_work";
    public static final String EXTRA_REST = "demo_rest";
    public static final String EXTRA_ROUNDS = "demo_rounds";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_edit);

        EditText nameInput = findViewById(R.id.nameInput);
        EditText workInput = findViewById(R.id.workInput);
        EditText restInput = findViewById(R.id.restInput);
        EditText roundsInput = findViewById(R.id.roundsInput);

        // 수정 화면으로 전달된 루틴 값 채우기
        if (getIntent().hasExtra(EXTRA_NAME)) {
            nameInput.setText(getIntent().getStringExtra(EXTRA_NAME));
            workInput.setText(String.valueOf(
                    getIntent().getIntExtra(EXTRA_WORK, 20)));
            restInput.setText(String.valueOf(
                    getIntent().getIntExtra(EXTRA_REST, 10)));
            roundsInput.setText(String.valueOf(
                    getIntent().getIntExtra(EXTRA_ROUNDS, 8)));
        }

        Button cancelButton = findViewById(R.id.cancelEditButton);
        Button saveButton = findViewById(R.id.saveRoutineButton);

        // 저장은 안내 후 화면 복귀만 수행
        cancelButton.setOnClickListener(view -> finish());
        saveButton.setOnClickListener(view -> {
            Toast.makeText(
                    this,
                    R.string.demo_save_notice,
                    Toast.LENGTH_SHORT
            ).show();
            finish();
        });
    }
}