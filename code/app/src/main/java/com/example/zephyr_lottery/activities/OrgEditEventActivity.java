package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.Event;
import com.example.zephyr_lottery.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class OrgEditEventActivity extends AppCompatActivity {
    private ImageView eventImage;
    private Button imageButton;
    private Button saveButton;
    private int eventCode;
    private String userEmail;
    private FirebaseFirestore db;
    private DocumentReference docRef;
    private Bitmap image_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.org_edit_event_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //initialization of needed variables
        db = FirebaseFirestore.getInstance();
        imageButton = findViewById(R.id.button_edit_image_upload);
        eventImage = findViewById(R.id.edit_event_image_poster);
        saveButton = findViewById(R.id.button_edit_event_back);
        eventCode = getIntent().getIntExtra("EVENT_CLICKED_CODE", -1);
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        loadEventDetails();

        //activity for selecting images from gallery. updates in database if successful.
        ActivityResultLauncher<Intent> activityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == AddEventActivity.RESULT_OK){
                            Intent data = result.getData();
                            Uri uri = data.getData();

                            try {
                                image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                eventImage.setImageBitmap(image_bitmap);
                                editEventImage();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });

        //listener for changing the image. goes to activity above
        imageButton.setOnClickListener(view ->{
            Intent img_intent = new Intent(Intent.ACTION_PICK);
            img_intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(img_intent);
        });

        //listener for saving. returns to the previous activity
        saveButton.setOnClickListener(view ->{
            Intent intent = new Intent(OrgEditEventActivity.this, OrgMyEventDetailsActivity.class);
            intent.putExtra("USER_EMAIL", userEmail);
            intent.putExtra("EVENT_CLICKED_CODE", eventCode);
            startActivity(intent);
        });
    }

    /**
     * load the data from the event to display image for edit event activity
     */
    private void loadEventDetails() {
        if (eventCode == -1) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            return;
        }
        String docId = Integer.toString(eventCode);
        docRef = db.collection("events").document(docId);
        docRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Event e = snapshot.toObject(Event.class);
                    if (e == null) {
                        Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //get image from class, convert to bitmap, display image.
                    String image_base64 = e.getPosterImage();
                    if (image_base64 != null) {
                        byte[] decodedBytes = Base64.decode(image_base64, Base64.DEFAULT);
                        Bitmap image_bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                        eventImage.setImageBitmap(image_bitmap);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show()
                );
    }

    private void editEventImage() {
        docRef.update("posterImage", bitmap_to_base64(image_bitmap))
                .addOnSuccessListener(aVoid -> {
                    Log.d("AdminAction", "Event image reference updated");
                    Toast.makeText(this, "Event image updated successfully", Toast.LENGTH_SHORT).show();
                    loadEventDetails();
                })
                .addOnFailureListener(e -> {
                    Log.e("AdminAction", "Error updating image", e);
                    Toast.makeText(this, "Failed to update image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String bitmap_to_base64(Bitmap bitmap_image) {
        if (bitmap_image == null) {
            return null;
        }

        //convert image
        ByteArrayOutputStream byteArray_image = new ByteArrayOutputStream();
        bitmap_image.compress(Bitmap.CompressFormat.JPEG, 100, byteArray_image);
        byte[] byteArray = byteArray_image.toByteArray();

        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
