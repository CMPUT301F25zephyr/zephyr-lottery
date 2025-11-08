package com.example.zephyr_lottery.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyr_lottery.R;

import java.util.ArrayList;

public class DrawWinnersPopupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_draw_winners);

        TextView winnersText = findViewById(R.id.text_draw_winners);
        Button closeButton = findViewById(R.id.button_close_popup);

        ArrayList<String> winners = getIntent().getStringArrayListExtra("WINNERS");

        if (winners != null && !winners.isEmpty()) {
            StringBuilder sb = new StringBuilder("Selected winners:\n\n");
            for (String w : winners) {
                sb.append("• ").append(w).append("\n");
            }
            winnersText.setText(sb.toString());
        } else {
            winnersText.setText("No winners drawn.");
        }

        closeButton.setOnClickListener(v -> finish());
    }
}
