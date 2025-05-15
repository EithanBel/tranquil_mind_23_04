package com.example.final_project;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
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
import android.app.AlertDialog;
import android.widget.EditText;
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



public class Music extends AppCompatActivity {

    // Views for UI elements
    ImageView meditationIcon, searchIcon, homeIcon;
    Button closeButton;
    private MediaPlayer mediaPlayer;  // MediaPlayer for audio playback
    private MediaController mediaController; // Controller for media playback control

    private static final int PICK_AUDIO_REQUEST = 1;  // Constant for file selection
    private Uri audioUri;  // URI of the selected audio file
    private StorageReference storageReference; // Firebase Storage reference
    private DatabaseReference databaseReference; // Firebase Database reference
    private FirebaseAuth mAuth; // Firebase Auth instance for user authentication
    LinearLayout searchOptionMusic; // Layout for search option

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);// Edge-to-edge screen support
        setContentView(R.layout.activity_music); // Set the layout for the activity

        // Initialize UI elements
        meditationIcon = findViewById(R.id.meditationIcon);
        searchIcon = findViewById(R.id.searchIcon);
        homeIcon = findViewById(R.id.homeIcon);
        closeButton=findViewById(R.id.closeButton);
        searchOptionMusic=findViewById(R.id.searchOptionMusic);


        // Set up buttons for different music categories
        Button musicStudyBtn = findViewById(R.id.musicStudyBtn);
        Button musicSleepBtn = findViewById(R.id.musicSleepBtn);
        Button musicRelaxBtn = findViewById(R.id.musicRelaxBtn);
        Button musicFocusBtn = findViewById(R.id.musicFocusBtn);


        // Set onClick listeners for play button
        musicStudyBtn.setOnClickListener(v -> playMusic(R.raw.music_study));
        musicSleepBtn.setOnClickListener(v -> playMusic(R.raw.music_sleep));
        musicRelaxBtn.setOnClickListener(v -> playMusic(R.raw.music_relax));
        musicFocusBtn.setOnClickListener(v -> playMusic(R.raw.music_focus));

        // Search functionality setup
        EditText searchField = findViewById(R.id.searchFieldMusic);
        Button searchButton = findViewById(R.id.searchButtonMusic);

        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString().trim();
            if (!query.isEmpty()) {
                searchAudios(query); // Search for audios based on the query
            } else {
                Toast.makeText(Music.this, "Enter a search term", Toast.LENGTH_SHORT).show();
            }
        });

        // Search option icon toggle visibility
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchOptionMusic.getVisibility() == View.GONE) {
                    searchOptionMusic.setVisibility(View.VISIBLE);// Show search options
                } else {
                    searchOptionMusic.setVisibility(View.GONE); // Hide search options
                }
            }

        });


       // Navigation setup
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
      // Set up the music view frame for media control
        FrameLayout musicViewFrame = findViewById(R.id.musicViewFrame);


        musicViewFrame.setOnClickListener(v -> {
            if (mediaController != null) {
                mediaController.show(0); // Show media controller on click
            }
        });
       // Close button listener to stop and hide audio playback
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

        // Buttons for uploading and selecting audio
        Button uploadAudioBtn = findViewById(R.id.uploadAudioBtn);
        Button selectAudioBtn = findViewById(R.id.selectAudioBtn);


        uploadAudioBtn.setOnClickListener(v -> selectAudio());


        selectAudioBtn.setOnClickListener(v -> fetchAllAudios());






    }

    private void fetchAllAudios() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID

        // Retrieve the user's audios from Firebase Database

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Retrieve the user's audios from Firebase Database
                List<AudioModel> userAudios = new ArrayList<>();
                // Add audios to the list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AudioModel audio = snapshot.getValue(AudioModel.class);
                    if (audio != null) {
                        userAudios.add(audio);
                    }
                }

                //  Check if the list is not empty before showing selection dialog
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
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();// Get user ID
    // Search audios from Firebase based on query
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AudioModel> matchedAudios = new ArrayList<>();

                // Match audios based on name or tags
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    AudioModel audio = snapshot.getValue(AudioModel.class);
                    if (audio != null && (audio.getAudioName().toLowerCase().contains(query.toLowerCase()) ||
                            audio.getTags().toLowerCase().contains(query.toLowerCase()))) {
                        matchedAudios.add(audio);
                    }
                }

                // Show search results if matched audios are found
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

    // Method to select audio file
    private void selectAudio() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_AUDIO_REQUEST);
    }

    // Handling the result of the audio file selection
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            audioUri = data.getData();  // Get selected audio URI

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

            // Set dialog buttons
            builder.setPositiveButton("Upload", (dialog, which) -> {
                String audioName = nameInput.getText().toString().trim();
                String audioTags = tagsInput.getText().toString().trim();

                if (!audioName.isEmpty()) {
                    uploadAudio(audioName, audioTags);// Upload audio with name and tags
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

            // Upload the audio file to Firebase Storage
            fileReference.putFile(audioUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String audioId = databaseReference.child(userId).push().getKey(); // Generate unique audio ID

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






    private void playMusic(int rawResourceId) {
        // Stop and release the current MediaPlayer if it's already playing
        // This ensures that there are no conflicts with previous playback sessions
        if (mediaPlayer != null) {
            mediaPlayer.stop();  // Stop the current media playback
            mediaPlayer.release();  // Release resources held by the current MediaPlayer
            mediaPlayer = null;  // Set the MediaPlayer object to null, as it's no longer needed
        }

        // Initialize the MediaPlayer using a raw resource ID
        mediaPlayer = MediaPlayer.create(this, rawResourceId); // The raw resource ID refers to an audio file in the 'raw' folder

        // Check if the MediaPlayer was successfully created
        if (mediaPlayer != null) {
            // Set an OnPreparedListener to start playback when the media player is ready
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();  // Start the audio playback
                // Display a Toast message to inform the user that the audio is playing
                Toast.makeText(this, "Playing Audio", Toast.LENGTH_SHORT).show();

                // Set up a media controller for the user to control playback (e.g., pause, skip)
                setupMediaController();  // This is a separate method that configures the UI controller for the media player
            });

            // Set an OnErrorListener to handle any errors during playback
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                // Log the error details to help with debugging
                Log.e("MediaPlayer", "Error: what=" + what + ", extra=" + extra);
                return false;  // Return false to indicate that the error was not handled here
            });
        } else {
            // If the MediaPlayer could not be created, display an error message to the user
            Toast.makeText(this, "Error Loading Audio", Toast.LENGTH_SHORT).show();
        }
    }




    // Updated playMusic method
    // Overloaded method to handle strings

    private void playMusic(String audioUrl) {
        // Stop and release the current MediaPlayer if it's already playing
        // This ensures that there are no conflicts with previous playback sessions
        if (mediaPlayer != null) {
            mediaPlayer.stop();  // Stop the current media playback
            mediaPlayer.release();  // Release resources held by the current MediaPlayer
            mediaPlayer = null;  // Set the MediaPlayer object to null, as it's no longer needed
        }

        // Initialize a new MediaPlayer instance
        mediaPlayer = new MediaPlayer();
        try {
            // Set the data source for the MediaPlayer to the provided audio URL
            mediaPlayer.setDataSource(audioUrl); // The audio URL could be from Firebase or a local resource

            // Prepare the MediaPlayer to begin playback
            mediaPlayer.prepare();  // It prepares the media player asynchronously, reading the audio stream

            // Start playing the audio once preparation is complete
            mediaPlayer.start();  // Begin audio playback

            // Display a Toast message to inform the user that audio playback has started
            Toast.makeText(this, "Playing Audio", Toast.LENGTH_SHORT).show();

            // Set up a media controller to provide playback controls such as pause, play, and skip
            setupMediaController();  // This is a separate method that configures the UI controller for the media player
        } catch (IOException e) {
            e.printStackTrace();  // Print the stack trace if an error occurs during the preparation or playback process
            // Display a Toast message to inform the user that there was an error loading the audio
            Toast.makeText(this, "Error Loading Audio", Toast.LENGTH_SHORT).show();
        }
    }



    private void setupMediaController() {
        // Check if the media controller is not already initialized
        if (mediaController == null) {
            // Initialize a new MediaController for controlling playback
            mediaController = new MediaController(this);

            // Set the anchor view where the media controller should be displayed
            // The media controller will be displayed above this view
            mediaController.setAnchorView(findViewById(R.id.musicViewFrame));

            // Set the MediaPlayerControl for controlling the playback of the media player
            mediaController.setMediaPlayer(new MediaController.MediaPlayerControl() {
                @Override
                public void start() {
                    // Start the audio playback
                    mediaPlayer.start();
                }

                @Override
                public void pause() {
                    // Pause the audio playback
                    mediaPlayer.pause();
                }

                @Override
                public int getDuration() {
                    // Return the total duration of the media (audio file)
                    return mediaPlayer.getDuration();
                }

                @Override
                public int getCurrentPosition() {
                    // Return the current position (time) of the media playback
                    return mediaPlayer.getCurrentPosition();
                }

                @Override
                public void seekTo(int pos) {
                    // Seek to a specific position in the media (audio)
                    mediaPlayer.seekTo(pos);
                }

                @Override
                public boolean isPlaying() {
                    // Return whether the media is currently playing
                    return mediaPlayer.isPlaying();
                }

                @Override
                public int getBufferPercentage() {
                    // Return the buffer percentage of the media (not used in this case)
                    return 0;
                }

                @Override
                public boolean canPause() {
                    // Return whether the media can be paused (always true for this case)
                    return true;
                }

                @Override
                public boolean canSeekBackward() {
                    // Return whether seeking backward is possible (always true for this case)
                    return true;
                }

                @Override
                public boolean canSeekForward() {
                    // Return whether seeking forward is possible (always true for this case)
                    return true;
                }

                @Override
                public int getAudioSessionId() {
                    // Return the audio session ID for the media player
                    return mediaPlayer.getAudioSessionId();
                }
            });
        }

        // Display the media controller
        mediaController.show();

        // Make the anchor view visible (where the media controller is attached)
        findViewById(R.id.musicViewFrame).setVisibility(View.VISIBLE);
    }


    private void showAudioSelectionDialog(List<AudioModel> userAudios) {
        // Create an AlertDialog.Builder to build the audio selection dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select an Audio");  // Set the title of the dialog

        // Create an array of labels (audio names) to display in the dialog
        // If the audio name is null or empty, set a default name
        String[] audioLabels = new String[userAudios.size()];
        for (int i = 0; i < userAudios.size(); i++) {
            // Get the name of each audio and handle null or empty values
            String audioName = userAudios.get(i).getAudioName();
            audioLabels[i] = (audioName != null && !audioName.trim().isEmpty()) ? audioName : "Unknown Audio " + (i + 1);
        }

        // Set the items in the dialog as the audio labels and define an action when an item is selected
        builder.setItems(audioLabels, (dialog, which) -> {
            // Get the selected audio object based on the index (which)
            AudioModel selectedAudio = userAudios.get(which);

            // Create a new dialog for choosing the action (play or delete)
            AlertDialog.Builder actionDialog = new AlertDialog.Builder(this);
            actionDialog.setTitle("Choose Action");  // Set the title of the action dialog
            actionDialog.setMessage("Do you want to play or delete this audio?");  // Set the message

            // Action: Play the selected audio
            actionDialog.setPositiveButton("Play", (playDialog, p) -> {
                // Get the URL of the selected audio
                String audioUrl = selectedAudio.getAudioUrl();
                if (audioUrl != null && !audioUrl.trim().isEmpty()) {
                    // If the URL is valid, play the audio
                    playMusic(audioUrl);
                } else {
                    // If the URL is invalid, show a toast message
                    Toast.makeText(this, "Invalid audio file!", Toast.LENGTH_SHORT).show();
                }
            });

            // Action: Delete the selected audio
            actionDialog.setNegativeButton("Delete", (deleteDialog, d) -> {
                // Create a confirmation dialog for deletion
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this audio?")
                        .setPositiveButton("Yes", (confirmDialog, c) -> {
                            // If confirmed, delete the audio
                            deleteAudio(selectedAudio);
                        })
                        .setNegativeButton("Cancel", (confirmDialog, c) -> confirmDialog.dismiss())  // Cancel the deletion
                        .show();  // Show the confirmation dialog
            });

            // Action: Cancel the action dialog
            actionDialog.setNeutralButton("Cancel", (cancelDialog, c) -> cancelDialog.dismiss());

            // Show the action dialog
            AlertDialog dialogBox = actionDialog.create();
            dialogBox.show();

            // Customize the appearance of the delete button (red color and smaller size)
            dialogBox.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(12);
            dialogBox.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        });

        // Set the "Cancel" button for the audio selection dialog
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();  // Show the audio selection dialog
    }

//    private void deleteAudio(AudioModel audio) {
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        // Delete from Firebase Storage
//        StorageReference audioRef = FirebaseStorage.getInstance().getReferenceFromUrl(audio.getAudioUrl());
//        audioRef.delete().addOnSuccessListener(aVoid -> {
//            // Delete from Realtime Database
//            databaseReference.child(userId).child(audio.getId()).removeValue().addOnSuccessListener(aVoid1 -> {
//                Toast.makeText(Music.this, "Audio deleted successfully!", Toast.LENGTH_SHORT).show();
//                fetchAllAudios(); // Refresh audio list
//            }).addOnFailureListener(e -> Toast.makeText(Music.this, "Failed to delete from database!", Toast.LENGTH_SHORT).show());
//        }).addOnFailureListener(e -> Toast.makeText(Music.this, "Failed to delete video from storage!", Toast.LENGTH_SHORT).show());
//    }

    private void deleteAudio(AudioModel audio) {
        // Get the current user's ID from Firebase Authentication
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get a reference to the audio file in Firebase Storage using its URL
        StorageReference audioRef = FirebaseStorage.getInstance().getReferenceFromUrl(audio.getAudioUrl());

        // Delete the audio file from Firebase Storage
        audioRef.delete().addOnSuccessListener(aVoid -> {
            // If deletion from Storage is successful, now delete from Firebase Realtime Database
            databaseReference.child(userId).child(audio.getId()).removeValue()
                    .addOnSuccessListener(aVoid1 -> {
                        // If deletion from Database is successful, show success message
                        Toast.makeText(Music.this, "Audio deleted successfully!", Toast.LENGTH_SHORT).show();
                        fetchAllAudios(); // Refresh the list of audios after deletion
                    })
                    .addOnFailureListener(e ->
                            // If deletion from Database fails, show error message
                            Toast.makeText(Music.this, "Failed to delete from database!", Toast.LENGTH_SHORT).show()
                    );
        }).addOnFailureListener(e ->
                // If deletion from Storage fails, show error message
                Toast.makeText(Music.this, "Failed to delete audio from storage!", Toast.LENGTH_SHORT).show()
        );
    }

    private void showSearchResults(List<AudioModel> matchedAudios) {
        // Create a dialog builder to display search results
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Results");

        // Prepare an array of audio names to display in the dialog
        String[] audioNames = new String[matchedAudios.size()];
        for (int i = 0; i < matchedAudios.size(); i++) {
            audioNames[i] = matchedAudios.get(i).getAudioName(); // Get each audio's name
        }

        // Set the list of audio names as selectable items in the dialog
        builder.setItems(audioNames, (dialog, which) -> {
            // When an item is clicked, get the selected AudioModel
            AudioModel selectedAudio = matchedAudios.get(which);

            // Play the selected audio
            playMusic(selectedAudio.getAudioUrl());
        });

        // Add a "Close" button to dismiss the dialog
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

        // Display the dialog
        builder.show();
    }


}

//Firebase Realtime Database need a structured object to save and retrieve data easily — this class gives Firebase the structure it expects.
//It makes it easy to pass audio data around inside the app
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