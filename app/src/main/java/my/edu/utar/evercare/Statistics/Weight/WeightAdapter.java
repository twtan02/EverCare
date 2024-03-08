package my.edu.utar.evercare.Statistics.Weight;

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

public class WeightAdapter extends RecyclerView.Adapter<WeightAdapter.ViewHolder> {

    private List<WeightData> weightList;

    public WeightAdapter(List<WeightData> weightList) {
        this.weightList = weightList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weight, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeightData weightData = weightList.get(position);
        holder.bind(weightData);
    }

    @Override
    public int getItemCount() {
        return weightList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView weightTextView;
        TextView dateTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            weightTextView = itemView.findViewById(R.id.weightTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(WeightData weightData) {
            weightTextView.setText(weightData.getWeight());

            // Format the date
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd.MM.yy", Locale.getDefault());
            String formattedDate = sdf.format(weightData.getDate());

            dateTextView.setText(formattedDate);
        }
    }
}
