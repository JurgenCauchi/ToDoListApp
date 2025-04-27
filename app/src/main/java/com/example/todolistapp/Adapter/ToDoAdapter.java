package com.example.todolistapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolistapp.AddNewTask;
import com.example.todolistapp.MainActivity;
import com.example.todolistapp.Model.Task;
import com.example.todolistapp.R;
import com.example.todolistapp.Utils.DatabaseHelper;

import java.util.List;

// Adapter class for binding To-Do list items to RecyclerView
public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {
    private Context context;
    private List<Task> todoList;
    private MainActivity activity;
    private DatabaseHelper db;

    // Constructor for initializing DatabaseHelper and MainActivity reference
    public ToDoAdapter(DatabaseHelper db, MainActivity activity) {
        this.db = db;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the task layout for each item
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(v);
    }

    // Return the associated context
    public Context getContext() {
        return activity;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to views for each item
        final Task item = todoList.get(position);
        holder.checkBox.setText(item.getTask());
        holder.checkBox.setChecked(toBoolean(item.getStatus()));
        holder.dueDateText.setText("Due: " + item.getDueDate()); // Display due date

        // Add priority display and handling - START
        int priority = item.getPriority();
        int colorRes;
        String priorityText;

        // Set priority text and color based on value
        switch (priority) {
            case 0: // Low
                colorRes = R.color.priority_low;
                priorityText = "Low";
                break;
            case 1: // Medium
                colorRes = R.color.priority_medium;
                priorityText = "Medium";
                break;
            case 2: // High
                colorRes = R.color.priority_high;
                priorityText = "High";
                break;
            default:
                colorRes = R.color.priority_medium;
                priorityText = "Medium";
        }

        holder.priorityText.setText("Priority: " + priorityText);
        holder.priorityText.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), colorRes));
        // Add priority display and handling - END

        // Update task status (checked/unchecked) in the database
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                    db.updateStatus(item.getId(), isChecked ? 1 : 0);
                }
            }
        });

        // Optional: Add logic to change priority on click
        holder.priorityText.setOnClickListener(v -> {
            // Placeholder for future priority change logic
        });
    }

    // Convert integer status to boolean
    public boolean toBoolean(int num) {
        return num != 0;
    }

    @Override
    public int getItemCount() {
        // Return number of tasks
        return todoList.size();
    }

    // Update adapter with a new list of tasks
    public void setTasks(List<Task> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
    }

    // Delete a task from the database and update RecyclerView
    public void deleteTask(int position) {
        Task item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    // Edit a task by opening the AddNewTask dialog pre-filled with task data
    public void editTask(int position) {
        Task item = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId()); // Use lowercase "id" to match your DatabaseHelper usage
        bundle.putString("task", item.getTask());
        bundle.putString("dueDate", item.getDueDate().toString());

        AddNewTask task = new AddNewTask();
        task.setArguments(bundle);
        task.show(activity.getSupportFragmentManager(), task.getTag());
    }

    // ViewHolder class to hold references to the views of each item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView dueDateText;
        TextView priorityText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views
            checkBox = itemView.findViewById(R.id.checkbox);
            dueDateText = itemView.findViewById(R.id.dueDateText);
            priorityText = itemView.findViewById(R.id.priorityText);
        }
    }
}
