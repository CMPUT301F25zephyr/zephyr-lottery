package com.example.zephyr_lottery;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Custom ArrayAdapter used to display Event items in a ListView.
 * Each list item shows:
 *  - Event name
 *  - Event time
 *  - Current number of entrants on the waiting list (fetched live from Firestore)
 */
public class EventArrayAdapter extends android.widget.ArrayAdapter<Event> {

    private static final String TAG = "EventArrayAdapter";
    private final FirebaseFirestore db; // Firestore database reference

    /**
     * Constructor — initializes the adapter with context and event list.
     */
    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Populates each row in the ListView with data from the corresponding Event object.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the Event at the current position
        Event event = getItem(position);

        // Inflate the row layout if it's not already created
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_list, parent, false);
        }

        // Get UI references from event_list.xml
        TextView eventName = convertView.findViewById(R.id.event_name);
        TextView eventTime = convertView.findViewById(R.id.event_time);
        TextView entrantCount = convertView.findViewById(R.id.event_entrant_count);

        if (event != null) {
            // Display event name and time
            eventName.setText(event.getName());
            eventTime.setText(event.getTimes() != null ? event.getTimes() : "Time not set");

            // 🔹 Fetch the number of entrants for this event from Firestore
            if (event.getName() != null) {
                db.collection("events")
                        .whereEqualTo("name", event.getName()) // Find the event document by name
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            // If we found a matching event document
                            if (!querySnapshot.isEmpty()) {
                                String eventId = querySnapshot.getDocuments().get(0).getId();

                                // Query the waitingList subcollection for entrant count
                                db.collection("events")
                                        .document(eventId)
                                        .collection("waitingList")
                                        .get()
                                        .addOnSuccessListener(waitSnapshot -> {
                                            int count = waitSnapshot.size(); // number of docs = number of entrants
                                            entrantCount.setText("Entrants: " + count);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error loading waiting list size", e);
                                            entrantCount.setText("Entrants: -");
                                        });
                            } else {
                                // No event found with that name
                                entrantCount.setText("Entrants: -");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error finding event ID", e);
                            entrantCount.setText("Entrants: -");
                        });
            } else {
                entrantCount.setText("Entrants: -");
            }
        }

        // Return the completed list item view
        return convertView;
    }
}
