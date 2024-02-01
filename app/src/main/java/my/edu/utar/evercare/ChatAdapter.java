package my.edu.utar.evercare;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatMessage, ChatAdapter.MessageViewHolder> {
    private String currentUserId;
    private static final int OUTGOING_MESSAGE = 1;
    private static final int INCOMING_MESSAGE = 2;

    public ChatAdapter(FirestoreRecyclerOptions<ChatMessage> options, String currentUserId) {
        super(options);
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == OUTGOING_MESSAGE) ?
                R.layout.item_outgoing_message : R.layout.item_incoming_message;

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull ChatMessage model) {
        holder.setMessage(model.getText());

        // Check if the message sender is the current user and update the style if needed
        if (currentUserId != null && currentUserId.equals(model.getSenderID())) {
            // If the message sender is the current user, change the background style of the message content
            holder.messageText.setBackgroundResource(R.drawable.bg_message_outgoing);

            // Set the text alignment to right for outgoing messages
            holder.messageText.setGravity(Gravity.END); // or Gravity.RIGHT

            // Display selected image in the ImageView for outgoing messages
            if (model.getImageUrl() != null && !model.getImageUrl().isEmpty()) {
                holder.imageView.setVisibility(View.VISIBLE);
                // Load the image using your preferred image loading library or method
                // For simplicity, assuming you are using Glide
                 Glide.with(holder.itemView).load(model.getImageUrl()).into(holder.imageView);
            } else {
                holder.imageView.setVisibility(View.GONE);
            }
        } else {
            // Otherwise, use the default background style and left-aligned text
            holder.messageText.setBackgroundResource(R.drawable.bg_message_incoming);
            holder.messageText.setGravity(Gravity.START); // or Gravity.LEFT

            // Display selected image in the ImageView for incoming messages
            if (model.getImageUrl() != null && !model.getImageUrl().isEmpty()) {
                holder.imageView.setVisibility(View.VISIBLE);
                // Load the image using your preferred image loading library or method
                // For simplicity, assuming you are using Glide
                 Glide.with(holder.itemView).load(model.getImageUrl()).into(holder.imageView);
            } else {
                holder.imageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = getItem(position);
        if (message != null && currentUserId != null && currentUserId.equals(message.getSenderID())) {
            return OUTGOING_MESSAGE;
        }
        return INCOMING_MESSAGE;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;
        private ImageView imageView;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            imageView = itemView.findViewById(R.id.imageView); // Add this line to initialize imageView
        }

        void setMessage(String text) {
            messageText.setText(text);
        }
    }
}
