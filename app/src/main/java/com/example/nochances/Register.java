package com.example.nochances;





import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


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
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;




public class Register extends AppCompatActivity {

    private static final String SIGNED_IN ="Is_User_signed_in" ;
    private static final int REQUEST_CODE_TAKE_FROM_GALLERY = 1;
    private static final String TAG = Register.class.getSimpleName();
    EditText mName;
    EditText mEmail;
    EditText mPassword;
    EditText mPhone;
    EditText mMajor;
    EditText mClass;
    RadioButton mFemale;
    RadioButton mMale;
    private int mGender=-1;
    private String NameText;
    private String EmailText;
    private String PasswordText;
    private static final String URI_INSTANCE_STATE_KEY = "saved_uri";
    public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;
    private Uri mImageCaptureUri;
    private ImageView mImageView;
    private boolean isTakenFromCamera;
    private Bitmap rotatedBitmap;
    private boolean permission_recieved=false;
    private boolean signedIn;
    private FirebaseAuth mAuth;
    DatabaseReference Database;

    String EMAIL;


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mName=findViewById(R.id.name);
        mEmail=findViewById(R.id.email);
        mPassword=findViewById(R.id.password);
        mPhone=findViewById(R.id.phone);
        mMajor=findViewById(R.id.major);
        mClass=findViewById(R.id.Dartmouth_class);
        mImageView = findViewById(R.id.imageProfile);
        mFemale=findViewById(R.id.female_button);
        mMale=findViewById(R.id.male_button);
        mAuth = FirebaseAuth.getInstance();
        Database= FirebaseDatabase.getInstance().getReference();
        checkPermissions();

        if (savedInstanceState != null) {

            mImageCaptureUri = savedInstanceState.getParcelable(URI_INSTANCE_STATE_KEY);//get uri
            mImageView.setImageURI(mImageCaptureUri);

        }
        else {
            signedIn = getIntent().getBooleanExtra(SIGNED_IN, false);
            if (signedIn) {
                fillInformation();
                setTitle("Profile");
            } else setTitle(R.string.sign_up);
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

    }

    /**
     * get info from database to fill in edit text
     */
    public void fillInformation (){

        Database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                EMAIL=FirebaseAuth.getInstance().getCurrentUser().getEmail();
                assert EMAIL != null;
                String userHash="users_"+ constant.md5(EMAIL);
                Log.d(TAG,userHash);

                mEmail.setText(Objects.requireNonNull(dataSnapshot.child(userHash).child("Profile")
                        .child("email").getValue()).toString(), TextView.BufferType.NORMAL);
                PasswordText= Objects.requireNonNull(dataSnapshot.child(userHash).child("Profile")
                        .child("password").getValue()).toString();
                mPassword.setText(PasswordText, TextView.BufferType.EDITABLE);
                mName.setText(Objects.requireNonNull(dataSnapshot.child(userHash).child("Profile")
                        .child("name").getValue()).toString(), TextView.BufferType.EDITABLE);
                mPhone.setText(Objects.requireNonNull(dataSnapshot.child(userHash).child("Profile")
                        .child("phone Number").getValue()).toString(), TextView.BufferType.EDITABLE);
                mMajor.setText(Objects.requireNonNull(dataSnapshot.child(userHash).child("Profile")
                        .child("major").getValue()).toString(), TextView.BufferType.EDITABLE);
                mClass.setText(Objects.requireNonNull(dataSnapshot.child(userHash).child("Profile")
                        .child("class").getValue()).toString(), TextView.BufferType.EDITABLE);
                mGender=Integer.parseInt(Objects.requireNonNull(dataSnapshot.child(userHash).child("Profile")
                        .child("gender").getValue()).toString());
                if(mGender==0) mFemale.setChecked(true);
                else if(mGender==1)  mMale.setChecked(true);
                mEmail.setEnabled(false);
                // ...

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        });
        loadSnap();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(signedIn){
            EMAIL= Objects.requireNonNull(mAuth.getCurrentUser()).getEmail();
        }


    }

    public void ButtonRadioCheck(View view){
        boolean isCheck=((RadioButton)view).isChecked();
        if(view.getId()==R.id.female_button){
            if(isCheck) mGender=0;
        }
        else if(view.getId()==R.id.male_button){
            if (isCheck) mGender=1;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(URI_INSTANCE_STATE_KEY, mImageCaptureUri);

    }

    public void registerUser(){
        NameText= mName.getText().toString();
        EmailText= mEmail.getText().toString();
        PasswordText= mPassword.getText().toString();
        registration();

    }
    /** tells if the user is signed in*/
    public static Intent RegisterIntent(Context context, boolean signedIn){
        Intent intent= new Intent(context, Register.class);
        intent.putExtra(SIGNED_IN,signedIn);
        return intent;
    }

    /**
     * helper function that checks if the email and password are valid when the user cliks on register
     */
    public void registration(){

        boolean check=true;
        if (EmailText.isEmpty()){
            mEmail.setError("Email is empty");
            check=false;
        }

        if(PasswordText.isEmpty()){
            mPassword.setError("This field cannot be empty");
            check=false;
        }

        if (NameText.isEmpty()){
            mName.setError(" is empty");
            check=false;
        }
        if (mGender==-1){
            check=false;
            Toast.makeText(Register.this," Gender is required"
                    ,Toast.LENGTH_LONG).show();
        }

        String email=mEmail.getText().toString();
        String password=mPassword.getText().toString();
        if(check) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                saveProfileEntries();
                                onSaveClicked();
                                /*

                                 */
                            }
                            else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Register.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }

                            // ...
                        }
                    });
        }
    }

    /**
     * if save is called when the user is signed in , if passord is changed the user is signed out
     */

    public void editProfile(){

        Log.d(TAG, "saved Password: "+PasswordText);
        if(!PasswordText.equals(mPassword.getText().toString())) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            String newPassword = mPassword.getText().toString();

            assert user != null;
            user.updatePassword(newPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                saveProfileEntries();
                                Log.d(TAG, "User password updated.");
                                Toast.makeText(Register.this, "User password updated.",
                                        Toast.LENGTH_SHORT).show();
                                saveSnap();
                                Intent intent= new Intent(Register.this, signInActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Register.this, "password is not updated.",
                                        Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    });
        }
        else{
            saveProfileEntries();
            onSaveClicked();
        }
    }
//saving the entries in the database
    public void saveProfileEntries(){

        String userHash="users_"+ constant.md5(mEmail.getText().toString());
        Log.d(TAG,"words[0} "+userHash);
        Database.child(userHash).child("Profile").child("email").setValue(mEmail.getText().toString());
        Database.child(userHash).child("Profile").child("name").setValue(mName.getText().toString());
        Database.child(userHash).child("Profile").child("gender").setValue(mGender);
        Database.child(userHash).child("Profile").child("password").setValue(mPassword.getText().toString());
        Database.child(userHash).child("Profile").child("class").setValue(mClass.getText().toString());
        Database.child(userHash).child("Profile").child("major").setValue(mMajor.getText().toString());
        Database.child(userHash).child("Profile").child("phone Number").setValue(mPhone.getText().toString());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(signedIn) {
            getMenuInflater().inflate(R.menu.edit_profile, menu);
        }
        else{
            getMenuInflater().inflate(R.menu.register_menu, menu);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.registrationItem){
            registerUser();
            return true;
        }
        else if(id==android.R.id.home){
            finish();
            return true;
        }
        else if(id==R.id.profile_edit){
            editProfile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onChangePhotoClicked(View v) {
        // changing the Profile image, show the dialog asking the user
        // to choose between taking a picture
        // Go to MyRunsDialogFragment for details.
        Log.d(TAG, "permission recieved:" +permission_recieved);
        if(permission_recieved) displayDialog(MyRunsDialogFragment.DIALOG_ID_PHOTO_PICKER);
        else checkPermissions(v);

    }

    /**
     * this method is called by on changePhotoCLicked
     * @param v view
     */
    private void checkPermissions(View v) {

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
        }
        else{
            permission_recieved=true;// if check for permission returns that permission is granted then
            onChangePhotoClicked(v);
        }
    }

    /**
     * this method is called as the register activity is launched
     */
    private void checkPermissions() {

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            permission_recieved=true;
        }
        else if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED){
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)||shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //Show an explanation to the user *asynchronously*
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("This permission is important for the app.")
                        .setTitle("Important permission required");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);

                    }
                });
                // requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
            }//Never ask again and handle your app without permission.


        }
    }


    // ****************** button click callbacks ***************************//

    public void onSaveClicked() {
        // Save picture
        saveSnap();
        // Making a "toast" informing the user the picture is saved.
        Toast.makeText(getApplicationContext(),
                "Profile Saved",
                Toast.LENGTH_SHORT).show();
        // Close the activity
        finish();
    }


    // Handle data after activity returns.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_CODE_TAKE_FROM_CAMERA:
                // Send image taken from camera for cropping
                beginCrop(mImageCaptureUri);
                break;

            case Crop.REQUEST_CROP: //We changed the RequestCode to the one being used by the library.
                // Update image view after image crop
                handleCrop(resultCode, data);

                // Delete temporary image taken by camera after crop.
                if (isTakenFromCamera) {
                    File f = new File(Objects.requireNonNull(mImageCaptureUri.getPath()));
                    if (f.exists())
                        f.delete();
                }

                break;
            case REQUEST_CODE_TAKE_FROM_GALLERY:

                mImageCaptureUri=data.getData();
                beginCrop(mImageCaptureUri);
                break;
        }
    }

    // ******* Photo picker dialog related functions ************//

    public void displayDialog(int id) {
        DialogFragment fragment = MyRunsDialogFragment.newInstance(id);
        fragment.show(getFragmentManager(),
                getString(R.string.dialog_fragment_tag_photo_picker));
    }

    public void onPhotoPickerItemSelected(int item) {
        Intent intent;

        switch (item) {

            case MyRunsDialogFragment.ID_PHOTO_PICKER_FROM_CAMERA:
                // Take photo from camera，
                // Construct an intent with action
                // MediaStore.ACTION_IMAGE_CAPTURE
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Construct temporary image path and name to save the taken
                // photo
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                mImageCaptureUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                intent.putExtra("return-data", true);
                try {
                    // Start a camera capturing activity
                    // REQUEST_CODE_TAKE_FROM_CAMERA is an integer tag you
                    // defined to identify the activity in onActivityResult()
                    // when it returns
                    startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                isTakenFromCamera = true;
                break;
            case MyRunsDialogFragment.DIALOG_ID_PHOTO_PICKER:
                Intent intent2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent2.setType("image/*");
                startActivityForResult(intent2,REQUEST_CODE_TAKE_FROM_GALLERY);

            default:
                return;
        }

    }
    private void loadSnap() {


        // Load Profile photo from internal storage
        try {
            FileInputStream fis = openFileInput(getString(R.string.profile_photo_file_name));
            Bitmap bmap = BitmapFactory.decodeStream(fis);
            mImageView.setImageBitmap(bmap);
            fis.close();
        } catch (IOException e) {
            // Default Profile photo if no photo saved before.
            mImageView.setImageResource(R.drawable.ic_add_a_photo_black_24dp);
        }
    }

    private void saveSnap() {

        // Commit all the changes into preference_setting_model file
        // Save Profile image into internal storage.
        mImageView.buildDrawingCache();
        Bitmap bmap = mImageView.getDrawingCache();
        try {
            FileOutputStream fos = openFileOutput(getString(R.string.profile_photo_file_name), MODE_PRIVATE);
            bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {

            Uri uri = Crop.getOutput(result);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                mImageView.setImageBitmap(imageOrientationValidator(bitmap, uri.getPath()));
                //               mImageCaptureUri = mImageView.
                //               mImageCaptureUri = uri;
            }catch (Exception e){
                Log.d("Error", "error");
            }

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    // code to handle image orientation issue -- sometimes the orientation is not right on the imageview
    // https://github.com/jdamcd/android-crop/issues/258
    private Bitmap imageOrientationValidator(Bitmap bitmap, String path) {
        ExifInterface ei;
        try {
            ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            rotatedBitmap = null;
            switch(orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);

                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);

                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);

                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;

                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return rotatedBitmap;
    }

    private Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

}