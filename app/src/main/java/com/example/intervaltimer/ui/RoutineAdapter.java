// RoutineAdapter.java: DB 루틴 데이터를 목록 카드에 표시
package com.example.intervaltimer.ui;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.intervaltimer.R;
import com.example.intervaltimer.data.Routine;

import java.util.ArrayList;
import java.util.List;

public class RoutineAdapter
        extends RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder> {

    public interface OnRoutineActionListener {
        void onStart(Routine routine);
        void onEdit(Routine routine);
        void onDelete(Routine routine);
    }

    private final List<Routine> routines = new ArrayList<>();
    private final OnRoutineActionListener listener;

    public RoutineAdapter(OnRoutineActionListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void submitRoutines(List<Routine> updated) {
        routines.clear();
        if (updated != null) {
            routines.addAll(updated);
        }
        notifyDataSetChanged();
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
    public void onBindViewHolder(
            @NonNull RoutineViewHolder holder, int position) {
        Routine routine = routines.get(position);

        holder.nameText.setText(routine.name);
        holder.summaryText.setText(holder.itemView.getContext().getString(
                R.string.routine_summary,
                routine.workSeconds,
                routine.restSeconds,
                routine.rounds
        ));

        // 카드 자체 또는 시작 버튼을 누르면 루틴을 선택한다.
        holder.itemView.setOnClickListener(
                view -> listener.onStart(routine));
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