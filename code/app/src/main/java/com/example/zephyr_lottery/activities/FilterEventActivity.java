package com.example.zephyr_lottery.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * activity for filtering events
 * can be launched from different activities and returns to the calling activity
 */
public class FilterEventActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBack;
    private EditText etEventName;
    private EditText etEventLocation;
    private TextView tvStartDateBefore;
    private TextView tvStartDateAfter;
    private TextView tvEndDateBefore;
    private TextView tvEndDateAfter;
    private EditText etPriceMin;
    private EditText etPriceMax;
    private Button btnResetFilters;
    private Button btnConfirm;

    // calendar for date picking
    private Calendar startBeforeCalendar;
    private Calendar startAfterCalendar;
    private Calendar endBeforeCalendar;
    private Calendar endAfterCalendar;
    private SimpleDateFormat dateFormat;

    // filter values
    private String eventName = "";
    private String eventLocation = "";
    private Long startDateBefore = null;
    private Long startDateAfter = null;
    private Long endDateBefore = null;
    private Long endDateAfter = null;
    private Double priceMin = null;
    private Double priceMax = null;

    // source activity tracking
    private String sourceActivity;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.filter_event_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // get source activity and user email from intent
        sourceActivity = getIntent().getStringExtra("SOURCE_ACTIVITY");
        userEmail = getIntent().getStringExtra("USER_EMAIL");

        // get existing filter values if any
        loadExistingFilters();

        // initialize calendars and date format
        initializeCalendars();
        dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        // initialize views
        initializeViews();

        // set up click listeners
        setupClickListeners();

        // populate existing filter values
        populateFilterFields();
    }

    private void initializeCalendars() {
        startBeforeCalendar = Calendar.getInstance();
        startAfterCalendar = Calendar.getInstance();
        endBeforeCalendar = Calendar.getInstance();
        endAfterCalendar = Calendar.getInstance();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etEventName = findViewById(R.id.etEventName);
        etEventLocation = findViewById(R.id.etEventLocation);
        tvStartDateBefore = findViewById(R.id.tvStartDateBefore);
        tvStartDateAfter = findViewById(R.id.tvStartDateAfter);
        tvEndDateBefore = findViewById(R.id.tvEndDateBefore);
        tvEndDateAfter = findViewById(R.id.tvEndDateAfter);
        etPriceMin = findViewById(R.id.etPriceMin);
        etPriceMax = findViewById(R.id.etPriceMax);
        btnResetFilters = findViewById(R.id.btnResetFilters);
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    private void setupClickListeners() {
        // back button
        btnBack.setOnClickListener(v -> {
            returnToSourceActivity(false);
        });

        // date picker listeners
        tvStartDateBefore.setOnClickListener(v -> showDatePicker(startBeforeCalendar, tvStartDateBefore, true));
        tvStartDateAfter.setOnClickListener(v -> showDatePicker(startAfterCalendar, tvStartDateAfter, false));
        tvEndDateBefore.setOnClickListener(v -> showDatePicker(endBeforeCalendar, tvEndDateBefore, true));
        tvEndDateAfter.setOnClickListener(v -> showDatePicker(endAfterCalendar, tvEndDateAfter, false));

        // reset filters button
        btnResetFilters.setOnClickListener(v -> resetFilters());

        // confirm button
        btnConfirm.setOnClickListener(v -> applyFilters());
    }

    private void loadExistingFilters() {
        Intent intent = getIntent();
        eventName = intent.getStringExtra("FILTER_EVENT_NAME");
        eventLocation = intent.getStringExtra("FILTER_EVENT_LOCATION");

        if (intent.hasExtra("FILTER_START_DATE_BEFORE")) {
            startDateBefore = intent.getLongExtra("FILTER_START_DATE_BEFORE", 0);
        }
        if (intent.hasExtra("FILTER_START_DATE_AFTER")) {
            startDateAfter = intent.getLongExtra("FILTER_START_DATE_AFTER", 0);
        }
        if (intent.hasExtra("FILTER_END_DATE_BEFORE")) {
            endDateBefore = intent.getLongExtra("FILTER_END_DATE_BEFORE", 0);
        }
        if (intent.hasExtra("FILTER_END_DATE_AFTER")) {
            endDateAfter = intent.getLongExtra("FILTER_END_DATE_AFTER", 0);
        }
        if (intent.hasExtra("FILTER_PRICE_MIN")) {
            priceMin = intent.getDoubleExtra("FILTER_PRICE_MIN", 0);
        }
        if (intent.hasExtra("FILTER_PRICE_MAX")) {
            priceMax = intent.getDoubleExtra("FILTER_PRICE_MAX", 0);
        }
    }

    private void populateFilterFields() {
        if (eventName != null && !eventName.isEmpty()) {
            etEventName.setText(eventName);
        }
        if (eventLocation != null && !eventLocation.isEmpty()) {
            etEventLocation.setText(eventLocation);
        }
        if (startDateBefore != null && startDateBefore > 0) {
            startBeforeCalendar.setTimeInMillis(startDateBefore);
            tvStartDateBefore.setText(dateFormat.format(startBeforeCalendar.getTime()));
        }
        if (startDateAfter != null && startDateAfter > 0) {
            startAfterCalendar.setTimeInMillis(startDateAfter);
            tvStartDateAfter.setText(dateFormat.format(startAfterCalendar.getTime()));
        }
        if (endDateBefore != null && endDateBefore > 0) {
            endBeforeCalendar.setTimeInMillis(endDateBefore);
            tvEndDateBefore.setText(dateFormat.format(endBeforeCalendar.getTime()));
        }
        if (endDateAfter != null && endDateAfter > 0) {
            endAfterCalendar.setTimeInMillis(endDateAfter);
            tvEndDateAfter.setText(dateFormat.format(endAfterCalendar.getTime()));
        }
        if (priceMin != null && priceMin > 0) {
            etPriceMin.setText(String.valueOf(priceMin));
        }
        if (priceMax != null && priceMax > 0) {
            etPriceMax.setText(String.valueOf(priceMax));
        }
    }

    private void showDatePicker(Calendar calendar, TextView textView, boolean isBefore) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    textView.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void resetFilters() {
        etEventName.setText("");
        etEventLocation.setText("");
        tvStartDateBefore.setText("[calendar input]");
        tvStartDateAfter.setText("[calendar input]");
        tvEndDateBefore.setText("[calendar input]");
        tvEndDateAfter.setText("[calendar input]");
        etPriceMin.setText("");
        etPriceMax.setText("");

        startDateBefore = null;
        startDateAfter = null;
        endDateBefore = null;
        endDateAfter = null;
        priceMin = null;
        priceMax = null;

        Toast.makeText(this, "Filters reset", Toast.LENGTH_SHORT).show();
    }

    private void applyFilters() {
        // validate and collect filter values
        eventName = etEventName.getText().toString().trim();
        eventLocation = etEventLocation.getText().toString().trim();

        // parse price values
        String priceMinStr = etPriceMin.getText().toString().trim();
        String priceMaxStr = etPriceMax.getText().toString().trim();

        try {
            if (!priceMinStr.isEmpty()) {
                priceMin = Double.parseDouble(priceMinStr);
            } else {
                priceMin = null;
            }
            if (!priceMaxStr.isEmpty()) {
                priceMax = Double.parseDouble(priceMaxStr);
            } else {
                priceMax = null;
            }

            // validate price range
            if (priceMin != null && priceMax != null && priceMin > priceMax) {
                Toast.makeText(this, "Minimum price cannot be greater than maximum price",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show();
            return;
        }

        // get date values if set
        if (!tvStartDateBefore.getText().toString().equals("[calendar input]")) {
            startDateBefore = startBeforeCalendar.getTimeInMillis();
        } else {
            startDateBefore = null;
        }
        if (!tvStartDateAfter.getText().toString().equals("[calendar input]")) {
            startDateAfter = startAfterCalendar.getTimeInMillis();
        } else {
            startDateAfter = null;
        }
        if (!tvEndDateBefore.getText().toString().equals("[calendar input]")) {
            endDateBefore = endBeforeCalendar.getTimeInMillis();
        } else {
            endDateBefore = null;
        }
        if (!tvEndDateAfter.getText().toString().equals("[calendar input]")) {
            endDateAfter = endAfterCalendar.getTimeInMillis();
        } else {
            endDateAfter = null;
        }

        // return to source activity with filters applied
        returnToSourceActivity(true);
    }

    private void returnToSourceActivity(boolean applyFilters) {
        Intent resultIntent = new Intent();

        if (applyFilters) {
            // add all filter values to intent
            if (eventName != null && !eventName.isEmpty()) {
                resultIntent.putExtra("FILTER_EVENT_NAME", eventName);
            }
            if (eventLocation != null && !eventLocation.isEmpty()) {
                resultIntent.putExtra("FILTER_EVENT_LOCATION", eventLocation);
            }
            if (startDateBefore != null) {
                resultIntent.putExtra("FILTER_START_DATE_BEFORE", startDateBefore);
            }
            if (startDateAfter != null) {
                resultIntent.putExtra("FILTER_START_DATE_AFTER", startDateAfter);
            }
            if (endDateBefore != null) {
                resultIntent.putExtra("FILTER_END_DATE_BEFORE", endDateBefore);
            }
            if (endDateAfter != null) {
                resultIntent.putExtra("FILTER_END_DATE_AFTER", endDateAfter);
            }
            if (priceMin != null) {
                resultIntent.putExtra("FILTER_PRICE_MIN", priceMin);
            }
            if (priceMax != null) {
                resultIntent.putExtra("FILTER_PRICE_MAX", priceMax);
            }

            setResult(RESULT_OK, resultIntent);
            Toast.makeText(this, "Filters applied", Toast.LENGTH_SHORT).show();
        } else {
            setResult(RESULT_CANCELED);
        }

        finish();
    }
}