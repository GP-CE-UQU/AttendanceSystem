package com.example.attendancewithfirebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    EditText emailEditText;
    EditText passEditText;
    private FirebaseAuth mAuth;
    FirebaseUser user;


    public void login(View view) {


        String loginEmail = emailEditText.getText().toString();
        String loginPass = passEditText.getText().toString();

        if (!TextUtils.isEmpty(loginEmail) && !TextUtils.isEmpty(loginPass)) {
            //loginProgress.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(loginEmail, loginPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete( Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser().isEmailVerified()) {

                            sendToHome();
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Verify your email first", Toast.LENGTH_SHORT).show();
                        }
                        //finish();

                    } else {
                        String errorMessage = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                    // loginProgress.setVisibility(View.INVISIBLE);
                }
            });
        }
    }


    public void goToSignUp(View view){
        Intent intent = new Intent(this,signupActivity.class);
        startActivity(intent);
        //finish if you don't want the previous activity to be stacked
        finish();
    }
    /*public void goToVerifcation(View view){
        Intent verifyActivity = new Intent(getApplicationContext(),VerifyEmailActivity.class);
        startActivity(verifyActivity);
        finish();
    }*/




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        emailEditText = findViewById(R.id.loginEmailEditText);
        passEditText = findViewById(R.id.loginPassEditText);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
            sendToHome();
        }

        checkEmail();


        //Get the user location------------------------------------
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("Location", location.toString());
                Log.i("Lat : ", Double.toString(location.getLatitude()));
                Log.i("Long : ", Double.toString(location.getLongitude()));


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            @Override
            public void onProviderEnabled(String s) {
            }

            @Override
            public void onProviderDisabled(String s) {
            }
        };
        //-------------------------------------------------------------------

        //Request Location in app permission---------------------------------

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else { // if permission is already granted
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

    }

    public void checkEmail(){
        //mAuth.getCurrentUser().isEmailVerified();
    }
    public void sendToHome() {

        //Intent mainIntent = new Intent(MainActivity.this, GetLocation.class);
        Intent mainIntent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(mainIntent);
        /*Immediately after you start a new activity, using startActivity,
            make sure you call finish() so that the current activity is not stacked behind the new one.*/
        finish();
    }

    /*public void goToEmailVerification(View view) {

        Intent verifyIntent = new Intent(MainActivity.this, VerifyEmailActivity.class);
        startActivity(verifyIntent);
        finish();
    }*/


}
