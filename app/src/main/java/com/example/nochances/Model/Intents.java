package com.example.nochances.Model;


import android.content.Context;
import android.content.Intent;
import com.example.nochances.ListActivity;

public class Intents {

    public static Intent MapsActivityToEnemiesList(Context context){
        Intent intent = new Intent(context, ListActivity.class);
        return intent;
    }
}
