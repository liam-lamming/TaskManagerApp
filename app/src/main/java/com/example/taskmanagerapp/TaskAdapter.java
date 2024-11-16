package com.example.taskmanagerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> tasks;
    private final OnTaskClickListener onTaskClickListener;

    // Interface for handling task click events
    public interface OnTaskClickListener {
        void onTaskClick(Task task, int position);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener onTaskClickListener) {
        this.tasks = tasks;
        this.onTaskClickListener = onTaskClickListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.taskTitle.setText(task.getTitle());
        holder.taskDescription.setText(task.getDescription());
        holder.taskPriority.setText(task.getPriority());
        holder.taskCategory.setText(task.getCategory());

        // Set click listener for each task item
        holder.itemView.setOnClickListener(v -> {
            if (onTaskClickListener != null) {
                onTaskClickListener.onTaskClick(task, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription, taskPriority, taskCategory;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskPriority = itemView.findViewById(R.id.taskPriority);
            taskCategory = itemView.findViewById(R.id.taskCategory);
        }
    }
}
