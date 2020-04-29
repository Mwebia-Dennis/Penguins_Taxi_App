package com.mwebia.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class DriversActivity extends AppCompatActivity implements LocationListener {

    DatabaseReference mDatabaseRef;
    ListView listView;
    ProgressBar progressBar;
    ArrayList<String> uberRequests;
    ArrayAdapter<String> adapter;
    FirebaseAuth mAuth;
    Double distance;
    LocationManager locationManager;
    String provider;
    Location usersLocation;
    String latitude;
    String longitude;
    String riderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drivers);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Requests");
        mAuth = FirebaseAuth.getInstance();
        mAuth = FirebaseAuth.getInstance();
        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progress_bar);
        uberRequests = new ArrayList<>();
        uberRequests.add("Finding new requests...");
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,uberRequests);
        listView.setAdapter(adapter);

        getLocation();
        updateListViewContent();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                Double lat_Double_requester = new GetDistanceFromStringLocation(latitude,longitude,usersLocation.getLatitude(),usersLocation.getLongitude()).lat_Double_requester;
                Double lon_Double_requester = new GetDistanceFromStringLocation(latitude,longitude,usersLocation.getLatitude(),usersLocation.getLongitude()).lon_Doublelon_requester;
                Double lat_Double_user = new GetDistanceFromStringLocation(latitude,longitude,usersLocation.getLatitude(),usersLocation.getLongitude()).lat_Double_user;
                Double lon_Double_user = new GetDistanceFromStringLocation(latitude,longitude,usersLocation.getLatitude(),usersLocation.getLongitude()).lon_Double_user;

                Intent intent  = new Intent(getApplicationContext(),DriversLocation.class);
                intent.putExtra("lat_Double_requester",lat_Double_requester);
                intent.putExtra("lon_Double_requester",lon_Double_requester);
                intent.putExtra("lat_Double_user",lat_Double_user);
                intent.putExtra("lon_Double_user",lon_Double_user);
                intent.putExtra("riderId",riderId);
                startActivity(intent);


            }
        });



    }
    private void getLocation(){

        //getting the current location of the user when the app is started and updating the the userLocation.

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(),true);

        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 400, 1, this);

            usersLocation = locationManager.getLastKnownLocation(provider);


        }
    }




    private void updateListViewContent() {
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot requestDatasnapshot : dataSnapshot.getChildren()){

                    if (requestDatasnapshot.child("DriversId").getValue() == null){

                        riderId =  Objects.requireNonNull(requestDatasnapshot.child("RiderId").getValue()).toString();
                        latitude = Objects.requireNonNull(requestDatasnapshot.child("usersLocation").child("latitude").getValue()).toString();
                        longitude = Objects.requireNonNull(requestDatasnapshot.child("usersLocation").child("longitude").getValue()).toString();

                        GetDistanceFromStringLocation getDistanceFromStringLocation = new GetDistanceFromStringLocation(latitude,longitude,usersLocation.getLatitude(),usersLocation.getLongitude());

                        distance = getDistanceFromStringLocation.distance;
                        uberRequests.clear();
                        uberRequests.add(distance.toString());
                        progressBar.setVisibility(View.INVISIBLE);
                        adapter.notifyDataSetChanged();



                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(DriversActivity.this, "could not get users location and requests", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.logOut){

            mAuth.signOut();
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onLocationChanged(Location location) {

        usersLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}
