package my.edu.utar.evercare.Statistics.HealthActivity;

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
import my.edu.utar.evercare.Statistics.HealthActivity.HealthActivityAdapter;
import my.edu.utar.evercare.Statistics.HealthActivity.HealthActivityData;

public class HealthActivity extends AppCompatActivity {

    private EditText healthDataEditText;
    private FirebaseFirestore db;
    private String currentUserID;
    private RecyclerView recyclerView;
    private HealthActivityAdapter adapter;
    private List<HealthActivityData> healthDataList;
    private List<Float> dataPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthactivity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Health");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        TextView dateTextView = findViewById(R.id.dateTextView);
        healthDataEditText = findViewById(R.id.healthActivityEditText);
        ImageButton saveButton = findViewById(R.id.saveButton);

        // Set today's date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());
        dateTextView.setText("Today's Date: " + currentDate);

        // Get the current user ID from the intent
        currentUserID = getIntent().getStringExtra("userID");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.healthActivityRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize ArrayList to hold health data
        healthDataList = new ArrayList<>();

        // Initialize adapter
        adapter = new HealthActivityAdapter(healthDataList); // Initialize the adapter here

        // Retrieve health data from Firestore
        db.collection("statistics")
                .document(currentUserID) // Assuming currentUserID is available
                .collection("health_data")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Retrieve health data from Firestore
                            String healthData = document.getString("health_data");
                            Date date = document.getDate("date"); // Assuming date is stored as a Date object in Firestore

                            // Create HealthData object and add to list
                            HealthActivityData data = new HealthActivityData(healthData, date);
                            healthDataList.add(data);
                        }

                        // Set the adapter to RecyclerView after data retrieval
                        recyclerView.setAdapter(adapter);

                        // Notify the adapter about the data change
                        adapter.notifyDataSetChanged();
                    } else {
                        // Log error or show error message if retrieval fails
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Failed to retrieve health data", Toast.LENGTH_SHORT).show();
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

            // Retrieve health data and dates from Firebase
            db.collection("statistics")
                    .document(currentUserID)
                    .collection("health_data")
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<Float> healthDataValues = new ArrayList<>();
                            ArrayList<Date> dates = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                String healthDataStr = document.getString("health_data");
                                if (healthDataStr != null) {
                                    // Extract numeric part of the health data string
                                    String numericPart = extractNumericPart(healthDataStr);
                                    if (numericPart != null) {
                                        // Convert the health data to a numeric type
                                        try {
                                            Float healthValue = Float.parseFloat(numericPart);
                                            healthDataValues.add(healthValue);
                                            dates.add(document.getDate("date"));
                                        } catch (NumberFormatException e) {
                                            Log.e("HealthActivity", "Invalid health data format", e);
                                        }
                                    }
                                }
                            }

                            // Pass healthDataValues and dates to GraphActivity
                            Intent intent = new Intent(HealthActivity.this, GraphActivity.class);
                            float[] healthDataArray = new float[healthDataValues.size()];
                            long[] datesArray = new long[dates.size()];
                            for (int i = 0; i < healthDataValues.size(); i++) {
                                healthDataArray[i] = healthDataValues.get(i);
                                datesArray[i] = dates.get(i).getTime();
                            }
                            intent.putExtra("datapoints", healthDataArray);
                            intent.putExtra("dates", datesArray);
                            intent.putExtra("title", "Line Graph for Health Data");
                            startActivity(intent);
                        } else {
                            Log.d("HealthActivity", "Error getting documents: ", task.getException());
                            Toast.makeText(HealthActivity.this, "Failed to retrieve health data", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // Method to extract numeric part of health data string
    private String extractNumericPart(String healthDataStr) {
        // Remove all non-numeric characters except the decimal point
        return healthDataStr.replaceAll("[^\\d.]", "");
    }

    // Method to handle button click to save health data
    public void onSaveButtonClick(View view) {
        // Get the health data entered by the user
        String healthDataInput = healthDataEditText.getText().toString().trim();

        // Check if the input is empty
        if (healthDataInput.isEmpty()) {
            Toast.makeText(this, "Please enter health data", Toast.LENGTH_SHORT).show();
            return;
        }

        // Append " units" to the health data input
        final String healthData = healthDataInput + " units";

        // Check if currentUserID is null
        if (currentUserID == null) {
            // Handle the case where currentUserID is null, such as displaying an error message
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new data map with health data and date
        Map<String, Object> data = new HashMap<>();
        data.put("health_data", healthData);
        data.put("date", new Date());

        // Add the data to Firestore with the current user's ID
        db.collection("statistics")
                .document(currentUserID) // Use the current user's ID
                .collection("health_data")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Health data saved successfully
                        Toast.makeText(HealthActivity.this, "Health data saved successfully", Toast.LENGTH_SHORT).show();

                        // Clear the EditText after successful save
                        healthDataEditText.setText("");

                        // Update the RecyclerView with the new data
                        HealthActivityData newHealthData = new HealthActivityData(healthDataInput, new Date());
                        healthDataList.add(newHealthData);
                        adapter.notifyDataSetChanged(); // Notify the adapter about the data change
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save health data
                        Toast.makeText(HealthActivity.this, "Failed to save health data", Toast.LENGTH_SHORT).show();
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
