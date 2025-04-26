package com.example.final_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class HomePageScreen extends AppCompatActivity {

    Button buttonLogin, buttonRegister;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // User is logged in
            Intent intent = new Intent(HomePageScreen.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {


            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_home_page_screen);


            buttonLogin = findViewById(R.id.login1);
            buttonRegister = findViewById(R.id.register1);


            buttonRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Register.class);
                    startActivity(intent);
                    finish();
                }


            });

            buttonLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                    finish();
                }

            });

        }
    }

}

