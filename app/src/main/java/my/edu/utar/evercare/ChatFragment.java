package my.edu.utar.evercare;

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

public class ChatFragment extends Fragment {

    private String selectedUserId;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private ChatManager chatManager;
    private String currentUserId;
    private EditText editTextMessage;
    private ImageView buttonSend;

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
        recyclerView.setLayoutManager(layoutManager);

        Query chatMessagesQuery = chatManager.getChatMessagesQuery();
        FirestoreRecyclerOptions<ChatMessage> options = new FirestoreRecyclerOptions.Builder<ChatMessage>()
                .setQuery(chatMessagesQuery, ChatMessage.class)
                .build();

        chatAdapter = new ChatAdapter(options);
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

        return view;
    }

    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {
            chatManager.sendMessage(messageText);
            editTextMessage.setText("");
        } else {
            Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
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
