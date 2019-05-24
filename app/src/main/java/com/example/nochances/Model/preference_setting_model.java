package com.example.nochances.Model;

public class preference_setting_model {
    private String radius, alarm_level;
    private boolean isTrackingEnabled;

    public preference_setting_model() {
    }

    public boolean isTrackingEnabled() {
        return isTrackingEnabled;
    }

    public void setTrackingEnabled(boolean trackingEnabled) {
        isTrackingEnabled = trackingEnabled;
    }

    public String getAlarm_level() {
        return alarm_level;
    }

    public void setAlarm_level(String alarm_level) {
        this.alarm_level = alarm_level;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }
}
