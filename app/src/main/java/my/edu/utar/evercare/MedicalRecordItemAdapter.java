package my.edu.utar.evercare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.util.List;

public class MedicalRecordItemAdapter extends RecyclerView.Adapter<MedicalRecordItemAdapter.ViewHolder> {

    private List<MedicalRecord> medicalRecords;

    public MedicalRecordItemAdapter(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords;
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
    }

    @Override
    public int getItemCount() {
        return medicalRecords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView elderlyNameTextView;
        TextView medicationDetailsTextView;
        ImageView profileImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            elderlyNameTextView = itemView.findViewById(R.id.elderly_name_textview);
            medicationDetailsTextView = itemView.findViewById(R.id.medications_textview);
            profileImageView = itemView.findViewById(R.id.profile_pic_imageview); // Make sure to add an ImageView in your XML layout with the ID "profile_pic_imageview"
        }
    }
}
