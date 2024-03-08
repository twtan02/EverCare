package my.edu.utar.evercare.Statistics.BloodPressure;

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

public class BloodPressureActivity extends AppCompatActivity {

    private EditText bloodPressureEditText;
    private FirebaseFirestore db;
    private String currentUserID;
    private RecyclerView recyclerView;
    private BloodPressureAdapter adapter;
    private List<BloodPressureData> bloodPressureList;
    private List<Float> dataPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloodpressure);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Blood Pressure");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        TextView dateTextView = findViewById(R.id.dateTextView);
        bloodPressureEditText = findViewById(R.id.bloodPressureEditText);
        ImageButton saveButton = findViewById(R.id.saveButton);

        // Set today's date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());
        dateTextView.setText("Today's Date: " + currentDate);

        // Get the current user ID from the intent
        currentUserID = getIntent().getStringExtra("userID");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.bloodPressureRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize ArrayList to hold blood pressure data
        bloodPressureList = new ArrayList<>();

        // Initialize adapter
        adapter = new BloodPressureAdapter(bloodPressureList); // Initialize the adapter here

        // Retrieve blood pressure data from Firestore
        db.collection("statistics")
                .document(currentUserID) // Assuming currentUserID is available
                .collection("blood_pressure")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Retrieve blood pressure data from Firestore
                            String pressureLevel = document.getString("blood_pressure_level");
                            Date date = document.getDate("date"); // Assuming date is stored as a Date object in Firestore

                            // Create BloodPressureData object and add to list
                            BloodPressureData pressureData = new BloodPressureData(pressureLevel, date);
                            bloodPressureList.add(pressureData);
                        }

                        // Set the adapter to RecyclerView after data retrieval
                        recyclerView.setAdapter(adapter);

                        // Notify the adapter about the data change
                        adapter.notifyDataSetChanged();
                    } else {
                        // Log error or show error message if retrieval fails
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Failed to retrieve blood pressure data", Toast.LENGTH_SHORT).show();
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
        showGraphButton.setOnClickListener(v -> {
            clearDataPointsIfNeeded();

            // Retrieve pressure levels and dates from Firebase
            db.collection("statistics")
                    .document(currentUserID)
                    .collection("blood_pressure")
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<Float> pressureLevels = new ArrayList<>();
                            ArrayList<Date> dates = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                String pressureLevelStr = document.getString("blood_pressure_level");
                                if (pressureLevelStr != null) {
                                    // Extract numeric part of the blood pressure level string
                                    String numericPart = extractNumericPart(pressureLevelStr);
                                    if (numericPart != null) {
                                        // Convert the blood pressure level to a numeric type
                                        try {
                                            Float pressureLevel = Float.parseFloat(numericPart);
                                            pressureLevels.add(pressureLevel);
                                            dates.add(document.getDate("date"));
                                        } catch (NumberFormatException e) {
                                            Log.e("BloodPressureActivity", "Invalid blood pressure level format", e);
                                        }
                                    }
                                }
                            }

                            // Pass pressureLevels and dates to GraphActivity
                            Intent intent = new Intent(BloodPressureActivity.this, GraphActivity.class);
                            float[] pressureLevelsArray = new float[pressureLevels.size()];
                            long[] datesArray = new long[dates.size()];
                            for (int i = 0; i < pressureLevels.size(); i++) {
                                pressureLevelsArray[i] = pressureLevels.get(i);
                                datesArray[i] = dates.get(i).getTime();
                            }
                            intent.putExtra("datapoints", pressureLevelsArray);
                            intent.putExtra("dates", datesArray);
                            intent.putExtra("title", "Line Graph for Blood Pressure");
                            startActivity(intent);
                        } else {
                            Log.d("BloodPressureActivity", "Error getting documents: ", task.getException());
                            Toast.makeText(BloodPressureActivity.this, "Failed to retrieve blood pressure data", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // Method to extract numeric part of blood pressure level string
    private String extractNumericPart(String pressureLevelStr) {
        // Remove all non-numeric characters except the decimal point
        return pressureLevelStr.replaceAll("[^\\d.]", "");
    }

    // Method to handle button click to save blood pressure level
    public void onSaveButtonClick(View view) {
        // Get the blood pressure level entered by the user
        String bloodPressureLevelInput = bloodPressureEditText.getText().toString().trim();

        // Check if the input is empty
        if (bloodPressureLevelInput.isEmpty()) {
            Toast.makeText(this, "Please enter blood pressure level", Toast.LENGTH_SHORT).show();
            return;
        }

        // Append " mmol/L" to the blood pressure level input
        final String bloodPressureLevel = bloodPressureLevelInput + " mmol/L";

        // Check if currentUserID is null
        if (currentUserID == null) {
            // Handle the case where currentUserID is null, such as displaying an error message
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new data map with blood pressure level and date
        Map<String, Object> data = new HashMap<>();
        data.put("blood_pressure_level", bloodPressureLevel);
        data.put("date", new Date());

        // Add the data to Firestore with the current user's ID
        db.collection("statistics")
                .document(currentUserID) // Use the current user's ID
                .collection("blood_pressure")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Blood pressure level saved successfully
                        Toast.makeText(BloodPressureActivity.this, "Blood pressure level saved successfully", Toast.LENGTH_SHORT).show();

                        // Clear the EditText after successful save
                        bloodPressureEditText.setText("");

                        // Update the RecyclerView with the new data
                        BloodPressureData newPressureData = new BloodPressureData(bloodPressureLevel, new Date());
                        bloodPressureList.add(newPressureData);
                        adapter.notifyDataSetChanged(); // Notify the adapter about the data change
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save blood pressure level
                        Toast.makeText(BloodPressureActivity.this, "Failed to save blood pressure level", Toast.LENGTH_SHORT).show();
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