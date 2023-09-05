package my.edu.utar.evercare;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class HomepageActivity extends BaseActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment selectedFragment = null;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the custom title text
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        // Set the Welcome Message and Notification
        String welcomeMessage = "Welcome to EVERCARE";
        String notification = "About Us";

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("HOME");

        WebView longParagraphWebView = findViewById(R.id.longParagraphWebView);
        String longParagraph = getResources().getString(R.string.long_paragraph);
        longParagraphWebView.loadDataWithBaseURL(null, longParagraph, "text/html", "utf-8", null);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("elderly_users").document(currentUserId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // The user exists in the 'elderly_users' collection
                                enableElderlyFeatures();
                            } else {
                                // The user doesn't exist in the 'elderly_users' collection,
                                // check if they are in 'staff_users' or 'caregiver_users' collections
                                checkStaffAndCaregiverRoles();
                            }
                        } else {
                            // Handle the error
                            disableAllFeatures();
                        }
                    }
                });

        // Set the listener for item selection
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle item selection here
                switch (item.getItemId()) {
                    case R.id.menu_medical_record:
                        // Handle medical record selection
                        if (isMedicalRecordEnabled) {
                            startActivity(new Intent(HomepageActivity.this, MedicalRecordActivity.class));
                        } else {
                            // Show a message or dialog indicating that this feature is not available
                            showFeatureNotAvailableMessage("Medical Record");
                        }
                        return true;
                    case R.id.menu_chat:
                        // Handle chat selection
                        if (isChatEnabled) {
                            Intent chatIntent = new Intent(HomepageActivity.this, ChatActivity.class);
                            chatIntent.putExtra("userId", currentUserId); // Pass the user ID
                            startActivity(chatIntent);
                        } else {
                            // Show a message or dialog indicating that this feature is not available
                            showFeatureNotAvailableMessage("Chat");
                        }
                        return true;
                    case R.id.menu_pill_reminder:
                        // Handle pill reminder selection
                        if (isPillReminderEnabled) {
                            startActivity(new Intent(HomepageActivity.this, PillReminderActivity.class));
                        } else {
                            // Show a message or dialog indicating that this feature is not available
                            showFeatureNotAvailableMessage("Pill Reminder");
                        }
                        return true;
                    case R.id.menu_emergency_help:
                        // Handle emergency help selection
                        if (isEmergencyHelpEnabled) {
                            startActivity(new Intent(HomepageActivity.this, EmergencyHelpActivity.class));
                        } else {
                            // Show a message or dialog indicating that this feature is not available
                            showFeatureNotAvailableMessage("Emergency Help");
                        }
                        return true;
                    case R.id.menu_remote_monitoring:
                        // Handle remote monitoring selection
                        if (isRemoteMonitoringEnabled) {
                            startActivity(new Intent(HomepageActivity.this, RemoteMonitoringActivity.class));
                        } else {
                            // Show a message or dialog indicating that this feature is not available
                            showFeatureNotAvailableMessage("Remote Monitoring");
                        }
                        return true;
                }
                return false;
            }
        });

    }

    private void checkStaffAndCaregiverRoles() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("staff_users").document(currentUserId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot staffDocument = task.getResult();
                            if (staffDocument != null && staffDocument.exists()) {
                                // The user exists in the 'staff_users' collection
                                enableStaffAndCaregiverFeatures();
                            } else {
                                // The user doesn't exist in the 'staff_users' collection,
                                // check the 'caregiver_users' collection
                                checkCaregiverRole();
                            }
                        } else {
                            // Handle the error
                            disableAllFeatures();
                        }
                    }
                });
    }

    private void checkCaregiverRole() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firestore.collection("caregiver_users").document(currentUserId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot caregiverDocument = task.getResult();
                            if (caregiverDocument != null && caregiverDocument.exists()) {
                                // The user exists in the 'caregiver_users' collection
                                enableStaffAndCaregiverFeatures();
                            } else {
                                // The user doesn't exist in any recognized role collection
                                disableAllFeatures();
                            }
                        } else {
                            // Handle the error
                            disableAllFeatures();
                        }
                    }
                });
    }

    // Flags to enable or disable features based on user role
    private boolean isMedicalRecordEnabled = false;
    private boolean isChatEnabled = false;
    private boolean isPillReminderEnabled = false;
    private boolean isEmergencyHelpEnabled = false;
    private boolean isRemoteMonitoringEnabled = false;

    // Method to enable features for elderly users
    private void enableElderlyFeatures() {
        isChatEnabled = true;
        isEmergencyHelpEnabled = true;
    }

    // Method to enable features for staff and caregiver users
    private void enableStaffAndCaregiverFeatures() {
        isMedicalRecordEnabled = true;
        isPillReminderEnabled = true;
        isChatEnabled = true;
        isRemoteMonitoringEnabled = true;
    }

    // Method to disable all features
    private void disableAllFeatures() {
        // Disable all features for unrecognized roles
    }

    // Method to show a message or dialog indicating that a feature is not available
    private void showFeatureNotAvailableMessage(String featureName) {
        Toast.makeText(this, featureName + " feature is not available for your role.", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_profile) {
            // Handle profile option
            startActivity(new Intent(HomepageActivity.this, ProfileActivity.class));
            return true;
        } else if (id == R.id.menu_font_size) {
            // Handle font size option
            startActivity(new Intent(HomepageActivity.this, FontSizeActivity.class));
            return true;
        } else if (id == R.id.menu_daily_schedule) {
            // Handle daily schedule option
            startActivity(new Intent(HomepageActivity.this, DailyScheduleActivity.class));
            return true;
        } else if (id == R.id.menu_contact_us) {
            // Handle contact us option
            startActivity(new Intent(HomepageActivity.this, ContactUsActivity.class));
            return true;
        } else if (id == R.id.menu_logout) {
            // Handle logout option
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(HomepageActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(HomepageActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
