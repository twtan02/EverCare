package my.edu.utar.evercare.EmergencyHelp;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import my.edu.utar.evercare.R;
import my.edu.utar.evercare.User.CaregiverUser;
import my.edu.utar.evercare.User.ElderlyUser;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final int FINE_PERMISSION_CODE = 1;
    private final int PHONE_CALL_PERMISSION_REQUEST_CODE = 2;
    private static final int REQUEST_PHONE_CALL = 1;
    private GoogleMap myMap;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ImageView backButton = findViewById(R.id.backButton);

        // Set OnClickListener to the back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle back button click event
                onBackPressed();
            }
        });

        // Set click listeners for emergency buttons
        Button alertButton = findViewById(R.id.alertButton);
        alertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    retrieveElderlyUserFromFirestore(userId);
                }
            }
        });

        Button emergencyCallButton = findViewById(R.id.emergencyCallButton);
        emergencyCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check and request phone call permission
                checkPhoneCallPermission();
            }
        });


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // Check and request location permission
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
        } else {
            // Location permission granted, proceed with getting last location
            getLastLocation();
        }
    }

    private void checkPhoneCallPermission() {
        // Check if phone call permission is granted
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Request phone call permission
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PHONE_CALL_PERMISSION_REQUEST_CODE);
        } else {
            // Permission granted, proceed with making the emergency call
            makeEmergencyCall();
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLocation = location;
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MapsActivity.this);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;

        // Log the latitude and longitude
        double latitude = currentLocation.getLatitude();
        double longitude = currentLocation.getLongitude();
        Log.d("MapsActivity", "Latitude: " + latitude + ", Longitude: " + longitude);

        LatLng currentLatLng = new LatLng(latitude, longitude);
        // Add marker at current location
        myMap.addMarker(new MarkerOptions().position(currentLatLng).title("My Location"));
        // Move camera to current location with appropriate zoom level
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15)); // Adjust zoom level as needed
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PHONE_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with making the emergency call
                makeEmergencyCall();
            } else {
                // Permission denied, display a message or handle it accordingly
                Toast.makeText(this, "Permission denied to make a phone call", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void retrieveElderlyUserFromFirestore(String userID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Example query to fetch the elderly user from Firestore
        db.collection("elderly_users")
                .document(userID)  // Replace "elderly_user_id" with the actual user ID
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Convert Firestore document to ElderlyUser object
                        ElderlyUser elderlyUser = documentSnapshot.toObject(ElderlyUser.class);
                        if (elderlyUser != null) {
                            String currentUsername = elderlyUser.getUsername();
                            Log.d(TAG, "Current Username: " + currentUsername);
                            sendAlert(currentUsername);
                        }
                    } else {
                        Log.d(TAG, "No such document");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error retrieving elderly user", e);
                    Toast.makeText(MapsActivity.this, "Error retrieving elderly user", Toast.LENGTH_SHORT).show();
                });
    }


    private void sendAlert(String currentElderlyParentUsername) {
        // Check if the current elderly parent username is not empty
        if (!TextUtils.isEmpty(currentElderlyParentUsername)) {
            // Get a reference to the Firestore database
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Query the caregiver_users collection to find the caregiver linked with the current elderly parent
            db.collection("caregiver_users")
                    .whereEqualTo("elderlyParentName", currentElderlyParentUsername)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                // Iterate through the results
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    // Get the caregiver's data
                                    String caregiverUserId = document.getString("userId");
                                    String caregiverEmail = document.getString("email");

                                    // Log caregiver information
                                    Log.d("CaregiverInfo", "Caregiver UserID: " + caregiverUserId + ", Caregiver Email: " + caregiverEmail);

                                    // Notify the caregiver (you can implement your own notification logic here)
                                    sendNotificationToCaregiver(caregiverUserId, caregiverEmail);
                                }
                            } else {
                                // Handle the case where no caregiver is found for the current elderly parent
                                Toast.makeText(MapsActivity.this, "No caregiver found for the current elderly parent", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Handle errors
                            Log.e("Firestore", "Error getting documents: ", task.getException());
                            Toast.makeText(MapsActivity.this, "Error getting caregiver data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Handle the case where the current elderly parent username is empty
            Toast.makeText(this, "Unable to retrieve current elderly parent username", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotificationToCaregiver(String caregiverUserId, String caregiverEmail) {
        // Send an email notification to the caregiver
        String subject = "Emergency Alert";
        String message = "Your elderly parent needs assistance. Please take action immediately.";
        sendEmailNotification(caregiverEmail, subject, message);
    }

    private void sendEmailNotification(String emailAddress, String subject, String message) {
        // Intent to send email
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + emailAddress));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Handle the case where no email app is available
            Toast.makeText(this, "No email app found to send notification", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeEmergencyCall() {
        // Check if the CALL_PHONE permission is granted
        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request permission
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.CALL_PHONE}, PHONE_CALL_PERMISSION_REQUEST_CODE);
        } else {
            // Permission granted, proceed with making the emergency call
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:999"));
            startActivity(intent);
        }
    }
}
