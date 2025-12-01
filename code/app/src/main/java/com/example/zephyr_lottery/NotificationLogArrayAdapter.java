package com.example.zephyr_lottery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.zephyr_lottery.models.NotificationLog;

import java.util.List;

/**
 * ArrayAdapter for displaying notification logs in a ListView.
 */
public class NotificationLogArrayAdapter extends ArrayAdapter<NotificationLog> {

    public NotificationLogArrayAdapter(@NonNull Context context, List<NotificationLog> logs) {
        super(context, 0, logs);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        NotificationLog log = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
        }
        TextView line1 = convertView.findViewById(android.R.id.text1);
        TextView line2 = convertView.findViewById(android.R.id.text2);

        if (log != null) {
            line1.setText("Event: " + log.getEventId() + " | To: " + log.getUserId());
            line2.setText("Type: " + log.getNotificationType() +
                    " | Sent at: " + log.getSentAt().toDate());
        }

        return convertView;
    }
}
