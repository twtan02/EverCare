package my.edu.utar.evercare;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CaregiverUserActivity extends AppCompatActivity {

    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caregiver_user);

        welcomeTextView = findViewById(R.id.welcomeTextView);

        // Get the username from the intent extras or Firebase User object
        // Example: String username = getIntent().getStringExtra("username");
        // or: FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //      String username = user != null ? user.getEmail() : "";

        // Display a welcome message to the caregiver user
        String welcomeMessage = "Welcome to the Caregiver User Page!";
        welcomeTextView.setText(welcomeMessage);
    }
}
