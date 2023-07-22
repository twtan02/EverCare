package my.edu.utar.evercare;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RemoteMonitoringActivity extends AppCompatActivity {

    private TextView textViewData;
    private Button buttonRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_monitoring);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the custom title text
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Remote Monitoring");

        // Enable the back button on the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        textViewData = findViewById(R.id.textViewData);
        buttonRefresh = findViewById(R.id.buttonRefresh);

        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Perform data refresh operation
                refreshData();
            }
        });
    }

    private void refreshData() {
        // Simulating data refresh
        Toast.makeText(this, "Refreshing data...", Toast.LENGTH_SHORT).show();
        // Update the data display
        textViewData.setText("Updated data will be displayed here");
    }

    // Handle back button click event
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
