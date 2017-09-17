package com.test.project03;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.test.project03.fragment.MallFragment;
import com.test.project03.fragment.MapFragment;
import com.test.project03.fragment.TaskFragment;
import com.test.project03.fragment.ThemeFragment;
import com.test.project03.fragment.WalletFragment;

import cn.bmob.v3.Bmob;

public class MainActivity extends CheckPermissionsActivity {
    private SlidingMenu slidingMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        Bmob.initialize(this,"b79f26077b57f9a2e2fd7a66c95603a4");

        //替换主界面内容
        getSupportFragmentManager().beginTransaction().replace(R.id.flContent, new MapFragment()).commit();
        slidingMenu=new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);//菜单靠左
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);//全屏支持触摸拖拉
        slidingMenu.setBehindOffset(320);//SlidingMenu划出时主页面显示的剩余宽度
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);//不包含ActionBar
        slidingMenu.setMenu(R.layout.left_content);

        ImageView menuImg = (ImageView) findViewById(R.id.title_bar_menu_btn);//menu按钮
        TextView tx_theme= (TextView) findViewById(R.id.theme);//主题
        TextView tx_task= (TextView) findViewById(R.id.task);//任务
        TextView tx_wallet= (TextView) findViewById(R.id.wallet);//钱包
        TextView tx_Mall= (TextView) findViewById(R.id.Mall);//商城
        TextView tx_map= (TextView) findViewById(R.id.map);//地图

        menuImg.setOnClickListener(onclick);
        //菜单跳转
        /**
         * 查看当前主题
         */
        tx_theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.flContent, new ThemeFragment()).commit();
                slidingMenu.toggle();
            }
        });
        /**
         * 任务
         */
        tx_task.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.flContent, new TaskFragment()).commit();
                slidingMenu.toggle();
            }
        });
        /**
         * 地图
         */
        tx_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.flContent,new MapFragment()).commit();
                slidingMenu.toggle();
            }
        });
        /**
         * 钱包
         */
        tx_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.flContent,new WalletFragment()).commit();
                slidingMenu.toggle();
            }
        });

        /**
         * 商城
         */
        tx_Mall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSupportFragmentManager().beginTransaction().replace(R.id.flContent,new MallFragment()).commit();
                slidingMenu.toggle();
            }
        });

    }

    /**
     * 侧边菜单栏
     */
    View.OnClickListener onclick=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.title_bar_menu_btn:
                    slidingMenu.toggle();
                    break;
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //实现按下手机Menu键弹出和关闭侧滑菜单
        if(keyCode==KeyEvent.KEYCODE_MENU){
            slidingMenu.toggle();
        }
        return super.onKeyDown(keyCode, event);
    }

}
