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

/**
 * dialog for confirming admin actions on events (delete event or remove image)
 * requires admin password verification and typed confirmation phrase
 */
public class AdmEventDialogActivity extends Dialog {

    private String eventName;
    private boolean isImageRemoval;
    private OnActionConfirmedListener listener;

    private TextView tvTitle;
    private TextView tvMessage;
    private EditText etPassword;
    private EditText etConfirmPhrase;
    private Button btnConfirm;
    private Button btnCancel;

    /**
     * Interface for handling confirmed actions
     */
    public interface OnActionConfirmedListener {
        void onActionConfirmed(String password);
    }

    /**
     * creates a new confirmation dialog
     * @param context context
     * @param eventName name of the event being acted upon
     * @param isImageRemoval true if removing image, false if deleting event
     * @param listener callback for when action is confirmed
     */
    public AdmEventDialogActivity(@NonNull Context context,
                                       String eventName,
                                       boolean isImageRemoval,
                                       OnActionConfirmedListener listener) {
        super(context);
        this.eventName = eventName;
        this.isImageRemoval = isImageRemoval;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adm_event_dialog_activity);

        initializeViews();
        setupContent();
        setupListeners();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvConfirmationTitle);
        tvMessage = findViewById(R.id.tvConfirmationMessage);
        etPassword = findViewById(R.id.etAdminPasswordConfirm);
        etConfirmPhrase = findViewById(R.id.etConfirmPhrase);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancelConfirm);

        // Initially disable confirm button
        btnConfirm.setEnabled(false);
    }

    private void setupContent() {
        String action = isImageRemoval ? "Remove Image" : "Delete Event";
        String confirmPhrase = getConfirmationPhrase();

        tvTitle.setText("Confirm " + action);

        String message = "Event: " + eventName +
                "\n\n" + (isImageRemoval ?
                "This will remove the event's image." :
                "WARNING: This will permanently delete the entire event!\n\nThis action cannot be undone.") +
                "\n\nEnter your admin password and type \"" + confirmPhrase + "\" to confirm.";

        tvMessage.setText(message);
        etConfirmPhrase.setHint("Type: " + confirmPhrase);
    }

    private void setupListeners() {
        // Text watchers to enable/disable confirm button
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

        etPassword.addTextChangedListener(validationWatcher);
        etConfirmPhrase.addTextChangedListener(validationWatcher);

        btnConfirm.setOnClickListener(v -> {
            String password = etPassword.getText().toString();
            String phrase = etConfirmPhrase.getText().toString();

            if (phrase.equals(getConfirmationPhrase())) {
                if (listener != null) {
                    listener.onActionConfirmed(password);
                }
                dismiss();
            }
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    /**
     * Gets the required confirmation phrase based on action type
     */
    private String getConfirmationPhrase() {
        return isImageRemoval ? "REMOVE IMAGE" : "DELETE EVENT";
    }

    /**
     * Validates input fields and enables/disables confirm button
     */
    private void validateInputs() {
        String password = etPassword.getText().toString();
        String phrase = etConfirmPhrase.getText().toString();
        String requiredPhrase = getConfirmationPhrase();

        boolean isValid = !password.isEmpty() && phrase.equals(requiredPhrase);
        btnConfirm.setEnabled(isValid);
    }
}