package com.example.nochances.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;

import com.example.nochances.Model.Intents;
import com.example.nochances.Model.preference_setting_model;
import com.example.nochances.R;
import com.example.nochances.Services.TrackingService;
import com.example.nochances.utils.constant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class settingsPrefFragment extends PreferenceFragment {

    public settingsPrefFragment() {}


    // keys in the settings.xml
    public static final String OUTER_RADIUS="outer_radius";
    public static final String MIDDLE_RADIUS="middle_radius";
    public static final String INNER_RADIUS="inner_radius";
    public static final String GENERAL_ALARM_LEVEL="general_alarm_level";
    public static final String ENABLE_TRACKING="isTrackingEnabled";
    public static final String RINGTONE_PREFERENCE="ringtone_preference_1";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(settingsPrefFragment.this.getContext());

        // get the ringtone preference
        final RingtonePreference ringtonePreference = (RingtonePreference)
                                                    findPreference(RINGTONE_PREFERENCE);
        Uri ringtoneUri = Uri.parse(PreferenceManager.getDefaultSharedPreferences
                (settingsPrefFragment.this.getContext()).getString
                (RINGTONE_PREFERENCE, "Life's Good"));
        ringtonePreference.setSummary(RingtoneManager.getRingtone
                            (settingsPrefFragment.this.getContext(), ringtoneUri).getTitle
                            (settingsPrefFragment.this.getContext()));
        ringtonePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Uri uri = Uri.parse(o.toString());
                ringtonePreference.setSummary(RingtoneManager.getRingtone
                                (settingsPrefFragment.this.getContext(), uri)
                                .getTitle(settingsPrefFragment.this.getContext()));
                return true;
            }
        });

        final ListPreference outer_radius =(ListPreference)findPreference(OUTER_RADIUS);
        outer_radius.setSummary(sharedPreferences.getString(OUTER_RADIUS, "100")+" feet");
        outer_radius.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                outer_radius.setSummary(o.toString()+" feet");
                return true;
            }
        });
        final ListPreference middle_radius =(ListPreference)findPreference(MIDDLE_RADIUS);
        middle_radius.setSummary(sharedPreferences.getString(MIDDLE_RADIUS, "40")+" feet");
        middle_radius.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                middle_radius.setSummary(o.toString()+" feet");
                return true;
            }
        });
        final ListPreference inner_radius =(ListPreference)findPreference(INNER_RADIUS);
        inner_radius.setSummary(sharedPreferences.getString(INNER_RADIUS, "15")+" feet");
        inner_radius.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                inner_radius.setSummary(o.toString()+" feet");
                return true;
            }
        });
        final ListPreference alarm_level =(ListPreference)findPreference(GENERAL_ALARM_LEVEL);
        alarm_level.setSummary(sharedPreferences.getString(GENERAL_ALARM_LEVEL, "green"));
        alarm_level.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                alarm_level.setSummary(o.toString());
                return true;
            }
        });

        final SwitchPreference enabled_tracking = (SwitchPreference) findPreference(ENABLE_TRACKING);
        enabled_tracking.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if(o.toString().equals("true")) {
                    // remember to stop the foreground service!
                    Intent stopIntent = new Intent(settingsPrefFragment.this.getContext(),
                            TrackingService.class);
                    stopIntent.setAction(Intents.STOP_FOREGROUND_SERVICE);
                    settingsPrefFragment.this.getActivity().startService(stopIntent);
                }
                return true;
            }
        });

        Preference signOut = findPreference("signout");
        signOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences preferences= PreferenceManager.
                        getDefaultSharedPreferences(getContext());

                String ringtone = preferences.getString(RINGTONE_PREFERENCE, "Life's Good");
                int outer_radius_value=Integer.parseInt(preferences.getString(OUTER_RADIUS,"15")
                                                            .split(" ")[0]);
                int middle_radius_value=Integer.parseInt(preferences.getString(MIDDLE_RADIUS,"40")
                                                            .split(" ")[0]);
                int inner_radius_value=Integer.parseInt(preferences.getString(INNER_RADIUS,"100")
                                                            .split(" ")[0]);
                String alarm_level=preferences.getString(GENERAL_ALARM_LEVEL,"green");
                boolean tracking_enabled = preferences.getBoolean(ENABLE_TRACKING,true);

                //updating the preferences
                preference_setting_model pref=new preference_setting_model();
                pref.setRingtone(ringtone);
                pref.setAlarm_level(alarm_level);
                pref.setInnerRadius(inner_radius_value);
                pref.setMiddleRadius(middle_radius_value);
                pref.setOuterRadius(outer_radius_value);
                pref.setTrackingEnabled(tracking_enabled);

                DatabaseReference database=FirebaseDatabase.getInstance().getReference();
                String email=FirebaseAuth.getInstance().getCurrentUser().getEmail();
                database.child("users_"+constant.md5(email)).child("preferences").setValue(pref);

                // remember to stop the foreground service!
                Intent stopIntent = new Intent(settingsPrefFragment.this.getContext(),
                                                TrackingService.class);
                stopIntent.setAction(Intents.STOP_FOREGROUND_SERVICE);
                settingsPrefFragment.this.getActivity().startService(stopIntent);


                FirebaseAuth.getInstance().signOut();
                // after signing out, go to main activity
                Intent i = Intents.SettingsToSignIn(settingsPrefFragment.this.getContext());
                startActivity(i);
                return true;
            }
        });

    }


}
