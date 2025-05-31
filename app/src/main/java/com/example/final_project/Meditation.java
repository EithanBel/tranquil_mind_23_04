package com.example.final_project;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.net.Uri;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// this classes screen is the meditation screen

public class Meditation extends AppCompatActivity {
    ImageView searchIcon, musicIcon, homeIcon; // Icons for search, music, and home navigation
    Button meditationHealthButton, meditationFocusButton, meditationStudyButton, meditationRelaxButton, closeButton; // Buttons for different meditation categories and a button for closing the video
    VideoView videoView; // VideoView to play the meditation videos
    MediaController mediaController; // Media controller to control the videos playback
    FrameLayout meditationVideoFrame; // FrameLayout to display the video
    TextView nowPlayingField; // TextView to display the currently playing video
    LinearLayout searchOption; // Layout for the search option

    // Constants and variables for video upload and storage
    private static final int PICK_VIDEO_REQUEST = 1; // Request code for picking a video from the storage
    private Uri videoUri; // URI to store the picked video
    private StorageReference storageReference; // Firebase Storage reference to store the videos
    private DatabaseReference databaseReference; // Firebase Database reference for video metadata
    private Button uploadVideoBtn; // Button to upload a video

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enabling edge-to-edge display mode
        setContentView(R.layout.activity_meditation); // Set the screen of the meditation

        // Initialize the UI components
        searchIcon = findViewById(R.id.searchIcon);
        musicIcon = findViewById(R.id.musicIcon);
        homeIcon = findViewById(R.id.homeIcon);
        meditationHealthButton = findViewById(R.id.meditationHealthBtn);
        meditationRelaxButton = findViewById(R.id.meditationRelaxBtn);
        meditationStudyButton = findViewById(R.id.meditationStudyBtn);
        meditationFocusButton = findViewById(R.id.meditationFocusBtn);

        // Initialize the video view and media controller
        videoView = findViewById(R.id.meditationVideoView);
        mediaController = new MediaController(this); // Create a media controller
        mediaController.setAnchorView(videoView); // Set the controller to the VideoView
        videoView.setMediaController(mediaController); // Link the controller to the VideoView

        nowPlayingField = findViewById(R.id.nowPlaying); // TextView for the "now playing" info
        meditationVideoFrame = findViewById(R.id.videoViewFrame); // FrameLayout for the video player
        closeButton = findViewById(R.id.closeButton); // Button to close the video
        searchOption = findViewById(R.id.searchOption); // Layout for search options

        // Initialize Firebase Storage and Database references
        storageReference = FirebaseStorage.getInstance().getReference("videos");
        databaseReference = FirebaseDatabase.getInstance().getReference("videos");

        // Button to upload a new video
        uploadVideoBtn = findViewById(R.id.uploadVideoBtn);
        uploadVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVideo(); // opens video selection when clicked
            }
        });




        // Music icon click listener to navigate to the Music activity
        musicIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // navigates to the music class
                Intent intent = new Intent(getApplicationContext(), Music.class);
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });

        // Search icon click listener to toggle the visibility of the search option
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checks if the search option is not visible
                if (searchOption.getVisibility() == View.GONE) {
                    searchOption.setVisibility(View.VISIBLE); // Show search options
                } else {
                    searchOption.setVisibility(View.GONE); // Hide search options
                }
            }
        });

        // Home icon click listener to navigate to the MainActivity
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish(); // Finish the current activity
            }
        });

        // Button click listeners for each meditation category to play a video
        meditationHealthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meditationVideoFrame.setVisibility(View.VISIBLE); // Show the video frame
                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meditation_health_video1; // Path to the health meditation video
                videoView.setVideoPath(videoPath);
                videoView.start(); // Starts playing the video
            }
        });

        meditationStudyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meditationVideoFrame.setVisibility(View.VISIBLE); // Show the video frame
                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meditation_study_video3; // Path to the study meditation video
                videoView.setVideoPath(videoPath);
                videoView.start(); // Starts playing the video
            }
        });

        meditationFocusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meditationVideoFrame.setVisibility(View.VISIBLE); // Show the video frame
                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meditation_focus_video_2; // Path to the focus meditation video
                videoView.setVideoPath(videoPath);
                videoView.start(); // Starts playing the video
            }
        });

        meditationRelaxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meditationVideoFrame.setVisibility(View.VISIBLE); // Show the video frame
                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meditation_relaxation_video4; // Path to the relaxation meditation video
                videoView.setVideoPath(videoPath);
                videoView.start(); // Start playing the video
            }
        });

        // Close button click listener to stop video and hide the video frame
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFrameLayoutAndStopVideo(); // Close the frame and stop the video
            }
        });

        // Button to select a video from local storage
        Button selectVideoBtn = findViewById(R.id.selectVideoBtn);
        selectVideoBtn.setOnClickListener(v -> fetchAllVideos()); // Fetch all videos when clicked

        // Search button click listener to search for videos based on the query
        EditText searchField = findViewById(R.id.searchField);
        Button searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString().trim(); // Get the search query
            if (!query.isEmpty()) {
                searchVideos(query); // Perform search if query is not empty
            } else {
                Toast.makeText(Meditation.this, "Enter a search term", Toast.LENGTH_SHORT).show(); // Show a message if query is empty
            }
        });


        // Set a listener to fetch video data from Firebase Database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Loop through all videos stored in Firebase
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get the video data as a map
                    Map<String, Object> videoData = (Map<String, Object>) snapshot.getValue();
                    // Extract video URL from the data
                    String videoUrl = (String) videoData.get("videoUrl"); // Replace with actual field name if needed

                    // If video URL is valid, play it
                    if (videoUrl != null) {
                        playVideo(videoUrl);
                    } else {
                        Log.e("VideoError", "No video URL found for " + snapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors in reading data
                Log.e("DatabaseError", "Failed to read value.", databaseError.toException());
            }
        });




    }




    // Method to select a video from the user's device
    private void selectVideo() {
        // Create an intent to open the file picker for selecting a video
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*"); // Filter to allow only video files
        intent.addCategory(Intent.CATEGORY_OPENABLE); // Allow selection of openable files
        startActivityForResult(intent, PICK_VIDEO_REQUEST); // Start the activity for result (pick video)
    }

    // Handle the result of the video selection (after picking a video)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the request is for picking a video and the result is OK
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            videoUri = data.getData(); // Get the URI of the selected video

            // Show a dialog to enter video details like name and tags
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Video Details");

            // Create a layout for the input fields (name and tags)
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL); // Stack the fields vertically
            layout.setPadding(20, 20, 20, 20); // Add padding around the input fields

            // Create an EditText field for the video name
            final EditText nameInput = new EditText(this);
            nameInput.setHint("Enter Video Name"); // Hint text to prompt user for video name
            layout.addView(nameInput); // Add the name input field to the layout

            // Create an EditText field for the video tags
            final EditText tagsInput = new EditText(this);
            tagsInput.setHint("Enter Tags (comma separated)"); // Hint text to prompt user for tags
            layout.addView(tagsInput); // Add the tags input field to the layout

            // Set the layout view for the dialog
            builder.setView(layout);

            // Set up the positive button for uploading the video
            builder.setPositiveButton("Upload", (dialog, which) -> {
                String videoName = nameInput.getText().toString().trim(); // Get the video name from the input
                String videoTags = tagsInput.getText().toString().trim(); // Get the video tags from the input

                // Check if the video name is not empty
                if (!videoName.isEmpty()) {
                    uploadVideo(videoName, videoTags); // Upload the video with the given name and tags
                } else {
                    // Show a toast if the video name is empty (required field)
                    Toast.makeText(Meditation.this, "Video name is required!", Toast.LENGTH_SHORT).show();
                }
            });

            // Set up the negative button to dismiss the dialog
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            // Show the dialog for entering video details
            builder.show();
        }
    }





    // Method to upload a selected video to Firebase Storage and store its metadata in Firebase Realtime Database
    private void uploadVideo(String videoName, String videoTags) {
        // Check if a valid video URI exists
        if (videoUri != null) {
            // Create and show a progress dialog while the upload is in progress
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Get the current authenticated user ID (to store the video under the user's section)
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Create a reference to Firebase Storage using the user ID and a timestamp to make the file name unique
            StorageReference fileReference = storageReference.child(userId + "/" + System.currentTimeMillis() + ".mp4");

            // Upload the video file to Firebase Storage
            fileReference.putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
                // On successful upload, get the downloadable URL of the uploaded video
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Generate a unique video ID using the user's reference in Firebase Realtime Database
                    String videoId = databaseReference.child(userId).push().getKey();

                    // Create a new VideoModel object with metadata (ID, name, tags, and video URL)
                    VideoModel video = new VideoModel(videoId, videoName, videoTags, uri.toString());

                    // Store the videos information in Firebase Realtime Database under the user's section
                    databaseReference.child(userId).child(videoId).setValue(video);

                    // Dismiss the progress dialog after the upload completes successfully
                    progressDialog.dismiss();

                    // Show a success message to the user
                    Toast.makeText(Meditation.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                // If upload fails, dismiss the progress dialog and show an error message
                progressDialog.dismiss();
                Toast.makeText(Meditation.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Method to stop the video playback and close the video frame layout
    private void closeFrameLayoutAndStopVideo() {
        // Stop the video if it's currently playing
        if (videoView.isPlaying()) {
            videoView.stopPlayback();
        }

        // Hide the FrameLayout that contains the video view to make it invisible
        meditationVideoFrame.setVisibility(View.GONE);
    }




    // Method to play a video given its URL
    private void playVideo(String videoUrl) {
        // Make the video frame layout visible to display the video player
        meditationVideoFrame.setVisibility(View.VISIBLE);

        // Set the URI of the video to be played
        videoView.setVideoURI(Uri.parse(videoUrl));

        // Start the video playback
        videoView.start();
    }

    // Method to fetch all videos uploaded by the current user from Firebase Realtime Database
    private void fetchAllVideos() {
        // Get the current authenticated user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Retrieve the user's videos from Firebase Realtime Database
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // List to store all the user's videos retrieved from the database
                List<VideoModel> userVideos = new ArrayList<>();

                // Loop through the data snapshot and add videos to the list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    VideoModel video = snapshot.getValue(VideoModel.class);
                    if (video != null) {
                        userVideos.add(video);
                    }
                }

                // If there are videos, show a dialog to select one; otherwise, display a message
                if (!userVideos.isEmpty()) {
                    showVideoSelectionDialog(userVideos);
                } else {
                    Toast.makeText(Meditation.this, "No videos found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the failure case when the data retrieval is canceled
                Toast.makeText(Meditation.this, "Failed to load videos!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to show a dialog with a list of the user's videos to select one
    private void showVideoSelectionDialog(List<VideoModel> userVideos) {
        // Create an AlertDialog.Builder to build the selection dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Video");

        // Create an array of video names to display in the dialog
        String[] videoLabels = new String[userVideos.size()];
        for (int i = 0; i < userVideos.size(); i++) {
            videoLabels[i] = userVideos.get(i).getVideoName(); // Display the video name
        }

        // Set the list of video names and define the action when a video is selected
        builder.setItems(videoLabels, (dialog, which) -> {
            // Get the selected video from the list
            VideoModel selectedVideo = userVideos.get(which);

            // Create an action dialog to either play or delete the selected video
            AlertDialog.Builder actionDialog = new AlertDialog.Builder(this);
            actionDialog.setTitle("Choose Action");
            actionDialog.setMessage("Do you want to play or delete this video?");

            // "Play" option: Play the selected video
            actionDialog.setPositiveButton("Play", (playDialog, p) -> {
                playVideo(selectedVideo.getVideoUrl()); // Play the video using its URL
            });

            // "Delete" option: Confirm deletion of the selected video
            actionDialog.setNegativeButton("Delete", (deleteDialog, d) -> {
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this video?")
                        .setPositiveButton("Yes", (confirmDialog, c) -> {
                            deleteVideo(selectedVideo); // Call the method to delete the video
                        })
                        .setNegativeButton("Cancel", (confirmDialog, c) -> confirmDialog.dismiss())
                        .show();
            });

            // "Cancel" option: Close the dialog without performing any action
            actionDialog.setNeutralButton("Cancel", (cancelDialog, c) -> cancelDialog.dismiss());

            // Show the action dialog
            AlertDialog dialogBox = actionDialog.create();
            dialogBox.show();

            // Customize the "Delete" button (red color and smaller size for emphasis)
            dialogBox.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(12);
            dialogBox.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        });

        // "Cancel" option to dismiss the selection dialog
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the selection dialog
        builder.show();
    }

    // Method to delete a video from both Firebase Storage and Realtime Database
    private void deleteVideo(VideoModel video) {
        // Get the current authenticated user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get a reference to the video file in Firebase Storage using its URL
        StorageReference videoRef = FirebaseStorage.getInstance().getReferenceFromUrl(video.getVideoUrl());

        // Delete the video file from Firebase Storage
        videoRef.delete().addOnSuccessListener(aVoid -> {
            // On successful deletion from Storage, remove the video metadata from the Realtime Database
            databaseReference.child(userId).child(video.getId()).removeValue().addOnSuccessListener(aVoid1 -> {
                Toast.makeText(Meditation.this, "Video deleted successfully!", Toast.LENGTH_SHORT).show();
                fetchAllVideos(); // Refresh the video list after deletion
            }).addOnFailureListener(e -> Toast.makeText(Meditation.this, "Failed to delete from database!", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(Meditation.this, "Failed to delete video from storage!", Toast.LENGTH_SHORT).show());
    }

    // Method to search for videos based on a query (video name or tags)
    private void searchVideos(String query) {
        // Get the current authenticated user ID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Search for videos in the user's section of the database
        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // List to store videos that match the search query
                List<VideoModel> matchedVideos = new ArrayList<>();

                // Loop through the data and add matching videos to the list
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    VideoModel video = snapshot.getValue(VideoModel.class);
                    if (video != null && (video.getVideoName().toLowerCase().contains(query.toLowerCase()) ||
                            video.getTags().toLowerCase().contains(query.toLowerCase()))) {
                        matchedVideos.add(video);
                    }
                }

                // If there are matching videos, display them; otherwise, show a "no videos found" message
                if (!matchedVideos.isEmpty()) {
                    showSearchResults(matchedVideos);
                } else {
                    Toast.makeText(Meditation.this, "No videos found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle search failure
                Toast.makeText(Meditation.this, "Search failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to display search results in a dialog with video names
    private void showSearchResults(List<VideoModel> matchedVideos) {
        // Create an AlertDialog.Builder to display the search results
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Results");

        // Create an array of video names to display in the dialog
        String[] videoNames = new String[matchedVideos.size()];
        for (int i = 0; i < matchedVideos.size(); i++) {
            videoNames[i] = matchedVideos.get(i).getVideoName(); // Display the name of each matched video
        }

        // Set the list of video names and define the action when a video is selected
        builder.setItems(videoNames, (dialog, which) -> {
            // Get the selected video and play it
            VideoModel selectedVideo = matchedVideos.get(which);
            playVideo(selectedVideo.getVideoUrl()); // Play the selected video
        });

        // "Close" option to dismiss the search results dialog
        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());

        // Show the search results dialog
        builder.show();
    }




}
// VideoModel class to represent a video and its metadata
class VideoModel {
    private String id;    // Unique identifier for the video
    private String name;  // Name of the video
    private String tags;  // Tags associated with the video for categorization or search
    private String url;   // URL to the video stored in Firebase Storage

    // Required empty constructor for Firebase. Firebase requires a no-argument constructor
    // to deserialize data into the model class.
    public VideoModel() { }

    // Constructor with parameters to initialize the video object with specific values
    public VideoModel(String id, String name, String tags, String url) {
        this.id = id;      // Set the unique video ID
        this.name = name;  // Set the name of the video
        this.tags = tags;  // Set the tags for the video
        this.url = url;    // Set the URL of the video
    }

    // Getter and Setter for ID
    public String getId() {
        return id;  // Return the video ID
    }

    public void setId(String id) {
        this.id = id;  // Set the video ID
    }

    // Getter and Setter for Name
    public String getVideoName() {
        return name;  // Return the video name
    }

    public void setVideoName(String name) {
        this.name = name;  // Set the video name
    }

    // Getter and Setter for Tags
    public String getTags() {
        return tags;  // Return the tags associated with the video
    }

    public void setTags(String tags) {
        this.tags = tags;  // Set the tags for the video
    }

    // Getter and Setter for URL
    public String getVideoUrl() {
        return url;  // Return the URL where the video is stored
    }

    public void setVideoUrl(String url) {
        this.url = url;  // Set the URL of the video
    }
}


