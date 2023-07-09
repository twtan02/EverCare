package my.edu.utar.evercare;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomePageActivity extends AppCompatActivity {

    private TextView welcomeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        welcomeTextView = findViewById(R.id.welcomeTextView);

        // Get the username from the intent extras or Firebase User object
        // Example: String username = getIntent().getStringExtra("username");
        // or: FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //      String username = user != null ? user.getEmail() : "";

        // Display a welcome message to the user
        String welcomeMessage = "Welcome to the Home Page!";
        welcomeTextView.setText(welcomeMessage);
    }
}
