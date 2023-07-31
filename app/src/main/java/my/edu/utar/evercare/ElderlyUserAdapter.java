package my.edu.utar.evercare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ElderlyUserAdapter extends ArrayAdapter<ElderlyUser> {

    private Context context;
    private List<ElderlyUser> elderlyUsers;

    public ElderlyUserAdapter(Context context, List<ElderlyUser> elderlyUsers) {
        super(context, 0, elderlyUsers);
        this.context = context;
        this.elderlyUsers = elderlyUsers;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_elderly_user, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.profileImageView = convertView.findViewById(R.id.profile_pic_imageview);
            viewHolder.usernameTextView = convertView.findViewById(R.id.username_textview);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Get the current ElderlyUser object
        ElderlyUser elderlyUser = elderlyUsers.get(position);

        // Set the username in the TextView
        viewHolder.usernameTextView.setText(elderlyUser.getUsername());

        // Load the profile picture using Glide
        if (!elderlyUser.getProfileImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(elderlyUser.getProfileImageUrl())
                    .placeholder(R.drawable.default_profile_image)
                    .error(R.drawable.default_failure_profile)
                    .into(viewHolder.profileImageView);
        } else {
            viewHolder.profileImageView.setImageResource(R.drawable.default_profile_image);
        }

        return convertView;
    }

    private static class ViewHolder {
        ImageView profileImageView;
        TextView usernameTextView;
    }
}
