package com.example.ubuntu.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.style.UpdateLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button buttonPart1;
    Button buttonPart2AddPicture;
    Button buttonPart2GetPicture;
    ImageView imageViewPart2;
    String TAG = "my firebase app";

    //TODO 10.1 Get a reference to the root node of the firebase database
    DatabaseReference mRootDatabaseRef = FirebaseDatabase.getInstance().getReference();

    //TODO 10.6 Get a reference to the root note of the firebase storage
    StorageReference storageReference = FirebaseStorage.getInstance().getReference();

    DatabaseReference databaseReferencePart1;
    DatabaseReference databaseReferencePart2;
    DatabaseReference databaseReferenceSampleNodeValue;
    ArrayList<String> randomStrings;

    String CHILD_NODE_PART1 = "Part1";
    String CHILD_NODE_PART2 = "Part2";

    String SAMPLE_NODE  = "Pokemon";
    TextView textViewSampleNodeValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO 10.0 Example
        textViewSampleNodeValue = findViewById(R.id.textViewSampleNodeValue);
        databaseReferenceSampleNodeValue = mRootDatabaseRef.child(SAMPLE_NODE);
        databaseReferenceSampleNodeValue.setValue("Psyduck");

        databaseReferenceSampleNodeValue.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        textViewSampleNodeValue.setText((String) dataSnapshot.getValue());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );


        //TODO 10.2 initialize the array of strings
        randomStrings = new ArrayList<>();
        randomStrings.add("pikachu");
        randomStrings.add("snorlax");
        randomStrings.add("charmander");


        //TODO 10.3 get a reference to the child node
        databaseReferencePart1 = mRootDatabaseRef.child(CHILD_NODE_PART1);

        //TODO 10.4 Get a reference to the “Add a Random Word” button, set up the OnClickListener and upload a random word to firebase.
        buttonPart1 = findViewById(R.id.buttonPart1);

        buttonPart1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // Selects a random string
                Random random = new Random();
                int position = random.nextInt(randomStrings.size());

                databaseReferencePart1.push().setValue(randomStrings.get(position));
            }
        });

        //TODO 10.8 Build a HashMap object with your data
        final Map<String, ImageData> imageDataMap = new HashMap<>();
        imageDataMap.put("image1", new ImageData("sample1", "picture of the SAC"));
        imageDataMap.put("image2", new ImageData("sample2", "Input buttons for com struct"));
        imageDataMap.put("image3", new ImageData("sample3", "showing the wires below the input buttons"));

        //TODO 10.9 Get reference to the root of the child node part 2
        databaseReferencePart2 = mRootDatabaseRef.child(CHILD_NODE_PART2);

        //TODO 10.10 Get reference to the Add Pictures button and write code to upload the HashMap data when button is clicked
        buttonPart2AddPicture = findViewById(R.id.buttonPart2AddPicture);

        Log.i("TAG", "Tag: " + imageDataMap.toString());  // for testing

        buttonPart2AddPicture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

//                databaseReferencePart2.setValue(imageDataMap);

                //TODO 10.11  Loop through each entry in the hashmap and do the necessary to upload the image to firebase
                for (String key: imageDataMap.keySet()){
                    ImageData imageData = imageDataMap.get(key);
                    String path = "images/" + imageData.filename + "jpg";
                    uploadFileToFirebaseStorage(imageData.filename, path);

                    databaseReferencePart2.child(key).child("path").setValue(path);
                }

            }
        });


        //TODO 10.12 Get a reference to the widgets and write code to download an image randomly when the Get Picture button is clicked
        buttonPart2GetPicture = findViewById(R.id.buttonPart2GetPicture);
        imageViewPart2 = findViewById(R.id.imageViewPart2);

        buttonPart2GetPicture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ArrayList<String> keys = new ArrayList<>(imageDataMap.keySet());
                Random r = new Random();
                int position = r.nextInt(keys.size());
                final String searchKey = keys.get(position);
                Log.i(TAG, keys.get(position));

                // Downloading the image
                databaseReferencePart2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            Log.i(TAG, "key:"+ds.getKey());
                            if (searchKey.equals(ds.getKey())){
                                Log.i(TAG, "path:"+ ds.child("path").getValue() );
                                downloadFromFirebaseStorage(
                                        (String) ds.child("path").getValue(),
                                        imageViewPart2);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG,"onStart");

        //TODO 10.5 invoke addValueEventListener on databaseReferencePart1
        //TODO 10.5 get a reference to the LinearLayoutpart1 and dynanmicaly
        databaseReferencePart1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LinearLayout linearLayout = findViewById(R.id.linearLayoutPart1);
                linearLayout.removeAllViews();
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    Log.i(TAG, ds.getKey());
                    Log.i(TAG, (String) ds.getValue());
                    TextView textView = new TextView(MainActivity.this);
                    textView.setText((String) ds.getValue());
                    linearLayout.addView(textView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i("MainActivity","Download is not successful");
            }
        });
    }

    //TODO 10.7 Write a static inner class for Part2

    static class ImageData{

        String filename;
        String description;

        ImageData(String filename, String description){
            this.filename = filename;
            this.description = description;
        }
    }

    void uploadFileToFirebaseStorage(String name, String path){

        int resID = getResources().getIdentifier(name , "drawable", getPackageName());

        Bitmap bitmap = BitmapFactory.decodeResource(
                MainActivity.this.getResources(),
                resID);

        Log.i(TAG, "Res ID " + resID);
        Log.i(TAG, "Bitmap " + bitmap.toString());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        StorageReference imageRef = storageReference.child(path);

        UploadTask uploadTask = imageRef.putBytes(data);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,
                        "cannot upload",
                        Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this,
                        "upload success",
                        Toast.LENGTH_LONG).show();

            }
        });

    }

    void downloadFromFirebaseStorage(String path, final ImageView imageView){

        final StorageReference imageRef = storageReference.child(path);

        final long ONE_MB = 1024*1024;
        imageRef.getBytes(ONE_MB).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {

                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imageView.setImageBitmap(bitmap);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this,
                        "cannot download",
                        Toast.LENGTH_LONG).show();
            }
        });
    }



}
