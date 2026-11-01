// RoutineAdapter.java: 루틴 목록 카드와 클릭 이벤트 연결
package com.example.intervaltimer.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.intervaltimer.R;

import java.util.List;

public class RoutineAdapter
        extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    // 테스트 루틴 정보를 담는 표시용 데이터
    public static final class DemoRoutine {
        public final long id;
        public final String name;
        public final int workSeconds;
        public final int restSeconds;
        public final int rounds;

        public DemoRoutine(long id, String name, int workSeconds,
                           int restSeconds, int rounds) {
            this.id = id;
            this.name = name;
            this.workSeconds = workSeconds;
            this.restSeconds = restSeconds;
            this.rounds = rounds;
        }
    }

    // 카드 버튼 클릭을 상위 화면으로 전달
    public interface OnRoutineActionListener {
        void onStart(DemoRoutine routine);
        void onEdit(DemoRoutine routine);
        void onDelete(DemoRoutine routine);
    }

    private final List<DemoRoutine> routines;
    private final OnRoutineActionListener listener;

    public RoutineAdapter(List<DemoRoutine> routines,
                          OnRoutineActionListener listener) {
        this.routines = routines;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RoutineViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_routine, parent, false);
        return new RoutineViewHolder(view);
    }

    @Override
    // 카드에 루틴 데이터와 클릭 이벤트 연결
    public void onBindViewHolder(
            @NonNull RoutineViewHolder holder, int position) {
        DemoRoutine routine = routines.get(position);

        holder.nameText.setText(routine.name);
        holder.summaryText.setText(holder.itemView.getContext().getString(
                R.string.routine_summary,
                routine.workSeconds,
                routine.restSeconds,
                routine.rounds
        ));

        holder.startButton.setOnClickListener(
                view -> listener.onStart(routine));
        holder.editButton.setOnClickListener(
                view -> listener.onEdit(routine));
        holder.deleteButton.setOnClickListener(
                view -> listener.onDelete(routine));
    }

    @Override
    public int getItemCount() {
        return routines.size();
    }

    static class RoutineViewHolder extends RecyclerView.ViewHolder {
        final TextView nameText;
        final TextView summaryText;
        final Button startButton;
        final Button editButton;
        final Button deleteButton;

        RoutineViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.routineNameText);
            summaryText = itemView.findViewById(R.id.routineSummaryText);
            startButton = itemView.findViewById(R.id.startRoutineButton);
            editButton = itemView.findViewById(R.id.editRoutineButton);
            deleteButton = itemView.findViewById(R.id.deleteRoutineButton);
        }
    }
}