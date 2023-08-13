package my.edu.utar.evercare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class PillReminderAdapter extends RecyclerView.Adapter<PillReminderAdapter.PillReminderViewHolder> {

    private List<PillReminder> pillReminders;
    private Context context;

    public PillReminderAdapter(List<PillReminder> pillReminders, Context context) {
        this.pillReminders = pillReminders;
        this.context = context;
    }

    @NonNull
    @Override
    public PillReminderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pill_reminder, parent, false);
        return new PillReminderViewHolder(view);
    }


    @Override
    public void onBindViewHolder(PillReminderViewHolder holder, int position) {
        PillReminder pillReminder = pillReminders.get(position);
        holder.bind(pillReminder);
    }

    @Override
    public int getItemCount() {
        return pillReminders.size();
    }

    public class PillReminderViewHolder extends RecyclerView.ViewHolder {
        TextView tvPillName, tvDosage, tvFrequency, tvReminderDate, tvReminderTime, tvElderlyUser;

        public PillReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPillName = itemView.findViewById(R.id.tv_pill_name);
            tvDosage = itemView.findViewById(R.id.tv_dosage);
            tvFrequency = itemView.findViewById(R.id.tv_frequency);
            tvReminderDate = itemView.findViewById(R.id.tv_reminder_date);
            tvReminderTime = itemView.findViewById(R.id.tv_reminder_time);
            tvElderlyUser = itemView.findViewById(R.id.tv_elderly_user);
        }

        public void bind(PillReminder pillReminder) {
            tvPillName.setText(pillReminder.getPillName());
            tvDosage.setText("Dosage: " + pillReminder.getDosage());
            tvFrequency.setText("Frequency: " + pillReminder.getFrequency());
            tvReminderDate.setText("Reminder Date: " + pillReminder.getReminderDate());
            tvReminderTime.setText("Reminder Time: " + pillReminder.getReminderTime());
            tvElderlyUser.setText("UserName: " + pillReminder.getElderlyUser());
        }
    }
}
