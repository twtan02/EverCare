package my.edu.utar.evercare;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.gridlayout.widget.GridLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.tasks.Task;


public class HomepageActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private Button btnMedicalRecord, btnChat, btnPillReminder, btnEmergencyHelp, btnRemoteMonitoring;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridLayout = findViewById(R.id.gridLayout);
        btnMedicalRecord = findViewById(R.id.btnMedicalRecord);
        btnChat = findViewById(R.id.btnChat);
        btnPillReminder = findViewById(R.id.btnPillReminder);
        btnEmergencyHelp = findViewById(R.id.btnEmergencyHelp);
        btnRemoteMonitoring = findViewById(R.id.btnRemoteMonitoring);

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


        // Set up button click listeners
        btnMedicalRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMedicalRecordEnabled) {
                    startActivity(new Intent(HomepageActivity.this, MedicalRecordActivity.class));
                } else {
                    // Show a message or dialog indicating that this feature is not available
                    showFeatureNotAvailableMessage("Medical Record");
                }
            }
        });

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isChatEnabled) {
                    Intent chatIntent = new Intent(HomepageActivity.this, ChatActivity.class);
                    chatIntent.putExtra("userId", currentUserId); // Pass the user ID
                    startActivity(chatIntent);
                } else {
                    // Show a message or dialog indicating that this feature is not available
                    showFeatureNotAvailableMessage("Chat");
                }
            }
        });

        btnPillReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPillReminderEnabled) {
                    startActivity(new Intent(HomepageActivity.this, PillReminderActivity.class));
                } else {
                    // Show a message or dialog indicating that this feature is not available
                    showFeatureNotAvailableMessage("Pill Reminder");
                }
            }
        });

        btnEmergencyHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEmergencyHelpEnabled) {
                    startActivity(new Intent(HomepageActivity.this, EmergencyHelpActivity.class));
                } else {
                    // Show a message or dialog indicating that this feature is not available
                    showFeatureNotAvailableMessage("Emergency Help");
                }
            }
        });

        btnRemoteMonitoring.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRemoteMonitoringEnabled) {
                    startActivity(new Intent(HomepageActivity.this, RemoteMonitoringActivity.class));
                } else {
                    // Show a message or dialog indicating that this feature is not available
                    showFeatureNotAvailableMessage("Remote Monitoring");
                }
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


    public void showDropdownMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.dropdown_menu); // Create a dropdown_menu.xml in the 'res/menu' folder

        // Set click listeners for menu items in the dropdown menu
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Handle menu item clicks
                switch (item.getItemId()) {
                    case R.id.menu_profile:
                        startActivity(new Intent(HomepageActivity.this, ProfileActivity.class));
                        return true;
                    case R.id.menu_font_size:
                        startActivity(new Intent(HomepageActivity.this, FontSizeActivity.class));
                        return true;
                    case R.id.menu_daily_schedule:
                        startActivity(new Intent(HomepageActivity.this, DailyScheduleActivity.class));
                        return true;
                    case R.id.menu_contact_us:
                        startActivity(new Intent(HomepageActivity.this, ContactUsActivity.class));
                        return true;
                    case R.id.menu_logout:
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(HomepageActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(HomepageActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    default:
                        return false;
                }
            }
        });

        popupMenu.show();
    }


}
