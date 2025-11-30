package com.example.zephyr_lottery.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.repositories.EventRepository;

import java.util.ArrayList;
import java.util.List;

public class OrgMyEventChosenEntrantsActivity extends AppCompatActivity {

    private static final String TAG = "OrgChosenEntrants";

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private final ArrayList<String> chosenEntrants = new ArrayList<>();
    private final EventRepository repo = new EventRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.org_my_event_entrants_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Change title text to distinguish from normal entrants screen
        TextView title = findViewById(R.id.textView_latest_event);
        title.setText("Chosen Entrants");

        listView = findViewById(R.id.ListView_entrants);

        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                chosenEntrants
        );
        listView.setAdapter(adapter);

        int eventCode = getIntent().getIntExtra("EVENT_CLICKED_CODE", -1);
        if (eventCode == -1) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            return;
        }
        String eventId = Integer.toString(eventCode);

        loadChosenEntrants(eventId);
    }

    private void loadChosenEntrants(String eventId) {
        repo.getSelectedEntrants(
                eventId,
                this::onEntrantsLoaded,
                e -> {
                    Log.e(TAG, "Failed to load chosen entrants", e);
                    Toast.makeText(this, "Failed to load chosen entrants.", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void onEntrantsLoaded(List<String> entrants) {
        chosenEntrants.clear();
        chosenEntrants.addAll(entrants);
        adapter.notifyDataSetChanged();

        if (entrants.isEmpty()) {
            Toast.makeText(this, "No chosen entrants yet.", Toast.LENGTH_SHORT).show();
        }
    }
}
