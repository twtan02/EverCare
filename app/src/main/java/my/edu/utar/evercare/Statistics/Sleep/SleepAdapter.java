package my.edu.utar.evercare.Statistics.Sleep;

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

public class SleepAdapter extends RecyclerView.Adapter<SleepAdapter.ViewHolder> {

    private List<SleepData> sleepList;

    public SleepAdapter(List<SleepData> sleepList) {
        this.sleepList = sleepList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sleep, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SleepData sleepData = sleepList.get(position);
        holder.bind(sleepData);
    }

    @Override
    public int getItemCount() {
        return sleepList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sleepDurationTextView;
        TextView dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            sleepDurationTextView = itemView.findViewById(R.id.sleepDurationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(SleepData sleepData) {
            String sleepDuration = sleepData.getSleepDuration();

            // Extract only numeric characters from the sleep duration string
            String numericSleepDuration = sleepDuration.replaceAll("[^0-9]", "");

            // Check if sleep duration exceeds the range
            int duration = Integer.parseInt(numericSleepDuration);
            if (duration < 5 || duration > 8) {
                sleepDurationTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_red_dark));
            } else {
                sleepDurationTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.black));
            }

            sleepDurationTextView.setText(sleepDuration);

            // Format the date and time
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd.MM.yy HH:mm", Locale.getDefault());
            String formattedDateTime = sdf.format(sleepData.getDate());

            dateTextView.setText(formattedDateTime);
        }

    }
}
