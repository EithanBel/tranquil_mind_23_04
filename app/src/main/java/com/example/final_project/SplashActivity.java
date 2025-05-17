package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set splash theme
        setTheme(R.style.Theme_TranquilMind_Splash);

        // Delay before opening HomePageScreen
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, HomePageScreen.class));
            finish();
        }, SPLASH_DURATION);
    }
}
