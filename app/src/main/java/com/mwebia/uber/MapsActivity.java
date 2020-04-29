package com.mwebia.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;
import java.util.zip.Inflater;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    LocationManager locationManager;
    String provider;
    Button requestOrCancelButton;
    TextView infoTextView;
    DatabaseReference mDatabaseRef;
    DatabaseReference mDatabaseRef1;
    FirebaseAuth mAuth;
    String id;
    Boolean requestingUber;
    Location userLocation;
    Location driversLocation;
    String driverId;
    Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        requestOrCancelButton = findViewById(R.id.button);
        requestingUber = false;
        infoTextView = findViewById(R.id.textView);
        id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Requests");
        mDatabaseRef1 = FirebaseDatabase.getInstance().getReference("Users");
        mAuth = FirebaseAuth.getInstance();

        requestOrCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestUber(requestOrCancelButton);
            }
        });


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    //setting up the action bar.


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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();

    }
    private void setUpMap(){

        //getting the current location of the user when the app is started and updating the map and marker.

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(),true);

        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 400, 1, this);

            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
             updateLocation(location);
            }
        }
    }

    //This function is called to check if user had already sent a request in the instance of reopening the app;
    public void checkIfUserSentRequest(){
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    if (dataSnapshot1.child("RiderId").getValue().toString().equals(id)){

                        requestingUber = true;
                        requestOrCancelButton.setText(R.string.cancel_uber);
                        infoTextView.setText(R.string.finding_uber);

                        if(!dataSnapshot1.child("DriverUsername").getValue().toString().equals("")){

                            // get drivers location from firebase and show to user drivers location and update location every
                            //1 minute;
                            // show user distance between user and driver;
                            mDatabaseRef1.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    for (DataSnapshot mDataSnapshot : dataSnapshot.getChildren()){

                                        driversLocation = (Location) mDataSnapshot.child("DriverLocation").getValue();
                                        Log.i("driverslocation",driversLocation.toString());
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {


                                    Log.i("gettingdriverslocation",databaseError.getMessage());
                                }
                            });

                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {


        // Add a marker and move the camera when the user location changes.

        updateLocation(location);
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



    //requesting uber.

    public void requestUber(View view){

        if (!requestingUber){

            mDatabaseRef.child(id).child("RiderId").setValue(id).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    requestOrCancelButton.setText(R.string.cancel_uber);
                    infoTextView.setText(R.string.finding_uber);
                    requestingUber = true;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MapsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            //post users location to firebase.
            postLocationToFirebase();
            setDriversChild();

        }else{

            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot deleteDataSnapshot : dataSnapshot.getChildren()) {

                        if ( Objects.requireNonNull(deleteDataSnapshot.child("RiderId").getValue()).toString().equals(id)){

                           deleteDataSnapshot.getRef().removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                                   requestOrCancelButton.setText(R.string.request_uber);
                                   infoTextView.setText("");
                                   requestingUber = false;
                               }
                           }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   Toast.makeText(MapsActivity.this, "Failed, Please try again later", Toast.LENGTH_SHORT).show();
                               }
                           });
                        }
                        else{
                            Toast.makeText(MapsActivity.this, "Failed, Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    Toast.makeText(MapsActivity.this, "failed to cancel uber,Try again", Toast.LENGTH_SHORT).show();
                    Log.d("database_error",databaseError.getMessage());
                }
            });

        }

    }

    private void setDriversChild() {
        mDatabaseRef.child(id).child("DriversId").setValue("");
    }

    private void updateLocation(final Location location) {

        userLocation = location;
        mMap.clear();

        if (!requestingUber){

            checkIfUserSentRequest();
        }


        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot2 : dataSnapshot.getChildren()){
            //checking if there is a driver who has accepted the request;

                    driverId = dataSnapshot2.child("DriversId").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //checking if there is a driver who has accepted request if not show user his/her location;
        if (driverId.equals("")){

            mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("Your Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));
        }
        if(!driverId.equals("")){
            //checking if the user has requested a uber in the first place;
            if (requestingUber){

                requestOrCancelButton.setVisibility(View.INVISIBLE);
                mDatabaseRef1.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                            //get the drivers id from users class from firebase
                            driverId = dataSnapshot1.child("userId").getValue().toString();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                mDatabaseRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                            //comparing if the drivers id from firebase user class is equal to the drivers id in the request class
                            if (dataSnapshot1.child("DriversId").getValue() == driverId){

                                mDatabaseRef1.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                                            //getting drivers location
                                            driversLocation = (Location) dataSnapshot1.child("DriverLocation").getValue();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                if (driversLocation.getLatitude() != 0 && driversLocation.getLongitude() != 0){

                    //checking if we got drivers location so as to display to user;
                    GetDistanceFromStringLocation getDistanceFromStringLocation = new GetDistanceFromStringLocation(String.valueOf(userLocation.getLatitude()),String.valueOf(userLocation.getLongitude()),driversLocation.getLatitude(),driversLocation.getLongitude());
                    Double distance = getDistanceFromStringLocation.distance;
                    infoTextView.setText(R.string.uber_is + distance.toString() + R.string.km_away);
                    ArrayList<Marker> markers = new ArrayList<>();

                    markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(driversLocation.getLatitude(),driversLocation.getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).title("passengers's location")));
                    markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(userLocation.getLatitude(),userLocation.getLongitude())).title("Your location")));

                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Marker marker : markers) {
                        builder.include(marker.getPosition());
                    }
                    LatLngBounds bounds = builder.build();

                    int padding = 50; // offset from edges of the map in pixels

                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                }
            }

        }

        //constantly updating users location and drivers location
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateLocation(location);
            }
        },5000);
    }

    private void postLocationToFirebase() {

           mDatabaseRef.child(id).child("usersLocation").setValue(userLocation).addOnFailureListener(new OnFailureListener() {
               @Override
               public void onFailure(@NonNull Exception e) {
                   Log.i("location post error",e.getMessage());
               }
           }).addOnSuccessListener(new OnSuccessListener<Void>() {
               @Override
               public void onSuccess(Void aVoid) {
                   Log.i("location post","successful");
               }
           });

    }


    @Override
    protected void onPause() {
        super.onPause();
       // locationManager.removeUpdates(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(),true);
        if (provider != null) {
            locationManager.requestLocationUpdates(provider,400,1,this);
        }
    }
}
