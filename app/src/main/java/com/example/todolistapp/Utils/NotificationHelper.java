package com.example.todolistapp.Utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.todolistapp.R;

// Helper class for managing notifications
public class NotificationHelper {
    private static final String CHANNEL_ID = "TASK_REMINDER_CHANNEL"; // Notification channel ID
    private final Context context;

    // Constructor
    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel(); // Create channel when helper is instantiated
    }

    // Create a notification channel for Android O and above
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminders", // Channel name shown to user
                    NotificationManager.IMPORTANCE_DEFAULT // Importance level
            );

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);

            // Debug logs to verify channel creation
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                Log.e("Notification", "Channel creation failed!");
            } else {
                Log.d("Notification", "Channel created successfully");
            }
        }
    }

    // Show a notification with the given task title
    public void showNotification(String taskTitle) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle("Task Due: " + taskTitle) // Notification title
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Priority level
                .setAutoCancel(true); // Automatically dismiss when clicked

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check notification permission before showing
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build()); // Use timestamp as notification ID
        }
    }
}
