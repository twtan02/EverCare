package my.edu.utar.evercare;

import java.util.UUID;

public class Task {
    private String taskId;
    private String taskTitle;
    private String taskDescription;
    private String dateString;

    public Task() {
        // Default constructor required for Firebase
    }

    public Task(String taskId, String taskTitle, String taskDescription, String dateString) {
        if (taskId == null) {
            // Generate a default value for taskId if it's null
            this.taskId = UUID.randomUUID().toString();
        } else {
            this.taskId = taskId;
        }
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.dateString = dateString;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }
}