package my.edu.utar.evercare.DropDownMenu.DailySchedule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import my.edu.utar.evercare.R;


public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskItemClickListener onTaskItemClickListener; // Interface for handling item clicks

    // Constructor that takes the interface as a parameter
    public TaskAdapter(List<Task> taskList, OnTaskItemClickListener onTaskItemClickListener) {
        this.taskList = taskList;
        this.onTaskItemClickListener = onTaskItemClickListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.textViewTaskTitle.setText(task.getTaskTitle());
        holder.textViewTaskDescription.setText(task.getTaskDescription());
        holder.textViewDate.setText(task.getDateString());

        // Set click listeners for modify and delete buttons
        holder.imageViewModifyTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the interface method when modify button is clicked
                onTaskItemClickListener.onModifyTaskClick(task);
            }
        });

        holder.imageViewDeleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call the interface method when delete button is clicked
                onTaskItemClickListener.onDeleteTaskClick(task);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewTaskTitle;
        public TextView textViewTaskDescription;
        public TextView textViewDate;
        public ImageView imageViewModifyTask;
        public ImageView imageViewDeleteTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTaskTitle = itemView.findViewById(R.id.textViewTaskTitle);
            textViewTaskDescription = itemView.findViewById(R.id.textViewTaskDescription);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            imageViewModifyTask = itemView.findViewById(R.id.imageViewModifyTask);
            imageViewDeleteTask = itemView.findViewById(R.id.imageViewDeleteTask);
        }
    }

    // Interface for handling button clicks
    public interface OnTaskItemClickListener {
        void onModifyTaskClick(Task task);
        void onDeleteTaskClick(Task task);
    }

}
