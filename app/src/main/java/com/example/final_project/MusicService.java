
package com.example.final_project;


import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;


 // This class represents a background service that plays background music when started. It continues running even if the app is in the background.


public class MusicService extends Service {

    // Field to manage and control media playback
    private MediaPlayer mediaPlayer;


    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize the MediaPlayer with a music file from res/raw
        mediaPlayer = MediaPlayer.create(this, R.raw.app_background_music);

        // Set the music to loop continuously
        mediaPlayer.setLooping(true);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // checks if the music is playing
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start(); // Starts playing music
        }
        return START_STICKY; // tells the system to recreate the service
    }

    // called when the service is no longer in use
    @Override
    public void onDestroy() {
        super.onDestroy();
        // checks if the music is still playing
        if (mediaPlayer != null) {
            mediaPlayer.stop();      // Stop playback
            mediaPlayer.release();   // Release MediaPlayer resources
            mediaPlayer = null;      // Clean up the reference
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
