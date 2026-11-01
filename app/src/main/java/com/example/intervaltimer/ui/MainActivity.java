// MainActivity.java: 루틴 목록 및 화면 이동
package com.example.intervaltimer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.intervaltimer.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 목록에 표시할 테스트 데이터
        List<RoutineAdapter.DemoRoutine> routines = new ArrayList<>();
        routines.add(new RoutineAdapter.DemoRoutine(
                1L, "기본 루틴", 20, 10, 8));
        routines.add(new RoutineAdapter.DemoRoutine(
                2L, "두 번째 루틴", 30, 0, 4));

        RecyclerView recyclerView =
                findViewById(R.id.routineRecyclerView);
        View emptyState = findViewById(R.id.emptyState);
        Button addButton = findViewById(R.id.addRoutineButton);
        Button emptyAddButton = findViewById(R.id.emptyAddButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 시작·수정·삭제 버튼의 화면 동작 연결
        recyclerView.setAdapter(new RoutineAdapter(
                routines,
                new RoutineAdapter.OnRoutineActionListener() {
                    @Override
                    public void onStart(RoutineAdapter.DemoRoutine routine) {
                        Intent intent = new Intent(
                                MainActivity.this, TimerActivity.class);
                        intent.putExtra(
                                TimerActivity.EXTRA_ROUTINE_ID, routine.id);
                        startActivity(intent);
                    }

                    @Override
                    public void onEdit(RoutineAdapter.DemoRoutine routine) {
                        Intent intent = new Intent(
                                MainActivity.this,
                                RoutineEditActivity.class);
                        intent.putExtra(
                                RoutineEditActivity.EXTRA_NAME, routine.name);
                        intent.putExtra(
                                RoutineEditActivity.EXTRA_WORK,
                                routine.workSeconds);
                        intent.putExtra(
                                RoutineEditActivity.EXTRA_REST,
                                routine.restSeconds);
                        intent.putExtra(
                                RoutineEditActivity.EXTRA_ROUNDS,
                                routine.rounds);
                        startActivity(intent);
                    }

                    @Override
                    public void onDelete(RoutineAdapter.DemoRoutine routine) {
                        showDeleteConfirmation();
                    }
                }
        ));

        addButton.setOnClickListener(view -> openNewRoutine());
        emptyAddButton.setOnClickListener(view -> openNewRoutine());

        boolean isEmpty = routines.isEmpty();
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void openNewRoutine() {
        startActivity(new Intent(this, RoutineEditActivity.class));
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) ->
                        Toast.makeText(
                                this,
                                R.string.demo_delete_notice,
                                Toast.LENGTH_SHORT
                        ).show()
                )
                .show();
    }
}