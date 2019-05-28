package com.example.nochances.Model;

public class preference_setting_model {
    private int OuterRadius;
    private int MiddleRadius;
    private int InnerRadius;
    private String alarm_level;
    private boolean isTrackingEnabled;
    private String ringtone;

    public preference_setting_model() { }

    public int getOuterRadius() {
        return OuterRadius;
    }

    public void setOuterRadius(int outerRadius) {
        OuterRadius = outerRadius;
    }

    public int getMiddleRadius() {
        return MiddleRadius;
    }

    public void setMiddleRadius(int middleRadius) {
        MiddleRadius = middleRadius;
    }

    public int getInnerRadius() {
        return InnerRadius;
    }

    public void setInnerRadius(int innerRadius) {
        InnerRadius = innerRadius;
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

    public String getRingtone() {
        return ringtone;
    }

    public void setRingtone(String ringtone) {
        this.ringtone = ringtone;
    }
}
