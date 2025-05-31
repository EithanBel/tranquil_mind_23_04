package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


// this class checks if the user is already authenticated and navigates accordingly. If not authenticated, it shows options to Login or Register.

public class HomePageScreen extends AppCompatActivity {

    // Declare buttons for login and register
    Button buttonLogin, buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checks if a user is already logged in using Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // If a user session exists, navigate directly to the MainActivity
            Intent intent = new Intent(HomePageScreen.this, MainActivity.class);
            startActivity(intent);
            finish(); // Finish the current activity so user can't come back to HomePageScreen with the back button
        } else {
            // Enable edge-to-edge display for modern Android UI look
            EdgeToEdge.enable(this);

            // Set the layout resource for this class
            setContentView(R.layout.activity_home_page_screen);

            // starts the background music here
            Intent musicIntent = new Intent(this, MusicService.class);
            startService(musicIntent);

            // Initialize the Login and Register buttons by finding them in the xml
            buttonLogin = findViewById(R.id.login1);
            buttonRegister = findViewById(R.id.register1);

            // Set click listener for the Register button
            buttonRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // When Register is clicked, navigates to the register class
                    Intent intent = new Intent(getApplicationContext(), Register.class);
                    startActivity(intent);
                    finish(); // Finish HomePageScreen so user can't return to it after registering
                }
            });

            // Set click listener for the Login button
            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // When Login is clicked, navigates to the login class
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                    finish(); // Finish HomePageScreen so user can't return to it after logging in
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // stops the background music here (when leaving HomePageScreen)
        Intent musicIntent = new Intent(this, MusicService.class);
        stopService(musicIntent);
    }
}
