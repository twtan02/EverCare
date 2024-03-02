package my.edu.utar.evercare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BloodGlucoseAdapter extends RecyclerView.Adapter<BloodGlucoseAdapter.ViewHolder> {

    private List<BloodGlucoseData> bloodGlucoseList;

    public BloodGlucoseAdapter(List<BloodGlucoseData> bloodGlucoseList) {
        this.bloodGlucoseList = bloodGlucoseList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_blood_glucose, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BloodGlucoseData bloodGlucoseData = bloodGlucoseList.get(position);
        holder.bind(bloodGlucoseData);
    }

    @Override
    public int getItemCount() {
        return bloodGlucoseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bloodGlucoseLevelTextView;
        TextView dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bloodGlucoseLevelTextView = itemView.findViewById(R.id.bloodGlucoseLevelTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(BloodGlucoseData bloodGlucoseData) {
            bloodGlucoseLevelTextView.setText(bloodGlucoseData.getBloodGlucoseLevel());
            dateTextView.setText(bloodGlucoseData.getDate().toString());
        }
    }
}
