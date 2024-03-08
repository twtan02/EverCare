package my.edu.utar.evercare.Statistics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Date;

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
        if (intent != null && intent.hasExtra("datapoints") && intent.hasExtra("dates") && intent.hasExtra("title")) {
            float[] datapointsArray = intent.getFloatArrayExtra("datapoints");
            long[] datesArray = intent.getLongArrayExtra("dates");
            ArrayList<Float> datapoints = new ArrayList<>();
            ArrayList<Date> dates = new ArrayList<>();
            String title = intent.getStringExtra("title");
            for (float value : datapointsArray) {
                datapoints.add(value);
            }
            for (long timestamp : datesArray) {
                dates.add(new Date(timestamp));
            }

            graphView = findViewById(R.id.graphView);
            if (graphView != null) {
                graphView.setDataPoints(datapoints, dates);

                // Set the title of the graph
                TextView graphTitle = findViewById(R.id.graphTitle);
                graphTitle.setText(title);
            } else {
                Log.e("GraphActivity", "graphView is null");
            }
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
