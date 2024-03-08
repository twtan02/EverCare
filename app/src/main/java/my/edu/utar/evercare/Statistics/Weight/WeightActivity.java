package my.edu.utar.evercare.Statistics.Weight;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class WeightActivity extends AppCompatActivity {

    private EditText weightEditText;
    private FirebaseFirestore db;
    private String currentUserID;
    private RecyclerView recyclerView;
    private WeightAdapter adapter;
    private List<WeightData> weightList;
    private List<Float> dataPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weight);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Weight");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        TextView dateTextView = findViewById(R.id.dateTextView);
        weightEditText = findViewById(R.id.weightEditText);
        ImageButton saveButton = findViewById(R.id.saveButton);

        // Set today's date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());
        dateTextView.setText("Today's Date: " + currentDate);

        // Get the current user ID from the intent
        currentUserID = getIntent().getStringExtra("userID");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.weightRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize ArrayList to hold weight data
        weightList = new ArrayList<>();

        // Initialize adapter
        adapter = new WeightAdapter(weightList); // Initialize the adapter here

        // Retrieve weight data from Firestore
        db.collection("statistics")
                .document(currentUserID) // Assuming currentUserID is available
                .collection("weight")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Retrieve weight data from Firestore
                            String weightValue = document.getString("weight");
                            Date date = document.getDate("date"); // Assuming date is stored as a Date object in Firestore

                            // Create WeightData object and add to list
                            WeightData weightData = new WeightData(weightValue, date);
                            weightList.add(weightData);
                        }

                        // Set the adapter to RecyclerView after data retrieval
                        recyclerView.setAdapter(adapter);

                        // Notify the adapter about the data change
                        adapter.notifyDataSetChanged();
                    } else {
                        // Log error or show error message if retrieval fails
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Failed to retrieve weight data", Toast.LENGTH_SHORT).show();
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

            // Retrieve weight values and dates from Firebase
            db.collection("statistics")
                    .document(currentUserID)
                    .collection("weight")
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<Float> weightValues = new ArrayList<>();
                            ArrayList<Date> dates = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                String weightValueStr = document.getString("weight");
                                if (weightValueStr != null) {
                                    // Extract numeric part of the weight string
                                    String numericPart = extractNumericPart(weightValueStr);
                                    if (numericPart != null) {
                                        // Convert the weight value to a numeric type
                                        try {
                                            Float weightValue = Float.parseFloat(numericPart);
                                            weightValues.add(weightValue);
                                            dates.add(document.getDate("date"));
                                        } catch (NumberFormatException e) {
                                            Log.e("WeightActivity", "Invalid weight format", e);
                                        }
                                    }
                                }
                            }

                            // Pass weightValues and dates to GraphActivity
                            Intent intent = new Intent(WeightActivity.this, GraphActivity.class);
                            float[] weightValuesArray = new float[weightValues.size()];
                            long[] datesArray = new long[dates.size()];
                            for (int i = 0; i < weightValues.size(); i++) {
                                weightValuesArray[i] = weightValues.get(i);
                                datesArray[i] = dates.get(i).getTime();
                            }
                            intent.putExtra("datapoints", weightValuesArray);
                            intent.putExtra("dates", datesArray);
                            intent.putExtra("title", "Line Graph for Weight");
                            startActivity(intent);
                        } else {
                            Log.d("WeightActivity", "Error getting documents: ", task.getException());
                            Toast.makeText(WeightActivity.this, "Failed to retrieve weight data", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // Method to extract numeric part of weight string
    private String extractNumericPart(String weightValueStr) {
        // Remove all non-numeric characters except the decimal point
        return weightValueStr.replaceAll("[^\\d.]", "");
    }

    // Method to handle button click to save weight
    public void onSaveButtonClick(View view) {
        // Get the weight entered by the user
        String weightInput = weightEditText.getText().toString().trim();

        // Check if the input is empty
        if (weightInput.isEmpty()) {
            Toast.makeText(this, "Please enter weight", Toast.LENGTH_SHORT).show();
            return;
        }

        // Append " kg" to the weight input
        final String weight = weightInput + " kg";

        // Check if currentUserID is null
        if (currentUserID == null) {
            // Handle the case where currentUserID is null, such as displaying an error message
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new data map with weight and date
        Map<String, Object> data = new HashMap<>();
        data.put("weight", weight);
        data.put("date", new Date());

        // Add the data to Firestore with the current user's ID
        db.collection("statistics")
                .document(currentUserID) // Use the current user's ID
                .collection("weight")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Weight saved successfully
                        Toast.makeText(WeightActivity.this, "Weight saved successfully", Toast.LENGTH_SHORT).show();

                        // Clear the EditText after successful save
                        weightEditText.setText("");

                        // Update the RecyclerView with the new data
                        WeightData newWeightData = new WeightData(weight, new Date());
                        weightList.add(newWeightData);
                        adapter.notifyDataSetChanged(); // Notify the adapter about the data change
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save weight
                        Toast.makeText(WeightActivity.this, "Failed to save weight", Toast.LENGTH_SHORT).show();
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
