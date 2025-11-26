package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AddEventActivity extends AppCompatActivity {

    private Button save_event_button;
    private Button back_add_event_button;
    private Button upload_image_button;
    private ImageView upload_image_view;

    private FirebaseFirestore db;
    private CollectionReference eventsRef;

    //image poster
    private Bitmap image_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.add_event_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner spinner = findViewById(R.id.weekday_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.weekday_spinner_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        String user_email = getIntent().getStringExtra("USER_EMAIL");

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        back_add_event_button = findViewById(R.id.button_back_add_event);
        back_add_event_button.setOnClickListener(view -> {
            Intent intent = new Intent(AddEventActivity.this, OrgMyEventsActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });

        //activity for the selecting images from gallery thing.
        ActivityResultLauncher<Intent> activityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == AddEventActivity.RESULT_OK){
                            Intent data = result.getData();
                            Uri uri = data.getData();

                            try {
                                image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                upload_image_view.setImageBitmap(image_bitmap);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                });

        //listener for the image. click image to select from gallery. goes to activity above
        upload_image_view = findViewById(R.id.add_event_image_poster);
        upload_image_view.setOnClickListener(view ->{
            Intent img_intent = new Intent(Intent.ACTION_PICK);
            img_intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(img_intent);
        });

        save_event_button = findViewById(R.id.button_save_event_add_event);
        save_event_button.setOnClickListener(view -> {
            String event_name = ((EditText) findViewById(R.id.add_event_name)).getText().toString();
            String event_time = ((EditText) findViewById(R.id.add_event_times)).getText().toString();
            String event_weekday = spinner.getSelectedItem().toString();
            String event_price = ((EditText) findViewById(R.id.add_event_price)).getText().toString();
            String event_location = ((EditText) findViewById(R.id.add_event_location)).getText().toString();
            String event_description = ((EditText) findViewById(R.id.add_event_description)).getText().toString();
            String event_sample_size = ((EditText) findViewById(R.id.add_event_sample_size)).getText().toString();
            String event_period = ((EditText) findViewById(R.id.add_event_period)).getText().toString();
            String base64_image = bitmap_to_base64(image_bitmap); //if no image added, image_bitmap = null

            int event_limit = 0;
            String limitText = ((EditText) findViewById(R.id.add_event_ent_limit)).getText().toString();
            if (!limitText.isEmpty()) {
                try {
                    event_limit = Integer.parseInt(limitText);
                } catch (NumberFormatException e) {
                    event_limit = 0;
                }
            }

            if (event_name.isEmpty() || event_time.isEmpty() || event_price.isEmpty() ||
                    event_location.isEmpty() || event_description.isEmpty()) {
                Toast.makeText(AddEventActivity.this, "Incomplete Information.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!validFloat(event_price)) {
                Toast.makeText(AddEventActivity.this, "Price must be a number.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (base64_image != null && base64_image.length() > 500000) {
                Toast.makeText(this, "Image too large, please select a smaller image", Toast.LENGTH_SHORT).show();
                return;
            }

            Event event = new Event(event_name, event_time, user_email);
            event.setWeekdayString(event_weekday);
            event.setPrice(Float.parseFloat(event_price));
            event.setDescription(event_description);
            event.setLocation(event_location);
            event.setPeriod(event_period);
            event.setLimit(event_limit);
            event.setEntrants(new ArrayList<>());
            event.setPosterImage(base64_image);

            int sampleSize = 0;
            if (!event_sample_size.isEmpty()) {
                try {
                    sampleSize = Integer.parseInt(event_sample_size);
                    if (sampleSize < 0) sampleSize = 0;
                } catch (NumberFormatException e) {
                    sampleSize = 0;
                }
            }
            event.setSampleSize(sampleSize);

            DocumentReference docRef = eventsRef.document(Integer.toString(event.hashCode()));
            docRef.set(event);

            Intent intent = new Intent(AddEventActivity.this, OrgMyEventsActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            startActivity(intent);
        });
    }

    private boolean validFloat(String value) {
        try {
            Float.parseFloat(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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
