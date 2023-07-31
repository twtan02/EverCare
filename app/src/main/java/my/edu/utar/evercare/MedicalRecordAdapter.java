package my.edu.utar.evercare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MedicalRecordAdapter extends RecyclerView.Adapter<MedicalRecordAdapter.ViewHolder> {

    private Context context;
    private List<MedicalRecord> medicalRecords;
    private List<ElderlyUser> elderlyUsers;
    private OnMedicalRecordClickListener listener;

    public MedicalRecordAdapter(Context context, List<MedicalRecord> medicalRecords, OnMedicalRecordClickListener listener) {
        this.context = context;
        this.medicalRecords = medicalRecords;
        this.listener = listener;
    }

    public void setElderlyUsers(List<ElderlyUser> elderlyUsers) {
        this.elderlyUsers = elderlyUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_medical_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MedicalRecord medicalRecord = medicalRecords.get(position);
        if (medicalRecord != null && elderlyUsers != null && elderlyUsers.size() > position) {
            ElderlyUser elderlyUser = elderlyUsers.get(position);
            holder.bind(medicalRecord, elderlyUser);
        }
    }

    @Override
    public int getItemCount() {
        return medicalRecords.size();
    }

    public void setMedicalRecords(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView elderlyNameTextView;
        private TextView medicineNameTextView;
        private TextView dosageTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            elderlyNameTextView = itemView.findViewById(R.id.elderly_name_textview);
            medicineNameTextView = itemView.findViewById(R.id.medicine_name_textview);
            dosageTextView = itemView.findViewById(R.id.dosage_textview);
            itemView.setOnClickListener(this);
        }

        public void bind(MedicalRecord medicalRecord, ElderlyUser elderlyUser) {
            elderlyNameTextView.setText(elderlyUser.getUsername());
            medicineNameTextView.setText(medicalRecord.getMedications().get(0).getMedicineName());
            dosageTextView.setText(medicalRecord.getMedications().get(0).getDosage());
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    MedicalRecord clickedRecord = medicalRecords.get(position);
                    listener.onMedicalRecordClick(clickedRecord);
                }
            }
        }
    }

    public interface OnMedicalRecordClickListener {
        void onMedicalRecordClick(MedicalRecord medicalRecord);
    }
}
