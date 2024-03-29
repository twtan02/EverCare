package my.edu.utar.evercare.Statistics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

import my.edu.utar.evercare.R;
import my.edu.utar.evercare.Statistics.BloodGlucose.BloodGlucoseActivity;
import my.edu.utar.evercare.Statistics.BloodLipids.BloodLipidsActivity;
import my.edu.utar.evercare.Statistics.BloodPressure.BloodPressureActivity;
import my.edu.utar.evercare.Statistics.HeartRate.HeartRateActivity;
import my.edu.utar.evercare.Statistics.Sleep.SleepActivity;
import my.edu.utar.evercare.Statistics.Weight.WeightActivity;

public class StatisticsActivity extends AppCompatActivity implements StatisticsPagerAdapter.OnItemClickListener {

    ViewPager2 viewPager2;
    StatisticsPagerAdapter vpAdapter;
    private String currentUserId;
    private FirebaseFirestore db;
    private String role;
    private String currentID;

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

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Check the current user's role and adjust EditText accordingly
        checkUserRole();

        viewPager2 = findViewById(R.id.viewpager);
    }

    private void retrieveDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (role != null) {
            switch (role) {
                case "staff":
                    // Retrieve data for staff
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
                                vpAdapter = new StatisticsPagerAdapter(viewPagerItemArrayList, this, currentUserId);
                                viewPager2.setAdapter(vpAdapter);

                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error getting documents: ", e);
                                Toast.makeText(StatisticsActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                            });
                    break;
                case "caregiver":
                    // Retrieve data for caregiver
                    db.collection("caregiver_users")
                            .document(currentID)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String elderlyParentName = documentSnapshot.getString("elderlyParentName");
                                    if (elderlyParentName != null) {
                                        // Retrieve data for the elderly parent
                                        db.collection("elderly_users")
                                                .whereEqualTo("username", elderlyParentName)
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
                                                    vpAdapter = new StatisticsPagerAdapter(viewPagerItemArrayList, this, currentUserId);
                                                    viewPager2.setAdapter(vpAdapter);

                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Firestore", "Error getting documents: ", e);
                                                    Toast.makeText(StatisticsActivity.this, "Failed to retrieve user data", Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(StatisticsActivity.this, "Elderly parent name not found for caregiver", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(StatisticsActivity.this, "Caregiver document not found", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error getting caregiver document: ", e);
                                Toast.makeText(StatisticsActivity.this, "Failed to retrieve caregiver document", Toast.LENGTH_SHORT).show();
                            });

                    break;
                case "elderly":
                    // Retrieve data for elderly
                    db.collection("elderly_users")
                            .document(currentID)
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String profileImageUrl = documentSnapshot.getString("profileImageUrl");
                                    String username = documentSnapshot.getString("username");
                                    String dateOfBirth = documentSnapshot.getString("dateOfBirth");
                                    String userId = documentSnapshot.getId(); // Retrieve the user ID

                                    ArrayList<ViewPagerItem> viewPagerItemArrayList = new ArrayList<>();
                                    ViewPagerItem viewPagerItem = new ViewPagerItem(userId, profileImageUrl, username, dateOfBirth);
                                    viewPagerItemArrayList.add(viewPagerItem);

                                    // Set adapter to ViewPager2
                                    vpAdapter = new StatisticsPagerAdapter(viewPagerItemArrayList, this, currentUserId);
                                    viewPager2.setAdapter(vpAdapter);
                                } else {
                                    Toast.makeText(StatisticsActivity.this, "Elderly user document not found", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e("Firestore", "Error getting elderly user document: ", e);
                                Toast.makeText(StatisticsActivity.this, "Failed to retrieve elderly user document", Toast.LENGTH_SHORT).show();
                            });
                    break;
                default:
                    // Handle unrecognized role
                    Toast.makeText(StatisticsActivity.this, "Unknown role: " + role, Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            // Handle null role
            Toast.makeText(StatisticsActivity.this, "Role not found", Toast.LENGTH_SHORT).show();
        }
    }



    private String getCurrentUserId() {
        // Assuming you are using Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is authenticated
        if (currentUser != null) {
            // The user is signed in, return their UID
            return currentUser.getUid();
        } else {
            // No user is signed in, handle accordingly (e.g., redirect to login)
            // Return an empty string or throw an exception based on your app's logic
            return "";
        }
    }

    private void checkUserRole() {
        // User that logged in the app
        currentID = getCurrentUserId();
        // Query Firestore to retrieve the user's role based on the currentUserID
        db.collection("all_users")
                .document(currentID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userRole = documentSnapshot.getString("role");
                        if (userRole != null) {
                            // Check the user's role and adjust EditText accordingly
                            switch (userRole) {
                                case "Caregiver":
                                    role = "caregiver";
                                    break;
                                case "Staff":
                                    role = "staff";
                                    break;
                                case "Elderly":
                                    role = "elderly";
                                    break;
                            }
                            retrieveDataFromFirestore();
                        } else {
                            // Handle case where role is null or not found
                            Toast.makeText(this, "User role not found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Handle case where document does not exist
                        Toast.makeText(this, "User document not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure in retrieving user document
                    Toast.makeText(this, "Failed to retrieve user document: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onItemClick(String healthRecordType) {
        // Get the current position of ViewPager2
        int currentPosition = viewPager2.getCurrentItem();

        // Get the ViewPagerItem at the current position
        ViewPagerItem viewPagerItem = vpAdapter.getViewPagerItemAtPosition(currentPosition);
        if (viewPagerItem != null) {
            // Update the currentUserId with the userId from the selected ViewPagerItem
            currentUserId = viewPagerItem.getUserId();

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
                case "Blood Lipids":
                    intent = new Intent(StatisticsActivity.this, BloodLipidsActivity.class);
                    intent.putExtra("userID", currentUserId);
                    startActivity(intent);
                    break;
                case "Heart Rate":
                    intent = new Intent(StatisticsActivity.this, HeartRateActivity.class);
                    intent.putExtra("userID", currentUserId);
                    startActivity(intent);
                    break;
                case "Weight":
                    intent = new Intent(StatisticsActivity.this, WeightActivity.class);
                    intent.putExtra("userID", currentUserId);
                    startActivity(intent);
                    break;
                case "Sleep":
                    intent = new Intent(StatisticsActivity.this, SleepActivity.class);
                    intent.putExtra("userID", currentUserId);
                    startActivity(intent);
                    break;
            }

            // Notify the adapter of the data change after starting the activity
            vpAdapter.notifyDataSetChanged();
        } else {
            Log.e("StatisticsActivity", "Error: ViewPagerItem is null at position " + currentPosition);
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
