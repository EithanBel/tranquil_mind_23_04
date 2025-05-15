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
public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        Log.d("NotificationWorker", "Checking inactivity...");

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        long lastOpenTime = prefs.getLong("last_open_time", 0);
        long currentTime = System.currentTimeMillis();

        // Check if more than 1 minute passed (for testing)
        if ((currentTime - lastOpenTime) >= (1 * 60 * 1000)) {
            Log.d("NotificationWorker", "Inactivity detected. Sending notification...");
            showNotification();
        } else {
            Log.d("NotificationWorker", "App was recently used. No notification needed.");
        }

        return Result.success();
    }


    private void showNotification() {
        Context context = getApplicationContext();
        String channelId = "notification_channel";

        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Reminder Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Tranquil Mind")
                .setContentText("It's been 15 minutes since your last session. Come back for a mindful break.")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        notificationManager.notify(1, notification);
    }
}
