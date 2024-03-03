package my.edu.utar.evercare.RemoteMonitoring;

// RemoteMonitoringActivity.java
import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.Arrays;

import my.edu.utar.evercare.R;

public class RemoteMonitoringActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final String TAG = "RemoteMonitoringActivity";

    private boolean isCameraDevice = false; // Flag to indicate if the device is acting as the camera

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_monitoring);

        surfaceView = findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(this);

        ImageButton cameraButton = findViewById(R.id.cameraImageButton);
        ImageButton viewerButton = findViewById(R.id.viewerImageButton);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if the camera permission is granted
                if (ContextCompat.checkSelfPermission(RemoteMonitoringActivity.this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    // Open the back camera
                    openCamera(CameraCharacteristics.LENS_FACING_BACK);
                } else {
                    // Request camera permission
                    ActivityCompat.requestPermissions(RemoteMonitoringActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION);
                }
            }
        });

        viewerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isCameraDevice = false;
                // Additional functionality for the viewer device if needed
                retrieveLiveVideoStream();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera(CameraCharacteristics.LENS_FACING_BACK);
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

    private void openCamera(int cameraFacingDirection) {
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
                            // Store the live video stream in Firebase Storage
                            storeLiveVideoStream();
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

    private void storeLiveVideoStream() {
        // Assuming generateVideoUri() method generates the URI of the recorded video
        Uri videoUri = generateVideoUri();
        Log.d(TAG, "Generated video URI: " + videoUri);


        if (videoUri != null) {
            StorageReference videoRef = FirebaseStorage.getInstance().getReference().child("videos").child("video1.mp4");
            videoRef.putFile(videoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Video upload successful
                        Toast.makeText(RemoteMonitoringActivity.this, "Live video stream uploaded successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Handle any errors
                        Toast.makeText(RemoteMonitoringActivity.this, "Failed to upload live video stream: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Handle the case where videoUri is null
            Toast.makeText(RemoteMonitoringActivity.this, "Failed to generate video URI", Toast.LENGTH_SHORT).show();
        }
    }


    // This method should generate the URI of the recorded video
    private Uri generateVideoUri() {
        // Replace this with your logic to generate the URI automatically
        // For example:
        String videoFileName = "video_" + System.currentTimeMillis() + ".mp4";
        File videoFile = new File(getFilesDir(), videoFileName);

        // Ensure that the directory exists
        if (!videoFile.getParentFile().exists()) {
            videoFile.getParentFile().mkdirs();
        }

        return FileProvider.getUriForFile(this, getPackageName() + ".provider", videoFile);
    }




    private void retrieveLiveVideoStream() {
        // Add code here to retrieve the live video stream from Firebase Storage
        // For example:
         StorageReference videoRef = FirebaseStorage.getInstance().getReference().child("videos").child("video1.mp4");
         videoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
             @Override
             public void onSuccess(Uri uri) {
                 String videoUrl = uri.toString();
                 // Now you can use the videoUrl to play the live video stream
             }
         }).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception exception) {
                 // Handle any errors
             }
         });
    }

}
