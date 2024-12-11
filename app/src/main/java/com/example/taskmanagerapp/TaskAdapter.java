package com.example.taskmanagerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> tasks = new ArrayList<>();
    private final OnTaskClickListener onTaskClickListener;

    /**
     * Interface for handling task click events.
     */
    public interface OnTaskClickListener {
        void onTaskClick(Task task, int position);
    }

    public TaskAdapter(OnTaskClickListener onTaskClickListener) {
        this.onTaskClickListener = onTaskClickListener;
    }

    /**
     * Updates the task list with new data using DiffUtil to calculate changes.
     *
     * @param newTasks The updated list of tasks.
     */
    public void setTasks(List<Task> newTasks) {
        if (newTasks == null) return; // Prevent null pointer exception
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return tasks.size();
            }

            @Override
            public int getNewListSize() {
                return newTasks.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return tasks.get(oldItemPosition).getId().equals(newTasks.get(newItemPosition).getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return tasks.get(oldItemPosition).equals(newTasks.get(newItemPosition));
            }
        });

        tasks.clear();
        tasks.addAll(newTasks);
        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Adds a new task to the list.
     *
     * @param task The task to add.
     */
    public void addTask(Task task) {
        if (task == null || tasks.contains(task)) return; // Avoid duplicates and null entries
        tasks.add(task);
        notifyItemInserted(tasks.size() - 1);
    }

    /**
     * Updates an existing task.
     *
     * @param updatedTask The updated task.
     */
    public void updateTask(Task updatedTask) {
        if (updatedTask == null) return; // Avoid null entries
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                notifyItemChanged(i);
                return;
            }
        }
    }

    /**
     * Removes a task by its ID.
     *
     * @param taskId The ID of the task to remove.
     */
    public void removeTaskById(String taskId) {
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(taskId)) {
                tasks.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
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
        holder.bind(task, position, onTaskClickListener);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    /**
     * ViewHolder class to hold task views.
     */
    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskTitle;
        private final TextView taskDescription;
        private final TextView taskPriority;
        private final TextView taskCategory;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            taskPriority = itemView.findViewById(R.id.taskPriority);
            taskCategory = itemView.findViewById(R.id.taskCategory);
        }

        void bind(Task task, int position, OnTaskClickListener onTaskClickListener) {
            taskTitle.setText(task.getTitle());
            taskDescription.setText(task.getDescription());
            taskPriority.setText(task.getPriority());
            taskCategory.setText(task.getCategory());

            itemView.setOnClickListener(v -> onTaskClickListener.onTaskClick(task, position));
        }
    }
}
