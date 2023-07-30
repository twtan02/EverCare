package my.edu.utar.evercare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class ElderlyUserAdapter extends BaseAdapter {

    private Context context;
    private List<ElderlyUser> elderlyUsers;

    public ElderlyUserAdapter(Context context, List<ElderlyUser> elderlyUsers) {
        this.context = context;
        this.elderlyUsers = elderlyUsers;
    }

    @Override
    public int getCount() {
        return elderlyUsers.size();
    }

    @Override
    public Object getItem(int position) {
        return elderlyUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_elderly_user, parent, false);
        }

        TextView usernameTextView = convertView.findViewById(R.id.usernameTextView);
        TextView ageTextView = convertView.findViewById(R.id.ageTextView);
        TextView genderTextView = convertView.findViewById(R.id.genderTextView);

        ElderlyUser elderlyUser = elderlyUsers.get(position);

        usernameTextView.setText(elderlyUser.getUsername());
        ageTextView.setText("Age: " + elderlyUser.getAge());

        return convertView;
    }
}
