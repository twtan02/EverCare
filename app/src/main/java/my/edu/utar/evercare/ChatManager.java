package my.edu.utar.evercare;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class ChatManager {
    private FirebaseFirestore firestore;
    private String currentUserId;
    private String selectedUserId;
    private CollectionReference chatsCollection;

    public ChatManager(FirebaseFirestore firestore, String currentUserId, String selectedUserId) {
        this.firestore = firestore;
        this.currentUserId = currentUserId;
        this.selectedUserId = selectedUserId;
        this.chatsCollection = firestore.collection("chats");
    }

    public Query getChatMessagesQuery() {
        return chatsCollection.document(currentUserId)
                .collection(selectedUserId)
                .orderBy("timestamp"); // Query chat messages sorted by timestamp
    }

    public void sendMessage(String messageText) {
        long timestamp = System.currentTimeMillis();

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("sender", currentUserId);
        messageMap.put("text", messageText);
        messageMap.put("timestamp", timestamp);

        // Add message only to the sender's collection
        chatsCollection.document(currentUserId)
                .collection(selectedUserId)
                .add(messageMap); // Add message to Firestore
    }

}
