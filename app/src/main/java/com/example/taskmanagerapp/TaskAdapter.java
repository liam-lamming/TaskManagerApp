package com.example.taskmanagerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private final List<Task> tasks = new ArrayList<>();
    private final OnTaskClickListener onTaskClickListener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task, int position);
    }

    public TaskAdapter(OnTaskClickListener onTaskClickListener) {
        this.onTaskClickListener = onTaskClickListener;
    }

    public void setTasks(List<Task> newTasks) {
        tasks.clear();
        tasks.addAll(newTasks);
        notifyDataSetChanged();
    }

    public void addTask(Task task) {
        tasks.add(task);
        notifyItemInserted(tasks.size() - 1);
    }

    public void updateTask(int position, Task task) {
        tasks.set(position, task);
        notifyItemChanged(position);
    }

    public void removeTask(int position) {
        tasks.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.taskTitle.setText(task.getTitle());
        holder.taskDescription.setText(task.getDescription());
        holder.taskPriority.setText(task.getPriority());
        holder.taskCategory.setText(task.getCategory());

        holder.itemView.setOnClickListener(v -> onTaskClickListener.onTaskClick(task, position));
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle, taskDescription, taskPriority, taskCategory;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskPriority = itemView.findViewById(R.id.taskPriority);
            taskCategory = itemView.findViewById(R.id.taskCategory);
        }
    }
}
