package com.test.project03.OtherActivity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.test.project03.R;


/**
 * 第一个菜单栏主页面
 * Created by donkor
 */
public class ThemeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.theme_content);
        getSupportActionBar().hide();
    }
    //    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.theme_content,container, false);
//    }
}
