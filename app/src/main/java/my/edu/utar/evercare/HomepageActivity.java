package my.edu.utar.evercare;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Set the title
        toolbar.setTitle("HOME");

        // Set the toolbar as the ActionBar
        setSupportActionBar(toolbar);

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
