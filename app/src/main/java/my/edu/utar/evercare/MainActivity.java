package my.edu.utar.evercare;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(navItemSelectedListener);

        // Load the initial fragment
        loadFragment(new MedicalRecordFragment());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.menu_medical_record:
                    selectedFragment = new MedicalRecordFragment();
                    break;
                case R.id.menu_chat:
                    selectedFragment = new ChatFragment();
                    break;
                case R.id.menu_pill_reminder:
                    selectedFragment = new PillReminderFragment();
                    break;
                case R.id.menu_emergency_help:
                    selectedFragment = new EmergencyHelpFragment();
                    break;
                case R.id.menu_remote_monitoring:
                    selectedFragment = new RemoteMonitoringFragment();
                    break;
            }

            return loadFragment(selectedFragment);
        }
    };

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
