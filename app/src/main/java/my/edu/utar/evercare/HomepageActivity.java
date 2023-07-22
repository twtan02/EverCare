package my.edu.utar.evercare;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class HomepageActivity extends BaseActivity  {

    private BottomNavigationView bottomNavigationView;
    private Fragment selectedFragment = null;

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
