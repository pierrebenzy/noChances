package com.example.nochances.Model;


import android.content.Context;
import android.content.Intent;

import com.example.nochances.FakePhoneCall;
import com.example.nochances.ListActivity;
import com.example.nochances.MapsActivity;

public class Intents {

    public static Intent MapsActivityToEnemiesList(Context context){
        return new Intent(context, ListActivity.class);
    }

    public static Intent NotificationToMapsActivity(Context context) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public static Intent MapsActivityToFakePhoneCall(Context context){
        Intent intent = new Intent(context, FakePhoneCall.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public static Intent FakePhoneCallToMapsActivity(Context context) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }
}
