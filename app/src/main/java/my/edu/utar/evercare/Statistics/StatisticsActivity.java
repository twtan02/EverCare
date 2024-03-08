package my.edu.utar.evercare.Statistics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import my.edu.utar.evercare.R;
import my.edu.utar.evercare.Statistics.BloodGlucose.BloodGlucoseActivity;
import my.edu.utar.evercare.Statistics.BloodPressure.BloodPressureActivity;
import my.edu.utar.evercare.Statistics.HealthActivity.HealthActivity;
import my.edu.utar.evercare.Statistics.HeartRate.HeartRateActivity;
import my.edu.utar.evercare.Statistics.Sleep.SleepActivity;
import my.edu.utar.evercare.Statistics.Weight.WeightActivity;

public class StatisticsActivity extends AppCompatActivity implements StatisticsPagerAdapter.OnItemClickListener {

    ViewPager2 viewPager2;
    StatisticsPagerAdapter vpAdapter;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Statistics");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        viewPager2 = findViewById(R.id.viewpager);

        // Retrieve data from Firestore and populate viewPagerItemArrayList
        retrieveDataFromFirestore();
    }

    private void retrieveDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("elderly_users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<ViewPagerItem> viewPagerItemArrayList = new ArrayList<>();
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

                    // Set adapter to ViewPager2
                    vpAdapter = new StatisticsPagerAdapter(viewPagerItemArrayList, StatisticsActivity.this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
