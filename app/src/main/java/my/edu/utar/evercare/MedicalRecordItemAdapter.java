package my.edu.utar.evercare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
    }

    @Override
    public int getItemCount() {
        return medicalRecords.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView elderlyNameTextView;
        TextView medicationDetailsTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            elderlyNameTextView = itemView.findViewById(R.id.elderly_name_textview);
            medicationDetailsTextView = itemView.findViewById(R.id.medications_textview);
        }
    }
}
