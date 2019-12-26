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
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


//public class GetLocation extends AppCompatActivity implements LocationListener {
public class GetLocation extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener{
    Button continueToHomeButton;
    TextView locationText;
    TextView openText;
    ProgressBar progress;
    ImageView locationImage;

    //UQU: lat:21.33062, long:39.94559
    String Lat="0.0";
    String Long="0.0";
    String currentLat = "0.0";
    String currentLong = "0.0";

    Boolean moved = false;
    String attendanceType;


    CountDownTimer timer;

    Boolean latReady = false;
    Boolean longReady=false;
    Boolean delayFinished=false;

    //DataDownloader task;

    //Location OLD
    //LocationManager locationManager;
    LocationListener locationListener;
    Boolean firstConnection =true;

    //Double distanceInMeters=0.0;
    Float distanceInMeters;

    //Location NEW
    private static final String TAG = "GetLocation";
    //private TextView mLatitudeTextView;
    //private TextView mLongitudeTextView;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager locationManager;

    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 2 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */

    //private LocationManager locationManager;


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



        //Location NEW
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        //-----------------------------------------------------------------

        //Get the user location------------------------------------
        /*locationListener = new LocationListener() {
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
        };*/
        //-------------------------------------------------------------------

        //Request Location in app permission---------------------------------

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        } else { // if permission is already granted
          //  locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        //-------------------------------------------------------------------

        getFirebaseLocation();

        Intent intent = getIntent();
        attendanceType = intent.getStringExtra("Attendance Type");

        //hideEverything();
        setDelay(10000);
        //Location.distanceBetween();
    }

    //Connect to Google Api Client
    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    //Disconnect to Google Api Client
    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
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

            //distanceInMeters = meterDistanceBetweenPoints(Float.valueOf(Lat),Float.valueOf(Long),Float.valueOf(currentLat),Float.valueOf(currentLong));
            //Log.i("Distance in meters (1):", distanceInMeters.toString());

            distanceInMeters = getDistance( Double.parseDouble(Lat) , Double.parseDouble(Long), Double.parseDouble(lat), Double.parseDouble(lon) );
            Log.i("Distance in meters (2):", distanceInMeters.toString());

            if (distanceInMeters<120) {
                if (Lat.regionMatches(0, lat, 0, 5) && Long.regionMatches(0, lon, 0, 5)) {
                    return true;
                }
            }
        }
            return false;
    }

    //-------------------------------------------------------
    //Location NEW
    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLocation == null){
            startLocationUpdates();
        }
        if (mLocation != null) {

            currentLat = String.valueOf(mLocation.getLatitude());
            currentLong = String.valueOf(mLocation.getLongitude());
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }


    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());

        Log.i("Lat:",String.valueOf(location.getLatitude()));
        currentLat = String.valueOf(mLocation.getLatitude());

        Log.i("Lat:",String.valueOf(location.getLongitude()) );
        currentLong = String.valueOf(mLocation.getLongitude());

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        Log.i("LatLng: ", latLng.toString());

        if (compareLocations(currentLat,currentLong )) {

            moveToNext();
        }
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
        if(!moved) {


            moved = true;
            Intent mainIntent = new Intent(GetLocation.this, Check_Fingerprint.class);
            mainIntent.putExtra("Attendance Type", attendanceType);
            startActivity(mainIntent);
            finish();

        }
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

    /*private double meterDistanceBetweenPoints(float lat_a, float lng_a, float lat_b, float lng_b) {
        float pk = (float) (180.f/Math.PI);

        float a1 = lat_a / pk;
        float a2 = lng_a / pk;
        float b1 = lat_b / pk;
        float b2 = lng_b / pk;

        double t1 = Math.cos(a1) * Math.cos(a2) * Math.cos(b1) * Math.cos(b2);
        double t2 = Math.cos(a1) * Math.sin(a2) * Math.cos(b1) * Math.sin(b2);
        double t3 = Math.sin(a1) * Math.sin(b1);
        double tt = Math.acos(t1 + t2 + t3);

        return 6366000 * tt;
    }*/

    //Get the distance in meters between two locations--------------------------

    private float getDistance(Double lat1, Double lng1,Double lat2,Double lng2 ){
        Location location1 = new Location("location 1");

        location1.setLatitude(lat1);
        location1.setLongitude(lng1);

        Location locationB = new Location("location 2");

        locationB.setLatitude(lat2);
        locationB.setLongitude(lng2);

        float distance = location1.distanceTo(locationB);

        return distance;
    }
}

