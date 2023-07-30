package my.edu.utar.evercare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.MedicalRecordViewHolder> {

    private List<MedicalRecord> medicalRecords;

    MedicalRecordAdapter(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords;
    }

    @NonNull
    @Override
    public MedicalRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item_medical_record.xml layout for each item in the RecyclerView
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_record, parent, false);
        return new MedicalRecordViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicalRecordViewHolder holder, int position) {
        // Get the data for the current item position
        MedicalRecord medicalRecord = medicalRecords.get(position);

        // Assuming MedicalRecord class has a profilePicUrl field
        String profilePicUrl = medicalRecord.getProfilePicUrl();
        String elderlyName = medicalRecord.getElderlyName();
        String medicineName = ""; // Initialize with an empty string
        String dosage = ""; // Initialize with an empty string

        // Get the first medication details if available
        List<Medication> medications = medicalRecord.getMedications();
        if (medications != null && medications.size() > 0) {
            // Assuming the medications list is not empty
            Medication firstMedication = medications.get(0);
            medicineName = firstMedication.getMedicineName();
            dosage = firstMedication.getDosage();
        }

        // Display user's profile picture, name, medicine name, and dosage using Glide
        Glide.with(holder.itemView.getContext())
                .load(profilePicUrl)
                .placeholder(R.drawable.default_profile_image) // Add a placeholder image
                .error(R.drawable.default_failure_profile) // Add an error image in case of failure
                .into(holder.profilePicImageView);

        holder.elderlyNameTextView.setText(elderlyName);
        holder.medicineNameTextView.setText(medicineName);
        holder.dosageTextView.setText(dosage);
    }

    @Override
    public int getItemCount() {
        return medicalRecords.size();
    }

    static class MedicalRecordViewHolder extends RecyclerView.ViewHolder {

        ImageView profilePicImageView;
        TextView elderlyNameTextView;
        TextView medicineNameTextView;
        TextView dosageTextView;

        MedicalRecordViewHolder(View itemView) {
            super(itemView);
            // Find the views in the item_medical_record.xml layout
            profilePicImageView = itemView.findViewById(R.id.profile_pic_imageview);
            elderlyNameTextView = itemView.findViewById(R.id.elderly_name_textview);
            medicineNameTextView = itemView.findViewById(R.id.medicine_name_textview);
            dosageTextView = itemView.findViewById(R.id.dosage_textview);
        }
    }

}
