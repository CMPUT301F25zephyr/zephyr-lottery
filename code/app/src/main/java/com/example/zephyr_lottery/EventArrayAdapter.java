package com.example.zephyr_lottery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class EventArrayAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> events;
    private Context context;

    public EventArrayAdapter(Context context, ArrayList<Event> events){
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.event_list, parent, false);
        }

        Event event = events.get(position);
        TextView event_name = view.findViewById(R.id.text_event_name);
        TextView event_time = view.findViewById(R.id.text_event_time);


        event_name.setText(event.getName());
        event_time.setText(event.getWeekdayString() + " at " + event.getTimes());

        return view;
    }

}
