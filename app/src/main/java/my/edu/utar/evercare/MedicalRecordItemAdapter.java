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
import java.util.List;

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

        holder.modifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModifyDialog(holder.itemView.getContext(), medicineNames);
            }
        });

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
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_modify_medical_record, null);
        Spinner medicationSpinner = dialogView.findViewById(R.id.medication_spinner);
        EditText dosageEditText = dialogView.findViewById(R.id.dosage_edittext);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, medicineNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        medicationSpinner.setAdapter(spinnerAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Modify");
        builder.setView(dialogView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedMedication = medicationSpinner.getSelectedItem().toString();
                String dosage = dosageEditText.getText().toString();
                // Call method to save updated medication details
                saveUpdatedMedicationDetails(selectedMedication, dosage);
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

    private void saveUpdatedMedicationDetails(String selectedMedication, String dosage) {

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
                String selectedMedication = medicationSpinner.getSelectedItem().toString();
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

    private void deleteMedicalRecord(MedicalRecord medicalRecord, String selectedMedication, int position) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference medicalRecordsRef = firestore.collection("medical_records");

        List<String> medicalRecordIds = new ArrayList<>();

        medicalRecordsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Access the document ID
                        String medicalRecordId = document.getId();
                        // Add the medicalRecordId to the list
                        medicalRecordIds.add(medicalRecordId);
                    }

                    // Now that we have the list of medical record IDs, find the ID for the specific medical record
                    String medicalRecordId = medicalRecordIds.get(position);
                    // Use this medical record ID to delete the medication
                    deleteMedicationFromMedicalRecord(medicalRecord, selectedMedication, position);
                } else {
                    Log.e("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void deleteMedicationFromMedicalRecord(MedicalRecord medicalRecord, String selectedMedicationId, int position) {

        if (medicalRecord == null || selectedMedicationId == null) {
            Log.e("MedicalRecordItemAdapter", "Medical record or selected medication is null");
            return;
        }
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference medicalRecordsRef = firestore.collection("medical_records");

        List<String> medicalRecordIds = new ArrayList<>();

        medicalRecordsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Access the document ID
                        String medicalRecordId = document.getId();
                        // Add the medicalRecordId to the list
                        medicalRecordIds.add(medicalRecordId);
                        Log.d("medicalRecordIds", "medicalRecordIds: " + medicalRecordIds.toString());
                    }

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String medicalRecordId = document.getId();
                        CollectionReference medicationsRef = firestore.collection("medical_records").document(medicalRecordId).collection("medications");
                        Log.d("Firestore", "medicationsRef: " + medicationsRef.getPath());

                        medicationsRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot medicationDoc : task.getResult()) {
                                        String medicationId = medicationDoc.getId();
                                        Log.d("medicationId", "medicationId: " + medicationId);
                                        if (selectedMedicationId.equals(medicationId)) {
                                            // Medication document with selectedMedicationId found
                                            // Delete the medication document and update the UI
                                            medicationDoc.getReference().delete()
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Log.d("MedicalRecordItemAdapter", "Medication record deleted successfully");
                                                            medicalRecord.getMedications().remove(position);
                                                            notifyItemRemoved(position);
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.e("MedicalRecordItemAdapter", "Error deleting medication record", e);
                                                        }
                                                    });
                                            return; // Exit the loop after deleting the medication
                                        }
                                    }
                                } else {
                                    Log.e("Firestore", "Error getting medication documents: ", task.getException());
                                }
                            }
                        });
                    }


                } else {
                    Log.e("Firestore", "Error getting documents: ", task.getException());
                }
            }
        });
    }



}
