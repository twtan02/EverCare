package my.edu.utar.evercare;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MedicalRecordItemAdapter extends RecyclerView.Adapter<MedicalRecordItemAdapter.ViewHolder> {

    private List<MedicalRecord> medicalRecords;
    private List<String> medicineNames;

    // Modify the constructor to initialize medicineNames properly
    public MedicalRecordItemAdapter(List<MedicalRecord> medicalRecords, List<String> medicineNames) {
        this.medicalRecords = medicalRecords;
        this.medicineNames = (medicineNames != null) ? medicineNames : new ArrayList<>();
        System.out.println("MedicineNAme: " + medicineNames);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_record_grouped, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalRecord medicalRecord = medicalRecords.get(position);

        holder.elderlyNameTextView.setText("Elderly Name: " + medicalRecord.getElderlyName());

        StringBuilder medicationDetails = new StringBuilder();
        for (Medication medication : medicalRecord.getMedications()) {
            medicationDetails.append("Medical Record: ").append(medication.getMedicineName())
                    .append("\nQuantity: ").append(medication.getDosage())
                    .append("\n\n");
        }

        holder.medicationDetailsTextView.setText(medicationDetails.toString().trim());


        // Load profile image for the group
        Glide.with(holder.itemView.getContext())
                .load(medicalRecord.getProfileImageUrl())
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_failure_profile)
                .transform(new CircleCrop())
                .into(holder.profileImageView);

        // Set click listener for Modify button
        holder.modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifyDialog(holder.itemView.getContext(), medicineNames);
            }
        });


        // Set click listener for Delete button
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(holder.itemView.getContext());
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicalRecords.size();
    }

    public void setMedicineNames(List<String> medicineNames) {
        this.medicineNames.clear();
        this.medicineNames.addAll(medicineNames);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView elderlyNameTextView;
        TextView medicationDetailsTextView;
        ImageView profileImageView;
        ImageButton modifyButton;
        ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            elderlyNameTextView = itemView.findViewById(R.id.elderly_name_textview);
            medicationDetailsTextView = itemView.findViewById(R.id.medications_textview);
            profileImageView = itemView.findViewById(R.id.profile_pic_imageview);
            modifyButton = itemView.findViewById(R.id.modify_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    private void showModifyDialog(Context context, List<String> medicineNames) {
        // Inflate the custom layout for the Modify dialog
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_modify_medical_record, null);

        // Find views in the custom layout
        Spinner medicationSpinner = dialogView.findViewById(R.id.medication_spinner);
        EditText dosageEditText = dialogView.findViewById(R.id.dosage_edittext);

        // Populate the spinner with medicine names
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, medicineNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        medicationSpinner.setAdapter(spinnerAdapter);

        // Build the AlertDialog using the custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Modify");
        builder.setView(dialogView);

        // Add any additional customization or actions for Modify dialog

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Retrieve selected medication and dosage from the views
                String selectedMedication = medicationSpinner.getSelectedItem().toString();
                String dosage = dosageEditText.getText().toString();

                // Perform any actions with the selected values

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void showDeleteDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete");
        builder.setMessage("Dialog content for Delete");

        // Add any additional customization or actions for Delete dialog

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

}