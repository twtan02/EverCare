package my.edu.utar.evercare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signUpButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Validate the input fields
                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create a new user with the provided email and password
                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Registration successful, store additional user data in Firestore
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    if (user != null) {
                                        storeUserData(user.getUid(), username);
                                    } else {
                                        // Error getting user, show error message
                                        Toast.makeText(SignUpActivity.this, "Error getting user", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Registration failed, show error message
                                    Toast.makeText(SignUpActivity.this, "Registration failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void storeUserData(String userId, String username) {
        // Create a new document in the "users" collection with the user's ID
        DocumentReference userRef = firestore.collection("users").document(userId);

        // Create a Map object to store the user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);

        // Set the user data in the document
        userRef.set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // User data stored successfully, navigate back to the login page
                        Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                        finish(); // Add this line to close the current activity
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error storing user data, show error message
                        Toast.makeText(SignUpActivity.this, "Error storing user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
