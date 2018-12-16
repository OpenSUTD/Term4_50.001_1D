package com.example.zifang.a3dprintermate;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class BackgroundCheckFirebase extends IntentService {

    // References for Firebase connection and listener
    private DatabaseReference dr = FirebaseDatabase.getInstance().getReference();
    private String firebaseAllData;  // for storing all data from root. If time permits find a way to retrieve from child node "3D Printer Index" only

    // References for Event listener
    public static volatile String listenerState;  // will be changed whether the user has logged in or not; used for ensuring notifs dont surface when user logs out

    // Reference to store hashmap
    private HashMap<String, Object> jsonHashmap;


    public BackgroundCheckFirebase(){
        super("BackgroundCheckFirebase");
    }

    @Override
    protected void onHandleIntent(Intent workIntent){

        try{
            // constant listener for values for printer with index "printerIndex"
            // note that firebase listeners by default run in parallel with the app, so we don't
            // have to put this under async task to have the listener constantly running
            dr.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    // if user has not log out, remain in background and give notifs
                    if (listenerState.equals(getString(R.string.background_start_state))){

                        firebaseAllData = (String) dataSnapshot.getValue();
                        jsonHashmap = jsonParser(firebaseAllData);  // parse json string into hashmap containing printer status, image bitmap, array of index of all printers

                        // store status and image bitmap into data persistence
                        String printerStatus = (String) jsonHashmap.get(getString(R.string.json_key_status));

                        // if status of printer changed to "offline", give notification
                        if (printerStatus.equals(getString(R.string.printer_status_stopped))){
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

    private HashMap<String, Object> jsonParser(String input){
        // Purpose: Parses json files into array for output
        HashMap<String, Object> hashMap = new HashMap<>();

        String[] inputSplit_1 = input.split("3D Printer Index");
        String withBitmapNStatus_1 = inputSplit_1[0];
        String withIndexArray_0 = inputSplit_1[1];

        String[] inputSplit_1_0 = withBitmapNStatus_1.split("Image Path");
        String withStatus_1_0 = inputSplit_1_0[0];
        String withBitmap_1_0 = inputSplit_1_0[1];

        // creating ArrayList for 3D printer index
        withIndexArray_0 = withIndexArray_0.substring(4, withIndexArray_0.length()-3);
        String[] indexArray = withIndexArray_0.split(",");
        ArrayList<String> indexArray1 = new ArrayList<>();
        for (String i:indexArray){
            String substring = i.split(":")[1];
            substring = substring.substring(1);
            indexArray1.add(substring);
        }

        // creating status string
        String[] withStatus_2_1 = withStatus_1_0.split("Status");
        String status = withStatus_2_1[1];
        status = status.substring(3,status.length()-3);

        // creating hash string
        String[] withBitmap_2_2 = withBitmap_1_0.split("SRF05");
        String bitmap = withBitmap_2_2[0];
        bitmap = bitmap.substring(3, bitmap.length()-3);

        hashMap.put(getString(R.string.json_key_index_array), indexArray1);
        hashMap.put(getString(R.string.json_key_status), status);
        hashMap.put(getString(R.string.json_key_bitmap_data), bitmap);

        return hashMap;
    }
}