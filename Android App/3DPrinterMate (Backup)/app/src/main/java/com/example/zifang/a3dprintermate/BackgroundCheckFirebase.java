package com.example.zifang.a3dprintermate;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;


public class BackgroundCheckFirebase extends IntentService {

    // References for Firebase connection and listener
    private DatabaseReference dr = FirebaseDatabase.getInstance().getReference();
    private String printerIndex;  // for saving index of printer
    private HashMap firebaseAllData;  // for storing all data from root. If time permits find a way to retrieve from child node "3D Printer Index" only?
    private HashMap printerData;  // for storing indices of all 3D printers
    private ValueEventListener listener;

    // References for data persistence
    SharedPreferences mPreferences;

    // References for Event listener
    public static volatile String listenerState;  // will be changed whether the user has logged in or not; used for ensuring notifs dont surface when user logs out


    public BackgroundCheckFirebase(){
        super("BackgroundCheckFirebase");
    }

    @Override
    protected void onHandleIntent(Intent workIntent){
        try{
            // from data persistence, the index of 3D printer is stored under printerIndex
            mPreferences = getSharedPreferences(getString(R.string.persistence_sharedPrefFile), MODE_PRIVATE);
            final String printerIndex = mPreferences.getString(getString(R.string.persistence_key),getString(R.string.persistence_default_value));

            // constant listener for values for printer with index "printerIndex"
            // note that firebase listeners by default run in parallel with the app, so we don't
            // have to put this under async task to have the listener constantly running
            listener = dr.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.i("ASDF TAG","PING");

                    // if user has not log out, remain in background and give notifs
                    if (listenerState.equals(getString(R.string.background_start_state))){
                        firebaseAllData = (HashMap) dataSnapshot.getValue();
                        printerData = (HashMap) firebaseAllData.get(printerIndex);

                        // updates status display on fragment_main2.xml
                        String printerStatus = (String) printerData.get(getString(R.string.printer_status_key));

                        // if status of printer changed to "offline", give notification
                        if (printerStatus.equals(getString(R.string.printer_status_stopped))){
                            Log.i("ASDF TAG", "PING 2.0");
                            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(BackgroundCheckFirebase.this, "notif_channel_id")
                                    .setSmallIcon(R.drawable.logo)
                                    .setContentTitle("3D Printer Mate")
                                    .setContentText("3D Printer has stopped moving!")
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(BackgroundCheckFirebase.this);

                            // notificationId is a unique int for each notification that you must define
                            notificationManager.notify(123, mBuilder.build());
                        }
                    }
                    // remove event listener once user has logged out
                    else if (listenerState.equals(getString(R.string.background_stop_state))){
                        dr.removeEventListener(this);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

        } catch (Exception e){
            e.printStackTrace();
        }
    }


}