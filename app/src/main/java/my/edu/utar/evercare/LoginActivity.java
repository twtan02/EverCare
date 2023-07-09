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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button signUpButton; // Add a sign-up button

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton); // Initialize the sign-up button

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Perform authentication based on username and password
                firebaseAuth.signInWithEmailAndPassword(username, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    if (user != null) {
                                        checkUserRoleAndOpenActivity(user.getUid());
                                    } else {
                                        // Error getting user, show error message
                                        Toast.makeText(LoginActivity.this, "Error getting user", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Authentication failed, show error message
                                    Toast.makeText(LoginActivity.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the sign-up activity
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
            }
        });
    }

    private void checkUserRoleAndOpenActivity(String userId) {
        // Query Firebase Firestore to determine the user role based on the userId
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(userId);
        userRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                String role = document.getString("role");
                                String username = document.getString("username");
                                if (role != null) {
                                    if (role.equals("elderly")) {
                                        // Pass the username to the ElderlyUserActivity
                                        Intent intent = new Intent(LoginActivity.this, ElderlyUserActivity.class);
                                        intent.putExtra("username", username);
                                        startActivity(intent);
                                    } else if (role.equals("staff")) {
                                        // Pass the username to the StaffUserActivity
                                        Intent intent = new Intent(LoginActivity.this, StaffUserActivity.class);
                                        intent.putExtra("username", username);
                                        startActivity(intent);
                                    } else if (role.equals("caregiver")) {
                                        // Pass the username to the CaregiverUserActivity
                                        Intent intent = new Intent(LoginActivity.this, CaregiverUserActivity.class);
                                        intent.putExtra("username", username);
                                        startActivity(intent);
                                    }
                                    finish();
                                } else {
                                    // Role not found, show error message
                                    Toast.makeText(LoginActivity.this, "Role not found", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // Document not found, show error message
                                Toast.makeText(LoginActivity.this, "User document not found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Error fetching document, show error message
                            Toast.makeText(LoginActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}
