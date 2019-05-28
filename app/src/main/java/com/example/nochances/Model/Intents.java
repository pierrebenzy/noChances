package com.example.nochances.Model;


import android.content.Context;
import android.content.Intent;

import com.example.nochances.FakePhoneCall;
import com.example.nochances.ListActivity;
import com.example.nochances.MapsActivity;
import com.example.nochances.settings_activity;
import com.example.nochances.signInActivity;

public class Intents {

    public static final String STOP_FOREGROUND_SERVICE = "stop foreground service";

    public static Intent MapsActivityToEnemiesList(Context context){
        return new Intent(context, ListActivity.class);
    }

    public static Intent NotificationToMapsActivity(Context context) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    public static Intent FakePhoneCallToMapsActivity(Context context) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }
    public static Intent MapsActivityToSettingsActivity(Context context) {
        return new Intent(context, settings_activity.class);
    }

    public static Intent SignInToMapsActivity(Context context) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    public static Intent SettingsToSignIn(Context context) {
        Intent intent = new Intent(context, signInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }
}
