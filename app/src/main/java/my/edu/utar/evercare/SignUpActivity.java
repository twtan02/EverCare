package my.edu.utar.evercare;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText dateOfBirthEditText; // Use EditText for date of birth
    private RadioGroup roleRadioGroup;
    private Button signUpButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private Calendar calendar;
    private DatePickerDialog datePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.usernameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        dateOfBirthEditText = findViewById(R.id.dateOfBirthTextView); // Use EditText for date of birth
        roleRadioGroup = findViewById(R.id.roleRadioGroup);
        signUpButton = findViewById(R.id.signUpButton);

        // Initialize calendar with the current date
        calendar = Calendar.getInstance();

        dateOfBirthEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a date picker dialog
                datePickerDialog = new DatePickerDialog(
                        SignUpActivity.this,
                        dateSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                // Show the date picker dialog
                datePickerDialog.show();
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String role = getSelectedRole();
                String dateOfBirth = dateOfBirthEditText.getText().toString(); // Get date of birth from EditText

                if (username.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty() || dateOfBirth.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Registration successful, store additional user data in Firestore
                                    FirebaseUser user = firebaseAuth.getCurrentUser();
                                    if (user != null) {
                                        String userId = user.getUid(); // Get the user ID
                                        storeUserData(userId, username, email, dateOfBirth, role); // Pass the dateOfBirth to the storeUserData method
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

    private String getSelectedRole() {
        int selectedRadioButtonId = roleRadioGroup.getCheckedRadioButtonId();
        if (selectedRadioButtonId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
            return selectedRadioButton.getText().toString();
        }
        return "";
    }

    // Date set listener for the date picker dialog
    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            // Update the EditText with the selected date
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            dateOfBirthEditText.setText(selectedDate);

            // Update the calendar with the selected date
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        }
    };

    private void storeUserData(String userId, String username, String email, String dateOfBirth, String role) {
        // Create a new document in the "users" collection with the user's ID
        DocumentReference userRef = firestore.collection("users").document(userId);

        // Create a Map object to store the user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId); // Add the user ID to the user data
        userData.put("username", username);
        userData.put("email", email); // Store the email in Firestore
        userData.put("dateOfBirth", dateOfBirth); // Store the date of birth in Firestore
        userData.put("role", role); // Store the role in Firestore

        // Set the user data in the document
        userRef.set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // User data stored successfully, navigate back to the login page
                        Toast.makeText(SignUpActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, MainActivity.class));
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
