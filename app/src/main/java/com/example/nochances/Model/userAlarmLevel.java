package com.example.nochances.Model;


public class userAlarmLevel {
    private String name;
    private int color;

    public int getColor() {
        return color;
    }

    public userAlarmLevel() {

    }
    public userAlarmLevel(String name, int color) {
        this.name=name;
        this.color=color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
