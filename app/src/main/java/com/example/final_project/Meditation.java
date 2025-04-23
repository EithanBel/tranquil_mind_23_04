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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Meditation extends AppCompatActivity {
    ImageView searchIcon,musicIcon,homeIcon;
    Button meditationHealthButton,meditationFocusButton,meditationStudyButton,meditationRelaxButton,closeButton;
    VideoView videoView;
    MediaController mediaController;
    FrameLayout meditationVideoFrame;
    TextView nowPlayingField;
    LinearLayout searchOption;

    private static final int PICK_VIDEO_REQUEST = 1;
    private Uri videoUri;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private Button uploadVideoBtn;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_meditation);

       searchIcon=findViewById(R.id.searchIcon);
        musicIcon=findViewById(R.id.musicIcon);
        homeIcon=findViewById(R.id.homeIcon);
        meditationHealthButton=findViewById(R.id.meditationHealthBtn);
        meditationRelaxButton=findViewById(R.id.meditationRelaxBtn);
        meditationStudyButton=findViewById(R.id.meditationStudyBtn);
        meditationFocusButton=findViewById(R.id.meditationFocusBtn);

        videoView= (VideoView) findViewById(R.id.meditationVideoView);
        mediaController=new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        nowPlayingField=findViewById(R.id.nowPlaying);
        meditationVideoFrame=findViewById(R.id.videoViewFrame);

        closeButton=findViewById(R.id.closeButton);
        searchOption=findViewById(R.id.searchOption);


        storageReference = FirebaseStorage.getInstance().getReference("videos");
        databaseReference = FirebaseDatabase.getInstance().getReference("videos");

        uploadVideoBtn = findViewById(R.id.uploadVideoBtn);
        uploadVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectVideo();
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Get the video data as a Map
                    Map<String, Object> videoData = (Map<String, Object>) snapshot.getValue();

                    // Extract the video URL from the data
                    String videoUrl = (String) videoData.get("videoUrl");  // Replace "videoUrl" with the actual field name if necessary

                    // Check if videoUrl is not null and play it
                    if (videoUrl != null) {
                        playVideo(videoUrl);
                    } else {
                        Log.e("VideoError", "No video URL found for " + snapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
                Log.e("DatabaseError", "Failed to read value.", databaseError.toException());
            }
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


            });


            musicIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Music.class);
                    startActivity(intent);
                    finish();
                }


            });
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchOption.getVisibility() == View.GONE) {
                    searchOption.setVisibility(View.VISIBLE);
                } else {
                    searchOption.setVisibility(View.GONE);
                }
            }

        });
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }

        });

        meditationHealthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               meditationVideoFrame.setVisibility(View.VISIBLE);

               String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meditation_health_video1;

               videoView.setVideoPath(videoPath);
                videoView.start();


            }
        });




        meditationStudyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                meditationVideoFrame.setVisibility(View.VISIBLE);
                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meditation_study_video3;
                videoView.setVideoPath(videoPath);
                videoView.start();


            }
        });



        meditationFocusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                meditationVideoFrame.setVisibility(View.VISIBLE);
                // nowPlayingField.setVisibility(View.VISIBLE);
                // videoView.setVisibility(View.VISIBLE);



                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meditation_focus_video_2;

                videoView.setVideoPath(videoPath);
                videoView.start();


            }
        });




        meditationRelaxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                meditationVideoFrame.setVisibility(View.VISIBLE);




                String videoPath = "android.resource://" + getPackageName() + "/" + R.raw.meditation_relaxation_video4;

                videoView.setVideoPath(videoPath);
                videoView.start();


            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                closeFrameLayoutAndStopVideo();

            }
        });
        Button selectVideoBtn = findViewById(R.id.selectVideoBtn);
        selectVideoBtn.setOnClickListener(v -> fetchAllVideos());


        EditText searchField = findViewById(R.id.searchField);
        Button searchButton = findViewById(R.id.searchButton);

        searchButton.setOnClickListener(v -> {
            String query = searchField.getText().toString().trim();
            if (!query.isEmpty()) {
                searchVideos(query);
            } else {
                Toast.makeText(Meditation.this, "Enter a search term", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void selectVideo() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_VIDEO_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            videoUri = data.getData();

            // Show dialog to input name & tags
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Video Details");

            // Create input fields
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(20, 20, 20, 20);

            final EditText nameInput = new EditText(this);
            nameInput.setHint("Enter Video Name");
            layout.addView(nameInput);

            final EditText tagsInput = new EditText(this);
            tagsInput.setHint("Enter Tags (comma separated)");
            layout.addView(tagsInput);

            builder.setView(layout);

            builder.setPositiveButton("Upload", (dialog, which) -> {
                String videoName = nameInput.getText().toString().trim();
                String videoTags = tagsInput.getText().toString().trim();

                if (!videoName.isEmpty()) {
                    uploadVideo(videoName, videoTags);
                } else {
                    Toast.makeText(Meditation.this, "Video name is required!", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.show();
        }
    }



    private void uploadVideo(String videoName, String videoTags) {
        if (videoUri != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get authenticated user ID
            StorageReference fileReference = storageReference.child(userId + "/" + System.currentTimeMillis() + ".mp4");

            fileReference.putFile(videoUri).addOnSuccessListener(taskSnapshot -> {
                fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String videoId = databaseReference.child(userId).push().getKey(); // Unique video ID

                    // Create video object with metadata
                    VideoModel video = new VideoModel(videoId, videoName, videoTags, uri.toString());

                    // Store in Firebase Realtime Database under user ID
                    databaseReference.child(userId).child(videoId).setValue(video);

                    progressDialog.dismiss();
                    Toast.makeText(Meditation.this, "Upload Successful!", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(Meditation.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
            });
        }
    }








    private void closeFrameLayoutAndStopVideo() {
        // Stop the video playback
        if (videoView.isPlaying()) {
            videoView.stopPlayback();

        }

        // clear previous views

        // Hide the FrameLayout
        meditationVideoFrame.setVisibility(View.GONE);
    }

    private void playVideo(String videoUrl) {
        meditationVideoFrame.setVisibility(View.VISIBLE);
        videoView.setVideoURI(Uri.parse(videoUrl));
        videoView.start();
    }


   /* private void fetchAllVideos() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("videos");

        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    List<StorageReference> videoRefs = new ArrayList<>();

                    if (listResult.getItems().isEmpty()) {
                        Log.e("FirebaseStorage", "No videos found in Storage");
                        Toast.makeText(this, "No videos available", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    videoRefs.addAll(listResult.getItems());

                    // Show selection dialog with Play & Delete options
                    showVideoSelectionDialog(videoRefs);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseStorage", "Error listing videos", e);
                    Toast.makeText(this, "Failed to load videos", Toast.LENGTH_SHORT).show();
                });
    }*/


    private void fetchAllVideos() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // Get current user ID

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // ✅ Define userVideos here to store retrieved videos
                List<VideoModel> userVideos = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    VideoModel video = snapshot.getValue(VideoModel.class);
                    if (video != null) {
                        userVideos.add(video);
                    }
                }

                // ✅ Check if the list is not empty before showing selection dialog
                if (!userVideos.isEmpty()) {
                    showVideoSelectionDialog(userVideos);
                } else {
                    Toast.makeText(Meditation.this, "No videos found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Meditation.this, "Failed to load videos!", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showVideoSelectionDialog(List<VideoModel> userVideos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Video");

        // Create custom labels using video names or default numbering
        String[] videoLabels = new String[userVideos.size()];
        for (int i = 0; i < userVideos.size(); i++) {
            videoLabels[i] = userVideos.get(i).getVideoName(); // Assuming `getVideoName()` exists in `VideoModel`
        }

        builder.setItems(videoLabels, (dialog, which) -> {
            VideoModel selectedVideo = userVideos.get(which);

            AlertDialog.Builder actionDialog = new AlertDialog.Builder(this);
            actionDialog.setTitle("Choose Action");
            actionDialog.setMessage("Do you want to play or delete this video?");

            actionDialog.setPositiveButton("Play", (playDialog, p) -> {
                playVideo(selectedVideo.getVideoUrl()); // Ensure `getVideoUrl()` is in `VideoModel`
            });

            // Red-colored "Delete" button with confirmation dialog
            actionDialog.setNegativeButton("Delete", (deleteDialog, d) -> {
                new AlertDialog.Builder(this)
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this video?")
                        .setPositiveButton("Yes", (confirmDialog, c) -> {
                            deleteVideo(selectedVideo); // Update delete function to use VideoModel
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







    private void deleteVideo(VideoModel video) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Delete from Firebase Storage
        StorageReference videoRef = FirebaseStorage.getInstance().getReferenceFromUrl(video.getVideoUrl());
        videoRef.delete().addOnSuccessListener(aVoid -> {
            // Delete from Realtime Database
            databaseReference.child(userId).child(video.getId()).removeValue().addOnSuccessListener(aVoid1 -> {
                Toast.makeText(Meditation.this, "Video deleted successfully!", Toast.LENGTH_SHORT).show();
                fetchAllVideos(); // Refresh video list
            }).addOnFailureListener(e -> Toast.makeText(Meditation.this, "Failed to delete from database!", Toast.LENGTH_SHORT).show());
        }).addOnFailureListener(e -> Toast.makeText(Meditation.this, "Failed to delete video from storage!", Toast.LENGTH_SHORT).show());
    }



    private void searchVideos(String query) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<VideoModel> matchedVideos = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    VideoModel video = snapshot.getValue(VideoModel.class);
                    if (video != null && (video.getVideoName().toLowerCase().contains(query.toLowerCase()) ||
                            video.getTags().toLowerCase().contains(query.toLowerCase()))) {
                        matchedVideos.add(video);
                    }
                }

                if (!matchedVideos.isEmpty()) {
                    showSearchResults(matchedVideos);
                } else {
                    Toast.makeText(Meditation.this, "No videos found!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Meditation.this, "Search failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void showSearchResults(List<VideoModel> matchedVideos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Search Results");

        String[] videoNames = new String[matchedVideos.size()];
        for (int i = 0; i < matchedVideos.size(); i++) {
            videoNames[i] = matchedVideos.get(i).getVideoName();
        }

        builder.setItems(videoNames, (dialog, which) -> {
            VideoModel selectedVideo = matchedVideos.get(which);
            playVideo(selectedVideo.getVideoUrl());
        });

        builder.setNegativeButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }



}
class VideoModel {
    private String id;
    private String name;
    private String tags;
    private String url;

    // Required empty constructor for Firebase
    public VideoModel() { }

    // Constructor with parameters
    public VideoModel(String id, String name, String tags, String url) {
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
    public String getVideoName() {
        return name;
    }

    public void setVideoName(String name) {
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
    public String getVideoUrl() {
        return url;
    }

    public void setVideoUrl(String url) {
        this.url = url;
    }
}

