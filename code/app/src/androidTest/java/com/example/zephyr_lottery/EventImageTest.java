package com.example.zephyr_lottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static java.util.regex.Pattern.matches;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.widget.ImageView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.zephyr_lottery.activities.EntEventDetailActivity;
import com.example.zephyr_lottery.activities.EventInvitationActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class EventImageTest {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    // Tests if image displays properly on entrant event (US 02.04.01)
    @Test
    public void testDisplayImage() throws InterruptedException {
        DocumentReference docRef = db.collection("events").document("-732375982");
        docRef.get().addOnSuccessListener(currentEvent -> {
            String testImage = currentEvent.getString("posterImage");
            byte[] decodedTestImage = Base64.decode(testImage, Base64.DEFAULT);
            Bitmap testBitmap = BitmapFactory.decodeByteArray(decodedTestImage, 0, decodedTestImage.length);

            Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EntEventDetailActivity.class);
            intent.putExtra("USER_EMAIL", "entrant@gmail.com");
            intent.putExtra("EVENT", "-732375982"); // Event with an image

            ActivityScenario<EntEventDetailActivity> scenario = ActivityScenario.launch(intent);
            scenario.onActivity(activity -> {
                Bitmap[] bitmapHolder = new Bitmap[1];
                ImageView iv = activity.findViewById(R.id.imageView_ent_eventImage);
                bitmapHolder[0] = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                assert(bitmapHolder[0].sameAs(testBitmap));
            });
        });


    }
}
