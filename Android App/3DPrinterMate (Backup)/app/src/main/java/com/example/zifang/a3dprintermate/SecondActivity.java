package com.example.zifang.a3dprintermate;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

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

public class SecondActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    // References for Firebase connection
    private DatabaseReference dr = FirebaseDatabase.getInstance().getReference();
    private String printerIndex;  // for saving index of printer
    private HashMap firebaseAllData;  // for storing all data from root. If time permits find a way to retrieve from child node "3D Printer Index" only?
    private HashMap printerData;  // for storing indices of all 3D printers



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


        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new MainFragment()).commit();
        navigationView.setCheckedItem(R.id.nav_main);


        // Index of 3D printer logged in is passed as intent method putExtra(). It is
        // received here
        Intent intent = getIntent();
        printerIndex = intent.getStringExtra(getString(R.string.intent_key_printerIndex));

        // constant listener for values for printer with index "printerIndex"
        // note that firebase listeners bby default run in parallel with the app, so we don't
        // have to put this under async task to have the listener constantly running
        dr.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        firebaseAllData = (HashMap) dataSnapshot.getValue();
                        printerData = (HashMap) firebaseAllData.get(printerIndex);

                        // updates status display on fragment_main2.xml
                        TextView statusView = findViewById(R.id.textView3);
                        statusView.setText((String) printerData.get("Status"));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }

    public void backtomain(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(getString(R.string.intent_key_printerIndex),printerIndex);
        startActivity(intent);
    }

    public void backtosecond(){
        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra(getString(R.string.intent_key_printerIndex),printerIndex);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_photo:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CameraFragment()).commit();
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
}
