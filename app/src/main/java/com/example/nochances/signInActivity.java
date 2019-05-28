package com.example.nochances;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nochances.Model.Intents;
import com.example.nochances.Model.preference_setting_model;
import com.example.nochances.utils.constant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static com.example.nochances.Register.RegisterIntent;
import static com.example.nochances.fragments.settingsPrefFragment.ENABLE_TRACKING;
import static com.example.nochances.fragments.settingsPrefFragment.GENERAL_ALARM_LEVEL;
import static com.example.nochances.fragments.settingsPrefFragment.INNER_RADIUS;
import static com.example.nochances.fragments.settingsPrefFragment.MIDDLE_RADIUS;
import static com.example.nochances.fragments.settingsPrefFragment.OUTER_RADIUS;
import static com.example.nochances.fragments.settingsPrefFragment.RINGTONE_PREFERENCE;
/*
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
*/

public class signInActivity extends AppCompatActivity {

    private static final String TAG = signInActivity.class.getSimpleName();
    EditText mEmail;
    EditText mPassword;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    public  static String EMAIL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("SignInActivity", "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        setTitle("Sign In");
        mEmail = findViewById(R.id.email_sign_in);
        mPassword = findViewById(R.id.password_sign_in);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // ADDED by THEMIS:
        // if current user exists, log in immediately!
        if(currentUser != null) {
            Intent i = Intents.SignInToMapsActivity(signInActivity.this);
            startActivity(i);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        /*
        if(currentUser!=null){
            EMAIL=currentUser.getEmail();
            Intent intent = new Intent(signInActivity.this, settings_activity.class);
            startActivity(intent);
        }*/
    }

    public void StartRegister_activity(View view) {
        startActivity(RegisterIntent(signInActivity.this, false));
    }

    /**
     * When trying to sign in, I just check if the email and password match with what the data I have in storage
     *
     * @param view view
     */
    public void SignInAttempt(View view) {
//        Profile User= new Profile(SignIn.this);
//        String[] words=User.getData().split(",");
        final String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        //       email= email.toLowerCase();
        boolean check = true;
        if (email.isEmpty()) {
            mEmail.setError("Email is empty");
            check = false;
        }
        if (password.isEmpty()) {
            mPassword.setError("This field cannot be empty");
            check = false;
        }
        //sign in
        if (check) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                EMAIL = email;
                                SetUpPreference();
                                Intent intent = Intents.SignInToMapsActivity(signInActivity.this);
                                startActivity(intent);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(signInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }
    }

    /*
     * setting up the preference if the user signed out. create a class in model call preference_setting_model that hold
     * the preferences and use that class to change the preferences in
     */
    public void SetUpPreference() {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users_" + constant.md5(EMAIL)).child("preferences").
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        SharedPreferences preferences = PreferenceManager.
                                getDefaultSharedPreferences(signInActivity.this);
                        SharedPreferences.Editor editor = preferences.edit();
                        preference_setting_model pref = dataSnapshot.getValue(preference_setting_model.class);
                        if(pref != null) {
                            editor.putString(RINGTONE_PREFERENCE, pref.getRingtone());
                            editor.putString(INNER_RADIUS, Integer.valueOf(pref.getInnerRadius()).toString()+" feet");
                            editor.putString(MIDDLE_RADIUS, Integer.valueOf(pref.getMiddleRadius()).toString()+" feet");
                            editor.putString(OUTER_RADIUS, Integer.valueOf(pref.getOuterRadius()).toString()+" feet");
                            editor.putString(GENERAL_ALARM_LEVEL, pref.getAlarm_level());
                            editor.putBoolean(ENABLE_TRACKING, pref.isTrackingEnabled());
                            editor.apply();
                        } else {
                            editor.putString(RINGTONE_PREFERENCE, "Life's Good");
                            editor.putString(INNER_RADIUS, "10 feet");
                            editor.putString(MIDDLE_RADIUS, "40 feet");
                            editor.putString(OUTER_RADIUS, "100 feet");
                            editor.putString(GENERAL_ALARM_LEVEL, "green");
                            editor.putBoolean(ENABLE_TRACKING, true);
                            editor.apply();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}


