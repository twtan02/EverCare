package my.edu.utar.evercare;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ContactUsActivity extends AppCompatActivity {

    private TextView textViewContactInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);

        textViewContactInfo = findViewById(R.id.textViewContactInfo);

        // Replace this text with your contact information
        String contactInfo = "EverCare Elderly Home\n" +
                "Address: 1234 Elderly Street, City\n" +
                "Phone: +1 234 567 890\n" +
                "Email: info@evercare.com";

        // Set the contact information text to the TextView
        textViewContactInfo.setText(contactInfo);
    }
}
