package my.edu.utar.evercare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

        private TextView elderlyNameTextView;
        private TextView medicineNameTextView;
        private TextView dosageTextView;

        MedicalRecordViewHolder(@NonNull View itemView) {
            super(itemView);
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
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onMedicalRecordClick(medicalRecords.get(getAdapterPosition()));
            }
        }
    }
}
