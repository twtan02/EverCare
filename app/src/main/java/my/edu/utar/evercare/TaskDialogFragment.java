package my.edu.utar.evercare;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDialogFragment extends DialogFragment {

    private static final String ARG_SELECTED_DATE = "selected_date";
    private Date selectedDate;

    public TaskDialogFragment() {
        // Required empty public constructor
    }

    public static TaskDialogFragment newInstance(Date selectedDate) {
        TaskDialogFragment fragment = new TaskDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SELECTED_DATE, selectedDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedDate = (Date) getArguments().getSerializable(ARG_SELECTED_DATE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create the custom dialog UI layout
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_task, null);

        // Find input fields in the custom layout
        EditText editTextTaskTitle = dialogView.findViewById(R.id.editTextTaskTitle);
        EditText editTextTaskDescription = dialogView.findViewById(R.id.editTextTaskDescription);

        // Create and configure the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setTitle("Schedule Task for " + formatDate(selectedDate))
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Get the user input from the input fields
                        String taskTitle = editTextTaskTitle.getText().toString().trim();
                        String taskDescription = editTextTaskDescription.getText().toString().trim();

                        // Save the task to Firebase
                        saveTaskToFirebase(taskTitle, taskDescription, selectedDate);

                        // Dismiss the dialog
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Dismiss the dialog if the user cancels
                        dialog.dismiss();
                    }
                });

        // Return the configured dialog
        return builder.create();
    }

    private void saveTaskToFirebase(String taskTitle, String taskDescription, Date selectedDate) {
        // Get a reference to your Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference tasksRef = database.getReference("tasks");

        // Generate a unique key for the new task
        String taskId = tasksRef.push().getKey();

        // Format the selected date as a string (you can use a different format if needed)
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String dateString = dateFormat.format(selectedDate);

        // Create a new Task object with the provided details
        Task task = new Task(taskId, taskTitle, taskDescription, dateString);

        // Save the task to Firebase
        tasksRef.child(taskId).setValue(task)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Task saved successfully
                        if (isAdded()) {
                            Log.d("TaskLog", "Task saved to Firebase");
                            Toast.makeText(requireContext(), "Task saved to Firebase", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to save the task
                        if (isAdded()) {
                            Log.e("TaskLog", "Failed to save task: " + e.getMessage());
                            Toast.makeText(requireContext(), "Failed to save task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Helper method to format the date for display in the dialog
    private String formatDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        return dateFormat.format(date);
    }
}
