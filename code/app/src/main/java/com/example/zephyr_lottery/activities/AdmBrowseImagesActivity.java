package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdmBrowseImagesActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ListView listViewImages;
    private TextView tvEmptyState;

    private FirebaseFirestore db;
    private List<EventWithImage> eventsList;
    private EventImageAdapter adapter;
    private ListenerRegistration eventsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.adm_browse_images_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        listViewImages = findViewById(R.id.listViewImages);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize list and adapter
        eventsList = new ArrayList<>();
        adapter = new EventImageAdapter(this, eventsList);
        listViewImages.setAdapter(adapter);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Set up real-time listener for events
        setupEventsListener();
    }

    private void setupEventsListener() {
        eventsListener = db.collection("events")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("AdmBrowseImages", "Error listening to events", error);
                        Toast.makeText(this, "Error loading events: " + error.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        eventsList.clear();

                        for (QueryDocumentSnapshot document : value) {
                            String posterImage = document.getString("posterImage");

                            // Only add events that have poster images
                            if (posterImage != null && !posterImage.isEmpty()) {
                                String eventId = document.getId();
                                String eventName = document.getString("name");
                                String location = document.getString("location");

                                // Get start date
                                Object startDateObj = document.get("startDate");
                                String startDate = "";
                                if (startDateObj instanceof com.google.firebase.Timestamp) {
                                    Date date = ((com.google.firebase.Timestamp) startDateObj).toDate();
                                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                                    startDate = sdf.format(date);
                                }

                                EventWithImage event = new EventWithImage(
                                        eventId,
                                        eventName,
                                        location,
                                        startDate,
                                        posterImage
                                );

                                eventsList.add(event);
                            }
                        }

                        adapter.notifyDataSetChanged();

                        // Show/hide empty state
                        if (eventsList.isEmpty()) {
                            listViewImages.setVisibility(View.GONE);
                            tvEmptyState.setVisibility(View.VISIBLE);
                        } else {
                            listViewImages.setVisibility(View.VISIBLE);
                            tvEmptyState.setVisibility(View.GONE);
                        }

                        Log.d("AdmBrowseImages", "Loaded " + eventsList.size() + " events with images");
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove listener when activity is destroyed to prevent memory leaks
        if (eventsListener != null) {
            eventsListener.remove();
            Log.d("AdmBrowseImages", "Events listener removed");
        }
    }

    private void showDeleteImageDialog(EventWithImage event, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete the poster image for \"" + event.eventName + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteEventImage(event);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteEventImage(EventWithImage event) {
        db.collection("events")
                .document(event.eventId)
                .update("posterImage", "")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Image deleted successfully", Toast.LENGTH_SHORT).show();
                    Log.d("AdmBrowseImages", "Deleted image for event: " + event.eventId);
                    // No need to manually update the list - the listener will handle it
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete image: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("AdmBrowseImages", "Error deleting image", e);
                });
    }

    // Inner class for event data
    private static class EventWithImage {
        String eventId;
        String eventName;
        String location;
        String startDate;
        String posterImage;

        EventWithImage(String eventId, String eventName, String location, String startDate, String posterImage) {
            this.eventId = eventId;
            this.eventName = eventName;
            this.location = location;
            this.startDate = startDate;
            this.posterImage = posterImage;
        }
    }

    // Custom adapter for displaying events with images
    private class EventImageAdapter extends ArrayAdapter<EventWithImage> {

        public EventImageAdapter(AdmBrowseImagesActivity context, List<EventWithImage> events) {
            super(context, 0, events);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            EventWithImage event = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.event_image_item, parent, false);
            }

            // Get views
            TextView tvEventName = convertView.findViewById(R.id.tvEventName);
            TextView tvEventDetails = convertView.findViewById(R.id.tvEventDetails);
            ImageView ivPosterImage = convertView.findViewById(R.id.ivPosterImage);
            Button btnDeleteImage = convertView.findViewById(R.id.btnDeleteImage);

            // Set event data
            tvEventName.setText(event.eventName);

            String details = event.location;
            if (event.startDate != null && !event.startDate.isEmpty()) {
                details += " • " + event.startDate;
            }
            tvEventDetails.setText(details);

            // Decode and display BASE64 image
            try {
                byte[] decodedBytes = Base64.decode(event.posterImage, Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                ivPosterImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("AdmBrowseImages", "Error decoding image for event: " + event.eventName, e);
                ivPosterImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Delete button
            btnDeleteImage.setOnClickListener(v -> {
                showDeleteImageDialog(event, position);
            });

            return convertView;
        }
    }
}