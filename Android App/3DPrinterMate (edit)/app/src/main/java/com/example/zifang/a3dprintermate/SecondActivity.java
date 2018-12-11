package com.example.zifang.a3dprintermate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;



public class SecondActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    // References for Firebase connection
    private DatabaseReference dr = FirebaseDatabase.getInstance().getReference();
    private String printerIndex;  // for saving index of printer
    private String printerData;  // for storing indices of all 3D printers

    // References for data persistence
    SharedPreferences mPreferences;

    // Reference to store hashmap
    private HashMap<String, Object> jsonHashmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Index of 3D printer logged in is passed as intent method putExtra(). It is
        // received here
        Intent intent = getIntent();
        printerIndex = intent.getStringExtra(getString(R.string.intent_key_printerIndex));

        // initiate shared preferences, a way to store data that last after you close the app
        mPreferences = getSharedPreferences(getString(R.string.persistence_sharedPrefFile), MODE_PRIVATE);


        // constant listener for values for printer with index "printerIndex"
        // note that firebase listeners by default run in parallel with the app, so we don't
        // have to put this under async task to have the listener constantly running
        dr.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        printerData = (String) dataSnapshot.getValue();
                        jsonHashmap = jsonParser(printerData);  // parse json string into hashmap containing printer status, image bitmap, array of index of all printers

                        // updates status display on fragment_main2.xml
                        String printerStatus = (String) jsonHashmap.get(getString(R.string.json_key_status));
                        TextView statusView = findViewById(R.id.textView3);
                        statusView.setText(printerStatus);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

    public void backtomain(){  // when logging out
        // clearing 3D printer id when logging out
        mPreferences = getSharedPreferences(getString(R.string.persistence_sharedPrefFile), MODE_PRIVATE);
        SharedPreferences.Editor preferencesEditor = mPreferences.edit();
        preferencesEditor.putString(getString(R.string.persistence_key), getString(R.string.persistence_default_value));
        preferencesEditor.apply();

        // stopping background activity
        BackgroundCheckFirebase.listenerState = getString(R.string.background_stop_state);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(getString(R.string.intent_key_printerIndex),printerIndex);
        startActivity(intent);
    }

    public void backtosecond(){
        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra(getString(R.string.intent_key_printerIndex),printerIndex);
        startActivity(intent);
    }

    public void tothird(){
        Intent intent = new Intent(this, ThirdActivity.class);
        intent.putExtra(getString(R.string.intent_key_printerIndex),printerIndex);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_photo:
                tothird();
                break;
            case R.id.nav_logout:
                backtomain();
                break;
            case R.id.nav_main:
                backtosecond();
                break;
            case R.id.nav_share:
                Toast.makeText(this,"Share",Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_send:
                Toast.makeText(this,"Send",Toast.LENGTH_SHORT).show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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