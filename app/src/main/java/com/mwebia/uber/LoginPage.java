package com.mwebia.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class LoginPage extends AppCompatActivity {

    EditText emailET;
    EditText passwordET;
    FirebaseAuth mAuth;
    DatabaseReference mDatabaseRef;
    FirebaseUser currentUser;
    String userType;
    String id;
    String firebaseUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        emailET = findViewById(R.id.editText);
        passwordET = findViewById(R.id.password);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Users");
        userType = getIntent().getStringExtra("userType");



    }

    public void loginToFirebase(View view){
        if (emailET.getText().toString().isEmpty()){

            emailET.setError("Enter your Email");
            emailET.requestFocus();
        }else if (passwordET.getText().toString().isEmpty()){
            passwordET.setError("Enter your password");
            passwordET.requestFocus();
        }else {

            mAuth.signInWithEmailAndPassword(emailET.getText().toString(),passwordET.getText().toString())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {


                    id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    mDatabaseRef.child(id).child("userId").setValue(id);
                    mDatabaseRef.child(id).child("userType").setValue(userType);

                    if (Objects.equals(userType, "Rider")){
                        openMapsActivity();
                    }else{

                       openDriverActivity();

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(LoginPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    public void signinToFirebase(View view){

        if (emailET.getText().toString().isEmpty()){

            emailET.setError("Enter your Email");
            emailET.requestFocus();
        }else if (passwordET.getText().toString().isEmpty()){
            passwordET.setError("Enter your password");
            passwordET.requestFocus();
        }else {

            mAuth.createUserWithEmailAndPassword(emailET.getText().toString(),passwordET.getText().toString())
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {


                    id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    mDatabaseRef.child(id).child("userId").setValue(id);
                    mDatabaseRef.child(id).child("userType").setValue(userType);

                    if (Objects.equals(userType, "Rider")){
                        openMapsActivity();
                    }else {

                       openDriverActivity();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Toast.makeText(LoginPage.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }



    }

    public void openMapsActivity(){

        Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
    public void openDriverActivity(){
        Intent intent = new Intent(getApplicationContext(),DriversActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUser != null){
            mDatabaseRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot userTypeDataSnapshot : dataSnapshot.getChildren()){

                        //verifying userId from firebase.

                        if (Objects.requireNonNull(userTypeDataSnapshot.child("userId").getValue()).toString().equals(currentUser.getUid())){

                            firebaseUserType = Objects.requireNonNull(userTypeDataSnapshot.child("userType").getValue()).toString();


                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    Toast.makeText(LoginPage.this, "Sorry could not open page,try again later", Toast.LENGTH_SHORT).show();
                }
            });
            if (Objects.equals(firebaseUserType, "Rider")){
                openMapsActivity();
            }else{

                openDriverActivity();
            }

        }
    }
}
