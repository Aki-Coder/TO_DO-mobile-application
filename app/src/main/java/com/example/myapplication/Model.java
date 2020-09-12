package com.example.myapplication;

public class Model {

    private String task, description, id, date, location;

    public Model () {

    }

    public Model(String task, String description, String id, String date, String location) {
        this.task = task;
        this.description = description;
        this.id = id;
        this.date = date;
        this.location = location;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location   = location;
    }
}
