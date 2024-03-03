package my.edu.utar.evercare.PillReminder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import my.edu.utar.evercare.R;

public class PillReminderFragment extends Fragment {

    public PillReminderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pill_reminder, container, false);

        // Initialize and configure UI elements or perform any other necessary operations

        return view;
    }
}
