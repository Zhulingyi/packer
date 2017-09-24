package com.test.project03.OtherActivity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.test.project03.R;

/**
 * Created by thinkpad on 2017/9/10.
 */

public class WalletActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_content);
        getSupportActionBar().hide();
    }
}
