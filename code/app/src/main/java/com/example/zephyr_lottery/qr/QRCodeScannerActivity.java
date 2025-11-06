package com.example.zephyr_lottery.qr;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.zephyr_lottery.activities.EventDetailActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRCodeScannerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setBeepEnabled(false);
        integrator.setPrompt("Scan event QR code");
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null) {
            String contents = result.getContents();
            if (contents.startsWith("event:")) {
                String eventId = contents.substring("event:".length());
                Intent i = EventDetailActivity.createIntent(this, eventId);
                startActivity(i);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
