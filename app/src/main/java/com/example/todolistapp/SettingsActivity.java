package com.example.todolistapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;

import android.provider.Settings;
import android.util.TypedValue;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

// Settings screen for toggling Night Mode and Notifications
public class SettingsActivity extends AppCompatActivity {

    // Constants for SharedPreferences keys
    private static final String PREFS_NAME = "MyPrefs";
    private static final String NIGHT_MODE_KEY = "nightMode";
    private static final String NOTIFICATIONS_KEY = "notifications";

    // Code for notification permission request
    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Load theme (light/dark) based on saved preference BEFORE setting content view
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isNightMode = prefs.getBoolean(NIGHT_MODE_KEY, false);
        AppCompatDelegate.setDefaultNightMode(
                isNightMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());

        // Initialize switches for Night Mode and Notifications
        SwitchCompat nightModeSwitch = findViewById(R.id.nightModeSwitch);
        SwitchCompat notificationsSwitch = findViewById(R.id.notificationsSwitch);

        // Load saved switch states
        nightModeSwitch.setChecked(isNightMode);
        notificationsSwitch.setChecked(prefs.getBoolean(NOTIFICATIONS_KEY, true));

        // Listener for Night Mode switch
        nightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(NIGHT_MODE_KEY, isChecked);
            editor.apply();

            // Apply the selected theme immediately
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

            // Update toolbar and status bar colors
            updateThemeDependentColors();
        });

        // Initially update colors based on theme
        updateThemeDependentColors();

        // Listener for Notifications switch
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(NOTIFICATIONS_KEY, isChecked);
            editor.apply();

            if (isChecked) {
                checkAndRequestNotificationPermission();
            } else {
                disableNotifications();
            }
        });
    }

    // Update toolbar and system bar colors based on current theme
    private void updateThemeDependentColors() {
        int colorPrimary = resolveThemeColor(com.google.android.material.R.attr.colorPrimary);
        int colorPrimaryDark = resolveThemeColor(com.google.android.material.R.attr.colorPrimaryDark);
        int textColorPrimary = resolveThemeColor(android.R.attr.textColorPrimary);


        getWindow().setStatusBarColor(colorPrimaryDark);
        getWindow().setNavigationBarColor(colorPrimaryDark);
    }

    // Helper to resolve current theme color from attribute
    private int resolveThemeColor(int attr) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    // Handle toolbar back button
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // Request POST_NOTIFICATIONS permission if needed (Android 13+)
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Request notification permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            } else {
                enableNotifications();
            }
        } else {
            // No runtime permission needed for notifications before Android 13
            enableNotifications();
        }
    }

    // Open app's notification settings manually
    private void openNotificationSettings() {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
        }
        startActivity(intent);
    }

    // Placeholder for enabling notifications (can integrate with notification manager)
    private void enableNotifications() {
        Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
    }

    // Placeholder for disabling notifications
    private void disableNotifications() {
        Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show();
    }
}
