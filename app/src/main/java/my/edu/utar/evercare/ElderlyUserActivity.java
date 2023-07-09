package my.edu.utar.evercare;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ElderlyUserActivity extends AppCompatActivity {

    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elderly_user);

        welcomeTextView = findViewById(R.id.welcomeTextView);

        // Get the username from the intent extras
        String username = getIntent().getStringExtra("username");

        // Display a welcome message to the elderly user
        String welcomeMessage = "Welcome, " + username + "!";
        welcomeTextView.setText(welcomeMessage);
    }
}
