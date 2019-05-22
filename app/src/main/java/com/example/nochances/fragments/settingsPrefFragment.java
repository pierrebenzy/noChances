package com.example.nochances.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;



import com.example.nochances.R;
import com.example.nochances.model.preference_setting_model;
import com.example.nochances.signInActivity;
import com.example.nochances.utils.constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class settingsPrefFragment extends PreferenceFragment {
    public static final String RADIUS="radius";
    public static final String GENERAL_ALARM_LEVEL="general_alarm_level";
    public static final String ENABLE_TRACKING="isTrackingEnabled";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
        final ListPreference minimum_radius =(ListPreference)findPreference(RADIUS);
        minimum_radius.setSummary(minimum_radius.getValue()+" feet");
        /*minimum_radius.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                minimum_radius.setV
                minimum_radius.setSummary(minimum_radius.getValue()+" feet");
                return false;
            }
        });
        final ListPreference alarm_level =(ListPreference)findPreference(GENERAL_ALARM_LEVEL);
        alarm_level.setSummary(alarm_level.getValue()+" feet");
        alarm_level.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                alarm_level.setSummary(alarm_level.getValue()+" feet");
                return false;
            }
        });*/

        Preference signOut = findPreference("signout");
        signOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences preferences= PreferenceManager.
                        getDefaultSharedPreferences(getContext());
                 String radius_value=preferences.getString(RADIUS,"5");
                String alarm_level=preferences.getString(GENERAL_ALARM_LEVEL,"green");
                 boolean tracking_enabled=preferences.getBoolean(ENABLE_TRACKING,true);
                //updating the preferences
                preference_setting_model pref=new preference_setting_model();
                pref.setAlarm_level(alarm_level);
                pref.setRadius(radius_value);
                pref.setTrackingEnabled(tracking_enabled);

                DatabaseReference database=FirebaseDatabase.getInstance().getReference();
                String email=FirebaseAuth.getInstance().getCurrentUser().getEmail();
                database.child("users_"+constant.md5(email)).child("preferences").setValue(pref);

                FirebaseAuth.getInstance().signOut();
                Intent i=new Intent(getContext(), signInActivity.class);
                //sent a flag to clear every other activities except sign in
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

                return true;
            }
        });

    }


}
