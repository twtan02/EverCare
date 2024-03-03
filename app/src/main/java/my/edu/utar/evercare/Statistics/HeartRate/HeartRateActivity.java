package my.edu.utar.evercare.Statistics.HeartRate;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import my.edu.utar.evercare.R;

public class HeartRateActivity extends AppCompatActivity {

    private EditText heartRateEditText;
    private FirebaseFirestore db;
    private String currentUserID;
    private RecyclerView recyclerView;
    private HeartRateAdapter adapter;
    private List<HeartRateData> heartRateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartrate);

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

        // Retrieve heart rate data from Firestore
        db.collection("statistics")
                .document(currentUserID) // Assuming currentUserID is available
                .collection("heart_rate")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Retrieve heart rate data from Firestore
                            String heartRateLevel = document.getString("heart_rate_level");
                            Date date = document.getDate("date"); // Assuming date is stored as a Date object in Firestore

                            // Create HeartRateData object and add to list
                            HeartRateData heartRateData = new HeartRateData(heartRateLevel, date);
                            heartRateList.add(heartRateData);
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

        // Initialize adapter
        adapter = new HeartRateAdapter(heartRateList); // Initialize the adapter here

        // Set onClickListener for saveButton
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveButtonClick(v);
            }
        });
    }

    // Method to handle button click to save heart rate level
    public void onSaveButtonClick(View view) {
        // Get the heart rate level entered by the user
        String heartRateLevelInput = heartRateEditText.getText().toString().trim();

        // Check if the input is empty
        if (heartRateLevelInput.isEmpty()) {
            Toast.makeText(this, "Please enter heart rate level", Toast.LENGTH_SHORT).show();
            return;
        }

        // Append " bpm" to the heart rate level input
        final String heartRateLevel = heartRateLevelInput + " bpm";

        // Check if currentUserID is null
        if (currentUserID == null) {
            // Handle the case where currentUserID is null, such as displaying an error message
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new data map with heart rate level and date
        Map<String, Object> data = new HashMap<>();
        data.put("heart_rate_level", heartRateLevel);
        data.put("date", new Date());

        // Add the data to Firestore with the current user's ID
        db.collection("statistics")
                .document(currentUserID) // Use the current user's ID
                .collection("heart_rate")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Heart rate level saved successfully
                        Toast.makeText(HeartRateActivity.this, "Heart rate level saved successfully", Toast.LENGTH_SHORT).show();

                        // Clear the EditText after successful save
                        heartRateEditText.setText("");

                        // Update the RecyclerView with the new data
                        HeartRateData newHeartRateData = new HeartRateData(heartRateLevel, new Date());
                        heartRateList.add(newHeartRateData);
                        adapter.notifyDataSetChanged(); // Notify the adapter about the data change
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save heart rate level
                        Toast.makeText(HeartRateActivity.this, "Failed to save heart rate level", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
