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
        setContentView(R.layout.activity_main);

        repository = new RoutineRepository(getApplicationContext());

        RecyclerView recyclerView =
                findViewById(R.id.routineRecyclerView);
        View emptyState = findViewById(R.id.emptyState);
        Button addButton = findViewById(R.id.addRoutineButton);
        Button emptyAddButton = findViewById(R.id.emptyAddButton);

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

        // DB 목록이 변경될 때마다 카드와 빈 화면을 갱신한다.
        repository.getAllRoutines().observe(this, routines -> {
            adapter.submitRoutines(routines);

            boolean isEmpty = routines == null || routines.isEmpty();
            recyclerView.setVisibility(
                    isEmpty ? View.GONE : View.VISIBLE);
            emptyState.setVisibility(
                    isEmpty ? View.VISIBLE : View.GONE);
        });

        addButton.setOnClickListener(view -> openNewRoutine());
        emptyAddButton.setOnClickListener(view -> openNewRoutine());
    }

    private void openNewRoutine() {
        startActivity(new Intent(this, RoutineEditActivity.class));
    }

    private void showDeleteConfirmation(Routine routine) {
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