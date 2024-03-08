package my.edu.utar.evercare.Statistics;

import static java.security.AccessController.getContext;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

import my.edu.utar.evercare.R;

public class StatisticsPagerAdapter extends RecyclerView.Adapter<StatisticsPagerAdapter.ViewHolder> {

    private ArrayList<ViewPagerItem> viewPagerItemArrayList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String healthRecordType);
    }

    public StatisticsPagerAdapter(ArrayList<ViewPagerItem> viewPagerItemArrayList, OnItemClickListener listener) {
        this.viewPagerItemArrayList = viewPagerItemArrayList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewpager_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViewPagerItem viewPagerItem = viewPagerItemArrayList.get(position);

        // Load profile picture using Glide
        Glide.with(holder.itemView.getContext())
                .load(viewPagerItem.getProfileImageUrl())
                .placeholder(R.drawable.default_profile_image)
                .into(holder.imageView);

        holder.tvHeading.setText(viewPagerItem.getUsername());
        holder.tvDesc.setText(viewPagerItem.getDateOfBirth());

        // Set click listeners for each health record linear layout
        holder.imageButtonBloodGlucose.setOnClickListener(v -> onItemClick("Blood Glucose"));
        holder.imageButtonBloodPressure.setOnClickListener(v -> onItemClick("Blood Pressure"));
        holder.imageButtonWeight.setOnClickListener(v -> onItemClick("Weight"));
        holder.imageButtonHeartRate.setOnClickListener(v -> onItemClick("Heart Rate"));
        holder.imageButtonActivity.setOnClickListener(v -> onItemClick("Activity"));
        holder.imageButtonSleep.setOnClickListener(v -> onItemClick("Sleep"));

        int[][] segmentColors = {{Color.parseColor("#FFA500")}, {Color.TRANSPARENT}}; // You can define your own colors here

        // Style the PieChartView with more colors
        holder.pieChartView.setStrokeWidth(4); // Example stroke width
        holder.pieChartView.setStrokeColor(0xFF000000); // Example stroke color
        holder.pieChartView.setData(new float[]{75, 25}, segmentColors);
    }


    @Override
    public int getItemCount() {
        return viewPagerItemArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView tvHeading, tvDesc;
        LinearLayout imageButtonBloodGlucose;
        LinearLayout imageButtonBloodPressure;
        LinearLayout imageButtonWeight;
        LinearLayout imageButtonHeartRate;
        LinearLayout imageButtonActivity;
        LinearLayout imageButtonSleep;
        PieChartView pieChartView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivimage);
            tvHeading = itemView.findViewById(R.id.tvHeading);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            imageButtonBloodGlucose = itemView.findViewById(R.id.imageButtonBloodGlucose);
            imageButtonBloodPressure = itemView.findViewById(R.id.imageButtonBloodPressure);
            imageButtonWeight = itemView.findViewById(R.id.imageButtonWeight);
            imageButtonHeartRate = itemView.findViewById(R.id.imageButtonHeartRate);
            imageButtonActivity = itemView.findViewById(R.id.imageButtonActivity);
            imageButtonSleep = itemView.findViewById(R.id.imageButtonSleep);
            pieChartView = itemView.findViewById(R.id.pieChartView);
        }
    }

    private void onItemClick(String healthRecordType) {
        if (listener != null) {
            listener.onItemClick(healthRecordType);
        }
    }
}
