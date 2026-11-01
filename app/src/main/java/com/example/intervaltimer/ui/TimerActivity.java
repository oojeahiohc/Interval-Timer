// TimerActivity.java: 선택한 루틴의 READY·RUNNING·완료 화면
package com.example.intervaltimer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.intervaltimer.R;
import com.example.intervaltimer.timer.TimerPhase;
import com.example.intervaltimer.timer.TimerState;
import com.example.intervaltimer.timer.TimerUiState;
import com.example.intervaltimer.timer.TimerViewModel;

public class TimerActivity extends AppCompatActivity {

    public static final String EXTRA_ROUTINE_ID = "routine_id";

    private TimerViewModel viewModel;

    private TextView phaseText;
    private TextView remainingText;
    private TextView roundText;
    private Button startTimerButton;
    private Button pauseButton;
    private Button resumeButton;
    private LinearLayout controlRow;
    private LinearLayout completedState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TimerViewModel.class);

        // 새 프로세스가 복원한 타이머라면 자동 재시작하지 않는다.
        // 같은 프로세스의 화면 회전 중 조회·엔진 상태는 유지한다.
        if (savedInstanceState != null
                && !viewModel.hasRoutineRequest()) {
            goToList();
            return;
        }

        long routineId = getIntent().getLongExtra(
                EXTRA_ROUTINE_ID, -1L);

        if (routineId <= 0L) {
            Toast.makeText(this, R.string.error_routine_missing,
                    Toast.LENGTH_SHORT).show();
            goToList();
            return;
        }

        setContentView(R.layout.activity_timer);

        phaseText = findViewById(R.id.timerPhaseText);
        remainingText = findViewById(R.id.timerRemainingText);
        roundText = findViewById(R.id.timerRoundText);
        startTimerButton = findViewById(R.id.startTimerButton);
        pauseButton = findViewById(R.id.pauseButton);
        resumeButton = findViewById(R.id.resumeButton);

        Button stopButton = findViewById(R.id.stopButton);
        Button backToListButton = findViewById(R.id.backToListButton);

        controlRow = findViewById(R.id.controlRow);
        completedState = findViewById(R.id.completedState);

        // 조회 전에는 데모 시간과 시작 버튼을 표시하지 않는다.
        phaseText.setText(R.string.loading_routine);
        remainingText.setText(R.string.loading_time);
        roundText.setText("");
        startTimerButton.setEnabled(false);
        startTimerButton.setVisibility(View.GONE);
        controlRow.setVisibility(View.GONE);
        completedState.setVisibility(View.GONE);

        startTimerButton.setOnClickListener(
                view -> viewModel.startByUser());
        pauseButton.setOnClickListener(
                view -> viewModel.pauseByUser());
        resumeButton.setOnClickListener(
                view -> viewModel.resumeByUser());
        stopButton.setOnClickListener(view -> stopAndLeave());
        backToListButton.setOnClickListener(view -> goToList());

        // DB 조회가 완료되면 READY 상태를 표시한다.
        viewModel.getUiState().observe(this, this::render);
        viewModel.getLoadFailed().observe(this, failed -> {
            if (Boolean.TRUE.equals(failed) && !isFinishing()) {
                Toast.makeText(this, R.string.error_routine_missing,
                        Toast.LENGTH_SHORT).show();
                goToList();
            }
        });

        // 이전 조회 요청이 있으면 같은 ViewModel의 결과를 재사용한다.
        viewModel.loadRoutine(routineId);

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        TimerState state = viewModel.getCurrentState();

                        // 시작 전·완료 상태는 확인 대화상자 없이 목록 복귀.
                        if (state == TimerState.READY
                                || state == TimerState.COMPLETED
                                || state == TimerState.STOPPED) {
                            goToList();
                        } else {
                            confirmBackNavigation();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (viewModel != null) {
            viewModel.onVisible();
        }
    }

    @Override
    protected void onStop() {
        if (viewModel != null) {
            viewModel.onNotVisible(isChangingConfigurations());
        }
        super.onStop();
    }

    private void render(TimerUiState state) {
        remainingText.setText(state.remainingText);
        roundText.setText(getString(R.string.round_progress,
                state.currentRound, state.totalRounds));

        if (state.state == TimerState.READY) {
            phaseText.setText(R.string.work_phase);
            phaseText.setTextColor(ContextCompat.getColor(
                    this, R.color.color_work));
            remainingText.setBackgroundResource(
                    R.drawable.bg_timer_panel);
            remainingText.setTextColor(ContextCompat.getColor(
                    this, R.color.clay_on_work));

            startTimerButton.setEnabled(true);
            startTimerButton.setVisibility(View.VISIBLE);
            controlRow.setVisibility(View.GONE);
            completedState.setVisibility(View.GONE);
            return;
        }

        startTimerButton.setVisibility(View.GONE);

        if (state.state == TimerState.COMPLETED) {
            phaseText.setText(R.string.timer_complete_phase);
            phaseText.setTextColor(ContextCompat.getColor(
                    this, R.color.clay_text));
            remainingText.setBackgroundResource(
                    R.drawable.bg_card_clay);
            remainingText.setTextColor(ContextCompat.getColor(
                    this, R.color.clay_text));
            controlRow.setVisibility(View.GONE);
            completedState.setVisibility(View.VISIBLE);
            return;
        }

        boolean isWork = state.phase != TimerPhase.REST;

        phaseText.setText(state.state == TimerState.PAUSED
                ? (isWork ? R.string.work_paused : R.string.rest_paused)
                : (isWork ? R.string.work_phase : R.string.rest_phase));

        phaseText.setTextColor(ContextCompat.getColor(this,
                isWork ? R.color.color_work : R.color.color_rest));
        remainingText.setBackgroundResource(isWork
                ? R.drawable.bg_timer_panel
                : R.drawable.bg_timer_panel_rest);
        remainingText.setTextColor(ContextCompat.getColor(this,
                isWork ? R.color.clay_on_work : R.color.clay_on_rest));

        int buttonBackground = isWork
                ? R.drawable.bg_button_work : R.drawable.bg_button_rest;
        int buttonTextColor = isWork
                ? R.color.clay_on_work : R.color.clay_on_rest;

        pauseButton.setBackgroundResource(buttonBackground);
        resumeButton.setBackgroundResource(buttonBackground);
        pauseButton.setTextColor(
                ContextCompat.getColor(this, buttonTextColor));
        resumeButton.setTextColor(
                ContextCompat.getColor(this, buttonTextColor));

        controlRow.setVisibility(View.VISIBLE);
        completedState.setVisibility(View.GONE);
        pauseButton.setVisibility(state.state == TimerState.RUNNING
                ? View.VISIBLE : View.GONE);
        resumeButton.setVisibility(state.state == TimerState.PAUSED
                ? View.VISIBLE : View.GONE);
    }

    private void confirmBackNavigation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.stop_timer_title)
                .setMessage(R.string.stop_timer_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.stop,
                        (dialog, which) -> stopAndLeave())
                .show();
    }

    private void stopAndLeave() {
        viewModel.stopByUser();
        goToList();
    }

    private void goToList() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}