package com.example.zephyr_lottery.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.zephyr_lottery.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeOrgActivity extends AppCompatActivity {

    private ImageView imageQRCode;
    private Button back_qr_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.qr_code_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageQRCode = findViewById(R.id.imageView_QRCode);

        //we don't have to worry about the default value though.
        //because to get to this activity, we need to click an event.
        int event_code = getIntent().getIntExtra("EVENT_CLICKED_CODE", -1);
        generateQRCode(event_code);

        //get email from intent
        String user_email = getIntent().getStringExtra("USER_EMAIL");

        //listener for button to return to previous.
        back_qr_button = findViewById(R.id.button_back_qr_code);
        back_qr_button.setOnClickListener(view -> {
            Intent intent = new Intent(QRCodeOrgActivity.this, OrgMyEventDetailsActivity.class);
            intent.putExtra("USER_EMAIL", user_email);
            intent.putExtra("EVENT_CLICKED_CODE", event_code);
            startActivity(intent);
        });
    }

    private void generateQRCode(int num)
    {
        BarcodeEncoder barcodeEncoder
                = new BarcodeEncoder();
        try {//returns the bitmap(?) of the QR code
            Bitmap bitmap = barcodeEncoder.encodeBitmap(Integer.toString(num), BarcodeFormat.QR_CODE, 400, 400);

            //set new bitmap to the imageview.
            imageQRCode.setImageBitmap(bitmap);
        }
        catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
