package com.example.final_project;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.widget.Button;
import android.content.Context;
import android.graphics.Paint;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;

import android.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


//class VisualizerView extends View {
//
//    private final Paint paint = new Paint();
//    private Visualizer visualizer;
//
//
//
//    public VisualizerView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        paint.setColor(0xFF00FF00);
//        paint.setStrokeWidth(2f);
//    }
//
//    public void setAudioSessionId(int audioSessionId) {
//        if (audioSessionId == AudioManager.ERROR) {  // Check if audio session is invalid
//            Log.e("VisualizerView", "Invalid audio session ID");
//            return;
//        }
//
//        if (visualizer != null) {
//            visualizer.release();  // Release the old visualizer if it exists
//        }
//
//        try {
//            visualizer = new Visualizer(audioSessionId);
//            visualizer.setEnabled(false);
//            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
//
//            // Check if visualizer is successfully initialized
//            if (visualizer.getEnabled()) {
//                Log.i("VisualizerView", "Visualizer initialized successfully");
//            } else {
//                Log.e("VisualizerView", "Visualizer initialization failed");
//            }
//
//            visualizer.setEnabled(true);
//        } catch (Exception e) {
//            Log.e("VisualizerView", "Error initializing Visualizer", e);
//        }
//    }
//
//
//}




public class Music extends AppCompatActivity {
    ImageView meditationIcon, searchIcon, homeIcon;
    Button closeButton;
    private MediaPlayer mediaPlayer;
    private VisualizerView visualizerView; // Moved the declaration here
    private MediaController mediaController;
    private static final int PICK_AUDIO_REQUEST = 1;
    private Uri audioUri;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
LinearLayout searchOptionMusic;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_music);
        meditationIcon = findViewById(R.id.meditationIcon);
        searchIcon = findViewById(R.id.searchIcon);
        homeIcon = findViewById(R.id.homeIcon);
        closeButton=findViewById(R.id.closeButton);
        searchOptionMusic=findViewById(R.id.searchOptionMusic);


        // Initialize buttons
        Button musicStudyBtn = findViewById(R.id.musicStudyBtn);
        Button musicSleepBtn = findViewById(R.id.musicSleepBtn);
        Button musicRelaxBtn = findViewById(R.id.musicRelaxBtn);
        Button musicFocusBtn = findViewById(R.id.musicFocusBtn);


        // Set onClick listeners for play button
        musicStudyBtn.setOnClickListener(v -> playMusic(R.raw.music_study));
        musicSleepBtn.setOnClickListener(v -> playMusic(R.raw.music_sleep));
        musicRelaxBtn.setOnClickListener(v -> playMusic(R.raw.music_relax));
        musicFocusBtn.setOnClickListener(v -> playMusic(R.raw.music_focus));

        EditText searchField = findViewById(R.id.searchFieldMusic);
        Button searchButton = findViewById(R.id.searchButtonMusic);

        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString().trim();
            if (!query.isEmpty()) {
                searchAudios(query);
            } else {
                Toast.makeText(Music.this, "Enter a search term", Toast.LENGTH_SHORT).show();
            }
        });

        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchOptionMusic.getVisibility() == View.GONE) {
                    searchOptionMusic.setVisibility(View.VISIBLE);
                } else {
                    searchOptionMusic.setVisibility(View.GONE);
                }
            }

        });




        meditationIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Meditation.class);
            startActivity(intent);
            finish();
        });
        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        });

        FrameLayout musicViewFrame = findViewById(R.id.musicViewFrame);


        musicViewFrame.setOnClickListener(v -> {
            if (mediaController != null) {
                mediaController.show(0); // Show the media controller when tapped
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                closeFrameLayoutAndStop();

            }

            private void closeFrameLayoutAndStop() {
                // Stop the audio playback
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaController.hide();

                }

                // clear previous views

                // Hide the FrameLayout
                musicViewFrame.setVisibility(View.GONE);
            }
        });

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("audios");
        databaseReference = FirebaseDatabase.getInstance().getReference("audios");

        Button uploadAudioBtn = findViewById(R.id.uploadAudioBtn);
        Button selectAudioBtn = findViewById(R.id.selectAudioBtn);


        uploadAudioBtn.setOnClickListener(v -> selectAudio());


        selectAudioBtn.setOnClickListener(v -> fetchAllAudios());





    }

    private void fetchAllAudios() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // ✅ Define userAudios here to store retrieved audios
                List<AudioModel> userAudios = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AudioModel audio = snapshot.getValue(AudioModel.class);
                    if (audio != null) {
                        userAudios.add(audio);
                    }
                }

                // ✅ Check if the list is not empty before showing selection dialog
                if (!userAudios.isEmpty()) {
                    showAudioSelectionDialog(userAudios);
                } else {
                    Toast.makeText(Music.this, "No audios found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Music.this, "Failed to load audios!", Toast.LENGTH_SHORT).show();
            }
        });



    }


    private void searchAudios(String query) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AudioModel> matchedAudios = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AudioModel audio = snapshot.getValue(AudioModel.class);
                    if (audio != null && (audio.getAudioName().toLowerCase().contains(query.toLowerCase()) ||
                            audio.getTags().toLowerCase().contains(query.toLowerCase()))) {
                        matchedAudios.add(audio);
                    }
                }

                if (!matchedAudios.isEmpty()) {
                    showSearchResults(matchedAudios);
                } else {
                    Toast.makeText(Music.this, "No audios found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Music.this, "Search failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to select audio
    private void selectAudio() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    // Handling file selection result
    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
//            audioUri = data.getData();  // Store the selected file URI
//            Toast.makeText(this, "Audio Selected!", Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "No Audio Selected!", Toast.LENGTH_SHORT).show();
//        }
//    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            audioUri = data.getData();

            // Show dialog to input name & tags
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Audio Details");

            // Create input fields
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(20, 20, 20, 20);

            final EditText nameInput = new EditText(this);
            nameInput.setHint("Enter Audio Name");
            layout.addView(nameInput);

            final EditText tagsInput = new EditText(this);
            tagsInput.setHint("Enter Tags (comma separated)");
            layout.addView(tagsInput);

            builder.setView(layout);

            builder.setPositiveButton("Upload", (dialog, which) -> {
                String audioName = nameInput.getText().toString().trim();
                String audioTags = tagsInput.getText().toString().trim();

                if (!audioName.isEmpty()) {
                    uploadAudio(audioName, audioTags);
                } else {
                    Toast.makeText(Music.this, "Audio name is required!", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.show();
        }
    }



    private void uploadAudio(String audioName, String audioTags) {
        if (audioUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get authenticated user ID
            StorageReference fileReference = storageReference.child(userId + "/" + System.currentTimeMillis() + ".mp4");

            fileReference.putFile(audioUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String audioId = databaseReference.child(userId).push().getKey(); // Unique audio ID

                    // Create audio object with metadata
                    AudioModel audio = new AudioModel(audioId, audioName, audioTags, uri.toString());

                    // Store in Firebase Realtime Database under user ID
                    databaseReference.child(userId).child(audioId).setValue(audio);

                    progressDialog.dismiss();
                    Toast.makeText(Music.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(Music.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
            });
        }
    }





    // Helper function to find resource ID by name
    private int getMusicResId(String name, String[] musicFiles, int[] musicResIds) {
        for (int i = 0; i < musicFiles.length; i++) {
            if (musicFiles[i].equals(name)) {
                return musicResIds[i];
            }
        }
        return -1;
    }
    private void playMusic(int rawResourceId) {
        // Stop and release the current MediaPlayer if it's already playing
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Initialize MediaPlayer with raw resource
        mediaPlayer = MediaPlayer.create(this, rawResourceId);
        if (mediaPlayer != null) {
            mediaPlayer.start();
            Toast.makeText(this, "Playing Audio", Toast.LENGTH_SHORT).show();

            // Initialize and attach the visualizer
            setupVisualizer();
            setupMediaController();
        } else {
            Toast.makeText(this, "Error Loading Audio", Toast.LENGTH_SHORT).show();
        }
    }

    // Updated playMusic method
    // Overloaded method to handle strings
    private void playMusic(String audioUrl) {
        // Stop and release the current MediaPlayer if it's already playing
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Initialize MediaPlayer with the audio URL
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioUrl); // Set data source to audio URL
            mediaPlayer.prepare(); // Prepare the MediaPlayer
            mediaPlayer.start(); // Start the playback
            Toast.makeText(this, "Playing Audio", Toast.LENGTH_SHORT).show();

            // Initialize and attach the visualizer
            setupVisualizer();

            // Set up media controller
            setupMediaController();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error Loading Audio", Toast.LENGTH_SHORT).show();
        }
    }


    private void setupMediaController() {
        if (mediaController == null) {
            mediaController = new MediaController(this);
            mediaController.setAnchorView(findViewById(R.id.musicViewFrame));
            mediaController.setMediaPlayer(new MediaController.MediaPlayerControl() {
                @Override
                public void start() {
                    mediaPlayer.start();

                }

                @Override
                public void pause() {
                    mediaPlayer.pause();
                }

                @Override
                public int getDuration() {
                    return mediaPlayer.getDuration();
                }

                @Override
                public int getCurrentPosition() {
                    return mediaPlayer.getCurrentPosition();
                }

                @Override
                public void seekTo(int pos) {
                    mediaPlayer.seekTo(pos);
                }

                @Override
                public boolean isPlaying() {
                    return mediaPlayer.isPlaying();
                }

                @Override
                public int getBufferPercentage() {
                    return 0;
                }

                @Override
                public boolean canPause() {
                    return true;
                }

                @Override
                public boolean canSeekBackward() {
                    return true;
                }

                @Override
                public boolean canSeekForward() {
                    return true;
                }

                @Override
                public int getAudioSessionId() {
                    return mediaPlayer.getAudioSessionId();
                }
            });
        }
        mediaController.show();
        findViewById(R.id.musicViewFrame).setVisibility(View.VISIBLE);
    }

    private void setupVisualizer() {
        if (visualizerView == null) {
            visualizerView = findViewById(R.id.visualizerView);
        }
        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1) {
            visualizerView.setAudioSessionId(audioSessionId);
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }





    private void showAudioSelectionDialog(List<AudioModel> userAudios) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Audio");

        // Create custom labels using audio names, ensuring no null values
        String[] audioLabels = new String[userAudios.size()];
        for (int i = 0; i < userAudios.size(); i++) {
            String audioName = userAudios.get(i).getAudioName();
            audioLabels[i] = (audioName != null && !audioName.trim().isEmpty()) ? audioName : "Unknown Audio " + (i + 1);
        }

        builder.setItems(audioLabels, (dialog, which) -> {
            AudioModel selectedAudio = userAudios.get(which);

            AlertDialog.Builder actionDialog = new AlertDialog.Builder(this);
            actionDialog.setTitle("Choose Action");
            actionDialog.setMessage("Do you want to play or delete this audio?");

            actionDialog.setPositiveButton("Play", (playDialog, p) -> {
                String audioUrl = selectedAudio.getAudioUrl();
                if (audioUrl != null && !audioUrl.trim().isEmpty()) {
                    playMusic(audioUrl);
                } else {
                    Toast.makeText(this, "Invalid audio file!", Toast.LENGTH_SHORT).show();
                }
            });

            actionDialog.setNegativeButton("Delete", (deleteDialog, d) -> {
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this audio?")
                        .setPositiveButton("Yes", (confirmDialog, c) -> {
                            deleteAudio(selectedAudio);
                        })
                        .setNegativeButton("Cancel", (confirmDialog, c) -> confirmDialog.dismiss())
                        .show();
            });

            actionDialog.setNeutralButton("Cancel", (cancelDialog, c) -> cancelDialog.dismiss());

            // Show the action dialog
            AlertDialog dialogBox = actionDialog.create();
            dialogBox.show();

            // Customize the delete button (red color and smaller size)
            dialogBox.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(12);
            dialogBox.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void deleteAudio(AudioModel audio) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Delete from Firebase Storage
        StorageReference audioRef = FirebaseStorage.getInstance().getReferenceFromUrl(audio.getAudioUrl());
        audioRef.delete().addOnSuccessListener(aVoid -> {
            // Delete from Realtime Database
            databaseReference.child(userId).child(audio.getId()).removeValue().addOnSuccessListener(aVoid1 -> {
                Toast.makeText(Music.this, "Audio deleted successfully!", Toast.LENGTH_SHORT).show();
                fetchAllAudios(); // Refresh audio list
            }).addOnFailureListener(e -> Toast.makeText(Music.this, "Failed to delete from database!", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(Music.this, "Failed to delete video from storage!", Toast.LENGTH_SHORT).show());
    }

    private void showSearchResults(List<AudioModel> matchedAudios) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Results");

        String[] audioNames = new String[matchedAudios.size()];
        for (int i = 0; i < matchedAudios.size(); i++) {
            audioNames[i] = matchedAudios.get(i).getAudioName();
        }

        builder.setItems(audioNames, (dialog, which) -> {
            AudioModel selectedAudio = matchedAudios.get(which);
            playMusic(selectedAudio.getAudioUrl());
        });

        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

}
class VisualizerView extends View {

    private final Paint paint = new Paint();
    private Visualizer visualizer;
    private byte[] waveform;

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(0xFFAA0000); // Red
        paint.setStrokeWidth(4f);
    }

    public void setAudioSessionId(int audioSessionId) {
        if (audioSessionId == AudioManager.ERROR) {
            Log.e("VisualizerView", "Invalid audio session ID");
            return;
        }

        if (visualizer != null) {
            visualizer.release();
        }

        try {
            visualizer = new Visualizer(audioSessionId);
            visualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

            visualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
                    VisualizerView.this.waveform = waveform;
                    invalidate(); // Redraw with new data
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                    // Optional: Implement FFT visualization here
                }
            }, Visualizer.getMaxCaptureRate() / 2, true, false);

            visualizer.setEnabled(true);
        } catch (Exception e) {
            Log.e("VisualizerView", "Error initializing Visualizer", e);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (waveform == null) return;

        int width = getWidth();
        int height = getHeight();
        int centerY = height / 2;
        float xIncrement = (float) width / (float) waveform.length;

        for (int i = 1; i < waveform.length; i++) {
            float x1 = (i - 1) * xIncrement;
            float y1 = centerY + ((byte) (waveform[i - 1] + 128)) * (centerY / 128f);
            float x2 = i * xIncrement;
            float y2 = centerY + ((byte) (waveform[i] + 128)) * (centerY / 128f);
            canvas.drawLine(x1, y1, x2, y2, paint);
        }
    }

    public void release() {
        if (visualizer != null) {
            visualizer.release();
        }
    }
}

class AudioModel {
    private String id;
    private String name;
    private String tags;
    private String url;

    // Required empty constructor for Firebase
    public AudioModel() { }

    // Constructor with parameters
    public AudioModel(String id, String name, String tags, String url) {
        this.id = id;
        this.name = name;
        this.tags = tags;
        this.url = url;
    }

    // Getter and Setter for ID
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Getter and Setter for Name
    public String getAudioName() {
        return name;
    }

    public void setAudioName(String name) {
        this.name = name;
    }

    // Getter and Setter for Tags
    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    // Getter and Setter for URL
    public String getAudioUrl() {
        return url;
    }


    public void setAudioUrl(String url) {
        this.url = url;
    }
}