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
        // Show splash theme for a moment
        setTheme(R.style.Theme_TranquilMind_Splash);

        // Set your custom layout
        setContentView(R.layout.splash_screen_layout);

        TextView welcomeText = findViewById(R.id.welcomeText);

        // Create and start a fade-in animation
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1000); // 1 second fade-in
        welcomeText.startAnimation(fadeIn);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, HomePageScreen.class));
            finish();
        }, SPLASH_DURATION);
    }
}
