package com.mwebia.uber;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    LinearLayout driveLinear;
    LinearLayout rideLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();
        driveLinear = findViewById(R.id.driveLinear);
        rideLinear = findViewById(R.id.rideLinear);

        driveLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getStartedDriver(driveLinear);
            }
        });
        rideLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getStartedRider(rideLinear);
            }
        });
    }


    public void getStartedRider(View view){

        Intent intent = new Intent(getApplicationContext(),LoginPage.class);
        intent.putExtra("userType","Rider");
        startActivity(intent);

    }
    public void getStartedDriver(View view){

        Intent intent = new Intent(getApplicationContext(),LoginPage.class);
        intent.putExtra("userType","Driver");
        startActivity(intent);

    }

}
