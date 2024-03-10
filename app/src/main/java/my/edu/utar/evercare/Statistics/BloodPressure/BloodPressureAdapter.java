package my.edu.utar.evercare.Statistics.BloodPressure;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import my.edu.utar.evercare.R;

public class BloodPressureAdapter extends RecyclerView.Adapter<my.edu.utar.evercare.Statistics.BloodPressure.BloodPressureAdapter.ViewHolder> {

    private List<BloodPressureData> bloodPressureList;

    public BloodPressureAdapter(List<BloodPressureData> bloodPressureList) {
        this.bloodPressureList = bloodPressureList;
    }

    @NonNull
    @Override
    public my.edu.utar.evercare.Statistics.BloodPressure.BloodPressureAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_pressure, parent, false);
        return new my.edu.utar.evercare.Statistics.BloodPressure.BloodPressureAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull my.edu.utar.evercare.Statistics.BloodPressure.BloodPressureAdapter.ViewHolder holder, int position) {
        BloodPressureData bloodPressureData = bloodPressureList.get(position);
        holder.bind(bloodPressureData);
    }

    @Override
    public int getItemCount() {
        return bloodPressureList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bloodPressureLevelTextView;
        TextView dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bloodPressureLevelTextView = itemView.findViewById(R.id.bloodPressureLevelTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(BloodPressureData bloodPressureData) {
            String bloodPressureLevel = bloodPressureData.getBloodPressureLevel();

            // Extract only numeric characters from the blood pressure level string
            String numericBloodPressureLevel = bloodPressureLevel.replaceAll("[^0-9]", "");

            // Check if blood pressure level exceeds the range
            int pressureLevel = Integer.parseInt(numericBloodPressureLevel);
            if (pressureLevel < 90 || pressureLevel > 120) {
                bloodPressureLevelTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
            } else {
                bloodPressureLevelTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
            }

            bloodPressureLevelTextView.setText(bloodPressureLevel);

            // Format the date and time
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd.MM.yy HH:mm", Locale.getDefault());
            String formattedDateTime = sdf.format(bloodPressureData.getDate());

            dateTextView.setText(formattedDateTime);
        }

    }


}
