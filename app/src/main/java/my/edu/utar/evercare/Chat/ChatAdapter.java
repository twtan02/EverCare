package my.edu.utar.evercare.Chat;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import my.edu.utar.evercare.R;

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
                // Load the image using Glide
                Glide.with(holder.itemView).load(model.getImageUrl()).into(holder.imageView);

                // Set OnClickListener for the image
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Display enlarged image in dialog
                        showImageDialog(holder.itemView.getContext(), model.getImageUrl());
                    }
                });
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
                // Load the image using Glide
                Glide.with(holder.itemView).load(model.getImageUrl()).into(holder.imageView);

                // Set OnClickListener for the image
                holder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Display enlarged image in dialog
                        showImageDialog(holder.itemView.getContext(), model.getImageUrl());
                    }
                });
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

    private void showImageDialog(Context context, String imageUrl) {
        Log.d("ImageDialog", "Image URL: " + imageUrl);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_image_viewer, null);
        ImageView dialogImageView = dialogView.findViewById(R.id.imageViewDialog);

        // Load the image using Glide
        Glide.with(context)
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("ImageDialog", "Failed to load image: " + e.getMessage());
                        return false; // Return false to allow Glide to call the onLoadFailed listener of any other RequestListener.
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d("ImageDialog", "Image loaded successfully");
                        return false; // Return false to allow Glide to call the onResourceReady listener of any other RequestListener.
                    }
                })
                .into(dialogImageView);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Set OnClickListener to dismiss the dialog when the background is clicked
        dialogView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


}
