package my.edu.utar.evercare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ChatAdapter extends FirestoreRecyclerAdapter<ChatMessage, ChatAdapter.ChatViewHolder> {

    public ChatAdapter(@NonNull FirestoreRecyclerOptions<ChatMessage> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatViewHolder holder, int position, @NonNull ChatMessage model) {
        holder.bind(model);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        public void bind(ChatMessage message) {
            messageTextView.setText(message.getText());
        }
    }
}
