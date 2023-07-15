package my.edu.utar.evercare;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RemoteMonitoringActivity extends AppCompatActivity {

    private TextView textViewData;
    private Button buttonRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_monitoring);

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
}
