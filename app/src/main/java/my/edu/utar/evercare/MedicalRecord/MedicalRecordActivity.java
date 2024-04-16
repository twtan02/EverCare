package my.edu.utar.evercare.MedicalRecord;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import my.edu.utar.evercare.User.ElderlyUser;
import my.edu.utar.evercare.R;
import timber.log.Timber;

public class MedicalRecordActivity extends AppCompatActivity implements MedicalRecordAdapter.OnMedicalRecordClickListener {

    private List<ElderlyUser> elderlyUsers = new ArrayList<>();
    private Map<String, List<MedicalRecord>> medicalRecordsMap = new HashMap<>();
    private MedicalRecordAdapter medicalRecordAdapter;
    private FirebaseFirestore firestore;
    private RecyclerView medicalRecordRecyclerView;
    private MedicalRecordItemAdapter medicalRecordItemAdapter;
    private AlertDialog dialog;
    private List<String> medicineNames;
    private Map<String, ElderlyUser> elderlyUserMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record);

        // Initialize Firestore instance and RecyclerView
        firestore = FirebaseFirestore.getInstance();
        medicalRecordRecyclerView = findViewById(R.id.medical_record_recyclerview);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);
        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Medical Record");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Fetch elderly users and medicine names from Firestore
        fetchElderlyUsersFromFirestore();
        fetchMedicineNamesFromFirestore();

        // Fetch the user's role and filter medical records
        fetchUserAndFilterMedicalRecords();

        // Set up RecyclerView
        setupRecyclerView(new ArrayList<>());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        medicalRecordRecyclerView.setLayoutManager(layoutManager);

        // Set up FloatingActionButton
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

    public void fetchMedicalRecordsForElderlyUsers() {
        medicalRecordsMap.clear();
        // Retrieve the current user's ID from Firebase Authentication
        String userId = getCurrentUserIdFromFirebase();
        // Query Firestore to get the corresponding user document
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userRef = firestore.collection("all_users").document(userId);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String userRole = documentSnapshot.getString("role");
                    if (userRole != null && userRole.equals("Caregiver")) {
                        String elderlyParentName = documentSnapshot.getString("elderlyParentName");
                        fetchMedicalRecordsForCaregiver(userId, elderlyParentName);
                    } else {
//                        fetchMedicalRecordsForStaff(userId);
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

                } else {
                    Log.e("MedicalRecordActivity", "User document does not exist");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("MedicalRecordActivity", "Error retrieving user document", e);
            }
        });
    }

    private void fetchMedicalRecordsForCaregiver(String userId, String elderlyParentName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("medical_records")
                .whereEqualTo("elderlyName", elderlyParentName)
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
                            medicalRecordsMap.put(userId, medicalRecords);
                            updateRecyclerView();
                        } else {
                            Log.e("MedicalRecordActivity", "Error getting medical records: ", task.getException());
                        }
                    }
                });
    }



    private void filterMedicalRecordsByUserRole(String userRole, List<MedicalRecord> filteredMedicalRecords) {
        // Retrieve the current user's ID from Firebase Authentication
        String userId = getCurrentUserIdFromFirebase();
        // Query Firestore to get the corresponding user document
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userRef = firestore.collection("all_users").document(userId);
        userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String elderlyParentName = documentSnapshot.getString("elderlyParentName");
                    // Filter medical records based on user's role and assigned elderly parent name
                    for (List<MedicalRecord> records : medicalRecordsMap.values()) {
                        for (MedicalRecord medicalRecord : records) {
                            Log.d("MedicalRecordActivity", "medicalRecord: " + medicalRecord);
                            if (userRole.equals("Caregiver") && medicalRecord.getElderlyName().equals(elderlyParentName)) {
                                filteredMedicalRecords.add(medicalRecord);
                            } else if (!userRole.equals("Caregiver")) {
                                filteredMedicalRecords.add(medicalRecord);
                            }
                        }
                    }

                    // Update RecyclerView to display the filtered medical records
                    medicalRecordAdapter.setMedicalRecords(filteredMedicalRecords);

                    // Log out filteredMedicalRecords
                    Log.d("MedicalRecordActivity", "Filtered Medical Records: " + filteredMedicalRecords);
                } else {
                    Log.e("MedicalRecordActivity", "User document does not exist");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("MedicalRecordActivity", "Error retrieving user document", e);
            }
        });
    }



    private void updateRecyclerView() {
        List<MedicalRecord> allMedicalRecords = new ArrayList<>();

        for (String elderlyUserId : medicalRecordsMap.keySet()) {
            List<MedicalRecord> records = medicalRecordsMap.get(elderlyUserId);

            if (!records.isEmpty()) {
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
        }

        MedicalRecordItemAdapter itemAdapter = new MedicalRecordItemAdapter(allMedicalRecords, medicineNames, this);
        medicalRecordRecyclerView.setAdapter(itemAdapter);
    }

    public void fetchMedicineNamesFromFirestore() {
        FirebaseFirestore.getInstance().collection("medical_records")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> updatedMedicineNames = new ArrayList<>(); // Create a new list for updated medicine names
                            for (DocumentSnapshot document : task.getResult()) {
                                String elderlyName = document.getString("elderlyName");
                                List<Map<String, Object>> medications = (List<Map<String, Object>>) document.get("medications");
                                if (medications != null) {
                                    for (Map<String, Object> medication : medications) {
                                        String medicineName = (String) medication.get("medicineName");
                                        if (medicineName != null) {
                                            // Append the elderly name before or after the medicine name
                                            // Here, we append it before the medicine name
                                            String modifiedMedicineName = elderlyName + " - " + medicineName;
                                            updatedMedicineNames.add(modifiedMedicineName);
                                        }
                                    }
                                }
                            }
                            // Ensure that medicineNames is initialized
                            if (medicineNames == null) {
                                medicineNames = new ArrayList<>();
                            }
                            // Update the medicineNames list with the new data
                            medicineNames.clear(); // Clear the old data
                            medicineNames.addAll(updatedMedicineNames); // Add the updated medicine names

                            // Log out the medicine names here
                            Log.d("MedicalRecordItemAdapter", "Medicine Names Retrieved: " + medicineNames.toString());

                            // Notify the adapter that medicine names are fetched
                            if (medicalRecordItemAdapter != null) {
                                medicalRecordItemAdapter.setMedicineNames(medicineNames);
                            }
                        } else {
                            Log.e("MedicalRecordActivity", "Error getting medicine names: ", task.getException());
                        }
                    }
                });
    }

    private String getCurrentUserIdFromFirebase() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            // Handle the case where the current user is null
            return null;
        }
    }

    private void fetchUserAndFilterMedicalRecords() {
        // Retrieve the current user's ID from Firebase Authentication
        String userId = getCurrentUserIdFromFirebase();

        // Query Firestore to get the corresponding user document
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userRef = firestore.collection("all_users").document(userId);
        userRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String userRole = documentSnapshot.getString("role");

                            // Initialize filteredMedicalRecords here
                            List<MedicalRecord> filteredMedicalRecords = new ArrayList<>();

                            // Filter medical records based on the user's role
                            filterMedicalRecordsByUserRole(userRole, filteredMedicalRecords);
                        } else {
                            Log.e("MedicalRecordActivity", "User document does not exist");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MedicalRecordActivity", "Error retrieving user document", e);
                    }
                });
    }



    private void showChooseElderlyUserDialog() {
        List<String> elderlyUserNames = new ArrayList<>();
        String currentUserId = getCurrentUserIdFromFirebase();
        elderlyUserMap.clear(); // Clear the map before populating it again

        if (currentUserId != null) {
            FirebaseFirestore.getInstance().collection("all_users")
                    .document(currentUserId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                String currentUserRole = documentSnapshot.getString("role");
                                String elderlyParentName = documentSnapshot.getString("elderlyParentName");

                                for (ElderlyUser elderlyUser : elderlyUsers) {
                                    if (currentUserRole.equals("Caregiver")) {
                                        if (elderlyUser.getUsername().equals(elderlyParentName)) {
                                            elderlyUserNames.add(elderlyUser.getUsername());
                                            elderlyUserMap.put(elderlyUser.getUsername(), elderlyUser); // Add to the map
                                        }
                                    } else if (currentUserRole.equals("Staff")){
                                        elderlyUserNames.add(elderlyUser.getUsername());
                                        elderlyUserMap.put(elderlyUser.getUsername(), elderlyUser); // Add to the map
                                    }
                                }

                                String[] elderlyUserArray = elderlyUserNames.toArray(new String[0]);

                                AlertDialog.Builder builder = new AlertDialog.Builder(MedicalRecordActivity.this, R.style.CustomAlertDialogStyle);
                                builder.setTitle("Choose Elderly User");
                                builder.setItems(elderlyUserArray, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // Retrieve the corresponding ElderlyUser object from the map using the selected username
                                        String selectedUsername = elderlyUserArray[which];
                                        ElderlyUser selectedElderlyUser = elderlyUserMap.get(selectedUsername);
                                        showAddMedicalRecordDialog(selectedElderlyUser);
                                    }
                                });
                                builder.show();
                            } else {
                                Log.e(TAG, "User document does not exist");
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Error retrieving user document", e);
                        }
                    });
        } else {
            Log.e(TAG, "Current user ID is null");
        }
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
                    .transform(new CircleCrop())
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
                // Get medicine name and dosage from EditText fields
                String medicineName = medicineNameEditText.getText().toString().trim();
                String dosageString = dosageEditText.getText().toString().trim();

                // Validate medicine name
                if (TextUtils.isEmpty(medicineName)) {
                    // Notify the user about the missing medicine name
                    Toast.makeText(MedicalRecordActivity.this, "Please enter the medicine name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate dosage
                if (TextUtils.isEmpty(dosageString)) {
                    // Notify the user about the missing dosage
                    Toast.makeText(MedicalRecordActivity.this, "Please enter the dosage", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Convert dosage string to integer
                int dosage;
                try {
                    dosage = Integer.parseInt(dosageString);
                    if (dosage <= 0) {
                        // Notify the user that dosage should be greater than 0
                        Toast.makeText(MedicalRecordActivity.this, "Dosage should be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Notify the user about the invalid dosage format
                    Toast.makeText(MedicalRecordActivity.this, "Invalid dosage format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If both fields are valid, proceed with saving the medical record to Firestore
                saveMedicalRecord(selectedElderlyUser, medicineName, dosage);
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

        // Check if there is an existing medical record for the elderly user
        medicalRecordsRef.whereEqualTo("elderlyId", elderlyUser.getUserId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Check if there are any existing medical records for the elderly user
                            if (!task.getResult().isEmpty()) {
                                // If there is an existing medical record, update it with the new medication
                                for (DocumentSnapshot document : task.getResult()) {
                                    MedicalRecord existingMedicalRecord = document.toObject(MedicalRecord.class);
                                    if (existingMedicalRecord != null) {
                                        // Generate a random ID for the medication
                                        String medicationId = medicalRecordsRef.document().getId();

                                        // Create the new medication object
                                        Medication medication = new Medication(medicationId, medicineName, dosageString);

                                        // Add the new medication to the existing medications list
                                        existingMedicalRecord.getMedications().add(medication);

                                        // Update the medical record in Firestore
                                        medicalRecordsRef.document(document.getId())
                                                .set(existingMedicalRecord)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d("MedicalRecordActivity", "Medication added to existing medical record");
                                                        // Refresh the UI
                                                        fetchMedicalRecordsForElderlyUsers();
                                                        // Fetch updated medicine names after adding a medical record
                                                        fetchMedicineNamesFromFirestore();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("MedicalRecordActivity", "Error updating medical record", e);
                                                    }
                                                });
                                    }
                                }
                            } else {
                                // If there is no existing medical record, create a new one with the new medication
                                String elderlyId = elderlyUser.getUserId();
                                String elderlyName = elderlyUser.getUsername();
                                String profilePicUrl = elderlyUser.getProfileImageUrl();

                                // Generate a random ID for the new medical record
                                String medicalRecordId = medicalRecordsRef.document().getId();

                                // Create the new medication object
                                Medication medication = new Medication(medicalRecordId, medicineName, dosageString);
                                List<Medication> medications = new ArrayList<>();
                                medications.add(medication);

                                // Create the new medical record object
                                MedicalRecord medicalRecord = new MedicalRecord(elderlyId, elderlyName, profilePicUrl, medications);

                                // Store the new medical record in Firestore
                                medicalRecordsRef.document(medicalRecordId)
                                        .set(medicalRecord)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("MedicalRecordActivity", "New medical record with medication added");
                                                fetchMedicalRecordsForElderlyUsers(); // Refresh the UI
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("MedicalRecordActivity", "Error adding new medical record", e);
                                            }
                                        });
                            }
                        } else {
                            Log.e("MedicalRecordActivity", "Error getting medical records: ", task.getException());
                        }
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

    private void setupRecyclerView(List<String> medicineNames) {
        medicalRecordAdapter = new MedicalRecordAdapter(new ArrayList<>(), this); // Initialize the adapter
        medicalRecordRecyclerView.setAdapter(medicalRecordAdapter); // Set the adapter to the RecyclerView
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