package my.edu.utar.evercare;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class ChatFragment extends Fragment {

    private String selectedUserId;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private ChatManager chatManager;
    private String currentUserId;
    private EditText editTextMessage;
    private ImageView buttonSend;
    private ImageView buttonUpload;
    private ImageView imageViewSelectedImage; // Add this line
    private Uri selectedImageUri; // Add this line

    // Request code for media upload
    private static final int MEDIA_UPLOAD_REQUEST_CODE = 123;

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

        // Receive selected user ID from arguments
        Bundle args = getArguments();
        if (args != null) {
            selectedUserId = args.getString("selectedUserId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Initialize views
        recyclerView = view.findViewById(R.id.chatRecyclerView);
        imageViewSelectedImage = view.findViewById(R.id.imageViewSelectedImage); // Add this line

        // Initialize Firebase Firestore and current user ID
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }

        // Initialize ChatManager with selected user ID
        chatManager = new ChatManager(firestore, currentUserId, selectedUserId);

        // Set up the RecyclerView and ChatAdapter
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Display messages from the bottom
        recyclerView.setLayoutManager(layoutManager);

        Query chatMessagesQuery = chatManager.getChatMessagesQuery();
        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(chatMessagesQuery, ChatMessage.class)
                .build();

        chatAdapter = new ChatAdapter(options, currentUserId);
        recyclerView.setAdapter(chatAdapter);

        // Inside your ChatFragment's onCreateView method
        editTextMessage = view.findViewById(R.id.editTextMessage);
        buttonSend = view.findViewById(R.id.buttonSend);

        editTextMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) && (event.getAction() == KeyEvent.ACTION_DOWN)) {
                    sendMessage();
                    return true; // Return true to indicate that the event is handled
                }
                return false;
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // Find the buttonUpload ImageView
        buttonUpload = view.findViewById(R.id.buttonUpload);

        // Set a click listener
        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the upload button click
                openMediaPicker(); // You can replace this with your desired logic
            }
        });

        return view;
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();

        if (!messageText.isEmpty()) {
            if (selectedImageUri != null) {
                // Send both text and image
                chatManager.sendMessageWithImage(messageText, selectedImageUri);
            } else {
                // Send only text
                chatManager.sendMessage(messageText);
            }

            // Clear the input fields
            editTextMessage.setText("");
            imageViewSelectedImage.setVisibility(View.GONE); // Hide the image view

        } else if (selectedImageUri != null) {
            // If there's no text, but there's an image, send the image
            chatManager.sendMessageWithImage("", selectedImageUri);
            imageViewSelectedImage.setVisibility(View.GONE); // Hide the image view

        } else {
            Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
        }
    }


    // Method to handle the upload action (you can replace this with your desired logic)
    private void openMediaPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // You can also use "video/*" for videos
        mediaUploadLauncher.launch(intent);
    }

    // Handle the result of the media upload
    private void handleMediaUploadResult(Intent data) {
        if (data != null) {
            // Get the selected file URI
            Uri selectedFileUri = data.getData();

            // Update imageViewSelectedImage with the selected image
            if (selectedFileUri != null) {
                // Set the selected image URI to the class variable
                selectedImageUri = selectedFileUri;

                // Show the selected image in imageViewSelectedImage
                imageViewSelectedImage.setImageURI(selectedImageUri);

                // Optionally, make the imageViewSelectedImage visible
                imageViewSelectedImage.setVisibility(View.VISIBLE);

                // You can also handle other logic related to the selected image here
            }
        }
    }


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
