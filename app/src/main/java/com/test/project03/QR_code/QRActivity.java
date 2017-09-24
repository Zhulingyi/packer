package com.test.project03.QR_code;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.test.project03.R;

/**
 * Created by thinkpad on 2017/9/23.
 */

public class QRActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_qrcode);
        getSupportActionBar().hide();
    }
}
