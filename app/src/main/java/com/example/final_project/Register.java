package com.example.final_project;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class Register extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword,editTextFirstName,editTextLastName,editTextPhoneNum;
    Button buttonReg;
    FirebaseAuth mAuth;
    FirebaseFirestore mStore;
    String userID;
ProgressBar progressBar;


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in , if yes redirect user to HP of TranquilApp.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mStore=FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_register);
        editTextFirstName=findViewById(R.id.firstName);
        editTextLastName=findViewById(R.id.lastName);
        editTextPhoneNum=findViewById(R.id.phone_number);
        progressBar=findViewById(R.id.progressBar);


        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email, password,firstName,lastName,phoneNum;
                email = String.valueOf(editTextEmail.getText());
                password = String.valueOf(editTextPassword.getText());

                firstName=String.valueOf(editTextFirstName.getText());
                lastName=String.valueOf(editTextLastName.getText());
                phoneNum=String.valueOf(editTextPhoneNum.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(Register.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {

                    Toast.makeText(Register.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;

                }
                if (password.length()<6){
                    editTextPassword.setError("Password must be at least 6 characters");
                    return;
                }
                if (phoneNum.length()!=10){
                    editTextPassword.setError("Phone number must be 10 digits");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {

                                    Toast.makeText(Register.this, "TranquilMind account created successfully.",
                                            Toast.LENGTH_SHORT).show();

                                    // SET DISPLAY NAME HERE
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        String fullName = firstName + " " + lastName;

                                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                .setDisplayName(fullName)
                                                .build();

//                                        firebaseUser.updateProfile(profileUpdates)
//                                                .addOnCompleteListener(profileTask -> {
//                                                    if (profileTask.isSuccessful()) {
//                                                        Log.d(TAG, "Display name set to: " + fullName);
//                                                    }
//                                                });
                                        firebaseUser.updateProfile(profileUpdates)
                                                .addOnCompleteListener(profileTask -> {
                                                    if (profileTask.isSuccessful()) {
                                                        Log.d(TAG, "Display name set to: " + fullName);

                                                        // Now save user data to Firestore
                                                        userID = firebaseUser.getUid();
                                                        DocumentReference documentReference = mStore.collection("users").document(userID);
                                                        Map<String, Object> user = new HashMap<>();
                                                        user.put("firstName", firstName);
                                                        user.put("lastName", lastName);
                                                        user.put("phone", phoneNum);
                                                        user.put("email", email);

                                                        documentReference.set(user).addOnSuccessListener(aVoid -> {
                                                            Log.d(TAG, "onSuccess: user Profile is created for " + userID);
                                                            // ✅ Navigate to MainActivity only after everything is ready
                                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        });
                                                    } else {
                                                        Toast.makeText(Register.this, "Failed to update display name", Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    }


                                    userID=mAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference=mStore.collection("users").document(userID);
                                    Map<String,Object> user=new HashMap<>();;
                                    user.put("firstName",firstName);
                                    user.put("lastName",lastName);
                                    user.put("phone",phoneNum);
                                    user.put("email",email);

                                    documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG,"onSuccess: user Profile is created for  "+ userID);
                                        }
                                    });
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                }


                                else

                                {
                                    // If sign in fails, display a message to the user.

                                    Toast.makeText(Register.this, "Something went wrong..Try again.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });


            }
        });
    }
}