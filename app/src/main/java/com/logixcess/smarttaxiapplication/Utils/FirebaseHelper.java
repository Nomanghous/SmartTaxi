package com.logixcess.smarttaxiapplication.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.logixcess.smarttaxiapplication.Models.Driver;
import com.logixcess.smarttaxiapplication.Models.Passenger;
import com.logixcess.smarttaxiapplication.Models.User;
import com.logixcess.smarttaxiapplication.SmartTaxiApp;

import static android.content.ContentValues.TAG;


public class FirebaseHelper
{
    Context my_context;
    Firebase firebase_instance;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    ValueEventListener valueEventListener;
    public FirebaseHelper(Context context)
    {
        my_context = context;
        firebase_instance = SmartTaxiApp.getInstance().getFirebaseInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();//Constants.Database_Path);

    }
    public void pushUser(final User user, Passenger passenger) {
        Log.d(TAG, "`````` In Push User...");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())//check if user exist
                {
                    User old_user = null;
                    Toast.makeText(my_context,"You are Registered already",Toast.LENGTH_SHORT).show();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        old_user = snapshot.getValue(User.class);
                    }
                    if(user.getPassword()=="")
                    {
                        user.setPassword(old_user.getPassword());
                    }
                }
                else
                {
                    firebase_instance.child("User").child(user.getUser_id()).setValue(user);
                    firebase_instance.child("Passenger").child(passenger.getFk_user_id()).setValue(passenger);
                    Toast.makeText(my_context,"You are Registered Successfully",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        };
        firebase_instance.child("User").child(user.getUser_id()).addListenerForSingleValueEvent(valueEventListener);//call onDataChange   executes OnDataChange method immediately and after executing that method once it stops listening to the reference location it is attached to.



    }
    public void pushUser(final User user, Driver driver) {
    Log.d(TAG, "`````` In Push User...");
    valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot)
        {
            if(dataSnapshot.exists())//check if user exist
            {
                User old_user = null;
                Toast.makeText(my_context,"You are Registered already",Toast.LENGTH_SHORT).show();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    old_user = snapshot.getValue(User.class);
                }
                if(user.getPassword()=="")
                {
                    user.setPassword(old_user.getPassword());
                }
                Toast.makeText(my_context,"Profile Updated Successfully",Toast.LENGTH_SHORT).show();
            }
            else
            {
                firebase_instance.child("User").child(user.getUser_id()).setValue(user);
                firebase_instance.child("Driver").child(driver.getFk_user_id()).setValue(driver);
                Toast.makeText(my_context,"You are Registered Successfully",Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
        }
    };
    firebase_instance.child("User").child(user.getUser_id()).addListenerForSingleValueEvent(valueEventListener);//call onDataChange   executes OnDataChange method immediately and after executing that method once it stops listening to the reference location it is attached to.



}
    public void updateUser(final User user) {
//        firebase_instance.child("User").child(user.getUser_id()).setValue(user);
        Log.d(TAG, "`````` In Push User...");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())//check if user exist
                {
                    User old_user = null;
                    //firebase_instance.child("users").child(user.getUser_id()).setValue(user);
                    //Toast.makeText(my_context,"You are Registered already",Toast.LENGTH_SHORT).show();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        old_user = snapshot.getValue(User.class);
                        //activeUsers.add(snapshot.getValue(User.class));
                    }
                    if(user.getPassword()=="")
                    {
                        user.setPassword(old_user.getPassword());
                    }
                    firebase_instance.child("User").child(user.getUser_id()).setValue(user);
                    Toast.makeText(my_context,"Profile Updated Successfully",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    firebase_instance.child("User").child(user.getUser_id()).setValue(user);
                    Toast.makeText(my_context,"You are Registered Successfully",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        };
        firebase_instance.child("User").child(user.getUser_id()).addListenerForSingleValueEvent(valueEventListener);//call onDataChange   executes OnDataChange method immediately and after executing that method once it stops listening to the reference location it is attached to.



    }
    public void updateToken(final User user) {
        Log.d(TAG, "`````` In Push User...");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())//check if user exist
                {
                    User old_user = null;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren())
                    {
                        old_user = snapshot.getValue(User.class);
                        old_user.setUser_token(user.getUser_token());
                        firebase_instance.child("User").child(old_user.getUser_id()).setValue(old_user);
                    }
                    //Toast.makeText(my_context,"Profile Updated Successfully",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    firebase_instance.child("User").child(user.getUser_id()).setValue(user);
                    //Toast.makeText(my_context,"You are Registered Successfully",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        };
        firebase_instance.child("User").child(user.getUser_id()).addListenerForSingleValueEvent(valueEventListener);//call onDataChange   executes OnDataChange method immediately and after executing that method once it stops listening to the reference location it is attached to.



    }
}
