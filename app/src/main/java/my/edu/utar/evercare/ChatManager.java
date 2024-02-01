package my.edu.utar.evercare;

import android.net.Uri;

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
        String chatDocumentId = generateChatDocumentId();
        return chatsCollection.document(chatDocumentId)
                .collection("messages")
                .orderBy("timestamp");
    }

    public void sendMessage(String messageText) {
        long timestamp = System.currentTimeMillis();
        Map<String, Object> messageMap = createMessageMap(messageText, timestamp);

        String chatDocumentId = generateChatDocumentId();

        // Add message to the chat document's "messages" collection for both users
        addMessageToCollection(chatDocumentId, messageMap);

        // Also, add the same message to the other user's collection
        String otherChatDocumentId = chatDocumentId.replace(currentUserId, selectedUserId);
        addMessageToCollection(otherChatDocumentId, messageMap);
    }

    public void sendMessageWithImage(String messageText, Uri imageUrl) {
        long timestamp = System.currentTimeMillis();
        Map<String, Object> messageMap = createMessageMap(messageText, timestamp);
        messageMap.put("imageUrl", imageUrl.toString()); // Add image URI to the message

        String chatDocumentId = generateChatDocumentId();

        // Add message to the chat document's "messages" collection for both users
        addMessageToCollection(chatDocumentId, messageMap);

        // Also, add the same message to the other user's collection
        String otherChatDocumentId = chatDocumentId.replace(currentUserId, selectedUserId);
        addMessageToCollection(otherChatDocumentId, messageMap);
    }

    private void addMessageToCollection(String chatDocumentId, Map<String, Object> messageMap) {
        chatsCollection.document(chatDocumentId)
                .collection("messages")
                .add(messageMap)
                .addOnSuccessListener(documentReference -> {
                    // Message added successfully
                })
                .addOnFailureListener(e -> {
                    // Handle any errors
                });
    }

    private Map<String, Object> createMessageMap(String messageText, long timestamp) {
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("senderID", currentUserId != null ? currentUserId : "");
        messageMap.put("text", messageText);
        messageMap.put("timestamp", timestamp);
        return messageMap;
    }

    private String generateChatDocumentId() {
        return currentUserId.compareTo(selectedUserId) < 0 ?
                currentUserId + "_" + selectedUserId :
                selectedUserId + "_" + currentUserId;
    }
}
