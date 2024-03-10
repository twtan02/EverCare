package my.edu.utar.evercare.Statistics;

import android.graphics.Color;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import my.edu.utar.evercare.R;

public class StatisticsPagerAdapter extends RecyclerView.Adapter<StatisticsPagerAdapter.ViewHolder> {

    private ArrayList<ViewPagerItem> viewPagerItemArrayList;
    private OnItemClickListener listener;
    private FirebaseFirestore db;
    private String currentUserId;

    // Define colors for each health record component
    private static final int[] CHART_COLORS = {
            Color.parseColor("#FFC107"), // Amber
            Color.parseColor("#4CAF50"), // Green
            Color.parseColor("#2196F3"), // Blue
            Color.parseColor("#F44336"), // Red
            Color.parseColor("#9C27B0")  // Purple
    };

    // Define a variable to store the adjusted percentage for each health record component
    private final SparseArray<Float> adjustedPercentages = new SparseArray<>();

    public interface OnItemClickListener {
        void onItemClick(String healthRecordType);
    }

    public StatisticsPagerAdapter(ArrayList<ViewPagerItem> viewPagerItemArrayList, OnItemClickListener listener, String currentUserId) {
        this.viewPagerItemArrayList = viewPagerItemArrayList;
        this.listener = listener;
        this.currentUserId = currentUserId;
    }

    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
        notifyDataSetChanged(); // Notify adapter that data has changed
    }

    public ViewPagerItem getViewPagerItemAtPosition(int position) {
        if (position >= 0 && position < viewPagerItemArrayList.size()) {
            return viewPagerItemArrayList.get(position);
        } else {
            return null;
        }
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
        holder.imageButtonBloodLipids.setOnClickListener(v -> onItemClick("Blood Lipids"));
        holder.imageButtonSleep.setOnClickListener(v -> onItemClick("Sleep"));

        currentUserId = viewPagerItem.getUserId();
        UpdateUserId(currentUserId, holder);
    }


    private void UpdateUserId(String currentUserId, ViewHolder holder){
        this.currentUserId = currentUserId;
        getAllStatisticsData(holder);
    }

    private void getAllStatisticsData(ViewHolder holder) {
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve data for blood glucose
        retrieveHealthRecordData("blood_glucose", holder);

        // Retrieve data for blood pressure
        retrieveHealthRecordData("blood_pressure", holder);

        // Retrieve data for blood lipids
        retrieveHealthRecordData("blood_lipids", holder);

        // Retrieve data for weight
        retrieveHealthRecordData("weight", holder);

        // Retrieve data for heart rate
        retrieveHealthRecordData("heart_rate", holder);

        // Retrieve data for sleep
        retrieveHealthRecordData("sleep", holder);
    }

    private void retrieveHealthRecordData(String component, ViewHolder holder) {
        db.collection("statistics")
                .document(currentUserId)
                .collection(component)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        switch (component) {
                            case "blood_glucose":
                                processBloodGlucoseData(task.getResult(), holder);
                                break;
                            case "blood_pressure":
                                processBloodPressureData(task.getResult(), holder);
                                break;
                            case "blood_lipids":
                                processBloodLipidsData(task.getResult(), holder);
                                break;
                            case "heart_rate":
                                processHeartRateData(task.getResult(), holder);
                                break;
                            case "sleep":
                                processSleepData(task.getResult(), holder);
                                break;
                            // Add cases for other health record components as needed
                        }
                    } else {
                        // Handle error
                        Log.d("Firestore", "Error getting " + component + " data: ", task.getException());
                    }
                });
    }

    // Helper method to check if two dates have the same date part
    private boolean isSameDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private void processBloodGlucoseData(QuerySnapshot querySnapshot, ViewHolder holder) {
        // Get today's date
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        Date todayDate = todayCalendar.getTime();

        // Define minimum and maximum range for blood glucose
        float minRange = 3.9f;
        float maxRange = 5.6f;

        // Variables to calculate average and percentage of accurate data within 20%
        int count = 0;
        float sum = 0.0f;
        boolean foundTodayRecord = false;

        // Iterate through all records to find today's records and calculate average
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            // Extract timestamp from the document
            Date recordDate = document.getDate("date");
            if (recordDate != null) {
                // Check if the record date is for today
                if (isSameDate(recordDate, todayDate)) {
                    // Mark that at least one record for today is found
                    foundTodayRecord = true;
                    // Extract blood glucose level
                    String glucoseLevelStr = document.getString("blood_glucose_level");
                    if (glucoseLevelStr != null) {
                        // Extract numeric part of the blood glucose level string and convert to float
                        String numericPart = extractNumericPart(glucoseLevelStr);
                        if (numericPart != null) {
                            try {
                                float glucoseLevel = Float.parseFloat(numericPart);
                                // Increment count and add glucose level to sum
                                count++;
                                sum += glucoseLevel;
                                // Log the blood glucose data for today
                                Log.d("BloodGlucoseActivity", "Blood Glucose Data for Today: " + glucoseLevel + " mmol/L");
                            } catch (NumberFormatException e) {
                                Log.e("BloodGlucoseActivity", "Invalid blood glucose level format", e);
                            }
                        }
                    }
                }
            }
        }

        // If at least one record for today is found, calculate average and adjust the percentage based on the distance from the middle of the range
        if (foundTodayRecord) {
            // Calculate average if there are records for today
            float average = (count > 0) ? (sum / count) : 0.0f;

            // Calculate the middle of the range
            float middle = (maxRange + minRange) / 2.0f;

            // Calculate the distance of the average from the middle of the range
            float distanceFromMiddle = Math.abs(average - middle);

            // Calculate the percentage adjustment based on the distance from the middle of the range
            float percentageAdjustment = 1.0f - (distanceFromMiddle / (maxRange - minRange) * 2.0f);
            percentageAdjustment = Math.max(percentageAdjustment, 0.0f); // Ensure percentage adjustment is at least 0

            // Calculate the final percentage adjusted to out of 20%
            float adjustedPercentage = percentageAdjustment * 20.0f;

            holder.setAdjustedPercentage(0, adjustedPercentage);

            // Log the results
            Log.d("BloodGlucoseActivity", "Average blood glucose level for today: " + average);
            Log.d("BloodGlucoseActivity", "Adjusted percentage: " + adjustedPercentage + "%");
        } else {
            Log.d("BloodGlucoseActivity", "No blood glucose records found for today");
            // Perform actions for when there are no records found for today
        }
    }

    private void processBloodPressureData(QuerySnapshot querySnapshot, ViewHolder holder) {
        // Get today's date
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        Date todayDate = todayCalendar.getTime();

        // Define minimum and maximum range for blood pressure
        float minRange = 90.0f; // Minimum blood pressure level (in mmHg)
        float maxRange = 120.0f; // Maximum blood pressure level (in mmHg)

        // Variables to calculate average and percentage of accurate data within 20%
        int count = 0;
        float sum = 0.0f;
        boolean foundTodayRecord = false;

        // Iterate through all records to find today's records and calculate average
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            // Extract timestamp from the document
            Date recordDate = document.getDate("date");
            if (recordDate != null) {
                // Check if the record date is for today
                if (isSameDate(recordDate, todayDate)) {
                    // Mark that at least one record for today is found
                    foundTodayRecord = true;
                    // Extract blood pressure level
                    String bloodPressureStr = document.getString("blood_pressure_level");
                    if (bloodPressureStr != null) {
                        // Extract numeric part of the blood pressure string and convert to float
                        String numericPart = extractNumericPart(bloodPressureStr);
                        if (numericPart != null) {
                            try {
                                float bloodPressureLevel = Float.parseFloat(numericPart);
                                // Increment count and add blood pressure level to sum
                                count++;
                                sum += bloodPressureLevel;
                                // Log the blood pressure data for today
                                Log.d("BloodPressureActivity", "Blood Pressure Data for Today: " + bloodPressureLevel + " mmHg");
                            } catch (NumberFormatException e) {
                                Log.e("BloodPressureActivity", "Invalid blood pressure level format", e);
                            }
                        }
                    }
                }
            }
        }

        // If at least one record for today is found, calculate average and adjust the percentage based on the distance from the middle of the range
        if (foundTodayRecord) {
            // Calculate average if there are records for today
            float average = (count > 0) ? (sum / count) : 0.0f;

            // Calculate the middle of the range
            float middle = (maxRange + minRange) / 2.0f;

            // Calculate the distance of the average from the middle of the range
            float distanceFromMiddle = Math.abs(average - middle);

            // Calculate the percentage adjustment based on the distance from the middle of the range
            float percentageAdjustment = 1.0f - (distanceFromMiddle / (maxRange - minRange) * 2.0f);
            percentageAdjustment = Math.max(percentageAdjustment, 0.0f); // Ensure percentage adjustment is at least 0

            // Calculate the final percentage adjusted to out of 20%
            float adjustedPercentage = percentageAdjustment * 20.0f;

            holder.setAdjustedPercentage(1, adjustedPercentage);

            // Log the results
            Log.d("BloodPressureActivity", "Average blood pressure level for today: " + average);
            Log.d("BloodPressureActivity", "Adjusted percentage: " + adjustedPercentage + "%");
        } else {
            Log.d("BloodPressureActivity", "No blood pressure records found for today");
            // Perform actions for when there are no records found for today
        }
    }


    private void processBloodLipidsData(QuerySnapshot querySnapshot, ViewHolder holder) {
        // Get today's date
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        Date todayDate = todayCalendar.getTime();

        // Define minimum and maximum range for blood lipids
        float minRange = 100.0f;
        float maxRange = 129.0f;

        // Variables to calculate average and percentage of accurate data within 20%
        int count = 0;
        float sum = 0.0f;
        boolean foundTodayRecord = false;

        // Iterate through all records to find today's records and calculate average
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            // Extract timestamp from the document
            Date recordDate = document.getDate("date");
            if (recordDate != null) {
                // Check if the record date is for today
                if (isSameDate(recordDate, todayDate)) {
                    // Mark that at least one record for today is found
                    foundTodayRecord = true;
                    // Extract blood lipids level as String
                    String bloodLipidsLevelStr = document.getString("blood_lipids_level");
                    if (bloodLipidsLevelStr != null) {
                        // Extract numeric part of the blood lipids level string and convert to float
                        String numericPart = extractNumericPart(bloodLipidsLevelStr);
                        if (numericPart != null) {
                            try {
                                float bloodLipidsLevelFloat = Float.parseFloat(numericPart);
                                // Increment count and add blood lipids level to sum
                                count++;
                                sum += bloodLipidsLevelFloat;
                                // Log the blood lipids data for today
                                Log.d("BloodLipidsActivity", "Blood Lipids Data for Today: " + bloodLipidsLevelFloat + " mg/dL");
                            } catch (NumberFormatException e) {
                                Log.e("BloodLipidsActivity", "Invalid blood lipids level format", e);
                            }
                        }
                    }
                }
            }
        }


        // If at least one record for today is found, calculate average and adjust the percentage based on the distance from the middle of the range
        if (foundTodayRecord) {
            // Calculate average if there are records for today
            float average = (count > 0) ? (sum / count) : 0.0f;

            // Calculate the middle of the range
            float middle = (maxRange + minRange) / 2.0f;

            // Calculate the distance of the average from the middle of the range
            float distanceFromMiddle = Math.abs(average - middle);

            // Calculate the percentage adjustment based on the distance from the middle of the range
            float percentageAdjustment = 1.0f - (distanceFromMiddle / (maxRange - minRange) * 2.0f);
            percentageAdjustment = Math.max(percentageAdjustment, 0.0f); // Ensure percentage adjustment is at least 0

            // Calculate the final percentage adjusted to out of 20%
            float adjustedPercentage = percentageAdjustment * 20.0f;

            holder.setAdjustedPercentage(2, adjustedPercentage);

            // Log the results
            Log.d("BloodLipidsActivity", "Average blood lipids level for today: " + average);
            Log.d("BloodLipidsActivity", "Adjusted percentage: " + adjustedPercentage + "%");
        } else {
            Log.d("BloodLipidsActivity", "No blood lipids records found for today");
            // Perform actions for when there are no records found for today
        }
    }


    private void processHeartRateData(QuerySnapshot querySnapshot, ViewHolder holder) {
        // Get today's date
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        Date todayDate = todayCalendar.getTime();

        // Define minimum and maximum range for heart rate
        int minRange = 60; // Minimum heart rate (in bpm)
        int maxRange = 100; // Maximum heart rate (in bpm)

        // Variables to calculate average and percentage of accurate data within 20%
        int count = 0;
        int sum = 0;
        boolean foundTodayRecord = false;

        // Iterate through all records to find today's records and calculate average
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            // Extract timestamp from the document
            Date recordDate = document.getDate("date");
            if (recordDate != null) {
                // Check if the record date is for today
                if (isSameDate(recordDate, todayDate)) {
                    // Mark that at least one record for today is found
                    foundTodayRecord = true;
                    // Extract heart rate as String
                    String heartRateString = document.getString("heart_rate");
                    if (heartRateString != null) {
                        // Extract numeric value from heart rate string
                        int heartRateValue = Integer.parseInt(heartRateString.split(" ")[0]);
                        // Increment count and add heart rate to sum
                        count++;
                        sum += heartRateValue;
                        // Log the heart rate data for today
                        Log.d("HeartRateActivity", "Heart Rate Data for Today: " + heartRateValue + " bpm");
                    }
                }
            }
        }


        // If at least one record for today is found, calculate average and adjust the percentage based on the distance from the middle of the range
        if (foundTodayRecord) {
            // Calculate average if there are records for today
            float average = (count > 0) ? ((float) sum / count) : 0.0f;

            // Calculate the middle of the range
            float middle = (maxRange + minRange) / 2.0f;

            // Calculate the distance of the average from the middle of the range
            float distanceFromMiddle = Math.abs(average - middle);

            // Calculate the percentage adjustment based on the distance from the middle of the range
            float percentageAdjustment = 1.0f - (distanceFromMiddle / (maxRange - minRange) * 2.0f);
            percentageAdjustment = Math.max(percentageAdjustment, 0.0f); // Ensure percentage adjustment is at least 0

            // Calculate the final percentage adjusted to out of 20%
            float adjustedPercentage = percentageAdjustment * 20.0f;

            holder.setAdjustedPercentage(3, adjustedPercentage);

            // Log the results
            Log.d("HeartRateActivity", "Average heart rate for today: " + average);
            Log.d("HeartRateActivity", "Adjusted percentage: " + adjustedPercentage + "%");
        } else {
            Log.d("HeartRateActivity", "No heart rate records found for today");
            // Perform actions for when there are no records found for today
        }
    }


    private void processSleepData(QuerySnapshot querySnapshot, ViewHolder holder) {
        // Get today's date
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        Date todayDate = todayCalendar.getTime();

        // Define minimum and maximum range for sleep duration (in hours)
        int minRange = 5; // Minimum sleep duration
        int maxRange = 8; // Maximum sleep duration

        // Variables to calculate average and percentage of accurate data within 20%
        int count = 0;
        int sum = 0;
        boolean foundTodayRecord = false;

        // Iterate through all records to find today's records and calculate average
        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
            // Extract timestamp from the document
            Date recordDate = document.getDate("date");
            if (recordDate != null) {
                // Check if the record date is for today
                if (isSameDate(recordDate, todayDate)) {
                    // Mark that at least one record for today is found
                    foundTodayRecord = true;
                    // Extract sleep duration
                    String sleepDurationStr = document.getString("sleep_duration");
                    if (sleepDurationStr != null) {
                        // Extract numeric part of the sleep duration string and convert to int
                        String numericPart = extractNumericPart(sleepDurationStr);
                        if (numericPart != null) {
                            try {
                                int sleepDuration = Integer.parseInt(numericPart);
                                // Increment count and add sleep duration to sum
                                count++;
                                sum += sleepDuration;
                                // Log the sleep duration data for today
                                Log.d("SleepActivity", "Sleep Duration Data for Today: " + sleepDuration + " hours");
                            } catch (NumberFormatException e) {
                                Log.e("SleepActivity", "Invalid sleep duration format", e);
                            }
                        }
                    }
                }
            }
        }

        // If at least one record for today is found, calculate average and adjust the percentage based on the distance from the middle of the range
        if (foundTodayRecord) {
            // Calculate average if there are records for today
            float average = (count > 0) ? ((float) sum / count) : 0.0f;

            // Calculate the middle of the range
            float middle = (maxRange + minRange) / 2.0f;

            // Calculate the distance of the average from the middle of the range
            float distanceFromMiddle = Math.abs(average - middle);

            // Calculate the percentage adjustment based on the distance from the middle of the range
            float percentageAdjustment = 1.0f - (distanceFromMiddle / (maxRange - minRange) * 2.0f);
            percentageAdjustment = Math.max(percentageAdjustment, 0.0f); // Ensure percentage adjustment is at least 0

            // Calculate the final percentage adjusted to out of 20%
            float adjustedPercentage = percentageAdjustment * 20.0f;

            holder.setAdjustedPercentage(4, adjustedPercentage);

            // Log the results
            Log.d("SleepActivity", "Average sleep duration for today: " + average);
            Log.d("SleepActivity", "Adjusted percentage: " + adjustedPercentage + "%");
        } else {
            Log.d("SleepActivity", "No sleep records found for today");
            // Perform actions for when there are no records found for today
        }
    }


    private String extractNumericPart(String glucoseLevelStr) {
        // Remove all non-numeric characters except the decimal point
        return glucoseLevelStr.replaceAll("[^\\d.]", "");
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
        LinearLayout imageButtonBloodLipids;
        LinearLayout imageButtonSleep;
        PieChartView pieChartView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivimage);
            tvHeading = itemView.findViewById(R.id.tvHeading);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            imageButtonBloodGlucose = itemView.findViewById(R.id.imageButtonBloodGlucose);
            imageButtonBloodPressure = itemView.findViewById(R.id.imageButtonBloodPressure);
            imageButtonBloodLipids = itemView.findViewById(R.id.imageButtonBloodLipids);
            imageButtonHeartRate = itemView.findViewById(R.id.imageButtonHeartRate);
            imageButtonWeight = itemView.findViewById(R.id.imageButtonWeight);
            imageButtonSleep = itemView.findViewById(R.id.imageButtonSleep);
            pieChartView = itemView.findViewById(R.id.pieChartView);
        }

        private void setAdjustedPercentage(int index, float adjustedPercentage) {
            // Store the adjusted percentage for the corresponding health record component index
            adjustedPercentages.put(index, adjustedPercentage);

            // Check if all health record components have been processed
            if (adjustedPercentages.size() == CHART_COLORS.length) {
                // Calculate the total adjusted percentage
                float totalPercentage = 0;
                for (int i = 0; i < CHART_COLORS.length; i++) {
                    totalPercentage += adjustedPercentages.get(i);
                }

                // Ensure the total percentage does not exceed 100%
                totalPercentage = Math.min(totalPercentage, 100f);

                // Log the total percentage for debugging
                Log.d("PieChartDebug", "Total percentage: " + totalPercentage);

                // Set the pie chart data using the total adjusted percentage
                setPieChartData(totalPercentage);

                // Clear the adjusted percentage values
                adjustedPercentages.clear();
            }
        }


        public void setPieChartData(float totalPercentage) {
            float[] data = new float[CHART_COLORS.length];
            int[][] segmentColors = new int[CHART_COLORS.length][1];

            // Calculate the adjusted percentage for each health record component
            for (int i = 0; i < CHART_COLORS.length; i++) {
                float adjustedPercentage = adjustedPercentages.get(i, 0f);
                float segmentPercentage = (totalPercentage / 100f) * adjustedPercentage;

                // Store the segment percentage and corresponding color
                data[i] = segmentPercentage;
                segmentColors[i][0] = CHART_COLORS[i];

                // Log the segment percentage for debugging
                Log.d("PieChartDebug", "Segment " + i + " percentage: " + segmentPercentage);
            }

            // Calculate the remaining percentage to fill with transparent color
            float remainingPercentage = 100f - totalPercentage;


            // If there's remaining percentage, fill it with transparent color
            if (remainingPercentage > 0) {
                // Add a transparent color for the remaining segment
                data = Arrays.copyOf(data, data.length + 1);
                segmentColors = Arrays.copyOf(segmentColors, segmentColors.length + 1);
                data[data.length - 1] = remainingPercentage;
                segmentColors[segmentColors.length - 1] = new int[]{Color.TRANSPARENT};

                // Log the remaining percentage
                Log.d("PieChartDebug", "Remaining percentage: " + remainingPercentage);
            }

            // Style the PieChartView
            pieChartView.setStrokeWidth(4); // Example stroke width
            pieChartView.setStrokeColor(0xFF000000); // Example stroke color
            pieChartView.setData(data, segmentColors);
        }


    }

    private void onItemClick(String healthRecordType) {
        if (listener != null) {
            listener.onItemClick(healthRecordType);
        }
    }
}