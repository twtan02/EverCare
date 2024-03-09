package my.edu.utar.evercare.Statistics.BloodLipids;

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

public class BloodLipidsActivity extends AppCompatActivity {

    private EditText bloodLipidsEditText;
    private FirebaseFirestore db;
    private String currentUserID;
    private RecyclerView recyclerView;
    private BloodLipidsAdapter adapter;
    private List<BloodLipidsData> bloodLipidsList;
    private List<Float> dataPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloodlipids);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Blood Lipids");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        TextView dateTextView = findViewById(R.id.dateTextView);
        bloodLipidsEditText = findViewById(R.id.bloodLipidsEditText);
        ImageButton saveButton = findViewById(R.id.saveButton);

        // Set today's date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());
        dateTextView.setText("Today's Date: " + currentDate);

        // Get the current user ID from the intent
        currentUserID = getIntent().getStringExtra("userID");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.bloodLipidsRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize ArrayList to hold blood lipids data
        bloodLipidsList = new ArrayList<>();

        // Initialize adapter
        adapter = new BloodLipidsAdapter(bloodLipidsList); // Initialize the adapter here

        // Retrieve blood lipids data from Firestore
        db.collection("statistics")
                .document(currentUserID) // Assuming currentUserID is available
                .collection("blood_lipids")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            // Retrieve blood lipids data from Firestore
                            String lipidsLevel = document.getString("blood_lipids_level");
                            Date date = document.getDate("date"); // Assuming date is stored as a Date object in Firestore

                            // Create BloodLipidsData object and add to list
                            BloodLipidsData lipidsData = new BloodLipidsData(lipidsLevel, date);
                            bloodLipidsList.add(lipidsData);
                        }

                        // Set the adapter to RecyclerView after data retrieval
                        recyclerView.setAdapter(adapter);

                        // Notify the adapter about the data change
                        adapter.notifyDataSetChanged();
                    } else {
                        // Log error or show error message if retrieval fails
                        Log.d("Firestore", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Failed to retrieve blood lipids data", Toast.LENGTH_SHORT).show();
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

            // Retrieve lipids levels and dates from Firebase
            db.collection("statistics")
                    .document(currentUserID)
                    .collection("blood_lipids")
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            ArrayList<Float> lipidsLevels = new ArrayList<>();
                            ArrayList<Date> dates = new ArrayList<>();
                            for (DocumentSnapshot document : task.getResult()) {
                                String lipidsLevelStr = document.getString("blood_lipids_level");
                                if (lipidsLevelStr != null) {
                                    // Extract numeric part of the blood lipids level string
                                    String numericPart = extractNumericPart(lipidsLevelStr);
                                    if (numericPart != null) {
                                        // Convert the blood lipids level to a numeric type
                                        try {
                                            Float lipidsLevel = Float.parseFloat(numericPart);
                                            lipidsLevels.add(lipidsLevel);
                                            dates.add(document.getDate("date"));
                                        } catch (NumberFormatException e) {
                                            Log.e("BloodLipidsActivity", "Invalid blood lipids level format", e);
                                        }
                                    }
                                }
                            }

                            // Pass lipidsLevels and dates to GraphActivity
                            Intent intent = new Intent(BloodLipidsActivity.this, GraphActivity.class);
                            float[] lipidsLevelsArray = new float[lipidsLevels.size()];
                            long[] datesArray = new long[dates.size()];
                            for (int i = 0; i < lipidsLevels.size(); i++) {
                                lipidsLevelsArray[i] = lipidsLevels.get(i);
                                datesArray[i] = dates.get(i).getTime();
                            }
                            intent.putExtra("datapoints", lipidsLevelsArray);
                            intent.putExtra("dates", datesArray);
                            intent.putExtra("title", "Line Graph for Blood Lipids");
                            startActivity(intent);
                        } else {
                            Log.d("BloodLipidsActivity", "Error getting documents: ", task.getException());
                            Toast.makeText(BloodLipidsActivity.this, "Failed to retrieve blood lipids data", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    // Method to extract numeric part of blood lipids level string
    private String extractNumericPart(String lipidsLevelStr) {
        // Remove all non-numeric characters except the decimal point
        return lipidsLevelStr.replaceAll("[^\\d.]", "");
    }

    // Method to handle button click to save blood lipids level
    public void onSaveButtonClick(View view) {
        // Get the blood lipids level entered by the user
        String bloodLipidsLevelInput = bloodLipidsEditText.getText().toString().trim();

        // Check if the input is empty
        if (bloodLipidsLevelInput.isEmpty()) {
            Toast.makeText(this, "Please enter blood lipids level", Toast.LENGTH_SHORT).show();
            return;
        }

        // Append " mg/dL" to the blood lipids level input
        final String bloodLipidsLevel = bloodLipidsLevelInput + " mg/dL";

        // Check if currentUserID is null
        if (currentUserID == null) {
            // Handle the case where currentUserID is null, such as displaying an error message
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new data map with blood lipids level and date
        Map<String, Object> data = new HashMap<>();
        data.put("blood_lipids_level", bloodLipidsLevel);
        data.put("date", new Date());

        // Add the data to Firestore with the current user's ID
        db.collection("statistics")
                .document(currentUserID) // Use the current user's ID
                .collection("blood_lipids")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Blood lipids level saved successfully
                        Toast.makeText(BloodLipidsActivity.this, "Blood lipids level saved successfully", Toast.LENGTH_SHORT).show();

                        // Clear the EditText after successful save
                        bloodLipidsEditText.setText("");

                        // Update the RecyclerView with the new data
                        BloodLipidsData newLipidsData = new BloodLipidsData(bloodLipidsLevel, new Date());
                        bloodLipidsList.add(newLipidsData);
                        adapter.notifyDataSetChanged(); // Notify the adapter about the data change
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save blood lipids level
                        Toast.makeText(BloodLipidsActivity.this, "Failed to save blood lipids level", Toast.LENGTH_SHORT).show();
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
