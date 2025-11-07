package com.example.zephyr_lottery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.UserProfile;

import java.util.ArrayList;

public class ProfileArrayAdapter extends ArrayAdapter<UserProfile> {
    private ArrayList<UserProfile> profiles;
    private Context context;

    public ProfileArrayAdapter(Context context, ArrayList<UserProfile> profiles){
        super(context, 0, profiles);
        this.profiles = profiles;
        this.context = context;
    }

    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View view = convertView;
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.profile_list, parent, false);
        }

        UserProfile profile = profiles.get(position);
        TextView profile_name = view.findViewById(R.id.text_profile_name);
        TextView profile_type = view.findViewById(R.id.text_profile_type);

        profile_name.setText(profile.getUsername());
        profile_type.setText(profile.getType());

        return view;
    }
}
