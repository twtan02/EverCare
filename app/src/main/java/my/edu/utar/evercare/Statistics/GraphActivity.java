package my.edu.utar.evercare.Statistics;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import my.edu.utar.evercare.R;
import my.edu.utar.evercare.Statistics.LineGraphView;

public class GraphActivity extends AppCompatActivity {

    private LineGraphView graphView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Graph");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get data passed from previous activity
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("dataPoints") && intent.hasExtra("title")) {
            float[] dataArray = intent.getFloatArrayExtra("dataPoints");
            ArrayList<Float> dataPoints = new ArrayList<>();
            String title = intent.getStringExtra("title");
            for (float value : dataArray) {
                dataPoints.add(value);
            }
            graphView = findViewById(R.id.graphView);
            graphView.setDataPoints(dataPoints);

            // Set the title of the graph
            TextView graphTitle = findViewById(R.id.graphTitle);
            graphTitle.setText(title);
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
