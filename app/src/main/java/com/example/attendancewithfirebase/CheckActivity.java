package com.example.attendancewithfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.instacart.library.truetime.TrueTime;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CheckActivity extends AppCompatActivity {

    EditText nationalID_EditText;
    Button submitBtn;
    Boolean timeReady = false;
    Boolean idReady = false;
    Boolean fingeridReady = false;
    String attendanceType;
    String userFirebaseID="empty";
    String userFirebaseFingerID="empty";

    DatabaseReference databaseAttendace;
    DatabaseReference databaseIDs;

    String currentTime;

    SimpleDateFormat formater;
    String currentDate;

    //UserID
    final String currentuser = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        submitBtn= findViewById(R.id.submitButton);

        nationalID_EditText = findViewById(R.id.id_verifyeEditText);

        Intent intent = getIntent();
        attendanceType = intent.getStringExtra("Attendance Type");

        //Set activity title
        if (attendanceType.matches("in") ) {
           setTitle(R.string.check_in);
        } else {
            setTitle(R.string.check_out);
        }

        databaseAttendace = FirebaseDatabase.getInstance().getReference("attendance");
        databaseIDs = FirebaseDatabase.getInstance().getReference("attendance//uniqueAttendID//"+currentuser);

        verifyID();
        getFirebaseTime();
    }

    public void submitAttendance(View view){

        if (userFirebaseID.matches( nationalID_EditText.getText().toString() ) && idReady && fingeridReady) {
            //Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show();

            if (attendanceType.matches("in") && timeReady) {
                addAttendanceIn();
            } else if (timeReady) {
                addAttendanceOut();
            }
        } else{
            Toast.makeText(this, "Wrong ID!", Toast.LENGTH_SHORT).show();
        }
    }

    public void cancelAttendance(View view){

        Intent intent = new Intent(CheckActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }


    private void addAttendanceIn() {

        String id = currentDate;

        final Attendance attend = new Attendance(id,currentTime,null);

        //----------------------------//

        // Check for attendance first
        databaseAttendace.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // If there is no attendance
                if(! dataSnapshot.child(userFirebaseFingerID).child(currentDate).exists()){

                    databaseAttendace.child(userFirebaseFingerID).child(currentDate).setValue(attend);
                    Toast.makeText(CheckActivity.this, "Done!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CheckActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else{
                    Toast.makeText(CheckActivity.this, R.string.alreadyCheckedIn, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void addAttendanceOut() {

        // Check for attendance first
        databaseAttendace.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // If there is no attendance
                if(! dataSnapshot.child(userFirebaseFingerID).child(currentDate).child("dateOut").exists()){

                    //Saving the Out
                    // Make sure its the correct node
                    databaseAttendace.child(userFirebaseFingerID).child(currentDate).child("dateOut").setValue(currentTime);
                    Toast.makeText(CheckActivity.this, "Done!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CheckActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else{
                    Toast.makeText(CheckActivity.this, R.string.alreadyCheckedOut, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public void verifyID(){

        databaseIDs.child("nationalID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userFirebaseID = dataSnapshot.getValue().toString();
                idReady = true;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                idReady = false;
            }
        });

        databaseIDs.child("FingerPrintID").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userFirebaseFingerID = dataSnapshot.getValue().toString();
                fingeridReady = true;
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                fingeridReady = false;
            }
        });
    }


    //NEW------------------------------

    public void getFirebaseTime(){


        //Firebase time
        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");

        offsetRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot snapshot) {
                double offset = snapshot.getValue(Double.class);
                double estimatedServerTimeMs = System.currentTimeMillis() + offset;

                Double millis = estimatedServerTimeMs % 1000;
                Double second = (estimatedServerTimeMs / 1000) % 60;
                Double minute = (estimatedServerTimeMs / (1000 * 60)) % 60;
                Double hour = (estimatedServerTimeMs / (1000 * 60 * 60)) % 24;
                hour +=3; //KSA Time

                //String time = String.format("%02f:%02f:%02f.%f", hour, minute, second, millis);

                //Get time and remove the ( . )
                String stringHour = fixTime( hour.toString() , true);
                String stringMinute = fixTime( minute.toString() , false);
                String stringSecond = fixTime( second.toString() , false);


                currentTime = (stringHour  +":"+  stringMinute  +":"+stringSecond );
                timeReady=true;
                Log.i("ftime Minute",currentTime);
                submitBtn.setEnabled(true);
                getDateOnly();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Listener was cancelled");
            }
        });
    }

    public String fixTime(String time,Boolean isHour) {

        time = time.substring(0,2);

        // if hour is bigger than 24 fix it
        if (isHour){
            switch (time){

                case "24": //when fb time is 21
                    time= "00";
                    break;

                case "25": //when fb time is 22
                    time= "01";
                    break;

                case "26": //when fb time is 23
                    time= "02";
                    break;

                case "00": //when fb time is 00
                    time= "03";
                    break;

            }
        }

        // Fix Dot (.)
        if (time.endsWith(".")) {
            time = time.substring(0,1);
            time = "0"+time;
            return time;
        } else {
            return time;
        }
    }

    public void getDateOnly() {
        //---‐//Date Only------------------------------------------
        Date date = new Date(); // this object contains the current date value
        formater = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        currentDate = formater.format(date);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CheckActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
