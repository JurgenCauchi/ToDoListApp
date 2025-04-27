package com.example.todolistapp.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.todolistapp.Model.Task;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

// DatabaseHelper class for managing the SQLite database operations
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database constants
    private static final String DATABASE_NAME = "TODO_DATABASE";
    private static final String TABLE_NAME = "TODO_TABLE";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "TASK";
    private static final String COL_3 = "STATUS";
    private static final String COL_4 = "DUEDATE";
    private static final String COL_5 = "PRIORITY";
    private static final int DATABASE_VERSION = 5;

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create the To-Do table if it doesn't exist
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "TASK TEXT, STATUS INTEGER, DUEDATE TEXT, PRIORITY INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Upgrade logic: add PRIORITY column if upgrading from an old version
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN PRIORITY INTEGER DEFAULT 0");
        }
    }

    // Insert a new task into the database
    public long insertTask(Task model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_2, model.getTask());
        cv.put(COL_3, 0); // Default status is 0 (incomplete)
        cv.put(COL_4, model.getDueDate().toString());
        cv.put(COL_5, model.getPriority()); // Add priority
        long id = db.insert(TABLE_NAME, null, cv); // Insert and get row ID
        db.close();
        return id; // Returns -1 if insertion failed
    }

    // Update the priority of a task by ID
    public void updatePriority(int id, int priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_5, priority);
        db.update(TABLE_NAME, cv, "ID=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Update task name, due date, and priority by ID
    public void updateTask(int id, String task, String date, int priority) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, task);
        contentValues.put(COL_4, date);
        contentValues.put(COL_5, priority);
        db.update(TABLE_NAME, contentValues, "ID=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Update the completion status of a task by ID
    public void updateStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_3, status);
        db.update(TABLE_NAME, contentValues, "ID=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Delete a task from the database by ID
    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "ID=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Get all tasks from the database and return them as a list
    public List<Task> getAllTasks() {
        SQLiteDatabase db = this.getWritableDatabase();
        List<Task> modelList = new ArrayList<>();

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME,
                    new String[]{COL_1, COL_2, COL_3, COL_4, COL_5},
                    null, null, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Task task = new Task();
                    task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_1)));
                    task.setTask(cursor.getString(cursor.getColumnIndexOrThrow(COL_2)));
                    task.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COL_3)));

                    // Parse due date
                    String dateString = cursor.getString(cursor.getColumnIndexOrThrow(COL_4));
                    if (dateString != null && !dateString.isEmpty()) {
                        try {
                            task.setDueDate(LocalDate.parse(dateString));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    task.setPriority(cursor.getInt(cursor.getColumnIndexOrThrow(COL_5)));
                    modelList.add(task); // Add task to list
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return modelList; // Return the list of tasks
    }
}
