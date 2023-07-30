package my.edu.utar.evercare;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MedicalRecordActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView elderlyListView;
    private List<ElderlyUser> elderlyUsers;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_record);

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


        elderlyListView.setOnItemClickListener(this);
        elderlyUsers = new ArrayList<>();

        firestore = FirebaseFirestore.getInstance();

        FloatingActionButton addButton = findViewById(R.id.fab_add_medical_record);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the dialog to add a medical record
                showSelectElderlyUserDialog();
            }
        });

        fetchElderlyUsersFromFirestore();
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

                        } else {
                            Log.e("MedicalRecordActivity", "Error getting elderly users: ", task.getException());
                        }
                    }
                });
    }


    private void showSelectElderlyUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Elderly User");

        // Set the list of elderly users to choose from
        List<String> elderlyUserNames = new ArrayList<>();
        for (ElderlyUser elderlyUser : elderlyUsers) {
            elderlyUserNames.add(elderlyUser.getUsername());
        }

        builder.setItems(elderlyUserNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked on an elderly user
                ElderlyUser selectedElderlyUser = elderlyUsers.get(which);
                // Show the add medical record dialog with the selected elderly user
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
            // Handle the case when the profileImageUrl is null or empty
            profileImageView.setImageResource(R.drawable.default_profile_image);
        }
        elderlyNameTextView.setText(selectedElderlyUser.getUsername());

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

        // Rest of the code for the dialog remains unchanged
        // ...

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Get the values entered by the user
                String medicineName = medicineNameEditText.getText().toString();
                String dosage = dosageEditText.getText().toString();

                // Validate input (you can add your validation logic here)

                // Create a Medication object with the entered data
                Medication medication = new Medication(medicineName, dosage);

                // Save the medical record with the selected elderly user and medication
                saveMedicalRecord(selectedElderlyUser, medication);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        builder.create().show();
    }

    private void incrementDosage(EditText dosageEditText) {
        String currentDosage = dosageEditText.getText().toString();
        int dosage = 0;
        if (!TextUtils.isEmpty(currentDosage)) {
            dosage = Integer.parseInt(currentDosage);
        }
        dosage++;
        dosageEditText.setText(String.valueOf(dosage));
    }

    private void decrementDosage(EditText dosageEditText) {
        String currentDosage = dosageEditText.getText().toString();
        int dosage = 0;
        if (!TextUtils.isEmpty(currentDosage)) {
            dosage = Integer.parseInt(currentDosage);
            if (dosage > 0) {
                dosage--;
                dosageEditText.setText(String.valueOf(dosage));
            }
        }
    }

    private void saveMedicalRecord(ElderlyUser elderlyUser, Medication medication) {
        // Get a reference to the Firebase Realtime Database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        // Generate a unique ID for the medical record
        String recordId = databaseRef.push().getKey();

        // Get the elderly's ID, name, and profile pic URL
        String elderlyId = elderlyUser.getUserId();
        String elderlyName = elderlyUser.getUsername();
        String profilePicUrl = elderlyUser.getProfileImageUrl();



    }

    public class InputFilterOnlyNumeric implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            StringBuilder builder = new StringBuilder();
            for (int i = start; i < end; i++) {
                char currentChar = source.charAt(i);
                if (Character.isDigit(currentChar)) {
                    builder.append(currentChar);
                }
            }
            return builder.toString();
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Retrieve the selected elderly user's ID
        String selectedElderlyUserId = elderlyUsers.get(position).getUserId();
        // Retrieve the medical records of the selected elderly user from Firestore

        // Show the add medical record dialog with the selected elderly user
        showAddMedicalRecordDialog(elderlyUsers.get(position));
    }
}
