package com.example.final_project;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// This class defines a background worker that checks user inactivity and sends a notification reminder if the app hasn't been used recently

public class NotificationWorker extends Worker {


    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    //  This method is called when the worker is executed.
    //  It checks if the app has been inactive for a specific duration (1 min)
    // and sends a notification if so.
    //  @return Result.success() indicates the task was completed successfully.

    @NonNull
    @Override
    public Result doWork() {
        Log.d("NotificationWorker", "Checking inactivity...");

        // Access shared preferences to get the last recorded open time
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        long lastOpenTime = prefs.getLong("last_open_time", 0);
        long currentTime = System.currentTimeMillis();

        // Check if more than 1 minute has passed since the app was last opened
        if ((currentTime - lastOpenTime) >= (1 * 60 * 1000)) {  // 1 minute (used for testing)
            Log.d("NotificationWorker", "Inactivity detected. Sending notification...");
            showNotification();  // Call method to show notification
        } else {
            Log.d("NotificationWorker", "App was recently used. No notification needed.");
        }

        return Result.success(); // Notify WorkManager that the task completed successfully
    }


     // Builds and displays a notification reminding the user to come back to the app.

    private void showNotification() {
        Context context = getApplicationContext();
        String channelId = "notification_channel";  // Unique channel ID for notifications

        // Get the system's notification service
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel for Android 8.0+ (required)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Reminder Notifications", // Channel name
                    NotificationManager.IMPORTANCE_DEFAULT // Notification importance level
            );
            notificationManager.createNotificationChannel(channel);
        }

        // Build the notification using NotificationCompat
        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Tranquil Mind") // Notification title
                .setContentText("It's been 15 minutes since your last session. Come back for a mindful break.") // the Message
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Small icon shown in the notification bar
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Set high priority
                .build();

        // Display the notification with ID 1
        notificationManager.notify(1, notification);
    }
}
