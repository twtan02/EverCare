package my.edu.utar.evercare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordActivity extends AppCompatActivity implements MedicalRecordAdapter.OnMedicalRecordClickListener {

    private List<ElderlyUser> elderlyUsers;
    private List<MedicalRecord> medicalRecords = new ArrayList<>();
    private MedicalRecordAdapter medicalRecordAdapter;
    private FirebaseFirestore firestore;
    private RecyclerView medicalRecordRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record);

        // Initialize views and variables
        elderlyUsers = new ArrayList<>();
        medicalRecords = new ArrayList<>();
        firestore = FirebaseFirestore.getInstance();
        medicalRecordRecyclerView = findViewById(R.id.medical_record_recyclerview);

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

        // Set up the RecyclerView with a LinearLayoutManager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        medicalRecordRecyclerView.setLayoutManager(layoutManager);

        // Fetch elderly users from Firestore
        fetchElderlyUsersFromFirestore();
        // Setup RecyclerView with empty medicalRecords
        setupRecyclerView();

        // Check if there's an elderly user available
        if (!elderlyUsers.isEmpty()) {
            // Fetch and display medical records for the first elderly user
            fetchMedicalRecordsFromFirestore(elderlyUsers.get(0));
        }

        // Set up the FloatingActionButton click listener
        FloatingActionButton fabAddMedicalRecord = findViewById(R.id.fab_add_medical_record);
        fabAddMedicalRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the dialog to select the elderly user first
                if (elderlyUsers.size() > 0) {
                    showChooseElderlyUserDialog();
                } else {
                    Toast.makeText(MedicalRecordActivity.this, "No elderly users found.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void fetchElderlyUsersFromFirestore() {
        firestore.collection("elderly_users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            elderlyUsers.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                ElderlyUser elderlyUser = document.toObject(ElderlyUser.class);
                                if (elderlyUser != null) {
                                    elderlyUsers.add(elderlyUser);
                                }
                            }
                            // Update the RecyclerView with the new data
                            medicalRecordAdapter.setElderlyUsers(elderlyUsers);
                            medicalRecordAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("MedicalRecordActivity", "Error getting elderly users: ", task.getException());
                        }
                    }
                });
    }

    private void fetchMedicalRecordsFromFirestore(ElderlyUser selectedElderlyUser) {
        firestore.collection("medical_records")
                .whereEqualTo("elderlyId", selectedElderlyUser.getUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            medicalRecords.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                MedicalRecord medicalRecord = document.toObject(MedicalRecord.class);
                                if (medicalRecord != null) {
                                    medicalRecords.add(medicalRecord);
                                }
                            }
                            // Update the RecyclerView with the new data
                            medicalRecordAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("MedicalRecordActivity", "Error getting medical records: ", task.getException());
                        }
                    }
                });
    }

    private void showChooseElderlyUserDialog() {
        List<String> elderlyUserNames = new ArrayList<>();
        for (ElderlyUser elderlyUser : elderlyUsers) {
            elderlyUserNames.add(elderlyUser.getUsername());
        }

        String[] elderlyUserArray = elderlyUserNames.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Elderly User");
        builder.setItems(elderlyUserArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ElderlyUser selectedElderlyUser = elderlyUsers.get(which);
                showAddMedicalRecordDialog(selectedElderlyUser);
            }
        });
        builder.show();
    }

    private void showAddMedicalRecordDialog(ElderlyUser selectedElderlyUser) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_medical_record, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ADD");
        builder.setView(dialogView);

        // Get references to the views in the dialog
        ImageView profileImageView = dialogView.findViewById(R.id.profile_pic_imageview);
        TextView elderlyNameTextView = dialogView.findViewById(R.id.elderly_name_textview);
        EditText medicineNameEditText = dialogView.findViewById(R.id.medicine_name_edittext);
        EditText dosageEditText = dialogView.findViewById(R.id.dosage_edittext);
        dosageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5), new InputFilterOnlyNumeric()});

        // Display the selected elderly user's profile picture and name in the dialog
        if (!TextUtils.isEmpty(selectedElderlyUser.getProfileImageUrl())) {
            Glide.with(this)
                    .load(selectedElderlyUser.getProfileImageUrl())
                    .placeholder(R.drawable.default_profile_image)
                    .error(R.drawable.default_failure_profile)
                    .into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.default_profile_image);
        }
        elderlyNameTextView.setText(selectedElderlyUser.getUsername());

        // Set the initial dosage to 0 by default
        dosageEditText.setText("0");

        // Set the click listener for increment and decrement dosage buttons
        Button incrementButton = dialogView.findViewById(R.id.increment_dosage_button);
        incrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                incrementDosage(dosageEditText);
            }
        });

        Button decrementButton = dialogView.findViewById(R.id.decrement_dosage_button);
        decrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrementDosage(dosageEditText);
            }
        });

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Save the medical record to Firestore
                String medicineName = medicineNameEditText.getText().toString();
                String dosageString = dosageEditText.getText().toString();
                if (!TextUtils.isEmpty(medicineName) && !TextUtils.isEmpty(dosageString)) {
                    int dosage = Integer.parseInt(dosageString);
                    saveMedicalRecord(selectedElderlyUser, medicineName, dosage);
                }

                // After saving the medical record, fetch and update the medical records for the selected elderly user
                fetchMedicalRecordsFromFirestore(selectedElderlyUser);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    private void incrementDosage(EditText dosageEditText) {
        String dosageString = dosageEditText.getText().toString();
        if (!TextUtils.isEmpty(dosageString)) {
            int dosage = Integer.parseInt(dosageString);
            dosage++;
            dosageEditText.setText(String.valueOf(dosage));
        }
    }

    private void decrementDosage(EditText dosageEditText) {
        String dosageString = dosageEditText.getText().toString();
        if (!TextUtils.isEmpty(dosageString)) {
            int dosage = Integer.parseInt(dosageString);
            dosage--;
            dosage = Math.max(0, dosage); // Ensure dosage is non-negative
            dosageEditText.setText(String.valueOf(dosage));
        }
    }

    private void saveMedicalRecord(ElderlyUser elderlyUser, String medicineName, int dosage) {
        // Convert the dosage integer to a String
        String dosageString = String.valueOf(dosage);

        // Get a reference to the Firestore collection where medical records will be stored
        CollectionReference medicalRecordsRef = firestore.collection("medical_records");

        // Create a new Medication object
        Medication medication = new Medication(medicineName, dosageString);
        List<Medication> medications = new ArrayList<>();
        medications.add(medication);

        // Create a new MedicalRecord object
        String elderlyId = elderlyUser.getUserId();
        String elderlyName = elderlyUser.getUsername();
        String profilePicUrl = elderlyUser.getProfileImageUrl();
        MedicalRecord medicalRecord = new MedicalRecord(elderlyId, elderlyName, profilePicUrl, medications);

        // Save the medical record to Firestore
        medicalRecordsRef.add(medicalRecord)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("MedicalRecordActivity", "Medical record added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MedicalRecordActivity", "Error adding medical record", e);
                    }
                });
    }

    @Override
    public void onMedicalRecordClick(MedicalRecord medicalRecord) {
        // Handle the click event for the medical record here
        // You can open a detailed view or perform any other action
        // based on the clicked medical record

        // For example, you can pass the medical record to another activity for detailed view
        Intent intent = new Intent(this, MedicalRecordActivity.class);
        intent.putExtra("medicalRecord", medicalRecord);
        startActivity(intent);
    }

    // Additional utility class for input filter to allow only numeric input
    private class InputFilterOnlyNumeric implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = start; i < end; i++) {
                char character = source.charAt(i);
                if (Character.isDigit(character)) {
                    stringBuilder.append(character);
                }
            }
            return stringBuilder.toString();
        }
    }

    private void setupRecyclerView() {
        medicalRecordAdapter = new MedicalRecordAdapter(this, medicalRecords, this);
        medicalRecordRecyclerView.setAdapter(medicalRecordAdapter);
        medicalRecordRecyclerView.setLayoutManager(new LinearLayoutManager(this));
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
