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

public class EventArrayAdapter extends android.widget.ArrayAdapter<Event> {

    private static final String TAG = "EventArrayAdapter";
    private final FirebaseFirestore db;

    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Event event = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_list, parent, false);
        }

        TextView eventName = convertView.findViewById(R.id.event_name);
        TextView eventTime = convertView.findViewById(R.id.event_time);
        TextView entrantCount = convertView.findViewById(R.id.event_entrant_count);

        if (event != null) {
            eventName.setText(event.getName());
            eventTime.setText(event.getTimes() != null ? event.getTimes() : "Time not set");

            // 🔹 Live Firestore query for waiting list size
            if (event.getName() != null) {
                db.collection("events")
                        .whereEqualTo("name", event.getName())
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            if (!querySnapshot.isEmpty()) {
                                String eventId = querySnapshot.getDocuments().get(0).getId();
                                db.collection("events")
                                        .document(eventId)
                                        .collection("waitingList")
                                        .get()
                                        .addOnSuccessListener(waitSnapshot -> {
                                            int count = waitSnapshot.size();
                                            entrantCount.setText("Entrants: " + count);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Error loading waiting list size", e);
                                            entrantCount.setText("Entrants: -");
                                        });
                            } else {
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

        return convertView;
    }
}
