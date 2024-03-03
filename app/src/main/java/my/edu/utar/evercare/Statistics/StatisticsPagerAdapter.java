package my.edu.utar.evercare.Statistics;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;

import my.edu.utar.evercare.R;

public class StatisticsPagerAdapter extends RecyclerView.Adapter<StatisticsPagerAdapter.ViewHolder> {

    private ArrayList<ViewPagerItem> viewPagerItemArrayList;
    private OnItemClickListener listener;

    // Define interface to handle item click events
    public interface OnItemClickListener {
        void onItemClick(String healthRecordType); // Pass the type of health record clicked
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
        holder.imageButtonBloodGlucose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick("Blood Glucose"); // Pass the type of health record clicked
                }
            }
        });

        holder.imageButtonBloodPressure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick("Blood Pressure"); // Pass the type of health record clicked
                }
            }
        });

        holder.imageButtonWeight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick("Weight"); // Pass the type of health record clicked
                }
            }
        });

        holder.imageButtonHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick("Heart Rate"); // Pass the type of health record clicked
                }
            }
        });

        holder.imageButtonActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick("Activity"); // Pass the type of health record clicked
                }
            }
        });

        holder.imageButtonSleep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick("Sleep"); // Pass the type of health record clicked
                }
            }
        });

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
        }
    }
}
