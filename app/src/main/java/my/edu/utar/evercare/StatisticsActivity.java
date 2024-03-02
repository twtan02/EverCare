package my.edu.utar.evercare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity implements StatisticsPagerAdapter.OnItemClickListener {

    ViewPager2 viewPager2;
    ArrayList<ViewPagerItem> viewPagerItemArrayList;
    StatisticsPagerAdapter vpAdapter;
    private String currentUserId;
    private RecyclerView recyclerView;
    private BloodGlucoseAdapter adapter;
    private List<BloodGlucoseData> bloodGlucoseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Initialize viewPagerItemArrayList
        viewPagerItemArrayList = new ArrayList<>();

        // Retrieve data from Firestore and populate viewPagerItemArrayList
        retrieveDataFromFirestore();

        // Create adapter with viewPagerItemArrayList
        vpAdapter = new StatisticsPagerAdapter(viewPagerItemArrayList, this);

        // Set adapter to ViewPager2
        viewPager2 = findViewById(R.id.viewpager);
        viewPager2.setAdapter(vpAdapter);

        // Register onPageChangeCallback for ViewPager2
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update currentUserId when the page changes
                currentUserId = viewPagerItemArrayList.get(position).getUserId();

            }
        });
    }


    private void retrieveDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("elderly_users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String profileImageUrl = document.getString("profileImageUrl");
                        String username = document.getString("username");
                        String dateOfBirth = document.getString("dateOfBirth");
                        String userId = document.getId(); // Retrieve the user ID

                        ViewPagerItem viewPagerItem = new ViewPagerItem(userId, profileImageUrl, username, dateOfBirth);
                        viewPagerItemArrayList.add(viewPagerItem);

                        if (currentUserId == null) {
                            currentUserId = userId;
                        }
                    }
                    vpAdapter = new StatisticsPagerAdapter(viewPagerItemArrayList, this);
                    viewPager2.setAdapter(vpAdapter);

//                    fetchBloodGlucoseData();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting documents: ", e);
                });
    }

    private void fetchBloodGlucoseData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("statistics")
                .document(currentUserId)
                .collection("blood_glucose")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String bloodGlucoseLevel = document.getString("blood_glucose_level");
                        BloodGlucoseData bloodGlucoseData = new BloodGlucoseData(bloodGlucoseLevel, document.getDate("date"));
                        bloodGlucoseList.add(bloodGlucoseData);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting blood glucose data: ", e);
                });
    }

    @Override
    public void onItemClick(String healthRecordType) {
        Intent intent;
        switch (healthRecordType) {
            case "Blood Glucose":
                intent = new Intent(StatisticsActivity.this, BloodGlucoseActivity.class);
                intent.putExtra("userID", currentUserId);
                startActivity(intent);
                break;
            case "Blood Pressure":
                intent = new Intent(StatisticsActivity.this, BloodPressureActivity.class);
                startActivity(intent);
                break;
            case "Weight":
                intent = new Intent(StatisticsActivity.this, WeightActivity.class);
                startActivity(intent);
                break;
            case "Heart Rate":
                intent = new Intent(StatisticsActivity.this, HeartRateActivity.class);
                startActivity(intent);
                break;
            case "Activity":
                intent = new Intent(StatisticsActivity.this, HealthActivity.class);
                startActivity(intent);
                break;
            case "Sleep":
                intent = new Intent(StatisticsActivity.this, SleepActivity.class);
                startActivity(intent);
                break;
        }
    }
}