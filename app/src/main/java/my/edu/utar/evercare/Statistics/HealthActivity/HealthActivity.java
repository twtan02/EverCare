package my.edu.utar.evercare.Statistics.HealthActivity;

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

public class HealthActivity extends AppCompatActivity {

    private EditText healthDataEditText;
    private FirebaseFirestore db;
    private String currentUserID;
    private RecyclerView recyclerView;
    private HealthActivityAdapter adapter;
    private List<HealthActivityData> healthDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_healthactivity);

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
                .collection("health_activity")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
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
                        Toast.makeText(HealthActivity.this, "Failed to retrieve health data", Toast.LENGTH_SHORT).show();
                    }
                });

        // Set onClickListener for saveButton
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveButtonClick(v);
            }
        });
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

        // Check if currentUserID is null
        if (currentUserID == null) {
            // Handle the case where currentUserID is null, such as displaying an error message
            Toast.makeText(this, "User ID is null", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new data map with health data and date
        Map<String, Object> data = new HashMap<>();
        data.put("health_data", healthDataInput);
        data.put("date", new Date());

        // Add the data to Firestore with the current user's ID
        db.collection("statistics")
                .document(currentUserID) // Use the current user's ID
                .collection("health_activity")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Health data saved successfully
                        Toast.makeText(HealthActivity.this, "Health data saved successfully", Toast.LENGTH_SHORT).show();

                        // Clear the EditText after successful save
                        healthDataEditText.setText("");

                        // Update the RecyclerView with the new data
                        HealthActivityData newData = new HealthActivityData(healthDataInput, new Date());
                        healthDataList.add(newData);
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
}
