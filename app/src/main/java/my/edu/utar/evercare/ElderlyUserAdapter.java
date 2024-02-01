package my.edu.utar.evercare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class ElderlyUserAdapter extends FirestoreRecyclerAdapter<ElderlyUser, ElderlyUserAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(ElderlyUser elderlyUser); // Modify the listener to pass the clicked user
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ElderlyUserAdapter(@NonNull FirestoreRecyclerOptions<ElderlyUser> options) {
        super(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_elderly_user, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull ElderlyUser elderlyUser) {
        holder.bind(elderlyUser);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView usernameTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_pic_imageview);
            usernameTextView = itemView.findViewById(R.id.username_textview);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getBindingAdapterPosition(); // Use getBindingAdapterPosition() or getAbsoluteAdapterPosition()
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getItem(position)); // Pass the clicked user object
                    }
                }
            });
        }

        void bind(ElderlyUser elderlyUser) {
            usernameTextView.setText(elderlyUser.getUsername());

            if (!elderlyUser.getProfileImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(elderlyUser.getProfileImageUrl())
                        .placeholder(R.drawable.default_profile_image)
                        .error(R.drawable.default_failure_profile)
                        .transform(new CircleCrop())
                        .into(profileImageView);
            } else {
                profileImageView.setImageResource(R.drawable.default_profile_image);
            }
        }
    }
}

