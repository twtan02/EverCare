package my.edu.utar.evercare;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BloodGlucoseActivity extends AppCompatActivity {

    private EditText bloodGlucoseEditText;
    private FirebaseFirestore db;
    private String currentUserID; // Variable to hold the current user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloodglucose); // Use activity_bloodglucose layout

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        TextView dateTextView = findViewById(R.id.dateTextView);
        bloodGlucoseEditText = findViewById(R.id.bloodGlucoseEditText);
        ImageButton saveButton = findViewById(R.id.saveButton);

        // Set today's date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());
        dateTextView.setText("Today's Date: " + currentDate);

        // Get the current user ID from the intent
        currentUserID = getIntent().getStringExtra("userID");

        // Set onClickListener for saveButton
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveButtonClick(v);
            }
        });
    }


    // Method to handle button click to save blood glucose level
    public void onSaveButtonClick(View view) {
        // Get the blood glucose level entered by the user
        String bloodGlucoseLevel = bloodGlucoseEditText.getText().toString().trim();

        // Check if the input is empty
        if (bloodGlucoseLevel.isEmpty()) {
            Toast.makeText(this, "Please enter blood glucose level", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if currentUserID is null
        if (currentUserID == null) {
            // Handle the case where currentUserID is null, such as displaying an error message
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new data map with blood glucose level and date
        Map<String, Object> data = new HashMap<>();
        data.put("blood_glucose_level", bloodGlucoseLevel);
        data.put("date", new Date());

        // Add the data to Firestore with the current user's ID
        db.collection("statistics")
                .document(currentUserID) // Use the current user's ID
                .collection("blood_glucose")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(BloodGlucoseActivity.this, "Blood glucose level saved successfully", Toast.LENGTH_SHORT).show();
                        bloodGlucoseEditText.setText(""); // Clear the EditText after successful save
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BloodGlucoseActivity.this, "Failed to save blood glucose level", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
