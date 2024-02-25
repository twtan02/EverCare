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
import com.google.android.gms.tasks.Task;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicalRecordItemAdapter extends RecyclerView.Adapter<MedicalRecordItemAdapter.ViewHolder> {

    private List<MedicalRecord> medicalRecords;
    private List<String> medicineNames;

    public MedicalRecordItemAdapter(List<MedicalRecord> medicalRecords, List<String> medicineNames) {
        this.medicalRecords = medicalRecords;
        this.medicineNames = (medicineNames != null) ? medicineNames : new ArrayList<>();
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
            medicationDetails.append("Medication: ").append(medication.getMedicineName())
                    .append("\nDosage: ").append(medication.getDosage())
                    .append("\n\n");
        }

        holder.medicationDetailsTextView.setText(medicationDetails.toString().trim());

        Glide.with(holder.itemView.getContext())
                .load(medicalRecord.getProfileImageUrl())
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_failure_profile)
                .transform(new CircleCrop())
                .into(holder.profileImageView);

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog(holder.itemView.getContext(), medicalRecord, medicineNames, holder.getAdapterPosition());
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
        ImageButton deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            elderlyNameTextView = itemView.findViewById(R.id.elderly_name_textview);
            medicationDetailsTextView = itemView.findViewById(R.id.medications_textview);
            profileImageView = itemView.findViewById(R.id.profile_pic_imageview);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }

    private void deleteMedicalRecord(MedicalRecord medicalRecord, String selectedMedicationName, int position) {
        if (medicalRecord == null) {
            Log.e("MedicalRecordItemAdapter", "Medical record is null");
            return;
        }

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference medicalRecordsRef = firestore.collection("medical_records");

        medicalRecordsRef.whereEqualTo("elderlyId", medicalRecord.getElderlyId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                List<Map<String, Object>> medications = (List<Map<String, Object>>) document.get("medications");
                                if (medications != null) {
                                    for (Map<String, Object> medication : medications) {
                                        String medicineName = (String) medication.get("medicineName");
                                        if (medicineName != null && medicineName.equals(selectedMedicationName)) {
                                            // Found the medication to delete
                                            medications.remove(medication);
                                            // Update the medical record in Firestore
                                            document.getReference().update("medications", medications)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("MedicalRecordItemAdapter", "Medication deleted successfully");
                                                            // Check if position is valid before removing item
                                                            if (position >= 0 && position < medicalRecord.getMedications().size()) {
                                                                medicalRecord.getMedications().remove(position);
                                                                // If medications list is empty, remove the MedicalRecord object
                                                                if (medicalRecord.getMedications().isEmpty()) {
                                                                    medicalRecords.remove(medicalRecord);
                                                                    notifyDataSetChanged();
                                                                } else {
                                                                    notifyItemRemoved(position);
                                                                }
                                                                // Refresh medical records from Firestore to ensure synchronization
                                                                refreshMedicalRecords();
                                                            } else {
                                                                Log.e("MedicalRecordItemAdapter", "Invalid position: " + position);
                                                            }
                                                        }

                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("MedicalRecordItemAdapter", "Error deleting medication", e);
                                                        }
                                                    });
                                            return;
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.e("MedicalRecordItemAdapter", "Error getting medical records", task.getException());
                        }
                    }
                });
    }

    private void refreshMedicalRecords() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference medicalRecordsRef = firestore.collection("medical_records");

        medicalRecordsRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            medicalRecords.clear(); // Clear existing data
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                MedicalRecord medicalRecord = document.toObject(MedicalRecord.class);
                                medicalRecords.add(medicalRecord);
                            }
                            notifyDataSetChanged(); // Notify adapter of the updated data
                        } else {
                            Log.e("MedicalRecordItemAdapter", "Error refreshing medical records", task.getException());
                        }
                    }
                });
    }


    private void showDeleteDialog(Context context, MedicalRecord medicalRecord, List<String> medicationNames, int position) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_medication, null);
        Spinner medicationSpinner = dialogView.findViewById(R.id.medication_spinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, medicationNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        medicationSpinner.setAdapter(spinnerAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete Medication");
        builder.setView(dialogView);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Retrieve the selected medication (without elderly name appended)
                String selectedSpinnerItem = (String) medicationSpinner.getSelectedItem();
                String[] parts = selectedSpinnerItem.split(" - ");
                String selectedMedication = parts[1]; // The medication name is in the second part
                deleteMedicalRecord(medicalRecord, selectedMedication, position);
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



}