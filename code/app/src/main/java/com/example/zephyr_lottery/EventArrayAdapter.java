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

/**
 * This class manages an ArrayList of Events into a ListView format
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> events;
    private Context context;

    /**
     * Creates a new EventArrayAdapter
     * @param context
     *  The current context
     * @param events
     *  The list of events to convert
     */
    public EventArrayAdapter(Context context, ArrayList<Event> events){
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    /**
     * Places an event into an event_list.xml view
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return
     * Returns the reformatted event at that position
     */
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
        event_time.setText(event.getWeekdayString() + " at " + event.getTime());

        return view;
    }

}
