package my.edu.utar.evercare;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MedicalRecordAdapter adapter;
    private List<MedicalRecord> medicalRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record);

        recyclerView = findViewById(R.id.medical_record_recyclerview);
        // Create an empty list to hold the medical records
        medicalRecords = new ArrayList<>();

        // Create and set the adapter
        adapter = new MedicalRecordAdapter(medicalRecords);
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the custom title text
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Medical Record");

        // Enable the back button on the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Add sample medical records to the list (you can fetch data from a database or API)
        medicalRecords.add(new MedicalRecord("John Doe", "Medicine A", "2 tablets"));
        medicalRecords.add(new MedicalRecord("Jane Smith", "Medicine B", "1 capsule"));

        Button addButton = findViewById(R.id.add_medical_record_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogToAddMedicalRecord();
            }
        });
    }


    private void showDialogToAddMedicalRecord() {

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_medical_record, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ADD");
        builder.setView(dialogView);

        // Get references to dialog views
        EditText elderlyNameEditText = dialogView.findViewById(R.id.elderly_name_edittext);
        EditText medicineNameEditText = dialogView.findViewById(R.id.medicine_name_edittext);
        EditText dosageEditText = dialogView.findViewById(R.id.dosage_edittext);
        Button addButton = dialogView.findViewById(R.id.add_button);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            // Get user inputs
            // Get the entered data
            String elderlyName = elderlyNameEditText.getText().toString();
            String medicineName = medicineNameEditText.getText().toString();
            String dosage = dosageEditText.getText().toString();

            // Create a new MedicalRecord object
            MedicalRecord medicalRecord = new MedicalRecord(elderlyName, medicineName, dosage);

            // Add the medical record to the list
            medicalRecords.add(medicalRecord);

            // Notify the adapter about the new item
            adapter.notifyItemInserted(medicalRecords.size() - 1);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Dismiss dialog
            dialog.dismiss();
        });

        // Show dialog
        builder.create().show();
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

