package com.example.attendancewithfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


//public class GetLocation extends AppCompatActivity implements LocationListener {
public class GetLocation extends AppCompatActivity  {
    Button continueToHomeButton;
    TextView locationText;
    TextView openText;
    ProgressBar progress;
    ImageView locationImage;

    //UQU: lat:21.33062, long:39.94559
    String Lat="init";
    String Long="init";
    String currentLat = "init";
    String currentLong = "init";

    Boolean locationReady = false;
    String attendanceType;


    CountDownTimer timer;

    Boolean latReady = false;
    Boolean longReady=false;
    Boolean delayFinished=false;

    //DataDownloader task;

    //Location
    LocationManager locationManager;
    LocationListener locationListener;
    Boolean firstConnection =true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        continueToHomeButton = findViewById(R.id.loctionBtn);
        locationText = findViewById(R.id.LocationtextView);
        openText = findViewById(R.id.openTextView);
        progress = findViewById(R.id.progressLocation);
        locationImage = findViewById(R.id.locationImageView);



        //Get the user location------------------------------------
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("Location",location.toString());
                Log.i("Lat : ",Double.toString(location.getLatitude()));
                Log.i("Long : ",Double.toString(location.getLongitude()));

                currentLat = Double.toString(location.getLatitude());
                currentLong = Double.toString(location.getLongitude());

                if(firstConnection) {
                    showEverything();
                    firstConnection= false;
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) { }
            @Override
            public void onProviderEnabled(String s) { }
            @Override
            public void onProviderDisabled(String s) { }
        };
        //-------------------------------------------------------------------

        //Request Location in app permission---------------------------------

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        } else { // if permission is already granted
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        //-------------------------------------------------------------------

        getFirebaseLocation();

        Intent intent = getIntent();
        attendanceType = intent.getStringExtra("Attendance Type");

        hideEverything();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    //--This method Compares the user location with the Firebase Location---------------------

    public Boolean compareLocations(String lat, String lon){

        if (latReady && longReady) {
            if (Lat.regionMatches(0, lat, 0, 5) && Long.regionMatches(0, lon, 0, 5)) {
                return true;
            }
        }
            return false;
    }

    //-------------------------------------------------------

    //This method is called when continue btn is pressed
    public void continueToNext(View view){

        if (compareLocations(currentLat,currentLong )) {

            moveToNext();

        } else {
            setDelay(10000);
        }
    }

    public void moveToNext(){
        Intent mainIntent = new Intent(GetLocation.this, Check_Fingerprint.class);
        mainIntent.putExtra("Attendance Type", attendanceType);
        startActivity(mainIntent);
        finish();
    }

    public void setDelay(int time){

        hideEverything();

        //Countdown interval = onTick method execute every given seconds
        timer = new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
                if (compareLocations(currentLat,currentLong )) {

                    moveToNext();
                }
            }
            public void onFinish() {
                delayFinished = true;
                showEverything();
            }
        };

        timer.start();
    }
    //-------------------------------------------------------

    public void getFirebaseLocation(){

        //get location Latitude from DB ----------------------------------------
        DatabaseReference databaseLocationLatitude = FirebaseDatabase.getInstance().getReference("attendance//Location//Latitude");
        databaseLocationLatitude.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                latReady= true;
                Lat = dataSnapshot.getValue().toString();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //get location Longitude from DB ----------------------------------------
        DatabaseReference databaseLocationLongitude = FirebaseDatabase.getInstance().getReference("attendance//Location//Longitude");
        databaseLocationLongitude.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                longReady = true;
                Long = dataSnapshot.getValue().toString();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    //---------------------------------------------------------------

    //Open google maps intent when the user views the location
    public void openGoogleMaps(View view){

        if (latReady && longReady) {
            String location = Lat +","+ Long;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("geo:"+location));
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {

        Intent mainIntent = new Intent(GetLocation.this, HomeActivity.class);
        startActivity(mainIntent);
        //task.cancel(true);
        finish();
    }


    //--Hide and show views methods-------------

    public void hideEverything(){
        continueToHomeButton.setVisibility(View.GONE);
        locationText.setVisibility(View.GONE);
        openText.setVisibility(View.GONE);
        locationImage.setVisibility(View.GONE);

        progress.setVisibility(View.VISIBLE);

    }

    public void showEverything(){
        continueToHomeButton.setVisibility(View.VISIBLE);
        locationText.setVisibility(View.VISIBLE);
        openText.setVisibility(View.VISIBLE);
        locationImage.setVisibility(View.VISIBLE);

        progress.setVisibility(View.GONE);
    }
}

