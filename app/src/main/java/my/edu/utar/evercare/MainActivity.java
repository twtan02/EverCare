package my.edu.utar.evercare;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the user is already logged in
        // Replace this condition with your own logic to determine if the user is logged in
        if (isLoggedIn()) {
            // If the user is already logged in, start the main activity or home screen
            startHomeActivity();
        } else {
            // If the user is not logged in, start the login activity
            startLoginActivity();
        }

        FirebaseApp.initializeApp(this);

        // Finish the current activity
        finish();
    }

    private boolean isLoggedIn() {
        // Implement your own logic here to check if the user is logged in
        // You can use shared preferences, a database, or any other method to store and check the login status
        // Return true if the user is logged in, false otherwise
        // For this example, assume the user is not logged in
        return false;
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }
}

