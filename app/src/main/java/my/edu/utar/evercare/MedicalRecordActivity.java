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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicalRecordActivity extends AppCompatActivity implements MedicalRecordAdapter.OnMedicalRecordClickListener {

    private List<ElderlyUser> elderlyUsers = new ArrayList<>();
    private Map<String, List<MedicalRecord>> medicalRecordsMap = new HashMap<>();
    private MedicalRecordAdapter medicalRecordAdapter;
    private FirebaseFirestore firestore;
    private RecyclerView medicalRecordRecyclerView;
    private MedicalRecordItemAdapter medicalRecordItemAdapter;
    private AlertDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record);

        firestore = FirebaseFirestore.getInstance();
        medicalRecordRecyclerView = findViewById(R.id.medical_record_recyclerview);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Medical Record");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        medicalRecordRecyclerView.setLayoutManager(layoutManager);

        fetchElderlyUsersFromFirestore();
        setupRecyclerView();

        FloatingActionButton fabAddMedicalRecord = findViewById(R.id.fab_add_medical_record);
        fabAddMedicalRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!elderlyUsers.isEmpty()) {
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
                            if (!elderlyUsers.isEmpty()) {
                                fetchMedicalRecordsForElderlyUsers();
                            } else {
                                medicalRecordAdapter.setMedicalRecords(new ArrayList<>());
                            }
                        } else {
                            Log.e("MedicalRecordActivity", "Error getting elderly users: ", task.getException());
                        }
                    }
                });
    }

    private void fetchMedicalRecordsForElderlyUsers() {
        medicalRecordsMap.clear();
        for (ElderlyUser elderlyUser : elderlyUsers) {
            firestore.collection("medical_records")
                    .whereEqualTo("elderlyId", elderlyUser.getUserId())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                List<MedicalRecord> medicalRecords = new ArrayList<>();
                                for (DocumentSnapshot document : task.getResult()) {
                                    MedicalRecord medicalRecord = document.toObject(MedicalRecord.class);
                                    if (medicalRecord != null) {
                                        medicalRecords.add(medicalRecord);
                                    }
                                }
                                medicalRecordsMap.put(elderlyUser.getUserId(), medicalRecords);
                                updateRecyclerView();
                            } else {
                                Log.e("MedicalRecordActivity", "Error getting medical records: ", task.getException());
                            }
                        }
                    });
        }
    }

    private void updateRecyclerView() {
        List<MedicalRecord> allMedicalRecords = new ArrayList<>();

        for (String elderlyUserId : medicalRecordsMap.keySet()) {
            List<MedicalRecord> records = medicalRecordsMap.get(elderlyUserId);
            MedicalRecord groupRecord = new MedicalRecord();

            groupRecord.setElderlyId(records.get(0).getElderlyId());
            groupRecord.setElderlyName(records.get(0).getElderlyName());
            groupRecord.setProfileImageUrl(records.get(0).getProfileImageUrl());

            List<Medication> medications = new ArrayList<>();
            for (MedicalRecord record : records) {
                medications.addAll(record.getMedications());
            }
            groupRecord.setMedications(medications);

            allMedicalRecords.add(groupRecord);
        }

        MedicalRecordItemAdapter itemAdapter = new MedicalRecordItemAdapter(allMedicalRecords);
        medicalRecordRecyclerView.setAdapter(itemAdapter);
    }



    private void showChooseElderlyUserDialog() {
        List<String> elderlyUserNames = new ArrayList<>();
        for (ElderlyUser elderlyUser : elderlyUsers) {
            elderlyUserNames.add(elderlyUser.getUsername());
        }

        String[] elderlyUserArray = elderlyUserNames.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogStyle);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogStyle);
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
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.black));
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.black));
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
            dosage = Math.max(0, dosage);
            dosageEditText.setText(String.valueOf(dosage));
        }
    }

    private void saveMedicalRecord(ElderlyUser elderlyUser, String medicineName, int dosage) {
        String dosageString = String.valueOf(dosage);
        CollectionReference medicalRecordsRef = firestore.collection("medical_records");

        Medication medication = new Medication(medicineName, dosageString);
        List<Medication> medications = new ArrayList<>();
        medications.add(medication);

        String elderlyId = elderlyUser.getUserId();
        String elderlyName = elderlyUser.getUsername();
        String profilePicUrl = elderlyUser.getProfileImageUrl();
        MedicalRecord medicalRecord = new MedicalRecord(elderlyId, elderlyName, profilePicUrl, medications);

        medicalRecordsRef.add(medicalRecord)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("MedicalRecordActivity", "Medical record added with ID: " + documentReference.getId());
                        fetchMedicalRecordsForElderlyUsers();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MedicalRecordActivity", "Error adding medical record", e);
                    }
                });
    }

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
        medicalRecordItemAdapter = new MedicalRecordItemAdapter(new ArrayList<>());
        medicalRecordRecyclerView.setAdapter(medicalRecordItemAdapter);
        medicalRecordRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMedicalRecordClick(MedicalRecord medicalRecord) {
        Intent intent = new Intent(this, MedicalRecordActivity.class);
        intent.putExtra("medicalRecord", medicalRecord);
        startActivity(intent);
    }
}
