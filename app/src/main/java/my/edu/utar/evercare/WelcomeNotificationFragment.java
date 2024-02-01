package my.edu.utar.evercare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class WelcomeNotificationFragment extends Fragment {

    public static WelcomeNotificationFragment newInstance(int pageNumber) {
        WelcomeNotificationFragment fragment = new WelcomeNotificationFragment();
        Bundle args = new Bundle();
        args.putInt("pageNumber", pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_welcome_notification, container, false);
    }
}
