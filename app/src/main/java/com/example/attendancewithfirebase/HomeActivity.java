package com.example.attendancewithfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//TRue TIME

import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    TextView userNameTextView;
    TextView nationalID_TextView;
    TextView emailTextView;
    ImageView checkOutImageView;
    ImageView checkInImageView;
    ImageView viewListImageView;
    ImageView userImage;
    ImageView backgroundImageView;
    TextView inTextView;
    TextView outTextView;
    //Names
    TextView userName;
    TextView nationalID;
    TextView email;

    Button viewAttendBtn;
    Button checkIndBtn;
    Button checkOutdBtn;

    ProgressBar homeProgress;

    private FirebaseAuth mAuth;
    FirebaseUser currentFirebaseUser;

    String userMac;
    String CurrentDeviceMac = getMacAddr();
    Boolean authorized = false;

    DatabaseReference attendanceDB;
    DatabaseReference databaseID;
    String userFirebaseNationalID;
    String userFirebaseFingerID = "empty";

    //UserID
    final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

    SimpleDateFormat formater;
    String currentDate;
    Boolean checkedIN = false;

    Boolean macReady = false;
    Boolean checkReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        checkOutImageView = findViewById(R.id.checkOutImageView);
        checkInImageView = findViewById(R.id.checkInImageView);
        viewListImageView = findViewById(R.id.viewListImageView);

        animate();

        // initialization
        userNameTextView = findViewById(R.id.userNametextView);
        nationalID_TextView = findViewById(R.id.nationalID_textView);
        emailTextView = findViewById(R.id.emailTextView);
        //---
        viewAttendBtn = findViewById(R.id.viewAttenButton);
        checkIndBtn = findViewById(R.id.check_out_Button);
        checkOutdBtn = findViewById(R.id.check_in_Button);
        userImage = findViewById(R.id.userImageView);
        backgroundImageView = findViewById(R.id.backgroundImageView);
        inTextView = findViewById(R.id.inTextView);
        outTextView = findViewById(R.id.outTextView);
        userName = findViewById(R.id.textView3);
        nationalID = findViewById(R.id.textView6);
        email = findViewById(R.id.textView11);

        homeProgress = findViewById(R.id.homeProgressBar);


        mAuth = FirebaseAuth.getInstance();
        currentFirebaseUser = mAuth.getCurrentUser();


        databaseID = FirebaseDatabase.getInstance().getReference("attendance//uniqueAttendID//" + currentuser);
        userNameTextView.setText(currentFirebaseUser.getDisplayName());
        emailTextView.setText(currentFirebaseUser.getEmail());

        checkIfUserCheckedIn();
        getUserIDs();
        getUserFirebaseMac();
        showUserInfo();

        hideEverything();
        downloadData();
    }


    //Add animation to the views--------------

    private void animate() {
        checkOutImageView.setX(-1000);
        checkOutImageView.animate().translationXBy(1000).setDuration(500);checkOutImageView.setX(-1000);
        checkInImageView.setX(+1000);
        checkInImageView.animate().translationXBy(-1000).setDuration(500);checkOutImageView.setX(-1000);
        viewListImageView.setY(+1000);
        viewListImageView.animate().translationYBy(-1000).setDuration(500);
    }

    // Get the user national id and fingerprint id from the Firebase
    private void getUserIDs() {


        databaseID.child("nationalID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userFirebaseNationalID = dataSnapshot.getValue().toString();

                nationalID_TextView.setText(userFirebaseNationalID);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        databaseID.child("FingerPrintID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userFirebaseFingerID = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void checkIn(View view) {

        //If the user is authorized (mac address is valid)
        if (authorized){
        Intent intent = new Intent(HomeActivity.this, GetLocation.class);
        intent.putExtra("Attendance Type", "in");
        startActivity(intent);
        finish();}
        else{
            Toast.makeText(this, R.string.not_auth, Toast.LENGTH_SHORT).show();
        }
    }

    public void checkOut(View view) {
        checkIfUserCheckedIn();
        if (authorized) {

            if (checkedIN) {

                Intent intent = new Intent(HomeActivity.this, GetLocation.class);
                intent.putExtra("Attendance Type", "out");
                startActivity(intent);
                finish();

            }

             else {
                Toast.makeText(this, "Check in first!", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(this, R.string.not_auth, Toast.LENGTH_SHORT).show();
        }
    }

    public void viewAttendance(View view) {

        if(userFirebaseFingerID != "empty") {
            Intent Intent = new Intent(HomeActivity.this, AttendanceActivity.class);
            Intent.putExtra("Finger ID", userFirebaseFingerID);
            startActivity(Intent);
            finish();
        }
    }

    public void logOut() {

        FirebaseAuth.getInstance().signOut();
        Toast.makeText(HomeActivity.this, "Log out Successful", Toast.LENGTH_LONG).show();

        Intent mainIntent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void showUserInfo() {

        ImageView navUserPhot = findViewById(R.id.userImageView);

        // now we will use Glide to load user image
        // first we need to import the library : https://github.com/bumptech/glide

        Glide.with(this).load(currentFirebaseUser.getPhotoUrl()).into(navUserPhot);
    }


    private void getUserFirebaseMac() {

        databaseID.child("macID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //mac is fetched from DB
                macReady = true;

                userMac = dataSnapshot.getValue().toString();

                if (CurrentDeviceMac.equals(userMac)) {
                    // phone mac and DB mac are the same
                    authorized = true;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }


    // This method gets the mac address of the phone
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


    public void checkIfUserCheckedIn() {

        if (userFirebaseFingerID != "empty") {

            attendanceDB = FirebaseDatabase.getInstance().getReference("attendance//" + userFirebaseFingerID);

            getDateOnly();
            //Check if user checked in the DB ----------------------------------------
            // Check for attendance first
            attendanceDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //check is fetched from DB
                    checkReady = true;
                    if (dataSnapshot.child(currentDate).exists()) {
                        checkedIN = true;
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public void getDateOnly() {
        //---‐//Date Only------------------------------------------
        Date date = new Date(); // this object contains the current date value
        formater = new SimpleDateFormat("yyyy-MM-dd");
        currentDate = formater.format(date);
    }

    //Menu------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logOutItem:
                logOut();
                return true;
            case R.id.exitItem:
                finish();
                System.exit(0);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }



    //--------Async Task---------------------------------------------

    public class DataDownloader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

                while(true){
                    checkIfUserCheckedIn();
                    getUserFirebaseMac();

                    if(macReady && checkReady){
                        break;
                    }
                }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Write some code you want to execute on UI after doInBackground() completes
            showEverything();
            animate();
            //authorized=false;
            //checkedIN=false;
            return ;
        }
    }



    //----
    public void downloadData() {
        // class instance
        DataDownloader task = new DataDownloader();
        task.execute();
    }

    public void hideEverything(){


        homeProgress.setVisibility(View.VISIBLE);

        checkOutImageView.setVisibility(View.GONE);
        checkInImageView.setVisibility(View.GONE);
        viewListImageView.setVisibility(View.GONE);

        // initialization
        userNameTextView.setVisibility(View.GONE);
        nationalID_TextView.setVisibility(View.GONE);
        emailTextView.setVisibility(View.GONE);

        userNameTextView.setVisibility(View.GONE);
        emailTextView.setVisibility(View.GONE);

        viewAttendBtn.setVisibility(View.GONE);
        checkIndBtn.setVisibility(View.GONE);
        checkOutdBtn.setVisibility(View.GONE);
        userImage.setVisibility(View.GONE);
        backgroundImageView.setVisibility(View.GONE);
        inTextView.setVisibility(View.GONE);
        outTextView.setVisibility(View.GONE);
        userName.setVisibility(View.GONE);
        nationalID.setVisibility(View.GONE);
        email.setVisibility(View.GONE);
    }

    public void showEverything(){

        homeProgress.setVisibility(View.GONE);

        checkOutImageView.setVisibility(View.VISIBLE);
        checkInImageView.setVisibility(View.VISIBLE);
        viewListImageView.setVisibility(View.VISIBLE);

        userNameTextView.setVisibility(View.VISIBLE);
        nationalID_TextView.setVisibility(View.VISIBLE);
        emailTextView.setVisibility(View.VISIBLE);

        userNameTextView.setVisibility(View.VISIBLE);
        emailTextView.setVisibility(View.VISIBLE);

        viewAttendBtn.setVisibility(View.VISIBLE);
        checkIndBtn.setVisibility(View.VISIBLE);
        checkOutdBtn.setVisibility(View.VISIBLE);
        userImage.setVisibility(View.VISIBLE);
        backgroundImageView.setVisibility(View.VISIBLE);
        inTextView.setVisibility(View.VISIBLE);
        outTextView.setVisibility(View.VISIBLE);
        userName.setVisibility(View.VISIBLE);
        nationalID.setVisibility(View.VISIBLE);
        email.setVisibility(View.VISIBLE);
    }


}
