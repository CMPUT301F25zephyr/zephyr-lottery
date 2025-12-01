package com.example.zephyr_lottery.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.models.WaitingListEntry;
import com.example.zephyr_lottery.repositories.EventRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationPluginImplKt;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions;

import java.util.ArrayList;
import java.util.List;

public class OrgEntrantsMapActivity extends AppCompatActivity {

    private static final String TAG = "OrgEntrantsMap";

    private MapView mapView;
    private EventRepository eventRepository;
    private String eventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_org_entrants_map);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mapView = findViewById(R.id.mapView);
        eventRepository = new EventRepository();

        // OrgMyEventDetailsActivity sends EVENT_ID as a String
        eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null) {
            Log.e(TAG, "eventId is null, cannot load map");
        }

        mapView.getMapboxMap().loadStyleUri(
                Style.MAPBOX_STREETS,
                style -> loadEntrantMarkers()
        );
    }

    private void loadEntrantMarkers() {
        if (eventId == null) {
            Log.e(TAG, "eventId is null, cannot load waiting list");
            return;
        }

        eventRepository.getWaitingListWithLocations(
                eventId,
                snapshots -> {
                    if (snapshots == null) {
                        Log.e(TAG, "Received null snapshots list");
                        return;
                    }

                    List<WaitingListEntry> entries = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        if (entry != null) {
                            entries.add(entry);
                        }
                    }

                    addMarkersToMap(entries);
                },
                e -> Log.e(TAG, "Failed to load waiting list", e)
        );
    }

    private void addMarkersToMap(@NonNull List<WaitingListEntry> entries) {
        if (entries.isEmpty()) {
            Log.d(TAG, "No entrants with location to display.");
            return;
        }

        AnnotationPlugin annotationApi = AnnotationPluginImplKt.getAnnotations(mapView);
        CircleAnnotationManager circleManager =
                CircleAnnotationManagerKt.createCircleAnnotationManager(
                        annotationApi,
                        new AnnotationConfig()
                );

        List<Point> points = new ArrayList<>();

        for (WaitingListEntry entry : entries) {
            Double lat = entry.getLatitude();
            Double lng = entry.getLongitude();
            if (lat == null || lng == null) continue;

            Point point = Point.fromLngLat(lng, lat);
            points.add(point);

            CircleAnnotationOptions opts = new CircleAnnotationOptions()
                    .withPoint(point)
                    .withCircleRadius(8.0)
                    .withCircleColor("#ee4e8b")
                    .withCircleStrokeWidth(2.0)
                    .withCircleStrokeColor("#ffffff");

            circleManager.create(opts);
        }

        if (!points.isEmpty()) {
            zoomToPoints(points);
        }
    }

    private void zoomToPoints(@NonNull List<Point> points) {
        double sumLat = 0;
        double sumLng = 0;

        for (Point p : points) {
            sumLat += p.latitude();
            sumLng += p.longitude();
        }

        double centerLat = sumLat / points.size();
        double centerLng = sumLng / points.size();

        CameraOptions camera = new CameraOptions.Builder()
                .center(Point.fromLngLat(centerLng, centerLat))
                .zoom(11.0)
                .build();

        mapView.getMapboxMap().setCamera(camera);
    }
}
