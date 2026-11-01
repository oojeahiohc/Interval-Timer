// RoutineEditActivity.java: 루틴 생성·수정 및 입력 검증
package com.example.intervaltimer.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.intervaltimer.R;
import com.example.intervaltimer.data.Routine;
import com.example.intervaltimer.data.RoutineRepository;

public class RoutineEditActivity extends AppCompatActivity {

    public static final String EXTRA_ROUTINE_ID = "edit_routine_id";

    private EditText nameInput;
    private EditText workInput;
    private EditText restInput;
    private EditText roundsInput;
    private Button saveButton;

    private RoutineRepository repository;
    private Routine editingRoutine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_edit);

        repository = new RoutineRepository(getApplicationContext());

        nameInput = findViewById(R.id.nameInput);
        workInput = findViewById(R.id.workInput);
        restInput = findViewById(R.id.restInput);
        roundsInput = findViewById(R.id.roundsInput);

        Button cancelButton = findViewById(R.id.cancelEditButton);
        saveButton = findViewById(R.id.saveRoutineButton);

        cancelButton.setOnClickListener(view -> finish());
        saveButton.setOnClickListener(view -> saveRoutine());

        // 수정 화면이면 DB에서 현재 ID에 해당하는 값을 가져온다.
        if (getIntent().hasExtra(EXTRA_ROUTINE_ID)) {
            long id = getIntent().getLongExtra(EXTRA_ROUTINE_ID, -1L);
            saveButton.setEnabled(false);

            repository.getRoutineById(id).observe(this, routine -> {
                if (routine == null) {
                    Toast.makeText(this, R.string.error_routine_missing,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                if (editingRoutine == null) {
                    editingRoutine = routine;

                    // 회전 후에는 Android가 복원한 입력값을 유지한다.
                    if (savedInstanceState == null) {
                        nameInput.setText(routine.name);
                        workInput.setText(String.valueOf(routine.workSeconds));
                        restInput.setText(String.valueOf(routine.restSeconds));
                        roundsInput.setText(String.valueOf(routine.rounds));
                    }
                    saveButton.setEnabled(true);
                }
            });
        }
    }

    private void saveRoutine() {
        String name = nameInput.getText().toString().trim();

        if (name.isEmpty() || name.length() > 30) {
            nameInput.setError(getString(R.string.error_routine_name));
            nameInput.requestFocus();
            return;
        }

        Integer work = readNumber(workInput, 1, 3600,
                R.string.error_work_seconds);
        if (work == null) return;

        Integer rest = readNumber(restInput, 0, 3600,
                R.string.error_rest_seconds);
        if (rest == null) return;

        Integer rounds = readNumber(roundsInput, 1, 99,
                R.string.error_rounds);
        if (rounds == null) return;

        Routine routine = new Routine(name, work, rest, rounds);
        saveButton.setEnabled(false);

        RoutineRepository.SaveCallback callback = success -> {
            if (success) {
                finish();
            } else {
                saveButton.setEnabled(true);
                Toast.makeText(this, R.string.error_routine_save,
                        Toast.LENGTH_SHORT).show();
            }
        };

        // 수정에서는 기존 기본 키를 유지해 동일한 행을 갱신한다.
        if (editingRoutine != null) {
            routine.id = editingRoutine.id;
            repository.updateRoutine(routine, callback);
        } else {
            repository.insertRoutine(routine, callback);
        }
    }

    private Integer readNumber(EditText input, int min, int max,
                               int errorMessage) {
        String text = input.getText().toString().trim();
        try {
            int value = Integer.parseInt(text);
            if (value >= min && value <= max) {
                return value;
            }
        } catch (NumberFormatException ignored) {
            // 빈 문자열·문자·정수 범위 초과 입력을 함께 처리한다.
        }

        input.setError(getString(errorMessage));
        input.requestFocus();
        return null;
    }
}