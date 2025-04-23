package com.example.final_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.FirebaseDatabaseKtxRegistrar;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.google.firebase.storage.ListResult;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth auth;
    Button button;
    TextView textViewName;
    FirebaseUser user;
    DatabaseReference reference;
   String userID;


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference users=db.collection("users");
    ImageView meditationIcon,musicIcon;
    ImageView dailyQuoteImage;
    FirebaseStorage storage;
    StorageReference storageRef;

    @SuppressLint("MissingInflatedId")


    
    
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        auth =FirebaseAuth.getInstance();
        button=findViewById(R.id.logout);
        meditationIcon=findViewById(R.id.meditationIcon);
        musicIcon=findViewById(R.id.musicIcon);
        dailyQuoteImage = findViewById(R.id.dailyQuoteImage);
// מאתחל פיירבייס
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("daily_qoutes");  // Folder name in Firebase Storage

        // Fetch and display a random image
        fetchRandomImage();

        meditationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Meditation.class);
                startActivity(intent);
                finish();
            }

        });
        musicIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Music.class);
                startActivity(intent);
                finish();
            }

        });

        textViewName = findViewById(R.id.user_details);
        user=auth.getCurrentUser();
        user=FirebaseAuth.getInstance().getCurrentUser();


        if (user==null){

            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        } else{


            textViewName.setText("Hello "+user.getDisplayName());

        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), HomePageScreen.class);
                startActivity(intent);
                finish();
            }
        });




    }/*
    // Function to fetch and display a random image
    private void fetchRandomImage() {
        storageRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> imageRefs = new ArrayList<>(listResult.getItems());

            if (!imageRefs.isEmpty()) {
                Random random = new Random();
                StorageReference randomImageRef = imageRefs.get(random.nextInt(imageRefs.size()));

                randomImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("Firebase", "Image URI: " + uri.toString());
                    Picasso.get().load(uri).into(dailyQuoteImage);
                }).addOnFailureListener(e -> {
                    Log.e("Firebase", "Error getting download URL", e);
                });
            } else {
                Log.e("Firebase", "No images found in the daily_quotes folder");
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Error fetching images", e);
        });
    }*/

    // Function to fetch and display a random image
    private void fetchRandomImage() {
        storageRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> imageRefs = new ArrayList<>(listResult.getItems());

            if (!imageRefs.isEmpty()) {
                // Preload all images
                for (StorageReference imageRef : imageRefs) {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Picasso.get().load(uri).fetch(); // Preload image into cache
                    });
                }

                // Select a random image after preloading
                Random random = new Random();
                StorageReference randomImageRef = imageRefs.get(random.nextInt(imageRefs.size()));

                randomImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("Firebase", "Image URI: " + uri.toString());
                    Picasso.get().load(uri).into(dailyQuoteImage); // Load image from cache
                }).addOnFailureListener(e -> {
                    Log.e("Firebase", "Error getting download URL", e);
                });
            } else {
                Log.e("Firebase", "No images found in the daily_quotes folder");
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Error fetching images", e);
        });
    }

}