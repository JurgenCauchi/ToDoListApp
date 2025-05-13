package com.example.todolistapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.todolistapp.Utils.NotificationHelper;

public class TaskNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = "Today";
        new NotificationHelper(context).showNotification(title);
    }
}
