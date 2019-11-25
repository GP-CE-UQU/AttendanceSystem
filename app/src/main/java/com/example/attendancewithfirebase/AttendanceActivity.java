package com.example.attendancewithfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AttendanceActivity extends AppCompatActivity {

    ListView listViewAttendance;

    //a list to store all the attendance from firebase database
    List<Attendance> attendance;

    //database reference object
    DatabaseReference databaseAttendace;

    String FIngerID;


////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        setTitle(R.string.viewAttendanceList);

        Intent intent = getIntent();
        FIngerID = intent.getStringExtra("Finger ID");

        databaseAttendace = FirebaseDatabase.getInstance().getReference("attendance//"+FIngerID);

        listViewAttendance = findViewById(R.id.listView);

        //list to store attendance
        attendance = new ArrayList<>();
    }


    //--------------------------------------------------------------

    @Override
    protected void onStart() {
        super.onStart();

        databaseAttendace.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                attendance.clear();

                // All values
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                    //Getting attendance
                    Attendance attend = postSnapshot.getValue(Attendance.class);

                    //Adding attendance to the list
                    attendance.add(attend);
                }

                //New Adapter
                AttendanceAdapter adapter = new AttendanceAdapter(AttendanceActivity.this, attendance);
                //Set the adapter
                listViewAttendance.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(AttendanceActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}