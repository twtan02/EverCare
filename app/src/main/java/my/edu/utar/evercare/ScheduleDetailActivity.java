package my.edu.utar.evercare;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleDetailActivity extends AppCompatActivity {

    private TextView textViewScheduleDate;
    private Button buttonAddTask;
    private List<Task> taskList;
    private TaskAdapter taskAdapter;
    private DatabaseReference tasksRef;
    private RecyclerView recyclerViewTasks;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the custom title text
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        textViewScheduleDate = findViewById(R.id.textViewScheduleDate);
        buttonAddTask = findViewById(R.id.buttonAddTask);

        // Enable the back button on the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerViewTasks = findViewById(R.id.recyclerViewTasks);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        recyclerViewTasks.setAdapter(taskAdapter);

        // Get a reference to your Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        tasksRef = database.getReference("tasks");

        // Get the selected date from the Intent extras
        if (getIntent().hasExtra("selected_date")) {
            long selectedDateTimestamp = getIntent().getLongExtra("selected_date", 0);
            Date selectedDate = new Date(selectedDateTimestamp);
            loadTasksForDate(selectedDate);

            // Format the selected date and display it in the title
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            String dateString = dateFormat.format(selectedDate);
            textViewScheduleDate.setText("Schedule Tasks for " + dateString);
        }

        // Set click listener for Add Task button
        buttonAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getIntent().hasExtra("selected_date")) {
                    long selectedDateTimestamp = getIntent().getLongExtra("selected_date", 0);
                    Date selectedDate = new Date(selectedDateTimestamp);
                    showTaskDialog(selectedDate);
                }
            }
        });
    }

    private void loadTasksForDate(Date selectedDate) {
        taskList.clear();
        // Format the selected date as a string (you can use a different format if needed)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateString = dateFormat.format(selectedDate);
        // Query the tasks in Firebase for the selected date
        tasksRef.orderByChild("dateString").equalTo(dateString).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // A new task is added to the database
                Task task = snapshot.getValue(Task.class);
                if (task != null) {
                    taskList.add(task);
                    taskAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // A task is updated in the database
                Task task = snapshot.getValue(Task.class);
                if (task != null) {
                    // Find the index of the existing task in the taskList
                    int index = -1;
                    for (int i = 0; i < taskList.size(); i++) {
                        if (taskList.get(i).getTaskId().equals(task.getTaskId())) {
                            index = i;
                            break;
                        }
                    }
                    if (index != -1) {
                        taskList.set(index, task);
                        taskAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // A task is removed from the database
                Task task = snapshot.getValue(Task.class);
                if (task != null) {
                    // Find the index of the existing task in the taskList
                    int index = -1;
                    for (int i = 0; i < taskList.size(); i++) {
                        if (taskList.get(i).getTaskId().equals(task.getTaskId())) {
                            index = i;
                            break;
                        }
                    }
                    if (index != -1) {
                        taskList.remove(index);
                        taskAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Ignored
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // An error occurred while loading tasks from Firebase
            }
        });
    }

    private void showTaskDialog(Date selectedDate) {
        TaskDialogFragment taskDialogFragment = TaskDialogFragment.newInstance(selectedDate);
        taskDialogFragment.show(getSupportFragmentManager(), "TaskDialog");
    }

    // Handle back button click event
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
