package com.example.zifang.a3dprintermate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private EditText Name;
    private TextView Info;
    private Button Login;
    private int counter = 5;

    // References for Firebase connection
    private DatabaseReference dr = FirebaseDatabase.getInstance().getReference();
    private String firebaseAllData;  // for storing all data from root. If time permits find a way to retrieve from child node "3D Printer Index" only?
    private ArrayList printerIndexList;  // for storing indices of all 3D printers

    // References for data persistence; Note that data persistence is for app to remember 3D printer index until we log out
    SharedPreferences mPreferences;

    // Reference to store hashmap
    private HashMap<String, Object> jsonHashmap;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Name = findViewById(R.id.etPrinter);
        Info = findViewById(R.id.Info);
        Login = findViewById(R.id.btnLogin);
        Info.setText("No. of attemps remaining: 5");
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate(Name.getText().toString());
            }
        });


        // initiate shared preferences, a way to store data that last after you close the app
        mPreferences = getSharedPreferences(getString(R.string.persistence_sharedPrefFile), MODE_PRIVATE);


        // if the user has not logged out, sign in the next time the application is opened, we assume Rate_text is either correct index or default index
        String Rate_text = mPreferences.getString(getString(R.string.persistence_key),getString(R.string.persistence_default_value));
        if (!Rate_text.equals(getString(R.string.persistence_default_value))){
            // creation of background intent; background intent handles notifications for any stops in 3D printer movement
            Intent backgroundIntent = new Intent(MainActivity.this, BackgroundCheckFirebase.class);
            BackgroundCheckFirebase.listenerState = getString(R.string.background_start_state);
            startService(backgroundIntent);

            // saving 3D printer ID until log out
            SharedPreferences.Editor preferencesEditor = mPreferences.edit();
            preferencesEditor.putString(getString(R.string.persistence_key), Rate_text);
            preferencesEditor.apply();

            // creation and execution of intent
            Intent intent = new Intent(this,SecondActivity.class);
            intent.putExtra(getString(R.string.intent_key_printerIndex), Rate_text);
            startActivity(intent);
        }


        // Retrieving data from Firebase once, to retrieve all available 3D printers for log in
        // We retrieve all data from root, then the ArrayList that has all indices of 3D printer available.
        dr.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        firebaseAllData = (String) dataSnapshot.getValue();
                        jsonHashmap = jsonParser(firebaseAllData);  // parse json string into hashmap containing printer status, image bitmap, array of index of all printers

                        // store array of index of all printers
                        printerIndexList = (ArrayList) jsonHashmap.get(getString(R.string.json_key_index_array));

                        // store status and image bitmap into data persistence
                        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                        preferencesEditor.putString(getString(R.string.json_key_status), (String) jsonHashmap.get(getString(R.string.json_key_status)));
                        preferencesEditor.putString(getString(R.string.json_key_bitmap_data), (String) jsonHashmap.get(getString(R.string.json_key_bitmap_data)));
                        preferencesEditor.apply();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );


        // Aesthetic log-in page
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.setStatusBarColor(getColor(R.color.mystatusbar));
        }
        else{
            window.setStatusBarColor(getResources().getColor(R.color.mystatusbar));
        }

        ConstraintLayout constraintLayout = findViewById(R.id.layout);
        AnimationDrawable animationDrawable = (AnimationDrawable) constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();
    }

    private void validate(String username){
        // If input a string value that matches index of any 3D printer --> log in success
        if (printerIndexList.contains(username)){

            // creation of background intent; background intent handles notifications for any stops in 3D printer movement
            Intent backgroundIntent = new Intent(MainActivity.this, BackgroundCheckFirebase.class);
            BackgroundCheckFirebase.listenerState = getString(R.string.background_start_state);
            startService(backgroundIntent);

            // saving 3D printer ID until log out
            SharedPreferences.Editor preferencesEditor = mPreferences.edit();
            preferencesEditor.putString(getString(R.string.persistence_key), username);
            preferencesEditor.apply();

            // creation and execution of intent
            Intent intent = new Intent(this,SecondActivity.class);
            intent.putExtra(getString(R.string.intent_key_printerIndex), username);
            startActivity(intent);

        } else{

            // TO-DO: Create toast to show that wrong index has been inserted
            counter--;

            Info.setText("No. of attemps remaining: " + String.valueOf(counter));
            if(counter == 0){
                Login.setEnabled(false);
            }
        }
    }

    @Override
    public void onBackPressed(){
        //do nothing
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
