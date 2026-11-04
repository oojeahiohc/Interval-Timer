// MainActivity.java: 저장된 루틴 목록·화면 이동·삭제
package com.example.intervaltimer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.intervaltimer.R;
import com.example.intervaltimer.data.Routine;
import com.example.intervaltimer.data.RoutineRepository;

public class MainActivity extends AppCompatActivity {

    private RoutineRepository repository;
    private RoutineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 목록 화면 XML을 먼저 만든 뒤 그 안의 View를 찾는다.
        setContentView(R.layout.activity_main);

        // 화면에서는 Repository를 통해 DB에 접근한다.
        repository = new RoutineRepository(getApplicationContext());

        RecyclerView recyclerView =
                findViewById(R.id.routineRecyclerView);
        View emptyState = findViewById(R.id.emptyState);
        Button addButton = findViewById(R.id.addRoutineButton);
        Button emptyAddButton = findViewById(R.id.emptyAddButton);

        // 목록을 세로로 배치
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new RoutineAdapter(
                new RoutineAdapter.OnRoutineActionListener() {
                    @Override
                    public void onStart(Routine routine) {
                        Intent intent = new Intent(
                                MainActivity.this, TimerActivity.class);
                        intent.putExtra(
                                TimerActivity.EXTRA_ROUTINE_ID, routine.id);
                        startActivity(intent);
                    }

                    @Override
                    public void onEdit(Routine routine) {
                        // 수정할 루틴의 ID를 편집 화면에 전달한다.
                        Intent intent = new Intent(
                                MainActivity.this,
                                RoutineEditActivity.class);
                        intent.putExtra(
                                RoutineEditActivity.EXTRA_ROUTINE_ID,
                                routine.id);
                        startActivity(intent);
                    }

                    @Override
                    public void onDelete(Routine routine) {
                        showDeleteConfirmation(routine);
                    }
                }
        );
        recyclerView.setAdapter(adapter);

        // 이 화면의 생명주기에 맞춰 목록을 관찰하고 변경 시 카드를 갱신한다.
        repository.getAllRoutines().observe(this, routines -> {
            adapter.submitRoutines(routines);

            boolean isEmpty = routines == null || routines.isEmpty();
            recyclerView.setVisibility(
                    isEmpty ? View.GONE : View.VISIBLE);
            emptyState.setVisibility(
                    isEmpty ? View.VISIBLE : View.GONE);
        });

        // 클릭했을 때 실행할 동작을 등록한다.
        addButton.setOnClickListener(view -> openNewRoutine());
        emptyAddButton.setOnClickListener(view -> openNewRoutine());
    }

    private void openNewRoutine() {
        // ID 없이 열면 새 루틴을 작성한다.
        startActivity(new Intent(this, RoutineEditActivity.class));
    }

    private void showDeleteConfirmation(Routine routine) {
        // 삭제 확인 버튼을 눌렀을 때만 DB 삭제를 요청한다.
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete,
                        (dialog, which) ->
                                repository.deleteRoutine(routine))
                .show();
    }
}
