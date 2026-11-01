// TimerActivity.java: 타이머 상태 표시·버튼·생명주기 관리
package com.example.intervaltimer.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        // 새 ViewModel과 복원 정보가 함께 있으면 프로세스 종료 뒤 재생성이다.
        if (savedInstanceState != null && !viewModel.isInitialized()) {
            goToList();
            return;
        }

        setContentView(R.layout.activity_timer);
        startTimerButton = findViewById(R.id.startTimerButton);
        startTimerButton.setOnClickListener(view -> viewModel.startByUser());

        phaseText = findViewById(R.id.timerPhaseText);
        remainingText = findViewById(R.id.timerRemainingText);
        roundText = findViewById(R.id.timerRoundText);
        pauseButton = findViewById(R.id.pauseButton);
        resumeButton = findViewById(R.id.resumeButton);
        Button stopButton = findViewById(R.id.stopButton);
        Button backToListButton = findViewById(R.id.backToListButton);
        controlRow = findViewById(R.id.controlRow);
        completedState = findViewById(R.id.completedState);

        // 동일한 ViewModel은 화면 회전 뒤에도 사용한다.
        viewModel.initializeDemo();

        viewModel.getUiState().observe(this, this::render);

        pauseButton.setOnClickListener(view -> viewModel.pauseByUser());
        resumeButton.setOnClickListener(view -> viewModel.resumeByUser());
        stopButton.setOnClickListener(view -> stopAndLeave());
        backToListButton.setOnClickListener(view -> goToList());

        // 버튼으로 종료하면 즉시 목록 이동, Android 뒤로가기는 확인한다.
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        TimerState state = viewModel.getCurrentState();
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
            // 회전으로 인한 Activity 교체와 실제 백그라운드를 구분한다.
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

            remainingText.setBackgroundResource(R.drawable.bg_timer_panel);
            remainingText.setTextColor(ContextCompat.getColor(
                    this, R.color.clay_on_work));

            startTimerButton.setVisibility(View.VISIBLE);
            controlRow.setVisibility(View.GONE);
            completedState.setVisibility(View.GONE);
            return;
        }

        // RUNNING·PAUSED·COMPLETED에서는 시작 버튼을 숨긴다.
        startTimerButton.setVisibility(View.GONE);

        if (state.state == TimerState.COMPLETED) {
            phaseText.setText(R.string.timer_complete_phase);
            phaseText.setTextColor(ContextCompat.getColor(
                    this, R.color.clay_text));
            remainingText.setBackgroundResource(R.drawable.bg_card_clay);
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
        int buttonText = isWork
                ? R.color.clay_on_work : R.color.clay_on_rest;

        pauseButton.setBackgroundResource(buttonBackground);
        resumeButton.setBackgroundResource(buttonBackground);
        pauseButton.setTextColor(ContextCompat.getColor(this, buttonText));
        resumeButton.setTextColor(ContextCompat.getColor(this, buttonText));

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