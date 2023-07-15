package my.edu.utar.evercare;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomepageActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set the listener for item selection
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle item selection here
                switch (item.getItemId()) {
                    case R.id.menu_medical_record:
                        // Handle medical record selection
                        startActivity(new Intent(HomepageActivity.this, MedicalRecordActivity.class));
                        return true;
                    case R.id.menu_chat:
                        // Handle chat selection
                        startActivity(new Intent(HomepageActivity.this, ChatActivity.class));
                        return true;
                    case R.id.menu_pill_reminder:
                        // Handle pill reminder selection
                        startActivity(new Intent(HomepageActivity.this, PillReminderActivity.class));
                        return true;
                    case R.id.menu_emergency_help:
                        // Handle emergency help selection
                        startActivity(new Intent(HomepageActivity.this, EmergencyHelpActivity.class));
                        return true;
                    case R.id.menu_remote_monitoring:
                        // Handle remote monitoring selection
                        startActivity(new Intent(HomepageActivity.this, RemoteMonitoringActivity.class));
                        return true;
                }
                return false;
            }
        });

        selectedFragment = new MedicalRecordFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();


        findViewById(R.id.btnLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform logout action
                FirebaseAuth.getInstance().signOut();

                // Show logout message
                Toast.makeText(HomepageActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                // Navigate back to the login activity
                Intent intent = new Intent(HomepageActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finish the current activity to prevent going back to it
            }
        });
    }
}
