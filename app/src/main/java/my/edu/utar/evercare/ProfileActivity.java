package my.edu.utar.evercare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView textViewName, textViewEmail, textViewAge;

    private ActivityResultLauncher<String> imageChooserLauncher;
    private FloatingActionButton selectImageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set the custom title text
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("User Profile");

        // Enable the back button on the toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find views by their IDs
        profileImageView = findViewById(R.id.profileImageView);
        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewAge = findViewById(R.id.textViewAge);

        // Initialize the ActivityResultLauncher for image selection
        imageChooserLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result != null) {
                            uploadProfilePic(result);
                        }
                    }
                });

        // Find the selectImageButton in the layout
        selectImageButton = findViewById(R.id.selectImageButton);
        selectImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageChooser();
            }
        });

        // Get the current logged-in user from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if the user is logged in
        if (currentUser != null) {
            // Get the user's unique ID
            String userId = currentUser.getUid();

            // Access the Firestore database to get the user details from the appropriate collection
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("elderly_users").document(userId).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document != null && document.exists()) {
                                    // Handle fetching data for elderly user
                                    String name = document.getString("username");
                                    String email = document.getString("email");
                                    String dateOfBirth = document.getString("dateOfBirth");
                                    String profileImageUrl = document.getString("profileImageUrl");

                                    // Load the user's profile image using Glide
                                    Glide.with(ProfileActivity.this)
                                            .load(profileImageUrl)
                                            .placeholder(R.drawable.default_profile_image)
                                            .into(profileImageView);

                                    // Calculate the user's age from the date of birth
                                    int age = calculateAge(dateOfBirth);

                                    // Update the views with the user details
                                    textViewName.setText("Name: " + name);
                                    textViewEmail.setText("Email: " + email);
                                    textViewAge.setText("Age: " + age);
                                } else {
                                    // If data is not found in elderly_users collection,
                                    // try to fetch data from staff_users collection
                                    fetchStaffUserData(userId);
                                }
                            } else {
                                // Handle error if unable to fetch user details
                            }
                        }
                    });
        } else {
            // User is not logged in, handle this scenario
        }
    }

    private void fetchStaffUserData(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("staff_users").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Handle fetching data for staff user
                                String name = document.getString("username");
                                String email = document.getString("email");
                                String dateOfBirth = document.getString("dateOfBirth");
                                String profileImageUrl = document.getString("profileImageUrl");

                                // Load the user's profile image using Glide
                                Glide.with(ProfileActivity.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.default_profile_image)
                                        .into(profileImageView);

                                // Calculate the user's age from the date of birth
                                int age = calculateAge(dateOfBirth);

                                // Update the views with the user details
                                textViewName.setText("Name: " + name);
                                textViewEmail.setText("Email: " + email);
                                textViewAge.setText("Age: " + age);
                            } else {
                                // If data is not found in staff_users collection,
                                // try to fetch data from caregiver_users collection
                                fetchCaregiverUserData(userId);
                            }
                        } else {
                            // Handle error if unable to fetch user details
                        }
                    }
                });
    }

    private void fetchCaregiverUserData(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("caregiver_users").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                // Handle fetching data for caregiver user
                                String name = document.getString("username");
                                String email = document.getString("email");
                                String dateOfBirth = document.getString("dateOfBirth");
                                String profileImageUrl = document.getString("profileImageUrl");

                                // Load the user's profile image using Glide
                                Glide.with(ProfileActivity.this)
                                        .load(profileImageUrl)
                                        .placeholder(R.drawable.default_profile_image)
                                        .into(profileImageView);

                                // Calculate the user's age from the date of birth
                                int age = calculateAge(dateOfBirth);

                                // Update the views with the user details
                                textViewName.setText("Name: " + name);
                                textViewEmail.setText("Email: " + email);
                                textViewAge.setText("Age: " + age);
                            } else {
                                // User data not found in any collection, handle this scenario
                            }
                        } else {
                            // Handle error if unable to fetch user details
                        }
                    }
                });
    }

    // Method to calculate the age from the date of birth
    private int calculateAge(String dateOfBirth) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        try {
            Date birthDate = sdf.parse(dateOfBirth);
            Calendar dob = Calendar.getInstance();
            dob.setTime(birthDate);

            Calendar today = Calendar.getInstance();

            int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }

            return age;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void openImageChooser() {
        // Use the ActivityResultLauncher to launch the image chooser
        imageChooserLauncher.launch("image/*");
    }

    // Method to upload the selected profile picture to Firebase Storage
    private void uploadProfilePic(Uri imageUri) {
        // Get the current logged-in user from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, handle this scenario
            return;
        }

        // Get the user's unique ID
        String userId = currentUser.getUid();

        // Access the Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference profilePicRef = storageRef.child("profile_pics").child(userId + ".jpg");

        // Upload the image to Firebase Storage
        profilePicRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Image upload successful, get the download URL of the image
                        profilePicRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                // Save the download URL in Firestore to associate it with the user
                                String imageUrl = downloadUri.toString();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("elderly_users").document(userId)
                                        .update("profileImageUrl", imageUrl)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Profile picture URL saved in Firestore, update the profileImageView with the uploaded image
                                                Glide.with(ProfileActivity.this)
                                                        .load(imageUrl)
                                                        .placeholder(R.drawable.default_profile_image)
                                                        .into(profileImageView);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle error if unable to save profile picture URL in Firestore
                                            }
                                        });

                                // Update the profile picture URL for staff users if applicable
                                db.collection("staff_users").document(userId)
                                        .update("profileImageUrl", imageUrl)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle error if unable to save profile picture URL in Firestore for staff users
                                            }
                                        });

                                // Update the profile picture URL for caregiver users if applicable
                                db.collection("caregiver_users").document(userId)
                                        .update("profileImageUrl", imageUrl)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle error if unable to save profile picture URL in Firestore for caregiver users
                                            }
                                        });

                                // Update the profile picture URL for all users if applicable
                                db.collection("all_users").document(userId)
                                        .update("profileImageUrl", imageUrl)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle error if unable to save profile picture URL in Firestore for all users
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle error if image upload fails
                    }
                });
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
