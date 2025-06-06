
package com.example.final_project;

import static android.content.ContentValues.TAG;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

// this classes screen is seen after the user clicks on the register button

public class Register extends AppCompatActivity {

    // UI elements for user input
    TextInputEditText editTextEmail, editTextPassword, editTextFirstName, editTextLastName, editTextPhoneNum;

    // button for registration
    Button buttonReg;

    // progress bar
    ProgressBar progressBar;

    // Firebase instances for Authentication and Firestore Database
    FirebaseAuth mAuth;
    FirebaseFirestore mStore;

    // a variable to store the user's ID after being registered
    String userID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable full screen edge-to-edge experience
        EdgeToEdge.enable(this);

        // Set layout for the registration screen
        setContentView(R.layout.activity_register);

        // Initialize Firebase Authentication and Firestore instances
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

        // Link UI elements from the XML
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_register);
        editTextFirstName = findViewById(R.id.firstName);
        editTextLastName = findViewById(R.id.lastName);
        editTextPhoneNum = findViewById(R.id.phone_number);
        progressBar = findViewById(R.id.progressBar);

        // Set click listener for the register button
        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // getting the input values from fields
                String email = String.valueOf(editTextEmail.getText());
                String password = String.valueOf(editTextPassword.getText());
                String firstName = String.valueOf(editTextFirstName.getText());
                String lastName = String.valueOf(editTextLastName.getText());
                String phoneNum = String.valueOf(editTextPhoneNum.getText());

                // Validate that the email is not empty
                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                // validate that the password is not empty
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(Register.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                // validate that the passwords length is at least 6 digits
                if (password.length() < 6) {
                    editTextPassword.setError("Password must be at least 6 characters");
                    return;
                }
                // validate that the phones number is 10 digits
                if (phoneNum.length() != 10) {
                    editTextPhoneNum.setError("Phone number must be 10 digits");
                    return;
                }

                // Show progress bar during registration
                progressBar.setVisibility(View.VISIBLE);

                // Create user with Firebase Authentication
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    // If account creation successful
                                    Toast.makeText(Register.this, "TranquilMind account created successfully.", Toast.LENGTH_SHORT).show();

                                    // Set display name in Firebase Authentication profile
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        String fullName = firstName + " " + lastName;

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(fullName)
                                                .build();

                                        firebaseUser.updateProfile(profileUpdates)
                                                .addOnCompleteListener(profileTask -> {
                                                    // if profile update succeeds
                                                    if (profileTask.isSuccessful()) {
                                                        Log.d(TAG, "Display name set to: " + fullName);

                                                        // Save additional user data into Firestore
                                                        userID = firebaseUser.getUid();
                                                        DocumentReference documentReference = mStore.collection("users").document(userID);
                                                        Map<String, Object> user = new HashMap<>();
                                                        user.put("firstName", firstName);
                                                        user.put("lastName", lastName);
                                                        user.put("phone", phoneNum);
                                                        user.put("email", email);

                                                        documentReference.set(user)
                                                                .addOnSuccessListener(aVoid -> {
                                                                    Log.d(TAG, "onSuccess: user Profile is created for " + userID);
                                                                    // Navigate to MainActivity after saving data
                                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                });
                                                    } else {
                                                        // If profile update fails
                                                        Toast.makeText(Register.this, "Failed to update display name", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    // If registration fails, show error
                                    Toast.makeText(Register.this, "Something went wrong..Try again.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
