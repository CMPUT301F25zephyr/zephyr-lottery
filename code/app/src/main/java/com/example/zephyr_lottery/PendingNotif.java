package com.example.zephyr_lottery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.concurrent.Callable;

public class PendingNotif extends DialogFragment {
    final private String title;
    final private String desc;
    final private AlertDialog dialog;
    public PendingNotif(String title, String desc) {
        this.title = title;
        this.desc = desc;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(desc)
                .setTitle(title)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        this.dialog = builder.create();
    }

    public void show() {
        dialog.show();
    }
}
