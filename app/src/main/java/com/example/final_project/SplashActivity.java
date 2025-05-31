package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

// this classes screen is the first screen that the user sees when opening the app

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_layout); // opens the design of the splash activity

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000); // waits 3 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // moves to HomePageScreen class
                Intent intent = new Intent(SplashActivity.this, HomePageScreen.class);
                startActivity(intent);
                finish(); // closes Splash screen
            }
        });

        thread.start(); // starts the thread
    }
}
