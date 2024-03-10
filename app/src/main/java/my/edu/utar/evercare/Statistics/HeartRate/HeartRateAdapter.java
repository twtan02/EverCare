package my.edu.utar.evercare.Statistics.HeartRate;

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

public class HeartRateAdapter extends RecyclerView.Adapter<HeartRateAdapter.ViewHolder> {

    private List<HeartRateData> heartRateList;

    public HeartRateAdapter(List<HeartRateData> heartRateList) {
        this.heartRateList = heartRateList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_heart_rate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HeartRateData heartRateData = heartRateList.get(position);
        holder.bind(heartRateData);
    }

    @Override
    public int getItemCount() {
        return heartRateList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView heartRateLevelTextView;
        TextView dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            heartRateLevelTextView = itemView.findViewById(R.id.heartRateLevelTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(HeartRateData heartRateData) {
            String heartRate = heartRateData.getHeartRate();

            // Extract only numeric characters from the heart rate string
            String numericHeartRate = heartRate.replaceAll("[^0-9]", "");

            // Check if heart rate exceeds the range
            int rate = Integer.parseInt(numericHeartRate);
            if (rate < 60 || rate > 100) {
                heartRateLevelTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
            } else {
                heartRateLevelTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
            }

            heartRateLevelTextView.setText(heartRate);

            // Format the date and time
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd.MM.yy HH:mm", Locale.getDefault());
            String formattedDateTime = sdf.format(heartRateData.getDate());

            dateTextView.setText(formattedDateTime);
        }

    }
}
