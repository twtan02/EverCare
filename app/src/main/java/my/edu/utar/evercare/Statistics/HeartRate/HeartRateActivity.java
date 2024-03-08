package my.edu.utar.evercare.Statistics.HeartRate;

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

public class HeartRateActivity extends AppCompatActivity {

    private EditText heartRateEditText;
    private FirebaseFirestore db;
    private String currentUserID;
    private RecyclerView recyclerView;
    private HeartRateAdapter adapter;
    private List<HeartRateData> heartRateList;
    private List<Float> dataPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartrate);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Heart Rate");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        TextView dateTextView = findViewById(R.id.dateTextView);
        heartRateEditText = findViewById(R.id.heartRateEditText);
        ImageButton saveButton = findViewById(R.id.saveButton);

        // Set today's date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());
        dateTextView.setText("Today's Date: " + currentDate);

        // Get the current user ID from the intent
        currentUserID = getIntent().getStringExtra("userID");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.heartRateRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize ArrayList to hold heart rate data
        heartRateList = new ArrayList<>();

        // Initialize adapter
        adapter = new HeartRateAdapter(heartRateList); // Initialize the adapter here

        // Retrieve heart rate data from Firestore
        db.collection("statistics")
                .document(currentUserID) // Assuming currentUserID is available
                .collection("heart_rate")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Retrieve heart rate data from Firestore
                            String heartRate = document.getString("heart_rate");
                            Date date = document.getDate("date"); // Assuming date is stored as a Date object in Firestore

                            // Create HeartRateData object and add to list
                            HeartRateData rateData = new HeartRateData(heartRate, date);
                            heartRateList.add(rateData);
                        }

                        // Set the adapter to RecyclerView after data retrieval
                        recyclerView.setAdapter(adapter);

                        // Notify the adapter about the data change
                        adapter.notifyDataSetChanged();
                    } else {
                        // Log error or show error message if retrieval fails
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Failed to retrieve heart rate data", Toast.LENGTH_SHORT).show();
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

            // Retrieve heart rates and dates from Firebase
            db.collection("statistics")
                    .document(currentUserID)
                    .collection("heart_rate")
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<Float> heartRates = new ArrayList<>();
                            ArrayList<Date> dates = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                String heartRateStr = document.getString("heart_rate");
                                if (heartRateStr != null) {
                                    // Extract numeric part of the heart rate string
                                    String numericPart = extractNumericPart(heartRateStr);
                                    if (numericPart != null) {
                                        // Convert the heart rate to a numeric type
                                        try {
                                            Float rate = Float.parseFloat(numericPart);
                                            heartRates.add(rate);
                                            dates.add(document.getDate("date"));
                                        } catch (NumberFormatException e) {
                                            Log.e("HeartRateActivity", "Invalid heart rate format", e);
                                        }
                                    }
                                }
                            }

                            // Pass heartRates and dates to GraphActivity
                            Intent intent = new Intent(HeartRateActivity.this, GraphActivity.class);
                            float[] heartRatesArray = new float[heartRates.size()];
                            long[] datesArray = new long[dates.size()];
                            for (int i = 0; i < heartRates.size(); i++) {
                                heartRatesArray[i] = heartRates.get(i);
                                datesArray[i] = dates.get(i).getTime();
                            }
                            intent.putExtra("datapoints", heartRatesArray);
                            intent.putExtra("dates", datesArray);
                            intent.putExtra("title", "Line Graph for Heart Rate");
                            startActivity(intent);
                        } else {
                            Log.d("HeartRateActivity", "Error getting documents: ", task.getException());
                            Toast.makeText(HeartRateActivity.this, "Failed to retrieve heart rate data", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // Method to extract numeric part of heart rate string
    private String extractNumericPart(String heartRateStr) {
        // Remove all non-numeric characters except the decimal point
        return heartRateStr.replaceAll("[^\\d.]", "");
    }

    // Method to handle button click to save heart rate
    public void onSaveButtonClick(View view) {
        // Get the heart rate entered by the user
        String heartRateInput = heartRateEditText.getText().toString().trim();

        // Check if the input is empty
        if (heartRateInput.isEmpty()) {
            Toast.makeText(this, "Please enter heart rate", Toast.LENGTH_SHORT).show();
            return;
        }

        // Append " bpm" to the heart rate input
        final String heartRate = heartRateInput + " bpm";

        // Check if currentUserID is null
        if (currentUserID == null) {
            // Handle the case where currentUserID is null, such as displaying an error message
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new data map with heart rate and date
        Map<String, Object> data = new HashMap<>();
        data.put("heart_rate", heartRate);
        data.put("date", new Date());

        // Add the data to Firestore with the current user's ID
        db.collection("statistics")
                .document(currentUserID) // Use the current user's ID
                .collection("heart_rate")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Heart rate saved successfully
                        Toast.makeText(HeartRateActivity.this, "Heart rate saved successfully", Toast.LENGTH_SHORT).show();

                        // Clear the EditText after successful save
                        heartRateEditText.setText("");

                        // Update the RecyclerView with the new data
                        HeartRateData newRateData = new HeartRateData(heartRate, new Date());
                        heartRateList.add(newRateData);
                        adapter.notifyDataSetChanged(); // Notify the adapter about the data change
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save heart rate
                        Toast.makeText(HeartRateActivity.this, "Failed to save heart rate", Toast.LENGTH_SHORT).show();
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
