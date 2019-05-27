package com.example.nochances;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.nochances.Model.Profile;
import com.example.nochances.Model.enemiesAlarmLevel;
import com.example.nochances.fragments.AllUserFragment;
import com.example.nochances.utils.CustomSeekBar;
import com.example.nochances.utils.ProgressItem;
import com.example.nochances.utils.constant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Objects;

public class EnemiesProfileActivity extends AppCompatActivity {
    private static final String ENEMY_EMAIL = "email of enemy";
    private static final String TAG =EnemiesProfileActivity.class.getSimpleName() ;
    public static final String GREEN ="green" ;
    public static final String BLUE ="blue" ;
    public static final String YELLOW ="yellow" ;
    public static final String ORANGE ="orange" ;
    public static final String RED ="red" ;
    TextView mName;
    TextView mEmail;
    CustomSeekBar seekBar;
    TextView mMajor;
    TextView instructions;
    private ImageView mImageView;
    DatabaseReference database;
    String enemyEmail;// the enemy email
    String activity; // the activity where it comes from
    TextView mColor;  // textView That shows the color
    int selectedColor;   // id of colors in drawable
    String enemyName;      //the name of the enemy
    private String ColorSelectedString;//the color string that will be saved
    //will use to calculate percentage of color in seekbar
    private float totalSpan = 1500;
    private float greenSpan = 300;//
    private float blueSpan = 300;
    private float  yellowSpan= 300;
    private float orangeSpan = 300;
    private float redSpan=300;
    private ArrayList<ProgressItem> progressItemList;
    private ProgressItem mProgressItem;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enemies_profile);
        seekBar=findViewById(R.id.SeekBar);
        mName=findViewById(R.id.name);
        mEmail=findViewById(R.id.email);
        mMajor=findViewById(R.id.major);
        mColor=findViewById(R.id.color_textView);
        instructions=findViewById(R.id.AlarmLevel);
        mImageView = findViewById(R.id.imageProfile);

        /*
         * getting intents
         * Activity: where it comes from
         * enemyEmail: the email of the enemy selected
         * enemyNAme: the name of the enemy selected
         */
        activity=getIntent().getStringExtra(ENEMY_EMAIL).split(",")[0];
        enemyEmail=getIntent().getStringExtra(ENEMY_EMAIL).split(",")[1];
        enemyName=getIntent().getStringExtra(ENEMY_EMAIL).split(",")[2];
        Log.d(TAG,"activity "+ activity);
        Log.d(TAG,"Email "+enemyEmail);
        database= FirebaseDatabase.getInstance().getReference();
        selectedColor=R.color.green;
        ColorSelectedString=GREEN;
        instructions.setText(R.string.instructions);
         mColor.setBackgroundResource(selectedColor);

        initDataToSeekbar();
         setColorSeekBar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillInformation();
    }

    public static Intent enemiesEmailIntent(Context context, String email){
        Intent intent= new Intent(context, EnemiesProfileActivity.class);
        intent.putExtra(ENEMY_EMAIL,email);
        return intent;
    }

    /**
     * get info from database to fill in edit text
     */
    public void fillInformation (){

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                assert enemyEmail != null;
                String userHash="users_"+ constant.md5(enemyEmail);
                Log.d(TAG,userHash);
                Profile profile=Objects.requireNonNull(dataSnapshot.child(userHash).child("profile").getValue(Profile.class));
                Log.d(TAG,profile.toString());
                mEmail.setText(profile.getEmail(), TextView.BufferType.NORMAL);
                mName.setText(profile.getName(), TextView.BufferType.EDITABLE);
                mMajor.setText(profile.getMajor(), TextView.BufferType.EDITABLE);
                //mClass.setText(profile.getDartClass(), TextView.BufferType.EDITABLE);

                mEmail.setEnabled(false);
                mName.setEnabled(false);
                mMajor.setEnabled(false);
                
                // ...
                Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
       // loadSnap();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(activity.equals(AllUserFragment.class.getSimpleName())) {
            getMenuInflater().inflate(R.menu.add_enemy, menu);
        }
        else{
            getMenuInflater().inflate(R.menu.delete_enemy, menu);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.add){
           // registerUser();
            saveEnemy();
            return true;
        }
        else if(id==android.R.id.home){
            finish();
            return true;
        }
        else if(id==R.id.delete){
           deleteEnemy();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * delete enemies
     */
    private void deleteEnemy() {
        String emailHash=constant.md5(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        //enemies_state is a model that contains the enemy Email, color, is it deleted?
        database.child("users_"+emailHash).child("enemies_list").child(constant.md5(enemyEmail)).child("deleted").setValue(true);

    }

    /**
     * add enemies to firebase
     */
    private void saveEnemy() {
        // get the path to the right user
        String emailHash=constant.md5(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        //enemies_state is a model that contains the enemy Email, color, is it deleted?
        database.child("users_"+emailHash).child("enemies_list").child(constant.md5(enemyEmail)).
                setValue(new enemiesAlarmLevel(enemyName, ColorSelectedString,enemyEmail,false)).
                addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(EnemiesProfileActivity.this,"enemy successfully added",
                        Toast.LENGTH_LONG).show();
            }

        });
    }

    /**
     * User Take color
     */
    private void setColorSeekBar(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.d(TAG,"seekbarValue "+i);
                covertSeekBarValueToColor(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    /**
     * getting the color depending on the progress level
     * @param value    of progress
     */
    private void covertSeekBarValueToColor(int value){
        if (value<320){
            selectedColor=R.color.green;
            ColorSelectedString=GREEN;
        }
        else if (value>320 && value <= 620){
            selectedColor=R.color.blue;
            ColorSelectedString=BLUE;
        }
        else if (value>620 && value < 920){
            selectedColor=R.color.yellow;
            ColorSelectedString=YELLOW;
        }
        else if (value>920 && value < 1220){
            selectedColor=R.color.orange;
            ColorSelectedString=ORANGE;
        }
        else if (value>1220 && value < 1500){
            selectedColor=R.color.red;
            ColorSelectedString=RED;
        }
        Log.d(TAG,"color "+ColorSelectedString);
        mColor.setBackgroundResource(selectedColor);
    }

    /**
     * setting up the seekbar with the different custom colors
     */
    private void initDataToSeekbar() {
        progressItemList = new ArrayList<>();
        // red span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = ((greenSpan / totalSpan) * 100);
        Log.i("Mainactivity", mProgressItem.progressItemPercentage + "");
        mProgressItem.color = R.color.green;
        progressItemList.add(mProgressItem);
        // blue span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = (blueSpan / totalSpan) * 100;
        mProgressItem.color = R.color.blue;
        progressItemList.add(mProgressItem);
        // green span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = (yellowSpan / totalSpan) * 100;
        mProgressItem.color = R.color.yellow;
        progressItemList.add(mProgressItem);
        // yellow span
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = (orangeSpan / totalSpan) * 100;
        mProgressItem.color = R.color.orange;
        progressItemList.add(mProgressItem);
        // greyspan
        mProgressItem = new ProgressItem();
        mProgressItem.progressItemPercentage = (redSpan / totalSpan) * 100;
        mProgressItem.color = R.color.red;
        progressItemList.add(mProgressItem);

        seekBar.initData(progressItemList);
        seekBar.invalidate();
    }

}
