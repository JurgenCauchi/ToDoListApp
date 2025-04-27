package com.example.todolistapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.todolistapp.Model.Task;
import com.example.todolistapp.Utils.DatabaseHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

// BottomSheetDialogFragment for adding or updating a task
public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";
    private static final String CHANNEL_ID = "todo_channel";
    private static final int NOTIFICATION_ID = 1;

    private EditText mEditText;
    private Button mSaveButton;
    private TextView dueDatePicker;
    private LocalDate selectedDueDate;
    private DatabaseHelper DB;
    private Button priorityButton;
    private int selectedPriority = 1; // Default priority: Medium (1)

    // Factory method to create a new instance
    public static AddNewTask newInstance(){
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_new_task, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        mEditText = view.findViewById(R.id.editText);
        mSaveButton = view.findViewById(R.id.addButton);
        dueDatePicker = view.findViewById(R.id.due_date_picker);
        priorityButton = view.findViewById(R.id.priority_button);

        // Setup priority selection
        priorityButton.setOnClickListener(v -> showPriorityDialog());
        updatePriorityButtonText();

        DB = new DatabaseHelper(getActivity());
        boolean isUpdate = false;
        updateDueDateText();

        // Check if updating an existing task
        Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = true;
            String task = bundle.getString("task");
            mEditText.setText(task);

            if (bundle.containsKey("dueDate")) {
                selectedDueDate = LocalDate.parse(bundle.getString("dueDate"));
                updateDueDateText();
            }

            if (task.length() > 0) {
                mSaveButton.setEnabled(true);
            }
        }

        // Enable/disable save button based on text input
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSaveButton.setEnabled(s.toString().trim().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dueDatePicker.setOnClickListener(v -> showDatePickerDialog());

        boolean finalIsUpdate = isUpdate;
        mSaveButton.setOnClickListener(v -> {
            String text = mEditText.getText().toString().trim();

            if (text.isEmpty()) {
                mEditText.setError("Task cannot be empty");
                return;
            }

            if (getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed()) {
                return;
            }

            try {
                if (finalIsUpdate) {
                    DB.updateTask(bundle.getInt("id"), text, selectedDueDate.toString(), selectedPriority);
                } else {
                    Task item = new Task();
                    item.setTask(text);
                    item.setStatus(0); // 0 = incomplete
                    item.setDueDate(selectedDueDate);
                    item.setPriority(selectedPriority);
                    DB.insertTask(item);

                    // Schedule notification for new tasks
                    scheduleNotification(text, selectedDueDate);
                }
                dismiss();
            } catch (Exception e) {
                e.printStackTrace();
                if (getContext() != null && !getActivity().isFinishing()) {
                    Toast.makeText(getContext(), "Failed to save task", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Schedule a notification for a task
    private void scheduleNotification(String task, LocalDate dueDate) {
        Context context = getContext();
        if (context == null || getActivity() == null || getActivity().isFinishing()) {
            Log.e(TAG, "Cannot schedule notification - invalid context");
            return;
        }

        if (task == null || task.trim().isEmpty()) {
            Log.e(TAG, "Cannot schedule notification - empty task");
            return;
        }

        if (dueDate == null) {
            Log.e(TAG, "Cannot schedule notification - null due date");
            Toast.makeText(context, "Please set a due date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set time to 9 AM on due date
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            // If 9 AM already passed, schedule for tomorrow
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        // Create the notification channel
        createNotificationChannel(context);

        // Prepare PendingIntent for broadcast
        Intent notificationIntent = new Intent(context, TaskNotificationReceiver.class);
        notificationIntent.putExtra("task", task);
        notificationIntent.putExtra("notification_id", NOTIFICATION_ID);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "AlarmManager is null");
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Exact alarm permission not granted - prompting user");

                    new AlertDialog.Builder(context)
                            .setTitle("Reminders Need Permission")
                            .setMessage("To remind you exactly when tasks are due, please allow exact alarms in settings.")
                            .setPositiveButton("Open Settings", (d, w) -> {
                                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                                intent.setData(Uri.parse("package:" + context.getPackageName()));
                                startActivity(intent);
                            })
                            .setNegativeButton("Later", null)
                            .show();

                    return;
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            } else {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );
            }

            Log.d(TAG, "Notification scheduled for " + calendar.getTime());
            Toast.makeText(context, "Reminder set for " + formatDate(calendar.getTime()), Toast.LENGTH_SHORT).show();

        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException - missing permission", e);
            Toast.makeText(context, "Couldn't set reminder - check app permissions", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error scheduling notification", e);
            Toast.makeText(context, "Failed to set reminder", Toast.LENGTH_SHORT).show();
        }
    }

    // Format date for display
    private String formatDate(Date date) {
        return SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM).format(date);
    }

    // Create notification channel (Android O+)
    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Reminders for your to-do tasks");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // Open a date picker dialog
    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDueDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay);
                    updateDueDateText();
                }, year, month, day);

        datePickerDialog.show();
    }

    // Update due date text view
    private void updateDueDateText() {
        if (selectedDueDate == null) {
            selectedDueDate = LocalDate.now();
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        dueDatePicker.setText("Due: " + selectedDueDate.format(formatter));
    }

    // Notify the activity when dialog is dismissed
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener)activity).onDialogClose(dialog);
        }
    }

    // Open a dialog to choose task priority
    private void showPriorityDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Priority")
                .setItems(new String[]{"Low", "Medium", "High"}, (dialog, which) -> {
                    selectedPriority = which;
                    updatePriorityButtonText();
                })
                .show();
    }

    // Update priority button text
    private void updatePriorityButtonText() {
        String[] priorities = {"Low", "Medium", "High"};
        priorityButton.setText("Priority: " + priorities[selectedPriority]);
    }
}
