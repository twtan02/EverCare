package my.edu.utar.evercare;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UserSelectionFragment extends Fragment {

    private RecyclerView userRecyclerView;
    private ElderlyUserAdapter userAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_selection, container, false);

        // Initialize views
        userRecyclerView = view.findViewById(R.id.userRecyclerView);

        // Initialize Firebase Firestore
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Set up the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        userRecyclerView.setLayoutManager(layoutManager);

        // Load and display users
        Query usersQuery = firestore.collection("elderly_users");
        FirestoreRecyclerOptions<ElderlyUser> options = new FirestoreRecyclerOptions.Builder<ElderlyUser>()
                .setQuery(usersQuery, ElderlyUser.class)
                .build();

        userAdapter = new ElderlyUserAdapter(options);
        userRecyclerView.setAdapter(userAdapter);

        // Set up item click listener
        userAdapter.setOnItemClickListener(new ElderlyUserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ElderlyUser user) {
                navigateToChatFragment(user.getUserId()); // Pass the user's ID to the navigation method
            }
        });

        // Set the title for the Fragment container
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle("Start Chatting with Your Friend");
            }
        }


        return view;
    }

    private void navigateToChatFragment(String selectedUserId) {
        // Create a new ChatFragment instance and pass the selected user's ID
        ChatFragment chatFragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("selectedUserId", selectedUserId);
        chatFragment.setArguments(args);

        // Navigate to the ChatFragment using FragmentTransaction
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        userAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        userAdapter.stopListening();
    }
}
