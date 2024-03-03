package my.edu.utar.evercare.User;

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

import my.edu.utar.evercare.R;

public class StaffUserAdapter extends FirestoreRecyclerAdapter<StaffUser, StaffUserAdapter.ViewHolder> {

    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(StaffUser staffUser);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public StaffUserAdapter(@NonNull FirestoreRecyclerOptions<StaffUser> options) {
        super(options);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff_user, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull StaffUser staffUser) {
        holder.bind(staffUser);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView;
        TextView usernameTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profile_pic_imageview);
            usernameTextView = itemView.findViewById(R.id.username_textview);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        void bind(StaffUser staffUser) {
            usernameTextView.setText(staffUser.getUsername());

            if (!staffUser.getProfileImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(staffUser.getProfileImageUrl())
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
