package my.edu.utar.evercare.Statistics.Sleep;

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

public class SleepActivity extends AppCompatActivity {

    private EditText sleepDurationEditText;
    private FirebaseFirestore db;
    private String currentUserID;
    private RecyclerView recyclerView;
    private SleepAdapter adapter;
    private List<SleepData> sleepList;
    private List<Float> dataPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sleep);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Sleep");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        TextView dateTextView = findViewById(R.id.dateTextView);
        sleepDurationEditText = findViewById(R.id.sleepDurationEditText);
        ImageButton saveButton = findViewById(R.id.saveButton);

        // Set today's date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());
        dateTextView.setText("Today's Date: " + currentDate);

        // Get the current user ID from the intent
        currentUserID = getIntent().getStringExtra("userID");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.sleepRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize ArrayList to hold sleep data
        sleepList = new ArrayList<>();

        // Initialize adapter
        adapter = new SleepAdapter(sleepList); // Initialize the adapter here

        // Retrieve sleep data from Firestore
        db.collection("statistics")
                .document(currentUserID) // Assuming currentUserID is available
                .collection("sleep")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Retrieve sleep data from Firestore
                            String sleepDuration = document.getString("sleep_duration");
                            Date date = document.getDate("date"); // Assuming date is stored as a Date object in Firestore

                            // Create SleepData object and add to list
                            SleepData sleepData = new SleepData(sleepDuration, date);
                            sleepList.add(sleepData);
                        }

                        // Set the adapter to RecyclerView after data retrieval
                        recyclerView.setAdapter(adapter);

                        // Notify the adapter about the data change
                        adapter.notifyDataSetChanged();
                    } else {
                        // Log error or show error message if retrieval fails
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Failed to retrieve sleep data", Toast.LENGTH_SHORT).show();
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

            // Retrieve sleep durations and dates from Firebase
            db.collection("statistics")
                    .document(currentUserID)
                    .collection("sleep")
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<Float> sleepDurations = new ArrayList<>();
                            ArrayList<Date> dates = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                String sleepDurationStr = document.getString("sleep_duration");
                                if (sleepDurationStr != null) {
                                    // Extract numeric part of the sleep duration string
                                    String numericPart = extractNumericPart(sleepDurationStr);
                                    if (numericPart != null) {
                                        // Convert the sleep duration to a numeric type
                                        try {
                                            Float sleepDuration = Float.parseFloat(numericPart);
                                            sleepDurations.add(sleepDuration);
                                            dates.add(document.getDate("date"));
                                        } catch (NumberFormatException e) {
                                            Log.e("SleepActivity", "Invalid sleep duration format", e);
                                        }
                                    }
                                }
                            }

                            // Pass sleepDurations and dates to GraphActivity
                            Intent intent = new Intent(SleepActivity.this, GraphActivity.class);
                            float[] sleepDurationsArray = new float[sleepDurations.size()];
                            long[] datesArray = new long[dates.size()];
                            for (int i = 0; i < sleepDurations.size(); i++) {
                                sleepDurationsArray[i] = sleepDurations.get(i);
                                datesArray[i] = dates.get(i).getTime();
                            }
                            intent.putExtra("datapoints", sleepDurationsArray);
                            intent.putExtra("dates", datesArray);
                            intent.putExtra("title", "Line Graph for Sleep Duration");
                            startActivity(intent);
                        } else {
                            Log.d("SleepActivity", "Error getting documents: ", task.getException());
                            Toast.makeText(SleepActivity.this, "Failed to retrieve sleep data", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // Method to extract numeric part of sleep duration string
    private String extractNumericPart(String sleepDurationStr) {
        // Remove all non-numeric characters except the decimal point
        return sleepDurationStr.replaceAll("[^\\d.]", "");
    }

    // Method to handle button click to save sleep duration
    public void onSaveButtonClick(View view) {
        // Get the sleep duration entered by the user
        String sleepDurationInput = sleepDurationEditText.getText().toString().trim();

        // Check if the input is empty
        if (sleepDurationInput.isEmpty()) {
            Toast.makeText(this, "Please enter sleep duration", Toast.LENGTH_SHORT).show();
            return;
        }

        // Append " hours" to the sleep duration input
        final String sleepDuration = sleepDurationInput + " hours";

        // Check if currentUserID is null
        if (currentUserID == null) {
            // Handle the case where currentUserID is null, such as displaying an error message
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new data map with sleep duration and date
        Map<String, Object> data = new HashMap<>();
        data.put("sleep_duration", sleepDuration);
        data.put("date", new Date());

        // Add the data to Firestore with the current user's ID
        db.collection("statistics")
                .document(currentUserID) // Use the current user's ID
                .collection("sleep")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Sleep duration saved successfully
                        Toast.makeText(SleepActivity.this, "Sleep duration saved successfully", Toast.LENGTH_SHORT).show();

                        // Clear the EditText after successful save
                        sleepDurationEditText.setText("");

                        // Update the RecyclerView with the new data
                        SleepData newSleepData = new SleepData(sleepDuration, new Date());
                        sleepList.add(newSleepData);
                        adapter.notifyDataSetChanged(); // Notify the adapter about the data change
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save sleep duration
                        Toast.makeText(SleepActivity.this, "Failed to save sleep duration", Toast.LENGTH_SHORT).show();
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
