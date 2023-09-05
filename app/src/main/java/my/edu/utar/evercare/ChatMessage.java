package my.edu.utar.evercare;

public class ChatMessage {
    private String senderID;
    private String text;
    private long timestamp;

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
}
