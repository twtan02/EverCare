package my.edu.utar.evercare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {

    private List<DailySchedule> scheduleData;
    private LayoutInflater inflater;

    public CalendarAdapter(Context context, List<DailySchedule> scheduleData) {
        this.inflater = LayoutInflater.from(context);
        this.scheduleData = scheduleData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.activity_calendar_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DailySchedule schedule = scheduleData.get(position);
        holder.dayTextView.setText(schedule.getDay());
        holder.eventTextView.setText(schedule.getEvent());
    }

    @Override
    public int getItemCount() {
        return scheduleData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayTextView;
        TextView eventTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            eventTextView = itemView.findViewById(R.id.eventTextView);
        }
    }
}
