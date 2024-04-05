package my.edu.utar.evercare.Chat;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import android.widget.ProgressBar;


import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import my.edu.utar.evercare.R;

public class ChatFragment extends Fragment {

    private static final int MEDIA_UPLOAD_REQUEST_CODE = 123;
    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private static final String TAG = "ChatFragment";

    private String selectedUserId;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private ChatManager chatManager;
    private String currentUserId;
    private EditText editTextMessage;
    private ImageView buttonSend, buttonUpload, imageViewSelectedImage, buttonDeleteImage, buttonVoice;
    private Uri selectedImageUri;
    private View rootView;
    private final ActivityResultLauncher<Intent> mediaUploadLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    handleMediaUploadResult(data);
                }
            }
    );

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            selectedUserId = args.getString("selectedUserId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        recyclerView = rootView.findViewById(R.id.chatRecyclerView);
        imageViewSelectedImage = rootView.findViewById(R.id.imageViewSelectedImage);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }

        chatManager = new ChatManager(firestore, currentUserId, selectedUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        Query chatMessagesQuery = chatManager.getChatMessagesQuery();
        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(chatMessagesQuery, ChatMessage.class)
                .build();

        chatAdapter = new ChatAdapter(options, currentUserId);
        recyclerView.setAdapter(chatAdapter);

        editTextMessage = rootView.findViewById(R.id.editTextMessage);
        buttonSend = rootView.findViewById(R.id.buttonSend);
        buttonUpload = rootView.findViewById(R.id.buttonUpload);
        buttonDeleteImage = rootView.findViewById(R.id.buttonDeleteImage);
        buttonVoice = rootView.findViewById(R.id.buttonVoice);

        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendMessage();
                return true;
            }
            return false;
        });

        buttonSend.setOnClickListener(v -> sendMessage());

        buttonUpload.setOnClickListener(v -> openMediaPicker());

        buttonDeleteImage.setOnClickListener(v -> deleteSelectedImage());

        buttonVoice.setOnClickListener(v -> startVoiceRecognition());

        return rootView;
    }

    private void deleteSelectedImage() {
        selectedImageUri = null;
        imageViewSelectedImage.setImageURI(null);
        imageViewSelectedImage.setVisibility(View.GONE);
        buttonDeleteImage.setVisibility(View.GONE);
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();

        // If the both text and image not null
        if (!messageText.isEmpty()) {
            if (selectedImageUri != null) {
                chatManager.sendMessageWithImage(messageText, selectedImageUri);
            } else {
                chatManager.sendMessage(messageText);
            }
            editTextMessage.setText("");
            clearSelectedImage();
        }
        // If the text is null but image not null
        else if (selectedImageUri != null) {
            chatManager.sendMessageWithImage("", selectedImageUri);
            clearSelectedImage();
        } else {
            Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to clear the selected image
    private void clearSelectedImage() {
        selectedImageUri = null;
        imageViewSelectedImage.setImageDrawable(null); // Clear the image
        imageViewSelectedImage.setVisibility(View.GONE);
        buttonDeleteImage.setVisibility(View.GONE);
    }

    private void openMediaPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        mediaUploadLauncher.launch(intent);
    }

    // Handle the result of the media upload
    private void handleMediaUploadResult(Intent data) {
        if (data != null) {
            // Get the selected file URI
            Uri selectedFileUri = data.getData();

            // Check if the selected file URI is not null
            if (selectedFileUri != null) {

                selectedImageUri = selectedFileUri;

                // Show the selected image using Glide
                Glide.with(this)
                        .load(selectedImageUri) // Load the image URI
                        .into(imageViewSelectedImage); // Set the ImageView

                // Optionally, make the imageViewSelectedImage visible
                imageViewSelectedImage.setVisibility(View.VISIBLE);
                buttonDeleteImage.setVisibility(View.VISIBLE);

                // Upload the selected image to Firebase Storage
                uploadImageToStorage(selectedImageUri);
            }
        }
    }


    private void uploadImageToStorage(Uri imageUri) {
        String imageId = UUID.randomUUID().toString();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + imageId);
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            selectedImageUri = Uri.parse(imageUrl);

                            // Set the imageViewSelectedImage URI
                            imageViewSelectedImage.setImageURI(selectedImageUri);

                            // Load the image using Glide with the selectedFileUri
                            Glide.with(requireContext())
                                    .load(imageUri)
                                    .into(imageViewSelectedImage);

                            imageViewSelectedImage.setVisibility(View.VISIBLE);
                            buttonDeleteImage.setVisibility(View.VISIBLE);
                        })
                        .addOnFailureListener(e -> Log.e(TAG, "Failed to get image download URL: " + e.getMessage())))
                .addOnFailureListener(e -> Log.e(TAG, "Image upload failed: " + e.getMessage()));
    }


    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            voiceRecognitionLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "Speech recognition not supported on this device", Toast.LENGTH_SHORT).show();
        }
    }

    private final ActivityResultLauncher<Intent> voiceRecognitionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<String> speechResults = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (speechResults != null && !speechResults.isEmpty()) {
                        editTextMessage.setText(speechResults.get(0));
                    }
                } else {
                    Toast.makeText(getContext(), "Speech recognition cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    public void onStart() {
        super.onStart();
        chatAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        chatAdapter.stopListening();
    }
}