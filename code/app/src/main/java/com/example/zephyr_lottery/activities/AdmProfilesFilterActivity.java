package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;

/**
 * Activity for filtering profiles by user type (All, Entrants, Organizers)
 * Admin can select filter mode and return to profiles list
 *
 * Filter Modes:
 * - 0: Show All Profiles (both entrants and organizers)
 * - 1: Show Entrants Only
 * - 2: Show Organizers Only
 *
 * Location: app/src/main/java/com/example/zephyr_lottery/activities/AdmProfilesFilterActivity.java
 */
public class AdmProfilesFilterActivity extends AppCompatActivity {

    // Filter mode constants
    public static final int FILTER_ALL = 0;
    public static final int FILTER_ENTRANTS = 1;
    public static final int FILTER_ORGANIZERS = 2;

    // UI Components
    private ImageButton btnBack;
    private RadioGroup radioGroupFilter;
    private RadioButton radioShowAll;
    private RadioButton radioEntrantsOnly;
    private RadioButton radioOrganizersOnly;
    private TextView tvCurrentFilter;
    private Button btnApplyFilter;
    private Button btnResetFilter;

    // Current filter mode
    private int currentFilterMode = FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.adm_profiles_filter_activity);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        initializeViews();

        // Get current filter mode from intent
        currentFilterMode = getIntent().getIntExtra("FILTER_MODE", FILTER_ALL);

        // Set initial radio button selection based on current filter
        setRadioButtonFromMode(currentFilterMode);

        // Update current filter display
        updateCurrentFilterText(currentFilterMode);

        // Set up click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        radioGroupFilter = findViewById(R.id.radioGroupFilter);
        radioShowAll = findViewById(R.id.radioShowAll);
        radioEntrantsOnly = findViewById(R.id.radioEntrantsOnly);
        radioOrganizersOnly = findViewById(R.id.radioOrganizersOnly);
        tvCurrentFilter = findViewById(R.id.tvCurrentFilter);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        btnResetFilter = findViewById(R.id.btnResetFilter);
    }

    private void setupClickListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Radio group - update current filter text when selection changes
        radioGroupFilter.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedMode = getFilterModeFromRadio(checkedId);
            updateCurrentFilterText(selectedMode);
        });

        // Apply filter button
        btnApplyFilter.setOnClickListener(v -> applyFilter());

        // Reset filter button
        btnResetFilter.setOnClickListener(v -> resetFilter());
    }

    /**
     * Sets the radio button selection based on filter mode
     */
    private void setRadioButtonFromMode(int mode) {
        switch (mode) {
            case FILTER_ALL:
                radioShowAll.setChecked(true);
                break;
            case FILTER_ENTRANTS:
                radioEntrantsOnly.setChecked(true);
                break;
            case FILTER_ORGANIZERS:
                radioOrganizersOnly.setChecked(true);
                break;
            default:
                radioShowAll.setChecked(true);
        }
    }

    /**
     * Gets filter mode from selected radio button
     */
    private int getFilterModeFromRadio(int radioButtonId) {
        if (radioButtonId == R.id.radioShowAll) {
            return FILTER_ALL;
        } else if (radioButtonId == R.id.radioEntrantsOnly) {
            return FILTER_ENTRANTS;
        } else if (radioButtonId == R.id.radioOrganizersOnly) {
            return FILTER_ORGANIZERS;
        }
        return FILTER_ALL; // Default
    }

    /**
     * Gets currently selected filter mode
     */
    private int getSelectedFilterMode() {
        int checkedId = radioGroupFilter.getCheckedRadioButtonId();
        return getFilterModeFromRadio(checkedId);
    }

    /**
     * Updates the current filter text display
     */
    private void updateCurrentFilterText(int mode) {
        String filterText;
        switch (mode) {
            case FILTER_ALL:
                filterText = "Current: Show All Profiles";
                break;
            case FILTER_ENTRANTS:
                filterText = "Current: Entrants Only";
                break;
            case FILTER_ORGANIZERS:
                filterText = "Current: Organizers Only";
                break;
            default:
                filterText = "Current: Show All Profiles";
        }
        tvCurrentFilter.setText(filterText);
    }

    /**
     * Applies the selected filter and returns to profiles activity
     */
    private void applyFilter() {
        int selectedMode = getSelectedFilterMode();

        // Create result intent with selected filter mode
        Intent resultIntent = new Intent();
        resultIntent.putExtra("FILTER_MODE", selectedMode);
        setResult(RESULT_OK, resultIntent);

        // Show confirmation toast
        String message = getFilterMessage(selectedMode);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Return to profiles activity
        finish();
    }

    /**
     * Resets filter to show all profiles
     */
    private void resetFilter() {
        // Select "Show All" radio button
        radioShowAll.setChecked(true);

        // Update display
        updateCurrentFilterText(FILTER_ALL);

        // Apply the reset
        Intent resultIntent = new Intent();
        resultIntent.putExtra("FILTER_MODE", FILTER_ALL);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Filter reset to show all profiles", Toast.LENGTH_SHORT).show();

        // Return to profiles activity
        finish();
    }

    /**
     * Gets a user-friendly message for the selected filter
     */
    private String getFilterMessage(int mode) {
        switch (mode) {
            case FILTER_ALL:
                return "Showing all profiles";
            case FILTER_ENTRANTS:
                return "Showing entrants only";
            case FILTER_ORGANIZERS:
                return "Showing organizers only";
            default:
                return "Filter applied";
        }
    }

    /**
     * Handle back button press
     */
    @Override
    public void onBackPressed() {
        // Don't apply filter if user presses back - just close
        super.onBackPressed();
    }
}