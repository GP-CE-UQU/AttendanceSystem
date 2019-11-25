package com.example.attendancewithfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerifyEmailActivity extends AppCompatActivity {

    FirebaseUser user;
    FirebaseAuth mAuth;

    ProgressBar progressBar;
    TextView verifyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        progressBar = findViewById(R.id.progressBar);
        verifyText = findViewById(R.id.verifyTextView);
        verifyText.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        // checkVerification();
        sendVerify();

    }


    private void sendVerify() {
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                verifyText.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(VerifyEmailActivity.this, R.string.emailSent, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(VerifyEmailActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void continueVerify(View view){
        sendVerify();
    }

    public void goToLogin(View view){
        Intent mainIntent = new Intent(VerifyEmailActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}
