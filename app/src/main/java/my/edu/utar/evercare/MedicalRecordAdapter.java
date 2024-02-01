package my.edu.utar.evercare;

import android.text.TextUtils;
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

public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.MedicalRecordViewHolder> {

    private List<MedicalRecord> medicalRecords;
    private OnMedicalRecordClickListener clickListener;

    public MedicalRecordAdapter(List<MedicalRecord> medicalRecords, OnMedicalRecordClickListener clickListener) {
        this.medicalRecords = medicalRecords;
        this.clickListener = clickListener;
    }

    public void setMedicalRecords(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MedicalRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_record, parent, false);
        return new MedicalRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicalRecordViewHolder holder, int position) {
        MedicalRecord medicalRecord = medicalRecords.get(position);
        holder.bind(medicalRecord);
    }

    @Override
    public int getItemCount() {
        return medicalRecords.size();
    }

    public interface OnMedicalRecordClickListener {
        void onMedicalRecordClick(MedicalRecord medicalRecord);
    }

    class MedicalRecordViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView profileImageView;
        private TextView elderlyNameTextView;
        private TextView medicineNameTextView;
        private TextView dosageTextView;

        MedicalRecordViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_pic_imageview);
            elderlyNameTextView = itemView.findViewById(R.id.elderly_name_textview);
            medicineNameTextView = itemView.findViewById(R.id.medications_textview);
            dosageTextView = itemView.findViewById(R.id.quantity_textview);
            itemView.setOnClickListener(this);
        }

        void bind(MedicalRecord medicalRecord) {
            elderlyNameTextView.setText(medicalRecord.getElderlyName());
            StringBuilder medicineText = new StringBuilder();
            for (Medication medication : medicalRecord.getMedications()) {
                medicineText.append(medication.getMedicineName()).append("\n");
                medicineText.append(medication.getDosage()).append("\n\n");
            }
            medicineNameTextView.setText(medicineText.toString().trim());
            dosageTextView.setVisibility(View.GONE);

            // Load profile image using Glide
            String profileImageUrl = medicalRecord.getProfileImageUrl();
            if (!TextUtils.isEmpty(profileImageUrl)) {
                Glide.with(itemView.getContext())
                        .load(profileImageUrl)
                        .placeholder(R.drawable.default_profile_image)
                        .error(R.drawable.default_failure_profile)
                        .transform(new CircleCrop())
                        .into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.default_profile_image);
            }
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onMedicalRecordClick(medicalRecords.get(getAdapterPosition()));
            }
        }
    }
}
