package com.example.zifang.a3dprintermate;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.JobIntentService;
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

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private EditText Name;
    private TextView Info;
    private Button Login;
    private int counter = 5;

    // References for Firebase connection
    private DatabaseReference dr = FirebaseDatabase.getInstance().getReference();
    private HashMap firebaseAllData;  // for storing all data from root. If time permits find a way to retrieve from child node "3D Printer Index" only?
    private ArrayList printerIndexList = new ArrayList();  // for storing indices of all 3D printers

    // References for data persistence
    // Note that data persistence is for the background processes to use
    SharedPreferences mPreferences;

    // References for background intent
    private static final int RSS_JOB_ID = 1000;




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

        // Retrieving data from Firebase once
        // We retrieve all data from root, then the ArrayList that has all indices of 3D printers
        // available.
        dr.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        firebaseAllData = (HashMap) dataSnapshot.getValue();
                        printerIndexList = (ArrayList) firebaseAllData.get("3D Printer Index");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

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

        // initiate shared preferences, a way to store data that last after you close the app
        mPreferences = getSharedPreferences(getString(R.string.persistence_sharedPrefFile), MODE_PRIVATE);
        String Rate_text = mPreferences.getString(getString(R.string.persistence_key),getString(R.string.persistence_default_value));

        // if the user has not logged out, sign in the next time the application is opened
        if (!Rate_text.equals(getString(R.string.persistence_default_value))){
            validate(Rate_text);
        }
    }

    private void validate(String username){
        // If input a string value that matches index of any 3D printer --> log in success
        if (printerIndexList.contains(username)){

            // saving 3D printer ID until log out
            SharedPreferences.Editor preferencesEditor = mPreferences.edit();
            preferencesEditor.putString(getString(R.string.persistence_key), username);
            preferencesEditor.apply();

            // creating background intent
            Intent mServiceIntent = new Intent(MainActivity.this, BackgroundCheckFirebase.class);
            BackgroundCheckFirebase.listenerState = getString(R.string.background_start_state);
            startService(mServiceIntent);


            // creation and execution of intent
            Intent intent = new Intent(this,SecondActivity.class);
            intent.putExtra(getString(R.string.intent_key_printerIndex), username);
            startActivity(intent);

        } else {

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
}
