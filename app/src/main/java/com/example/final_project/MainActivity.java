package com.example.final_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// this classes screen is seen after a successful registration or login

public class MainActivity extends AppCompatActivity {

    // Firebase authentication instance
    FirebaseAuth auth;
    // Logout button
    Button button;
    // TextView to display user's name
    TextView textViewName;
    // Currently signed-in Firebase user
    FirebaseUser user;
    // Firestore database reference
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference users = db.collection("users");

    // UI elements for navigation
    ImageView meditationIcon, musicIcon;

    // a variable for the quotes
    ImageView dailyQuoteImage;

    // Firebase Storage references
    FirebaseStorage storage;
    StorageReference storageRef;

    @SuppressLint("MissingInflatedId")



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main); // opens the screen of the main activity class


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();
        // Initialize UI elements
        button = findViewById(R.id.logout);
        meditationIcon = findViewById(R.id.meditationIcon);
        musicIcon = findViewById(R.id.musicIcon);
        dailyQuoteImage = findViewById(R.id.dailyQuoteImage);

        // Initialize Firebase Storage and reference to daily quotes folder
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("daily_qoutes"); // gets access to the daily quotes folder

        // Fetch and display a random daily quote image
        fetchRandomImage();

        // Set onClickListener for Meditation section
        meditationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Meditation Activity
                Intent intent = new Intent(getApplicationContext(), Meditation.class);
                startActivity(intent);
                finish(); // closes the main activity screen
            }
        });

        // Set onClickListener for Music section
        musicIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Music Activity
                Intent intent = new Intent(getApplicationContext(), Music.class);
                startActivity(intent);
                finish(); // closes the main activity screen
            }
        });

        // Setup the greeting text with user's display name
        textViewName = findViewById(R.id.user_details);
        user = auth.getCurrentUser();

            String displayName = user.getDisplayName();
            // checks if the display name is not empty
            if (displayName != null && !displayName.isEmpty()) {
                textViewName.setText("Hello " + displayName); // shows Hello and the display name
            } else {
                textViewName.setText(""); // shows empty text box
            }


        // set onClickListener for the logout button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut(); // signing out in firebase
                // navigates to home page screen class
                Intent intent = new Intent(getApplicationContext(), HomePageScreen.class);
                startActivity(intent);
                finish(); // closes the layout of the main activity
            }
        });
    }


     // Fetches a random daily quote image from Firebase Storage and displays it in the ImageView.
    private void fetchRandomImage() {
        storageRef.listAll().addOnSuccessListener(listResult -> {
            List<StorageReference> imageRefs = new ArrayList<>(listResult.getItems());

            if (!imageRefs.isEmpty()) { // checks if the list is not empty
                // Preload all images into memory for faster experience
                for (StorageReference imageRef : imageRefs) {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Picasso.get().load(uri).fetch();
                    });
                }


                Random random = new Random(); // random variable
                StorageReference randomImageRef = imageRefs.get(random.nextInt(imageRefs.size())); // stores reference to a random image

                randomImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    Log.d("Firebase", "Image URI: " + uri.toString());
                    Picasso.get().load(uri).into(dailyQuoteImage); // loads the random image into the screen
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


    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        scheduleTestReminderWorker();
    }

    @Override
    protected void onResume() { // a function that saves the last time that the app was used
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("last_open_time", System.currentTimeMillis());
        editor.apply();
    }
    private void scheduleTestReminderWorker() { // this function shows the user a message after not using the app for x time
        OneTimeWorkRequest testWorkRequest =
                new OneTimeWorkRequest.Builder(NotificationWorker.class)
                        .setInitialDelay(1, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(this).enqueue(testWorkRequest);
    }
}
