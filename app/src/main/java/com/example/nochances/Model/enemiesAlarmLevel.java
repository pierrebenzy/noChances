package com.example.nochances.Model;


public class enemiesAlarmLevel {
    private String name;
    private String color;
    private String email;
    private boolean deleted;


    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getColor() {
        return color;
    }

    public enemiesAlarmLevel() {

    }
    public enemiesAlarmLevel(String name, String color, String email) {
        this.name=name;
        this.color=color;
        this.email=email;
    }
    public enemiesAlarmLevel(String name, String color, String email, boolean deleted) {
        this.name=name;
        this.color=color;
        this.email=email;
        this.deleted=deleted;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
