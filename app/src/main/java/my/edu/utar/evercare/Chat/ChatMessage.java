package my.edu.utar.evercare.Chat;

public class ChatMessage {
    private String senderID;
    private String text;
    private long timestamp;
    private String imageUrl;

    public ChatMessage() {
        // Default constructor required for Firestore
    }

    public ChatMessage(String senderID, String text, long timestamp) {
        this.senderID = senderID;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getSenderID() {
        return senderID;
    }

    public void setSenderID(String senderID) {
        this.senderID = senderID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
