package my.edu.utar.evercare.MedicalRecord;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import my.edu.utar.evercare.R;

public class MedicalRecordItemAdapter extends RecyclerView.Adapter<MedicalRecordItemAdapter.ViewHolder> {

    private List<MedicalRecord> medicalRecords;
    private List<String> medicineNames;
    private MedicalRecordActivity medicalRecordActivity;

    public MedicalRecordItemAdapter(List<MedicalRecord> medicalRecords, List<String> medicineNames, MedicalRecordActivity medicalRecordActivity) {
        this.medicalRecords = medicalRecords;
        this.medicineNames = (medicineNames != null) ? medicineNames : new ArrayList<>();
        this.medicalRecordActivity = medicalRecordActivity;
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

        // Log out the selected medication name
        Log.d("MedicalRecordItemAdapter", "Selected Medication Name: " + selectedMedicationName);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference medicalRecordsRef = firestore.collection("medical_records");

        medicalRecordsRef.whereEqualTo("elderlyId", medicalRecord.getElderlyId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean medicationFound = false; // Flag to track if medication was found
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                List<Map<String, Object>> medications = (List<Map<String, Object>>) document.get("medications");
                                if (medications != null) {
                                    for (Map<String, Object> medication : medications) {
                                        String medicineName = (String) medication.get("medicineName");
                                        Log.d("medicineName", "medicineName: " + medicineName);
                                        if (medicineName != null && medicineName.equals(selectedMedicationName)) {
                                            // Found the medication to delete
                                            medicationFound = true; // Set flag to true
                                            medications.remove(medication);
                                            // Update the medical record in Firestore
                                            document.getReference().update("medications", medications)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("MedicalRecordItemAdapter", "Medication deleted successfully");
                                                            // Find the index of the medication in medicalRecord.getMedications()
                                                            int indexToRemove = -1;
                                                            for (int i = 0; i < medicalRecord.getMedications().size(); i++) {
                                                                Medication med = medicalRecord.getMedications().get(i);
                                                                if (med.getMedicineName().equals(selectedMedicationName)) {
                                                                    indexToRemove = i;
                                                                    break;
                                                                }
                                                            }
                                                            if (indexToRemove != -1) {
                                                                // Remove the medication from medicalRecord.getMedications() list
                                                                medicalRecord.getMedications().remove(indexToRemove);
                                                                // If medications list is empty, remove the MedicalRecord object
                                                                if (medicalRecord.getMedications().isEmpty()) {
                                                                    medicalRecords.remove(medicalRecord);
                                                                    notifyDataSetChanged();
                                                                } else {
                                                                    notifyItemRemoved(indexToRemove);
                                                                }
                                                                // Refresh medical records from Firestore to ensure synchronization
                                                                refreshMedicalRecords();
                                                                // Fetch medical records for elderly users again
                                                                medicalRecordActivity.fetchMedicalRecordsForElderlyUsers();
                                                                // Fetch updated medicine names after deleting a medical record
                                                                medicalRecordActivity.fetchMedicineNamesFromFirestore();
                                                            } else {
                                                                Log.e("MedicalRecordItemAdapter", "Medication not found in the list");
                                                            }
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("MedicalRecordItemAdapter", "Error deleting medication", e);
                                                        }
                                                    });
                                            break; // Exit loop once medication is found and deleted
                                        }
                                    }
                                }
                            }
                            // Handle if medication was not found for deletion
                            if (!medicationFound) {
                                Log.e("MedicalRecordItemAdapter", "Medication not found for deletion");
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
        String elderlyName = medicalRecord.getElderlyName();

        // Filter medication names based on the selected elderly user's name
        List<String> filteredMedicationNames = new ArrayList<>();
        for (String medicationName : medicationNames) {
            if (medicationName.startsWith(elderlyName)) {
                filteredMedicationNames.add(medicationName);
            }
        }

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_medication, null);
        Spinner medicationSpinner = dialogView.findViewById(R.id.medication_spinner);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, filteredMedicationNames);
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