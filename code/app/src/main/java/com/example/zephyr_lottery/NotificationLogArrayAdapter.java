package com.example.zephyr_lottery;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.zephyr_lottery.models.NotificationLog;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * ListView adapter for notification log entries.
 */
public class NotificationLogArrayAdapter extends ArrayAdapter<NotificationLog> {

    private final LayoutInflater inflater;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public NotificationLogArrayAdapter(@NonNull Context context,
                                       @NonNull List<NotificationLog> logs) {
        super(context, 0, logs);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.notification_log_list_item, parent, false);
        }

        NotificationLog log = getItem(position);
        if (log == null) return view;

        TextView titleView = view.findViewById(R.id.text_notification_title);
        TextView metaView = view.findViewById(R.id.text_notification_meta);
        TextView bodyView = view.findViewById(R.id.text_notification_body);

        String title = !TextUtils.isEmpty(log.getTitle())
                ? log.getTitle()
                : "(No title)";
        titleView.setText(title);

        StringBuilder meta = new StringBuilder();

        if (!TextUtils.isEmpty(log.getEventName())) {
            meta.append(log.getEventName()).append(" • ");
        } else if (!TextUtils.isEmpty(log.getEventId())) {
            meta.append("Event ").append(log.getEventId()).append(" • ");
        }

        if (!TextUtils.isEmpty(log.getSenderEmail())) {
            meta.append("From: ").append(log.getSenderEmail()).append(" • ");
        }
        if (!TextUtils.isEmpty(log.getRecipientEmail())) {
            meta.append("To: ").append(log.getRecipientEmail()).append(" • ");
        }

        Timestamp ts = log.getTimestamp();
        if (ts != null) {
            meta.append(dateFormat.format(ts.toDate()));
        }

        metaView.setText(meta.toString());

        String body = log.getBody();
        if (!TextUtils.isEmpty(body)) {
            bodyView.setVisibility(View.VISIBLE);
            bodyView.setText(body);
        } else {
            bodyView.setVisibility(View.GONE);
        }

        return view;
    }
}
