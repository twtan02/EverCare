package my.edu.utar.evercare.Statistics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import my.edu.utar.evercare.Statistics.BloodGlucose.BloodGlucoseActivity;
import my.edu.utar.evercare.Statistics.BloodGlucose.BloodGlucoseAdapter;
import my.edu.utar.evercare.Statistics.BloodGlucose.BloodGlucoseData;
import my.edu.utar.evercare.Statistics.BloodPressure.BloodPressureActivity;
import my.edu.utar.evercare.Statistics.HealthActivity.HealthActivity;
import my.edu.utar.evercare.Statistics.HeartRate.HeartRateActivity;
import my.edu.utar.evercare.R;
import my.edu.utar.evercare.Statistics.Sleep.SleepActivity;
import my.edu.utar.evercare.Statistics.Weight.WeightActivity;

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
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting documents: ", e);
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
                intent.putExtra("userID", currentUserId);
                startActivity(intent);
                break;
            case "Weight":
                intent = new Intent(StatisticsActivity.this, WeightActivity.class);
                intent.putExtra("userID", currentUserId);
                startActivity(intent);
                break;
            case "Heart Rate":
                intent = new Intent(StatisticsActivity.this, HeartRateActivity.class);
                intent.putExtra("userID", currentUserId);
                startActivity(intent);
                break;
            case "Activity":
                intent = new Intent(StatisticsActivity.this, HealthActivity.class);
                intent.putExtra("userID", currentUserId);
                startActivity(intent);
                break;
            case "Sleep":
                intent = new Intent(StatisticsActivity.this, SleepActivity.class);
                intent.putExtra("userID", currentUserId);
                startActivity(intent);
                break;
        }
    }
}