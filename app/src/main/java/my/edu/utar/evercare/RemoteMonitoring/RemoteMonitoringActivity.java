package my.edu.utar.evercare.RemoteMonitoring;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import my.edu.utar.evercare.R;

public class RemoteMonitoringActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private String videoFileName;
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final String TAG = "RemoteMonitoringActivity";
    private FirebaseFirestore db;
    private Spinner elderlySpinner;
    private Map<String, String> elderlyUserIds = new HashMap<>();
    private boolean isCameraDevice = false; // Flag to indicate if the device is acting as the camera
    private boolean mediaPlayerPrepared = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_monitoring);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_toolbar_title);

        TextView customTitleTextView = findViewById(R.id.customToolbarTitle);
        customTitleTextView.setText("Remote Monitoring");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize the spinner
        elderlySpinner = findViewById(R.id.elderlySpinner);

        // Retrieve usernames from Firestore and populate the spinner
        retrieveUsernames();

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);

        ImageButton cameraButton = findViewById(R.id.cameraImageButton);
        ImageButton viewerButton = findViewById(R.id.viewerImageButton);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        // Inside the OnClickListener for the cameraButton
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(RemoteMonitoringActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    String selectedUsername = (String) elderlySpinner.getSelectedItem();
                    if (selectedUsername != null) {
                        String selectedElderlyId = elderlyUserIds.get(selectedUsername);
                        if (selectedElderlyId != null) {
                            openCamera(CameraCharacteristics.LENS_FACING_BACK, selectedElderlyId);
                        } else {
                            Toast.makeText(RemoteMonitoringActivity.this, "Failed to retrieve user ID for selected elderly", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(RemoteMonitoringActivity.this, "Please select an elderly person", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    ActivityCompat.requestPermissions(RemoteMonitoringActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION);
                }
            }
        });

        viewerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedUsername = (String) elderlySpinner.getSelectedItem();
                Log.d(TAG, "SelectedUserName: " + selectedUsername);
                if (selectedUsername != null) {
                    String selectedUserId = elderlyUserIds.get(selectedUsername);
                    Log.d(TAG, "SelectedUserId: " + selectedUserId);
                    if (selectedUserId != null) {
                        retrieveLiveVideoStream(selectedUserId);
                    } else {
                        Log.e(TAG, "Failed to retrieve user ID");
                    }
                } else {
                    Toast.makeText(RemoteMonitoringActivity.this, "Please select an elderly person", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void retrieveUsernames() {
        db.collection("elderly_users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String username = document.getString("username");
                                String userId = document.getId();
                                if (username != null && userId != null) {
                                    elderlyUserIds.put(username, userId);
                                }
                            }
                            List<String> usernames = new ArrayList<>(elderlyUserIds.keySet());
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(RemoteMonitoringActivity.this,
                                    android.R.layout.simple_spinner_item, usernames);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            elderlySpinner.setAdapter(adapter);
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                            Toast.makeText(RemoteMonitoringActivity.this,
                                    "Error retrieving usernames from Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Assuming the back camera is used by default
                openCamera(CameraCharacteristics.LENS_FACING_BACK, null);
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        // Surface created, handled in openCamera() after permission check
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // No need to do anything here
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private void openCamera(int cameraFacingDirection, String selectedElderlyId) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Handle the case where permission is not granted
            return;
        }

        try {
            String[] cameraIds = cameraManager.getCameraIdList();
            for (String cameraId : cameraIds) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == cameraFacingDirection) {
                    cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(@NonNull CameraDevice camera) {
                            cameraDevice = camera;
                            createCameraPreview();
                            // Store the live video stream with the selected elderly ID
                            if (videoFileName == null) {
                                storeLiveVideoStream(selectedElderlyId);
                            }
                        }

                        @Override
                        public void onDisconnected(@NonNull CameraDevice camera) {
                            camera.close();
                            cameraDevice = null;
                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera, int error) {
                            camera.close();
                            cameraDevice = null;
                        }
                    }, null);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void createCameraPreview() {
        try {
            Surface surface = surfaceView.getHolder().getSurface();
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            if (cameraDevice == null) {
                                return;
                            }

                            cameraCaptureSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Toast.makeText(RemoteMonitoringActivity.this,
                                    "Failed to configure camera preview.", Toast.LENGTH_SHORT).show();
                        }
                    }, null);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePreview() {
        if (cameraDevice == null) {
            return;
        }

        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        try {
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
        } catch (Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void storeLiveVideoStream(String userId) {
        // Ensure the video file name is not null
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Invalid user ID");
            return;
        }

        // Generate the video file name with the format "video_userid.mp4"
        String videoFileName = "video_" + userId + ".mp4";

        // Store the video in Firebase Storage within the "videos" directory
        StorageReference videoRef = FirebaseStorage.getInstance().getReference().child("videos").child(videoFileName);

        // Set the content type to video/mp4
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("video/mp4")
                .build();

        // Get the URI for the video file using FileProvider
        Uri videoUri = Uri.fromFile(new File(getFilesDir(), videoFileName));

        // Upload the video file
        videoRef.putFile(videoUri, metadata)
                .addOnSuccessListener(taskSnapshot -> {
                    // Video upload successful
                    Toast.makeText(RemoteMonitoringActivity.this, "Live video stream uploaded successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                    Toast.makeText(RemoteMonitoringActivity.this, "Failed to upload live video stream: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void retrieveLiveVideoStream(String userId) {
        // Check if userId is not null and not empty before proceeding
        if (userId != null && !userId.isEmpty()) {
            // Get a reference to the uploaded video using the user's ID
            StorageReference videoRef = FirebaseStorage.getInstance().getReference().child("videos").child("video_" + userId + ".mp4");

            // Get the download URL for the video
            videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String videoUrl = uri.toString();
                    // Now you can use the videoUrl to play the live video stream
                    // For example, you can pass this URL to a media player to play the video
                    Log.d(TAG, "VideoUrl:" + videoUrl);
                    playVideo(videoUrl);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.e(TAG, "Failed to retrieve live video stream: " + exception.getMessage());
                }
            });
        } else {
            Log.e(TAG, "User ID is null or empty");
        }
    }

    private void playVideo(String videoUrl) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(videoUrl);
            mediaPlayer.setSurface(surfaceView.getHolder().getSurface());
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // Start playing the video
                    mp.start();
                }
            });
            mediaPlayer.prepareAsync(); // Prepare the media player asynchronously
        } catch (IOException e) {
            Log.e(TAG, "Error setting data source for video: " + e.getMessage());
            // Handle error
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}