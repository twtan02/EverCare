package my.edu.utar.evercare.Statistics.BloodLipids;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import my.edu.utar.evercare.R;

public class BloodLipidsAdapter extends RecyclerView.Adapter<BloodLipidsAdapter.ViewHolder> {

    private List<BloodLipidsData> bloodLipidsList;

    public BloodLipidsAdapter(List<BloodLipidsData> bloodLipidsList) {
        this.bloodLipidsList = bloodLipidsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_lipids, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BloodLipidsData bloodLipidsData = bloodLipidsList.get(position);
        holder.bind(bloodLipidsData);
    }

    @Override
    public int getItemCount() {
        return bloodLipidsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bloodLipidsLevelTextView;
        TextView dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bloodLipidsLevelTextView = itemView.findViewById(R.id.bloodLipidsLevelTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(BloodLipidsData bloodLipidsData) {
            bloodLipidsLevelTextView.setText(bloodLipidsData.getBloodLipidsLevel());

            // Format the date and time
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd.MM.yy HH:mm", Locale.getDefault());
            String formattedDateTime = sdf.format(bloodLipidsData.getDate());

            dateTextView.setText(formattedDateTime);
        }
    }
}
