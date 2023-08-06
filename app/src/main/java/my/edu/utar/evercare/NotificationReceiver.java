package my.edu.utar.evercare;

// Import necessary packages

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String reminderTitle = intent.getStringExtra("reminderTitle");
        String reminderText = intent.getStringExtra("reminderText");
        int notificationId = intent.getIntExtra("notificationId", 0); // Retrieve the notification ID

        // Build the notification
        String channelId = "pill_reminder_channel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(reminderTitle)
                .setContentText(reminderText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Create an explicit intent for your PillReminderActivity
        Intent activityIntent = new Intent(context, PillReminderActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        // Show the notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());
    }
}

