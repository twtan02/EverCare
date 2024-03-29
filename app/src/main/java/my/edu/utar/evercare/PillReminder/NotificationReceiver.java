package my.edu.utar.evercare.PillReminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import my.edu.utar.evercare.R;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String reminderTitle = intent.getStringExtra("reminderTitle");
        String reminderText = intent.getStringExtra("reminderText");
        int notificationId = intent.getIntExtra("notificationId", 0);

        // Create a sound Uri for the notification sound
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification);

        // Create a custom vibration pattern (example: long array of milliseconds)
        long[] vibrationPattern = {0, 500, 250, 500, 250, 500};

        // Create a notification channel
        NotificationChannel notificationChannel = new NotificationChannel("PillReminderChannel", "Pill Reminder Channel", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setVibrationPattern(vibrationPattern); // Set the custom vibration pattern
        notificationChannel.enableVibration(true);

        // Get the notification manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            // Create the notification channel
            notificationManager.createNotificationChannel(notificationChannel);

            // Add action to the notification intent to launch the app
            Intent launchIntent = new Intent(context, PillReminderActivity.class);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Build the notification with the action
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "PillReminderChannel")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(reminderTitle)
                    .setContentText(reminderText)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setSound(soundUri)
                    .setContentIntent(pendingIntent); // Set the pending intent to launch the app

            // Show the notification
            notificationManager.notify(notificationId, builder.build());
        }
    }
}
