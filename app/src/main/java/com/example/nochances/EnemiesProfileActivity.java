package com.example.nochances;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


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
    TextView mClass;
    TextView instructions;
    private ImageView mImageView;
    DatabaseReference database;
    String enemyEmail;// the enemy email
    String activity; // the activity where it comes from
    TextView mColor;  // textView That shows the color
    int selectedColor;   // id of colors in drawable
    String enemyName;      //the name of the enemy
    private String ColorSelectedString;//the color string that will be saved
    private StorageReference storage;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enemies_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackground(new ColorDrawable(getResources().getColor(R.color.light_orange)));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        seekBar=findViewById(R.id.SeekBar);
        mName=findViewById(R.id.name);
        mEmail=findViewById(R.id.email);
        mMajor=findViewById(R.id.major);
        mColor=findViewById(R.id.Dartmouth_class);
        mColor=findViewById(R.id.color_textView);
        instructions=findViewById(R.id.AlarmLevel);
        mImageView = findViewById(R.id.imageProfile);
        storage =FirebaseStorage.getInstance().getReference();
        /*
         * getting intents
         * Activity: where it comes from
         * enemyEmail: the email of the enemy selected
         * enemyNAme: the name of the enemy selected
         * imageUploaded checks if the enmy uploaded a picture
         */
        activity=getIntent().getStringExtra(ENEMY_EMAIL).split(",")[0];
        enemyEmail=getIntent().getStringExtra(ENEMY_EMAIL).split(",")[1];
        enemyName=getIntent().getStringExtra(ENEMY_EMAIL).split(",")[2];
        String color=getIntent().getStringExtra(ENEMY_EMAIL).split(",")[3];
        loadSnap();


        Log.d(TAG,"activity "+ activity);
        Log.d(TAG,"Email "+enemyEmail);

        database= FirebaseDatabase.getInstance().getReference();
        selectedColor=R.color.green;
        seekBar.setProgress(150);
        ColorSelectedString=color;
        /*
        putting the seekbar at the right progress and choosing the right selected color
         */
        switch (color){
            case RED:
                selectedColor=R.color.red;
                seekBar.setProgress(1400);
                break;
            case BLUE:
                selectedColor=R.color.blue;
                seekBar.setProgress(500);
                break;
            case YELLOW:
                seekBar.setProgress(700);
                selectedColor=R.color.yellow;
                break;
            case ORANGE:
                seekBar.setProgress(900);
                selectedColor=R.color.orange;
                break;
        }
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
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                assert enemyEmail != null;
                String userHash="users_"+ constant.md5(enemyEmail);
                Log.d(TAG,userHash);
                Profile profile=Objects.requireNonNull(dataSnapshot.child(userHash).child("profile").getValue(Profile.class));
                Log.d(TAG,profile.toString());
                mEmail.setText("Email: "+profile.getEmail(), TextView.BufferType.NORMAL);
                mName.setText("Name: "+profile.getName(), TextView.BufferType.EDITABLE);
                mMajor.setText("Major: "+profile.getMajor(), TextView.BufferType.EDITABLE);
               // mClass.setText("Class: "+profile.getDartClass());
                //mClass.setText(profile.getDartClass(), TextView.BufferType.EDITABLE);


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
            finish();
            return true;
        }
        else if(id==R.id.change_color){
            // registerUser();
            saveEnemy();
            finish();
            return true;
        }
        else if(id==android.R.id.home){
            finish();
            return true;
        }
        else if(id==R.id.delete){
           deleteEnemy();
           finish();
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
         Toast.makeText(EnemiesProfileActivity.this,"enemy successfully deleted",
                 Toast.LENGTH_LONG).show();
    }

    /**
     * add enemies to firebase
     */
    private void saveEnemy() {
        // get the path to the right user
        String emailHash=constant.md5(FirebaseAuth.getInstance().getCurrentUser().getEmail());
        //enemies_state is a model that contains the enemy Email, color, is it deleted?
        database.child("users_"+emailHash).child("enemies_list").child(constant.md5(enemyEmail))
                .setValue(new enemiesAlarmLevel(enemyName, ColorSelectedString,enemyEmail,false, false))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
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
        ArrayList<ProgressItem> progressItemList = new ArrayList<>();
        // red span
        ProgressItem mProgressItem = new ProgressItem();
        //will use to calculate percentage of color in seekbar
        float totalSpan = 1500;
        //
        float greenSpan = 300;
        mProgressItem.progressItemPercentage = ((greenSpan / totalSpan) * 100);
        Log.i("Mainactivity", mProgressItem.progressItemPercentage + "");
        mProgressItem.color = R.color.green;
        progressItemList.add(mProgressItem);
        // blue span
        mProgressItem = new ProgressItem();
        float blueSpan = 300;
        mProgressItem.progressItemPercentage = (blueSpan / totalSpan) * 100;
        mProgressItem.color = R.color.blue;
        progressItemList.add(mProgressItem);
        // green span
        mProgressItem = new ProgressItem();
        float yellowSpan = 300;
        mProgressItem.progressItemPercentage = (yellowSpan / totalSpan) * 100;
        mProgressItem.color = R.color.yellow;
        progressItemList.add(mProgressItem);
        // yellow span
        mProgressItem = new ProgressItem();
        float orangeSpan = 300;
        mProgressItem.progressItemPercentage = (orangeSpan / totalSpan) * 100;
        mProgressItem.color = R.color.orange;
        progressItemList.add(mProgressItem);
        // greyspan
        mProgressItem = new ProgressItem();
        float redSpan = 300;
        mProgressItem.progressItemPercentage = (redSpan / totalSpan) * 100;
        mProgressItem.color = R.color.red;
        progressItemList.add(mProgressItem);

        seekBar.initData(progressItemList);
        seekBar.invalidate();
    }

    /**
     *  Load Profile photo from internal storage, always called when signed in
     */
    private void loadSnap() {
        //
        //take storage reference

            final long ONE_MEGABYTE = 1024 * 1024 * 10;
            StorageReference fileReference = storage.child("uploads").child(constant.md5(enemyEmail) + ".jpg");

            fileReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    Log.d(TAG, "LoadSnap: success!");
                    //get bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    //put it in the image
                    mImageView.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.d(TAG, "LoadSnap: Failure");
                }
            });
        }

}
