package com.example.todolistapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolistapp.Adapter.ToDoAdapter;
import com.example.todolistapp.Model.Task;
import com.example.todolistapp.Utils.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Main activity class that displays the task list
public class MainActivity extends AppCompatActivity implements OnDialogCloseListener {

    RecyclerView recyclerView; // RecyclerView to display tasks
    FloatingActionButton addButton; // Button to add a new task
    ImageButton settingsButton; // Button to open settings
    DatabaseHelper db; // Database helper to interact with the SQLite database
    private List<Task> mList; // List to store tasks
    private ToDoAdapter adapter; // Adapter for the RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enable edge-to-edge display

        // Apply saved theme (light/dark mode) from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean nightMode = preferences.getBoolean("nightMode", false);
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_main); // Set layout file

        // Adjust padding for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.addButton);
        db = new DatabaseHelper(MainActivity.this); // Initialize database helper
        mList = new ArrayList<>(); // Initialize empty task list
        adapter = new ToDoAdapter(db, MainActivity.this); // Initialize adapter

        // Setup RecyclerView
        recyclerView.setHasFixedSize(true); // Improve performance
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set layout manager
        recyclerView.setAdapter(adapter); // Attach adapter

        // Load tasks from database
        mList = db.getAllTasks();
        Collections.reverse(mList); // Reverse list so newest tasks appear first
        adapter.setTasks(mList); // Update adapter with tasks

        // Set click listener for add button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open AddNewTask dialog
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
            }
        });

        // Attach swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerViewTouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Initialize and set click listener for settings button
        settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open SettingsActivity
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
    }

    // Refresh the task list when a dialog is closed (e.g., after adding/editing a task)
    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        mList = db.getAllTasks(); // Reload tasks from database
        Collections.reverse(mList); // Reverse list order
        adapter.setTasks(mList); // Update adapter
        adapter.notifyDataSetChanged(); // Refresh RecyclerView
    }
}
