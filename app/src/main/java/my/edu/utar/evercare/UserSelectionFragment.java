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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class UserSelectionFragment extends Fragment {

    private RecyclerView userRecyclerView;
    private ElderlyUserAdapter elderlyUserAdapter;
    private CaregiverUserAdapter caregiverUserAdapter;
    private StaffUserAdapter staffUserAdapter;
    private AllUserAdapter allUserAdapter;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_selection, container, false);

        // Initialize views
        userRecyclerView = view.findViewById(R.id.userRecyclerView);
        tabLayout = view.findViewById(R.id.tabLayout);

        // Set up the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        userRecyclerView.setLayoutManager(layoutManager);

        // Add tabs for All, Family, and Staff
        tabLayout.addTab(tabLayout.newTab().setText("All"));
        tabLayout.addTab(tabLayout.newTab().setText("Family"));
        tabLayout.addTab(tabLayout.newTab().setText("Staff"));

        // Default: Show all users
        loadUsers("all_users");

        // Set a listener to handle tab selection
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Handle tab selection, update RecyclerView data based on the selected category
                switch (tab.getPosition()) {
                    case 0:
                        // Show all users
                        loadUsers("all_users");
                        break;
                    case 1:
                        // Show family users
                        loadUsers("caregiver_users");
                        break;
                    case 2:
                        // Show staff users
                        loadUsers("staff_users");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Handle tab unselection if needed
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Handle tab reselection if needed
            }
        });

        return view;
    }


    private void loadUsers(String collectionReference) {
        // Assuming you have a way to retrieve the current user's ID
        String currentUserId = getCurrentUserId();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Query usersQuery = firestore.collection(collectionReference).whereNotEqualTo("userId", currentUserId);

        if (collectionReference.equals("all_users")) {
            if (allUserAdapter != null) {
                allUserAdapter.stopListening();
            }

            FirestoreRecyclerOptions<AllUser> options = new FirestoreRecyclerOptions.Builder<AllUser>()
                    .setQuery(usersQuery, AllUser.class)
                    .build();

            allUserAdapter = new AllUserAdapter(options);
            userRecyclerView.setAdapter(allUserAdapter);
            allUserAdapter.startListening();

            // Set up item click listener
            allUserAdapter.setOnItemClickListener(new AllUserAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(AllUser user) {
                    // Check if the clicked user is not the current user
                    if (!user.getUserId().equals(currentUserId)) {
                        navigateToChatFragment(user.getUserId());
                    }
                }
            });
        } else if (collectionReference.equals("caregiver_users")) {
            if (caregiverUserAdapter != null) {
                caregiverUserAdapter.stopListening();
            }

            FirestoreRecyclerOptions<CaregiverUser> options = new FirestoreRecyclerOptions.Builder<CaregiverUser>()
                    .setQuery(usersQuery, CaregiverUser.class)
                    .build();

            caregiverUserAdapter = new CaregiverUserAdapter(options);
            userRecyclerView.setAdapter(caregiverUserAdapter);
            caregiverUserAdapter.startListening();

            // Set up item click listener
            caregiverUserAdapter.setOnItemClickListener(new CaregiverUserAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(CaregiverUser user) {
                    navigateToChatFragment(user.getUserId()); // Pass the user's ID to the navigation method
                }
            });

        } else if (collectionReference.equals("staff_users")) {
            if (staffUserAdapter != null) {
                staffUserAdapter.stopListening();
            }

            FirestoreRecyclerOptions<StaffUser> options = new FirestoreRecyclerOptions.Builder<StaffUser>()
                    .setQuery(usersQuery, StaffUser.class)
                    .build();

            staffUserAdapter = new StaffUserAdapter(options);
            userRecyclerView.setAdapter(staffUserAdapter);
            staffUserAdapter.startListening();

            // Set up item click listener
            staffUserAdapter.setOnItemClickListener(new StaffUserAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(StaffUser user) {
                    navigateToChatFragment(user.getUserId()); // Pass the user's ID to the navigation method
                }
            });
        }
    }

    private String getCurrentUserId() {
        // Assuming you are using Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is authenticated
        if (currentUser != null) {
            // The user is signed in, return their UID
            return currentUser.getUid();
        } else {
            // No user is signed in, handle accordingly (e.g., redirect to login)
            // Return an empty string or throw an exception based on your app's logic
            return "";
        }
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
        if (elderlyUserAdapter != null) {
            elderlyUserAdapter.startListening();
        }
        if (caregiverUserAdapter != null) {
            caregiverUserAdapter.startListening();
        }
        if (staffUserAdapter != null) {
            staffUserAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (elderlyUserAdapter != null) {
            elderlyUserAdapter.stopListening();
        }
        if (caregiverUserAdapter != null) {
            caregiverUserAdapter.stopListening();
        }
        if (staffUserAdapter != null) {
            staffUserAdapter.stopListening();
        }
    }
}
