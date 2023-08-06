package my.edu.utar.evercare;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PillReminderActivity extends AppCompatActivity {

    private List<PillReminder> pillReminders = new ArrayList<>();
    private RecyclerView recyclerView;
    private PillReminderAdapter pillReminderAdapter;
    private FirebaseFirestore firestore;

    private AlertDialog dialog;
    private Spinner spinnerElderly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill_reminder);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Pill Reminder");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.pill_reminder_recyclerview);
        firestore = FirebaseFirestore.getInstance();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        pillReminderAdapter = new PillReminderAdapter(pillReminders, this);
        recyclerView.setAdapter(pillReminderAdapter);

        fetchPillRemindersFromFirestore();

        Button addPillReminderButton = findViewById(R.id.btn_add_pill_reminder);
        addPillReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddPillReminderDialog();
            }
        });
    }

    private void fetchPillRemindersFromFirestore() {
        firestore.collection("pill_reminders")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            pillReminders.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                PillReminder pillReminder = document.toObject(PillReminder.class);
                                if (pillReminder != null) {
                                    pillReminders.add(pillReminder);
                                }
                            }
                            pillReminderAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("PillReminderActivity", "Error getting pill reminders: ", task.getException());
                        }
                    }
                });
    }

    private void showAddPillReminderDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_add_pill_reminder, null);

        EditText pillNameEditText = dialogView.findViewById(R.id.edit_pill_name);
        EditText dosageEditText = dialogView.findViewById(R.id.edit_dosage);
        Spinner frequencySpinner = dialogView.findViewById(R.id.spinner_frequency);
        TextView editReminderTime = dialogView.findViewById(R.id.edit_reminder_time);
        TextView editReminderDate = dialogView.findViewById(R.id.edit_reminder_date);
        spinnerElderly = dialogView.findViewById(R.id.spinner_elderly_user);

        fetchElderlyUserNames();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.frequency_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(adapter);

        Button showDatePickerButton = dialogView.findViewById(R.id.btn_show_date_picker);
        showDatePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(dialogView);
            }
        });

        Button showTimePickerButton = dialogView.findViewById(R.id.btn_show_time_picker);
        showTimePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker(dialogView);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogStyle);
        builder.setTitle("Add Pill Reminder");
        builder.setView(dialogView);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener()  {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pillName = pillNameEditText.getText().toString();
                String dosageString = dosageEditText.getText().toString();
                String frequency = frequencySpinner.getSelectedItem().toString();
                String reminderDate = editReminderDate.getText().toString();
                String reminderTime = editReminderTime.getText().toString();
                String selectedElderlyUser = spinnerElderly.getSelectedItem().toString();

                int dosage = Integer.parseInt(dosageString);

                PillReminder newPillReminder = new PillReminder(pillName, dosage, frequency, reminderDate, reminderTime, selectedElderlyUser);
                pillReminders.add(newPillReminder);
                pillReminderAdapter.notifyDataSetChanged();
                addPillReminderToFirestore(newPillReminder);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    private void addPillReminderToFirestore(PillReminder pillReminder) {
        firestore.collection("pill_reminders")
                .add(pillReminder)
                .addOnSuccessListener(documentReference -> {
                    Log.d("PillReminderActivity", "Pill reminder added with ID: " + documentReference.getId());
                    Toast.makeText(this, "Pill reminder added successfully", Toast.LENGTH_SHORT).show();
                    // Schedule notification
                    scheduleNotification(pillReminder);
                })
                .addOnFailureListener(e -> {
                    Log.e("PillReminderActivity", "Error adding pill reminder", e);
                    Toast.makeText(this, "Error adding pill reminder", Toast.LENGTH_SHORT).show();
                });
    }

    private void showDatePicker(View view) {
        TextView editReminderDate = view.findViewById(R.id.edit_reminder_date);

        // Get the current date
        Calendar currentDate = Calendar.getInstance();
        int year = currentDate.get(Calendar.YEAR);
        int month = currentDate.get(Calendar.MONTH);
        int day = currentDate.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String formattedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                editReminderDate.setText(formattedDate);
            }
        }, year, month, day);

        dialog.show();
    }

    private void showTimePicker(View view) {
        TextView editReminderTime = view.findViewById(R.id.edit_reminder_time);

        // Get the current time
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                editReminderTime.setText(formattedTime);
            }
        }, hour, minute, true);

        dialog.show();
    }

    private void fetchElderlyUserNames() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("elderly_users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> elderlyUserNames = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Assuming that you have a field named "name" in your "elderly_users" collection
                                String userName = document.getString("username");
                                if (userName != null) {
                                    elderlyUserNames.add(userName);
                                }
                            }
                            setupElderlyUserSpinner(elderlyUserNames);
                        } else {
                            Log.e("PillReminderActivity", "Error fetching elderly user names: ", task.getException());
                        }
                    }
                });
    }

    private void setupElderlyUserSpinner(List<String> elderlyUserNames) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, elderlyUserNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerElderly.setAdapter(spinnerAdapter);
    }


    private Calendar getReminderDateTime(String reminderDate, String reminderTime) {
        // Parse reminder date and time and return a Calendar object
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        try {
            calendar.setTime(sdf.parse(reminderDate + " " + reminderTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return calendar;
    }


    private int calculateNotificationId(String reminderDate, String reminderTime) {
        String combinedDateTime = reminderDate + " " + reminderTime;
        return combinedDateTime.hashCode();
    }

    private void scheduleNotification(PillReminder pillReminder) {
        // Get the reminder date and time
        Calendar reminderDateTime = getReminderDateTime(pillReminder.getReminderDate(), pillReminder.getReminderTime());

        // Calculate the notification ID
        int notificationId = calculateNotificationId(pillReminder.getReminderDate(), pillReminder.getReminderTime());

        // Create an alarm intent
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra("reminderTitle", "Pill Reminder");
        notificationIntent.putExtra("reminderText", "It's time to take your pill!");
        notificationIntent.putExtra("notificationId", notificationId); // Pass the notification ID

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Schedule the alarm
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderDateTime.getTimeInMillis(), pendingIntent);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
