package com.example.attendancewithfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Check_Fingerprint extends AppCompatActivity {


    String attendanceType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check__fingerprint);

        Intent intent = getIntent();
        attendanceType = intent.getStringExtra("Attendance Type");



        Executor newExecutor = Executors.newSingleThreadExecutor();
        FragmentActivity activity = this;


        final BiometricPrompt myBiometricPrompt = new BiometricPrompt(activity, newExecutor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                } else {
                    //Log.d(TAG, "Error occurred");
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);


                Intent mainIntent = new Intent(Check_Fingerprint.this, CheckActivity.class);
                mainIntent.putExtra("Attendance Type", attendanceType);
                startActivity(mainIntent);
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                //Log.d(TAG, "Fingerprint not recognised");
            }


        });

        final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verify your fingerprint")
                .setDescription("Place your finger on the sensor to verify uour finger print")
                .setNegativeButtonText("Cancel")
                .build();


        myBiometricPrompt.authenticate(promptInfo);

        findViewById(R.id.fingerCheck_TextView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myBiometricPrompt.authenticate(promptInfo);
            }
        });

        findViewById(R.id.fingerprint_imageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myBiometricPrompt.authenticate(promptInfo);
            }
        });
       //
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent mainIntent = new Intent(Check_Fingerprint.this, HomeActivity.class);
        startActivity(mainIntent);
        finish();
    }
}

