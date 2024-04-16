package my.edu.utar.evercare.PillReminder;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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

import my.edu.utar.evercare.R;

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

        pillReminderAdapter.setOnDeleteClickListener(new PillReminderAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(PillReminder pillReminder) {
                if (pillReminder.getDocumentId() != null) {
                    deletePillReminderFromFirestore(pillReminder);
                } else {
                    Log.e("PillReminderActivity", "Document ID is null");
                }
            }
        });

    }

    private void fetchPillRemindersFromFirestore() {

        getCurrentUserRole(new OnUserRoleFetchedListener() {
            @Override
            public void onUserRoleFetched(String currentUserRole) {
                Log.d("PillReminderActivity", "Current user role: " + currentUserRole);
                firestore.collection("all_users")
                        .document(getCurrentUserId()) // Assuming you have a method to get the current user's ID
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        // Check if the current user is a caregiver
                                        if (currentUserRole.equals("Caregiver")) {
                                            // Fetch the linked elderly parent name
                                            String elderlyParentName = document.getString("elderlyParentName");
                                            if (elderlyParentName != null) {
                                                fetchPillRemindersForCaregiver(elderlyParentName);
                                            }
                                        } else {
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
                                                                        pillReminder.setDocumentId(document.getId()); // Set document ID
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
                                    } else {
                                        Log.e("PillReminderActivity", "User document does not exist");
                                    }
                                } else {
                                    Log.e("PillReminderActivity", "Error fetching current user data: ", task.getException());
                                }
                            }
                        });
            }
        });
    }

    private void fetchPillRemindersForCaregiver(String elderlyParentName) {
        firestore.collection("pill_reminders")
                .whereEqualTo("elderlyUser", elderlyParentName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            pillReminders.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                PillReminder pillReminder = document.toObject(PillReminder.class);
                                if (pillReminder != null) {
                                    pillReminder.setDocumentId(document.getId()); // Set document ID
                                    pillReminders.add(pillReminder);
                                }
                            }
                            pillReminderAdapter.notifyDataSetChanged();
                        } else {
                            Log.e("PillReminderActivity", "Error getting pill reminders for caregiver: ", task.getException());
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

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pillName = pillNameEditText.getText().toString().trim();
                String dosageString = dosageEditText.getText().toString().trim();
                String frequency = frequencySpinner.getSelectedItem().toString();
                String reminderDate = editReminderDate.getText().toString().trim();
                String reminderTime = editReminderTime.getText().toString().trim();
                String selectedElderlyUser = spinnerElderly.getSelectedItem().toString().trim(); // Get the selected elderly user

                if (pillName.isEmpty()) {
                    // Notify the user about the missing pill name
                    Toast.makeText(PillReminderActivity.this, "Please enter the pill name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (dosageString.isEmpty()) {
                    // Notify the user about the missing dosage
                    Toast.makeText(PillReminderActivity.this, "Please enter the dosage", Toast.LENGTH_SHORT).show();
                    return;
                }

                int dosage;
                try {
                    dosage = Integer.parseInt(dosageString);
                    if (dosage <= 0) {
                        // Dosage should be greater than 0
                        Toast.makeText(PillReminderActivity.this, "Dosage should be greater than 0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (NumberFormatException e) {
                    // Invalid dosage format
                    Toast.makeText(PillReminderActivity.this, "Invalid dosage format", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (reminderDate.isEmpty()) {
                    // Notify the user about the missing reminder date
                    Toast.makeText(PillReminderActivity.this, "Please select the reminder date", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (reminderTime.isEmpty()) {
                    // Notify the user about the missing reminder time
                    Toast.makeText(PillReminderActivity.this, "Please select the reminder time", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedElderlyUser.isEmpty()) {
                    // Notify the user about the missing selected elderly user
                    Toast.makeText(PillReminderActivity.this, "Please select the elderly user", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Proceed with adding the pill reminder to Firestore
                PillReminder newPillReminder = new PillReminder(pillName, dosage, frequency, reminderDate, reminderTime, selectedElderlyUser);
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

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setTextColor(getResources().getColor(R.color.black));
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        negativeButton.setTextColor(getResources().getColor(R.color.black));
    }

    private void addPillReminderToFirestore(PillReminder pillReminder) {
        firestore.collection("pill_reminders")
                .add(pillReminder)  // Using add() to generate unique document ID
                .addOnSuccessListener(documentReference -> {
                    String documentId = documentReference.getId(); // Get the generated document ID
                    Log.d("PillReminderActivity", "Pill reminder added with ID: " + documentId);
                    // Set the document ID for the added pill reminder
                    pillReminder.setDocumentId(documentId);
                    // Notify adapter of data change
                    pillReminders.add(pillReminder);
                    pillReminderAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Pill reminder added successfully", Toast.LENGTH_SHORT).show();
                    // Schedule notification
                    scheduleNotification(pillReminder, documentId); // Pass document ID to scheduleNotification
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

        // Fetch current user's role
        getCurrentUserRole(new OnUserRoleFetchedListener() {
            @Override
            public void onUserRoleFetched(String currentUserRole) {
                Log.d("PillReminderActivity", "Current user role: " + currentUserRole);
                firestore.collection("all_users")
                        .document(getCurrentUserId()) // Assuming you have a method to get the current user's ID
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        // Check if the current user is a caregiver
                                        if (currentUserRole.equals("Caregiver")) {
                                            // Fetch the linked elderly parent name
                                            String elderlyParentName = document.getString("elderlyParentName");
                                            if (elderlyParentName != null) {
                                                // Fetch all elderly user names where the name matches the linked elderly parent name
                                                fetchElderlyUserNamesForCaregiver(elderlyParentName);
                                            }
                                        } else {
                                            // If the current user is not a caregiver, fetch all elderly user names
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
                                    } else {
                                        Log.e("PillReminderActivity", "User document does not exist");
                                    }
                                } else {
                                    Log.e("PillReminderActivity", "Error fetching current user data: ", task.getException());
                                }
                            }
                        });
            }
        });
    }


    private void getCurrentUserRole(OnUserRoleFetchedListener listener) {
        // Retrieve the current user's ID from Firebase Authentication
        String userId = getCurrentUserId();

        // Query Firestore to get the corresponding user document
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference userRef = firestore.collection("all_users").document(userId);
        userRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userRole = documentSnapshot.getString("role");
                        listener.onUserRoleFetched(userRole);
                    } else {
                        Log.e("PillReminderActivity", "User document does not exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PillReminderActivity", "Error retrieving user document", e);
                });
    }

    // Define an interface for fetching user role asynchronously
    private interface OnUserRoleFetchedListener {
        void onUserRoleFetched(String userRole);
    }



    // Fetch elderly user names for the caregiver's linked elderly parent
    private void fetchElderlyUserNamesForCaregiver(String elderlyParentName) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("elderly_users")
                .whereEqualTo("username", elderlyParentName) // Assuming the field name for linked parent name is "linkedParentName"
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override

                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<String> elderlyUserNames = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String userName = document.getString("username");
                                if (userName != null) {
                                    elderlyUserNames.add(userName);
                                }
                            }
                            setupElderlyUserSpinner(elderlyUserNames);
                        } else {
                            Log.e("PillReminderActivity", "Error fetching elderly user names for caregiver's linked elderly parent: ", task.getException());
                        }
                    }
                });
    }



    private String getCurrentUserId() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            // Handle the case where the current user is null
            return null;
        }
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


    private int calculateNotificationId(String documentId) {
        // Use document ID directly as notification ID
        return documentId.hashCode();
    }


    private void scheduleNotification(PillReminder pillReminder, String documentId) {
        // Get the reminder date and time
        Calendar reminderDateTime = getReminderDateTime(pillReminder.getReminderDate(), pillReminder.getReminderTime());

        // Calculate the notification ID
        int notificationId = calculateNotificationId(documentId); // Use document ID for notification ID

        // Create an alarm intent
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra("reminderTitle", "Pill Reminder");
        notificationIntent.putExtra("reminderText", "It's time to take your pill!");
        notificationIntent.putExtra("notificationId", notificationId); // Pass the notification ID

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Schedule the alarm (using scheduleExactAlarm method)
        scheduleExactAlarm(pendingIntent, reminderDateTime.getTimeInMillis());
    }


    private void scheduleExactAlarm(PendingIntent pendingIntent, long triggerMillis) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            // AlarmManager is not available on this device
            Toast.makeText(this, "AlarmManager is not available on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the device supports scheduling exact alarms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // Device does not support scheduling exact alarms
                // Handle accordingly (e.g., fallback to inexact alarms)
                Toast.makeText(this, "Device does not support scheduling exact alarms", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
        } catch (SecurityException e) {
            // Handle SecurityException (e.g., show error message)
            Toast.makeText(this, "Failed to schedule exact alarm: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }



    private void deletePillReminderFromFirestore(PillReminder pillReminder) {
        String documentId = pillReminder.getDocumentId();
        if (documentId != null) {
            // Cancel the scheduled notification for this pill reminder
            cancelNotification(pillReminder);

            // Delete the pill reminder document from Firestore using its document ID
            firestore.collection("pill_reminders")
                    .document(documentId)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Pill reminder deleted successfully
                            Toast.makeText(PillReminderActivity.this, "Pill reminder deleted", Toast.LENGTH_SHORT).show();
                            // Remove the deleted pill reminder from the list
                            pillReminders.remove(pillReminder);
                            // Notify the adapter of the removal
                            pillReminderAdapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to delete pill reminder
                            Log.e("PillReminderActivity", "Error deleting pill reminder", e);
                            Toast.makeText(PillReminderActivity.this, "Failed to delete pill reminder", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Handle the case where the document ID is null
            Log.e("PillReminderActivity", "Error: Document ID is null");
            Toast.makeText(PillReminderActivity.this, "Error: Document ID is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelNotification(PillReminder pillReminder) {
        // Calculate the notification ID based on the document ID
        int notificationId = calculateNotificationId(pillReminder.getDocumentId());

        // Create an intent for the scheduled notification
        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationId, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // Cancel the scheduled notification
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
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