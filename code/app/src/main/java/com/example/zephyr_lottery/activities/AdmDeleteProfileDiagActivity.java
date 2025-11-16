package com.example.zephyr_lottery.activities;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.zephyr_lottery.R;
import com.example.zephyr_lottery.UserProfile;

public class AdmDeleteProfileDiagActivity extends Dialog {

    private UserProfile profile;
    private OnDeleteConfirmedListener listener;
    private static final String CONFIRMATION_PHRASE = "DELETE PROFILE";

    private EditText etAdminPassword;
    private EditText etConfirmationPhrase;
    private Button btnConfirmDelete;
    private Button btnCancel;
    private TextView tvProfileInfo;
    private TextView tvInstructions;

    public interface OnDeleteConfirmedListener {
        void onDeleteConfirmed(UserProfile profile, String password);
    }

    public AdmDeleteProfileDiagActivity(@NonNull Context context, UserProfile profile,
                                        OnDeleteConfirmedListener listener) {
        super(context);
        this.profile = profile;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adm_delete_profile_dialog_activity);

        initializeViews();
        setupListeners();
        displayProfileInfo();
    }

    private void initializeViews() {
        tvProfileInfo = findViewById(R.id.tvProfileInfo);
        tvInstructions = findViewById(R.id.tvInstructions);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        etConfirmationPhrase = findViewById(R.id.etConfirmationPhrase);
        btnConfirmDelete = findViewById(R.id.btnConfirmDelete);
        btnCancel = findViewById(R.id.btnCancel);

        // Initially disable delete button
        btnConfirmDelete.setEnabled(false);
    }

    private void setupListeners() {
        // Text watchers to enable/disable delete button
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etAdminPassword.addTextChangedListener(validationWatcher);
        etConfirmationPhrase.addTextChangedListener(validationWatcher);

        btnConfirmDelete.setOnClickListener(v -> {
            String password = etAdminPassword.getText().toString();
            String phrase = etConfirmationPhrase.getText().toString();

            if (phrase.equals(CONFIRMATION_PHRASE)) {
                if (listener != null) {
                    listener.onDeleteConfirmed(profile, password);
                }
                dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void displayProfileInfo() {
        String info = "You are about to delete:\n\n" +
                "Name: " + profile.getUsername() + "\n" +
                "Email: " + profile.getEmail() + "\n" +
                "Type: " + profile.getType();
        tvProfileInfo.setText(info);

        String instructions = "To confirm deletion:\n" +
                "1. Enter your admin password\n" +
                "2. Type \"" + CONFIRMATION_PHRASE + "\" exactly";
        tvInstructions.setText(instructions);
    }

    private void validateInputs() {
        String password = etAdminPassword.getText().toString();
        String phrase = etConfirmationPhrase.getText().toString();

        boolean isValid = !password.isEmpty() && phrase.equals(CONFIRMATION_PHRASE);
        btnConfirmDelete.setEnabled(isValid);
    }
}