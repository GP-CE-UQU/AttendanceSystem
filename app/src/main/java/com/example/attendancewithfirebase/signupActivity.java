package com.example.attendancewithfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class signupActivity extends AppCompatActivity {

    EditText emailEditText;
    EditText username;
    EditText nationalID;
    EditText passEditText;
    EditText confirmPassEditText;
    EditText fingerID_EditText;
    Button signUpBtn;
    TextView signupText;

    public String email;
    public String password;
    public String password2;
    public String fingerID;
    public String name;
    public String nID;

    private FirebaseAuth mAuth;
    DatabaseReference databaseMac;
    DatabaseReference databaseN_ID;
    DatabaseReference database_FingerID;

    Boolean idsReady=false;
    List<String> ids = new ArrayList<>();

    ImageView ImgUserPhoto;
    static int PReqCode = 1;
    static int REQUESCODE = 1;
    Uri pickedImgUri = null;

    private ProgressBar loadingProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);



        //ini views
        emailEditText = findViewById(R.id.signupEmailEditText);
        username = findViewById(R.id.usrenameEditText);
        nationalID = findViewById(R.id.idEditText);
        passEditText = findViewById(R.id.signupPassEditText);
        confirmPassEditText = findViewById(R.id.confirmPassEditText);
        fingerID_EditText = findViewById(R.id.fingerIDEditText);
        signUpBtn = findViewById(R.id.signupBtn);
        signupText = findViewById(R.id.signupText);

        loadingProgress = findViewById(R.id.regProgressBar);
        loadingProgress.setVisibility(View.INVISIBLE);


        mAuth = FirebaseAuth.getInstance();

        ImgUserPhoto = findViewById(R.id.signUpPhoto);

        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (Build.VERSION.SDK_INT >= 22) {

                    checkAndRequestForPermission();

                } else {
                    //get pickedImgUri to updateUserInfo
                    openGallery();
                }
            }
        });

        hideEverything();
        downloadData();
    }

    public void signUp(View view) {


        email = emailEditText.getText().toString();
        password = passEditText.getText().toString();
        password2 = confirmPassEditText.getText().toString();
        name = username.getText().toString();
        nID = nationalID.getText().toString();
        fingerID = fingerID_EditText.getText().toString();


        if (email.isEmpty() || name.isEmpty() || nID.isEmpty() || password.isEmpty() || fingerID.isEmpty() || pickedImgUri == null) {
            // something goes wrong : all fields must be filled
            // we need to display an error message
            showMessage("Please Verify all fields");
            signUpBtn.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.INVISIBLE);

        } else {

        if (!password.equals(password2)) {
            Toast.makeText(this, "Verify your password", Toast.LENGTH_SHORT).show();
        } else {


            //check if fingerprint id is not taken
            if (ids.contains(fingerID)) {
                Toast.makeText(this, "Fingerprint ID is not valid", Toast.LENGTH_SHORT).show();
            } else {
                signUpBtn.setVisibility(View.INVISIBLE);
                loadingProgress.setVisibility(View.VISIBLE);
                //if user picked image
                // everything is ok and all fields are filled now we can start creating user account
                // CreateUserAccount method will try to create the user if the email is valid
                CreateUserAccount(email, name, password);
            }
        }
    }



    }


    public void goToLogin(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //finish if you don't want the previous activity to be stacked
        finish();
    }



    private void CreateUserAccount(String email, final String name, String password) {


        // this method create user account with specific email and password

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            // user account created successfully
                            showMessage("Account created");

                            // after we created user account we need to update his profile picture and name
                            updateUserInfo(name, pickedImgUri, mAuth.getCurrentUser());

                            // Save mac address in the database
                            String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            databaseMac = FirebaseDatabase.getInstance().getReference("attendance//uniqueAttendID//" + currentuser + "//macID");
                            databaseMac.setValue(getMacAddr());
                            databaseN_ID = FirebaseDatabase.getInstance().getReference("attendance//uniqueAttendID//" + currentuser + "//nationalID");
                            databaseN_ID.setValue(nID);
                            database_FingerID = FirebaseDatabase.getInstance().getReference("attendance//uniqueAttendID//" + currentuser + "//FingerPrintID");
                            database_FingerID.setValue(fingerID);

                        } else {

                            // account creation failed
                            showMessage("account creation failed" + task.getException().getMessage());
                            signUpBtn.setVisibility(View.VISIBLE);
                            loadingProgress.setVisibility(View.INVISIBLE);

                        }
                    }
                });
    }

    // update user photo and name
    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {

        // first we need to upload user photo to firebase storage and get url

        // Storage directory
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");

        final StorageReference imageFilePath = mStorage.child(pickedImgUri.getLastPathSegment()); //insert last? get the last in the path

        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                //--------------------------------------------

                // image uploaded successfully to the storage
                // now we can get our image url

                // get url & do something after success
                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // uri contains user image url
                        //change request
                        UserProfileChangeRequest profleUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri) // photo is linked to the user
                                .build();


                        // update user display name and photo uri
                        currentUser.updateProfile(profleUpdate) //update user info
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            // user info updated successfully
                                            showMessage("Register Complete");
                                            updateUI();
                                        }

                                    }
                                });
                    }
                });
            }
        });
    }

    private void updateUI() {

        Intent homeActivity = new Intent(getApplicationContext(), VerifyEmailActivity.class);
        startActivity(homeActivity);
        finish();

    }

    // How toast message
    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
    //-------------------------------------------------

    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);
    }

    private void checkAndRequestForPermission() {

        if (ContextCompat.checkSelfPermission(signupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(signupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(signupActivity.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(signupActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }
        } else
            openGallery();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImgUri);
        }
    }

    // This method gets the mac address
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
        }
        return "02:00:00:00:00:00";
    }


    //--------- Get all taken Fingerpint ids
    public void getFingerIDs() {

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference idsRef = rootRef.child("FingerprintIDs");

        //Upload
        //ArrayList<String> ids = new ArrayList<>();
        //for( String friend :ids) {
        //fingers_ids.child("FingerprintIDs").child(friend).setValue(true); }

        //Get---------------------
        //NEW
        DatabaseReference fingerID_DB = FirebaseDatabase.getInstance().getReference("attendance//FingerprintIDs");
        fingerID_DB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String id = ds.getKey();
                    ids.add(id);
                }
                Log.i("ARRAY",ids.toString());
                idsReady = true;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                idsReady = false;
            }
        });
}

//--------Async Task------------------

    public class DataDownloader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            while(true){
                if(!idsReady){
                    getFingerIDs();
                }
                else{
                    break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            showEverything();
            return ;
        }
    }



    //----
    public void downloadData() {
        // Class instance
        signupActivity.DataDownloader task = new signupActivity.DataDownloader();
        task.execute();
    }

    public void hideEverything(){

        loadingProgress.setVisibility(View.VISIBLE);

        emailEditText.setVisibility(View.GONE);
        username.setVisibility(View.GONE);
        nationalID.setVisibility(View.GONE);
        passEditText.setVisibility(View.GONE);
        confirmPassEditText.setVisibility(View.GONE);
        fingerID_EditText.setVisibility(View.GONE);
        signUpBtn.setVisibility(View.GONE);
        signupText.setVisibility(View.GONE);
        ImgUserPhoto.setVisibility(View.GONE);

    }

    public void showEverything(){

        loadingProgress.setVisibility(View.INVISIBLE);

        emailEditText.setVisibility(View.VISIBLE);
        username.setVisibility(View.VISIBLE);
        nationalID.setVisibility(View.VISIBLE);
        passEditText.setVisibility(View.VISIBLE);
        confirmPassEditText.setVisibility(View.VISIBLE);
        fingerID_EditText.setVisibility(View.VISIBLE);
        signUpBtn.setVisibility(View.VISIBLE);
        ImgUserPhoto.setVisibility(View.VISIBLE);
        signupText.setVisibility(View.VISIBLE);

    }
}


