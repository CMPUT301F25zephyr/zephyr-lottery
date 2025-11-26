package com.example.zephyr_lottery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * This class manages an ArrayList of UserProfiles into a ListView format
 */
public class ProfileArrayAdapter extends ArrayAdapter<UserProfile> {
    private ArrayList<UserProfile> profiles;
    private Context context;
    private OnDeleteClickListener deleteClickListener;

    /**
     * Interface for handling delete button clicks
     */
    public interface OnDeleteClickListener {
        void onDeleteClick(UserProfile profile, int position);
    }

    /**
     * Creates a new ProfileArrayAdapter
     * @param context
     * The current context
     * @param profiles
     * The list of profiles to convert
     */
    public ProfileArrayAdapter(Context context, ArrayList<UserProfile> profiles){
        super(context, 0, profiles);
        this.profiles = profiles;
        this.context = context;
    }

    /**
     * Sets the delete click listener
     * @param listener
     * The listener to handle delete button clicks
     */
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.deleteClickListener = listener;
    }

    /**
     * Places a profile into a profile_list.xml view
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
     * Returns the reformatted profile at that position
     */
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.profile_list, parent, false);
        }

        UserProfile profile = profiles.get(position);
        TextView profile_name = view.findViewById(R.id.text_profile_name);
        TextView profile_type = view.findViewById(R.id.text_profile_type);
        Button btn_delete = view.findViewById(R.id.btn_delete_profile);

        profile_name.setText(profile.getUsername());
        profile_type.setText(profile.getType());

        // Hide delete button for admin accounts
        if ("admin".equalsIgnoreCase(profile.getType())) {
            btn_delete.setVisibility(View.GONE);
        } else {
            btn_delete.setVisibility(View.VISIBLE);

            // Set up delete button click listener
            btn_delete.setOnClickListener(v -> {
                if (deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(profile, position);
                }
            });
        }

        return view;
    }
}