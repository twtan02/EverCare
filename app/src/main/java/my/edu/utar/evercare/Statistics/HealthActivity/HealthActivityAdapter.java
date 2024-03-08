package my.edu.utar.evercare.Statistics.HealthActivity;

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

public class HealthActivityAdapter extends RecyclerView.Adapter<HealthActivityAdapter.ViewHolder> {

    private List<HealthActivityData> healthActivityList;

    public HealthActivityAdapter(List<HealthActivityData> healthActivityList) {
        this.healthActivityList = healthActivityList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_health_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HealthActivityData healthActivityData = healthActivityList.get(position);
        holder.bind(healthActivityData);
    }

    @Override
    public int getItemCount() {
        return healthActivityList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView healthActivityTextView;
        TextView dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            healthActivityTextView = itemView.findViewById(R.id.healthActivityTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(HealthActivityData healthActivityData) {
            healthActivityTextView.setText(healthActivityData.getHealthActivity());

            // Format the date and time
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd.MM.yy HH:mm", Locale.getDefault());
            String formattedDateTime = sdf.format(healthActivityData.getDate());

            dateTextView.setText(formattedDateTime);
        }
    }
}
