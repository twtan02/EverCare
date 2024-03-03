package my.edu.utar.evercare.Statistics.HeartRate;

import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;

import my.edu.utar.evercare.R;

public class HeartRateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heartrate);

        // Get the reference to the Today's Date TextView
        TextView dateTextView = findViewById(R.id.dateTextView);

        // Get the current date and format it
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String currentDate = sdf.format(new Date());

        // Set the current date to the TextView
        dateTextView.setText("Today's Date: " + currentDate);

        // Example code for adding past blood glucose history item dynamically
        // For demonstration purposes only, replace this with your actual data retrieval logic

        // Get the reference to the Past History Layout
        RelativeLayout pastHistoryLayout = findViewById(R.id.pastHistoryLayout);

        // Example: Create a TextView to represent a blood glucose history item
        TextView historyItemTextView = new TextView(this);
        historyItemTextView.setText("20/02/2022: 120 mg/dL"); // Example data
        historyItemTextView.setTextColor(getResources().getColor(android.R.color.black)); // Set text color to black

        // Define layout params
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        // Set position below the title
        layoutParams.addRule(RelativeLayout.BELOW, R.id.pastHistoryTitleTextView);

        // Add margins
        layoutParams.setMargins(0, 16, 0, 0);

        // Apply layout params
        historyItemTextView.setLayoutParams(layoutParams);

        // Add the TextView to the Past History Layout
        pastHistoryLayout.addView(historyItemTextView);
    }
}

