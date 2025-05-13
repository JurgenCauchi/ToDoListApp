package com.example.todolistapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.todolistapp.Model.Task;
import com.example.todolistapp.Utils.NotificationHelper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;

public class TaskRepo {
    private final AlarmManager alarmManager;  // AlarmManager used for scheduling notifications
    private final Context context;  // Context of the application

    // Constructor to initialize TaskRepo with context
    public TaskRepo(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);  // Get AlarmManager system service
    }

    // Method to schedule a task notification
    public void scheduleTaskNotification(Task task) {
        // Create an intent that will be triggered when the alarm goes off
        Intent intent = new Intent(context, TaskNotificationReceiver.class);
        intent.putExtra("task_title", task.getTask());  // Add task title to intent for notification

        // Create a PendingIntent that wraps the intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,  // Context
                task.getId(),  // Unique task ID to distinguish this alarm
                intent,  // The intent to be triggered
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE  // Flags for pending intent
        );

        // Convert LocalDate to ZonedDateTime (start of day)
        LocalDate dueDate = task.getDueDate();  // Get the task's due date
        ZonedDateTime zonedDateTime = dueDate.atStartOfDay(ZoneId.systemDefault());  // Convert LocalDate to ZonedDateTime

        // Set the alarm to trigger at the specified time
        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,  // Wake up the device if necessary
                zonedDateTime.toInstant().toEpochMilli(),  // Convert ZonedDateTime to epoch time in milliseconds
                pendingIntent  // PendingIntent that triggers the task notification
        );
    }
}
