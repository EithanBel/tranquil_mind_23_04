package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout AFTER theme background shows
        setContentView(R.layout.splash_screen_layout);

        // Animate the welcome text (optional)
        TextView welcomeText = findViewById(R.id.welcomeText);
        welcomeText.animate().alpha(1f).setDuration(1000).start();

        // After delay, go to home screen
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, HomePageScreen.class));
            finish();
        }, SPLASH_DURATION);
    }
}

