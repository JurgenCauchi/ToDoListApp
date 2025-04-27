package com.example.todolistapp.Model;

import java.time.LocalDate;

public class Task {

    private String task;
    private int id, status, priority;
    private LocalDate dueDate;

    public Task(int id, String task, int status,int priority,LocalDate dueDate) {
        this.id = id;
        this.task = task;
        this.status = status;
        this.dueDate = dueDate;
        this.priority = priority;
    }

    // Default constructor
    public Task() {

    }

    //Getters and Setters
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getTask() {
        return task;
    }
    public void setTask(String task) {
        this.task = task;
    }
    public LocalDate getDueDate() { return dueDate; } // Use Date
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public int getPriority() { return priority;}
    public void setPriority(int priority) { this.priority = priority;}
}