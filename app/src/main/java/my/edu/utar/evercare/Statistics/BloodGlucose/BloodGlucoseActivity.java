package my.edu.utar.evercare.Statistics.BloodGlucose;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import my.edu.utar.evercare.R;
import my.edu.utar.evercare.Statistics.GraphActivity;

public class BloodGlucoseActivity extends AppCompatActivity {

    private EditText bloodGlucoseEditText;
    private FirebaseFirestore db;
    private String currentUserID;
    private RecyclerView recyclerView;
    private BloodGlucoseAdapter adapter;
    private List<BloodGlucoseData> bloodGlucoseList;
    private List<Float> dataPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloodglucose);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Blood Glucose");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.bloodGlucoseRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize ArrayList to hold blood glucose data
        bloodGlucoseList = new ArrayList<>();

        // Initialize adapter
        adapter = new BloodGlucoseAdapter(bloodGlucoseList); // Initialize the adapter here

        // Retrieve blood glucose data from Firestore
        db.collection("statistics")
                .document(currentUserID) // Assuming currentUserID is available
                .collection("blood_glucose")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Retrieve blood glucose data from Firestore
                            String glucoseLevel = document.getString("blood_glucose_level");
                            Date date = document.getDate("date"); // Assuming date is stored as a Date object in Firestore

                            // Create BloodGlucoseData object and add to list
                            BloodGlucoseData glucoseData = new BloodGlucoseData(glucoseLevel, date);
                            bloodGlucoseList.add(glucoseData);
                        }

                        // Set the adapter to RecyclerView after data retrieval
                        recyclerView.setAdapter(adapter);

                        // Notify the adapter about the data change
                        adapter.notifyDataSetChanged();
                    } else {
                        // Log error or show error message if retrieval fails
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Failed to retrieve blood glucose data", Toast.LENGTH_SHORT).show();
                    }
                });

        // Set onClickListener for saveButton
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveButtonClick(v);
            }
        });

        ImageButton showGraphButton = findViewById(R.id.showGraphButton);
        dataPoints = new ArrayList<>();

        // Inside the onClickListener for showGraphButton
        showGraphButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearDataPointsIfNeeded();
                // Retrieve blood glucose data from Firestore
                db.collection("statistics")
                        .document(currentUserID) // Assuming currentUserID is available
                        .collection("blood_glucose")
                        .orderBy("date", Query.Direction.ASCENDING)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    // Retrieve blood glucose data from Firestore
                                    String glucoseLevelStr = document.getString("blood_glucose_level");
                                    if (glucoseLevelStr != null) {
                                        // Extract numeric part of the blood glucose level string
                                        String numericPart = extractNumericPart(glucoseLevelStr);
                                        if (numericPart != null) {
                                            // Convert the blood glucose level to a numeric type
                                            try {
                                                Float glucoseLevel = Float.parseFloat(numericPart);
                                                dataPoints.add(glucoseLevel);
                                            } catch (NumberFormatException e) {
                                                Log.e("BloodGlucoseActivity", "Invalid blood glucose level format", e);
                                            }
                                        }
                                    }
                                }
                                // Pass dataPoints to GraphActivity when showGraphButton is clicked
                                Intent intent = new Intent(BloodGlucoseActivity.this, GraphActivity.class);
                                float[] dataArray = new float[dataPoints.size()];
                                for (int i = 0; i < dataPoints.size(); i++) {
                                    dataArray[i] = dataPoints.get(i);
                                }
                                intent.putExtra("dataPoints", dataArray);
                                intent.putExtra("title", "Line Graph for Blood Glucose");
                                startActivity(intent);
                            } else {
                                // Log error or show error message if retrieval fails
                                Log.d("Firestore", "Error getting documents: ", task.getException());
                                Toast.makeText(BloodGlucoseActivity.this, "Failed to retrieve blood glucose data", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    // Method to extract numeric part of blood glucose level string
    private String extractNumericPart(String glucoseLevelStr) {
        // Remove all non-numeric characters except the decimal point
        return glucoseLevelStr.replaceAll("[^\\d.]", "");
    }

    // Method to handle button click to save blood glucose level
    public void onSaveButtonClick(View view) {
        // Get the blood glucose level entered by the user
        String bloodGlucoseLevelInput = bloodGlucoseEditText.getText().toString().trim();

        // Check if the input is empty
        if (bloodGlucoseLevelInput.isEmpty()) {
            Toast.makeText(this, "Please enter blood glucose level", Toast.LENGTH_SHORT).show();
            return;
        }

        // Append " mmol/L" to the blood glucose level input
        final String bloodGlucoseLevel = bloodGlucoseLevelInput + " mmol/L";

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
                        // Blood glucose level saved successfully
                        Toast.makeText(BloodGlucoseActivity.this, "Blood glucose level saved successfully", Toast.LENGTH_SHORT).show();

                        // Clear the EditText after successful save
                        bloodGlucoseEditText.setText("");

                        // Update the RecyclerView with the new data
                        BloodGlucoseData newGlucoseData = new BloodGlucoseData(bloodGlucoseLevel, new Date());
                        bloodGlucoseList.add(newGlucoseData);
                        adapter.notifyDataSetChanged(); // Notify the adapter about the data change
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save blood glucose level
                        Toast.makeText(BloodGlucoseActivity.this, "Failed to save blood glucose level", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearDataPointsIfNeeded() {
        if (!dataPoints.isEmpty()) {
            dataPoints.clear();
        }
    }
}
