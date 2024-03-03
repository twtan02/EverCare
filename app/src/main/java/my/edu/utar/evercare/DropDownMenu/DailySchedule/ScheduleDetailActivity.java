package my.edu.utar.evercare.DropDownMenu.DailySchedule;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import my.edu.utar.evercare.R;

public class ScheduleDetailActivity extends AppCompatActivity implements TaskAdapter.OnTaskItemClickListener {

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
        taskAdapter = new TaskAdapter(taskList, this);
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
                long selectedDateTimestamp = getIntent().getLongExtra("selected_date", 0);
                if (selectedDateTimestamp != 0) {
                    Date selectedDate = new Date(selectedDateTimestamp);
                    showTaskDialog(selectedDate);
                } else {
                    // Handle the case where "selected_date" is not present in Intent extras
                    Log.e("ScheduleDetailActivity", "selected_date not found in Intent extras");
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

    // Implement interface methods for handling modify and delete button clicks
    private void showModifyTaskDialog(Task task) {
        // Create a custom dialog for modifying the task

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_modify_task, null);
        builder.setView(view);

        EditText editTextModifiedTaskTitle = view.findViewById(R.id.editTextModifiedTaskTitle);
        EditText editTextModifiedTaskDescription = view.findViewById(R.id.editTextModifiedTaskDescription);

        // Set initial values for the task
        editTextModifiedTaskTitle.setText(task.getTaskTitle());
        editTextModifiedTaskDescription.setText(task.getTaskDescription());

        builder.setTitle("Modify Task")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the modified task details
                        String modifiedTaskTitle = editTextModifiedTaskTitle.getText().toString().trim();
                        String modifiedTaskDescription = editTextModifiedTaskDescription.getText().toString().trim();

                        // Update the task in Firebase
                        updateTask(task, modifiedTaskTitle, modifiedTaskDescription);

                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onModifyTaskClick(Task task) {
        // Handle modify task click
        showModifyTaskDialog(task);
    }

    // Method to update the task in Firebase
    private void updateTask(Task task, String modifiedTaskTitle, String modifiedTaskDescription) {
        if (task != null) {
            // Get a reference to your Firebase Realtime Database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference tasksRef = database.getReference("tasks");

            // Update the task in Firebase
            task.setTaskTitle(modifiedTaskTitle);
            task.setTaskDescription(modifiedTaskDescription);

            tasksRef.child(task.getTaskId()).setValue(task)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Task updated successfully
                            if (!isFinishing()) {
                                Log.d("TaskLog", "Task updated in Firebase");
                                Toast.makeText(ScheduleDetailActivity.this, "Task updated in Firebase", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to update the task
                            if (!isFinishing()) {
                                Log.e("TaskLog", "Failed to update task: " + e.getMessage());
                                Toast.makeText(ScheduleDetailActivity.this, "Failed to update task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    @Override
    public void onDeleteTaskClick(Task task) {
        // Handle delete task click
        // You can show a confirmation dialog and delete the task if confirmed
        showDeleteTaskConfirmationDialog(task);
    }

    private void showDeleteTaskConfirmationDialog(Task task) {
        // Implement the dialog for confirming task deletion
        // You can use AlertDialog or create a custom dialog
        // Delete the task if the user confirms

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Task");
        builder.setMessage("Are you sure you want to delete this task?");

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the Delete button
                deleteTask(task);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User clicked the Cancel button
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Inside deleteTask method
    private void deleteTask(Task task) {
        if (task != null && !isFinishing()) {
            // Get a reference to your Firebase Realtime Database
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference tasksRef = database.getReference("tasks");

            // Delete the task from Firebase
            tasksRef.child(task.getTaskId()).removeValue()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Task deleted successfully
                            if (!isFinishing()) {
                                Log.d("TaskLog", "Task deleted from Firebase");
                                Toast.makeText(ScheduleDetailActivity.this, "Task deleted from Firebase", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to delete the task
                            if (!isFinishing()) {
                                Log.e("TaskLog", "Failed to delete task: " + e.getMessage());
                                Toast.makeText(ScheduleDetailActivity.this, "Failed to delete task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


}
