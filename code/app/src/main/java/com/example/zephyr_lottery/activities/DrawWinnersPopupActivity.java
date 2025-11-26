package com.example.zephyr_lottery.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyr_lottery.R;

import java.util.ArrayList;

public class DrawWinnersPopupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_draw_winners);

        ListView profileList = findViewById(R.id.list_draw_winners);
        Button closeButton = findViewById(R.id.button_close_popup);

        ArrayList<String> winners = getIntent().getStringArrayListExtra("WINNERS");

        //set list view to list of emails chosen
        if (winners != null && !winners.isEmpty()) {
            ArrayAdapter<String> winner_arrayadapter = new ArrayAdapter<>(this,
                    R.layout.draw_winner_list_view,
                    R.id.textView_draw_winner_list,
                    winners);
            //System.out.println(winners);
            profileList.setAdapter(winner_arrayadapter);

        } else {//no profiles chosen
            ArrayList<String> emptyList = new ArrayList<>();
            emptyList.add("No winners drawn.");
            ArrayAdapter<String> emptyAdapter = new ArrayAdapter<>(this,
                    R.layout.draw_winner_list_view,
                    R.id.textView_draw_winner_list,
                    emptyList);
            profileList.setAdapter(emptyAdapter);
        }

        closeButton.setOnClickListener(v -> finish());
    }
}
