package my.edu.utar.evercare;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.ViewHolder> {
    private List<MedicalRecord> medicalRecords;

    public MedicalRecordAdapter(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medical_record, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalRecord medicalRecord = medicalRecords.get(position);

        // Set the data to the views in the ViewHolder
        holder.elderlyNameTextView.setText(medicalRecord.getElderlyName());
        holder.medicineNameTextView.setText(medicalRecord.getMedicineName());
        holder.dosageTextView.setText(medicalRecord.getDosage());
    }

    @Override
    public int getItemCount() {
        return medicalRecords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView elderlyNameTextView;
        TextView medicineNameTextView;
        TextView dosageTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            elderlyNameTextView = itemView.findViewById(R.id.elderly_name_textview);
            medicineNameTextView = itemView.findViewById(R.id.medicine_name_textview);
            dosageTextView = itemView.findViewById(R.id.dosage_textview);
        }
    }
}

