package com.test.project03;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by thinkpad on 2017/9/7.
 */

public class LoginActivity extends Activity {
    private EditText username;
    private EditText et_pwd;
    private Button login;
    private TextView register;
    private CheckBox look=null;
    String username_str;
    String password;
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        username=(EditText)findViewById(R.id.username);
        et_pwd=(EditText)findViewById(R.id.et_pwd1);
        login=(Button)findViewById(R.id.login);
        register=(TextView)findViewById(R.id.goregister);
        this.look=(CheckBox)findViewById(R.id.look);
        this.look.setOnClickListener(onShow);
        login.setOnClickListener(onclick);
        register.setOnClickListener(onregister);
        Bmob.initialize(this,"8a080bcc6df5e6fbbd0ee96ef18c4b1c");
        JustL();
    }


    View.OnClickListener onclick=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Login();
        }
    };
    View.OnClickListener onregister=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent=new Intent();
            intent.setClass(LoginActivity.this,Register.class);
            startActivity(intent);
//            finish();
        }
    };
    View.OnClickListener onShow=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (LoginActivity.this.look.isChecked()){
                LoginActivity.this.et_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }else{
                LoginActivity.this.et_pwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            }
        }
    };

    private void Login(){
        BmobUser user=new BmobUser();
        if (username.getText()!=null&&et_pwd.getText()!=null){
            username_str=username.getText().toString();
            password=et_pwd.getText().toString();
            user.setUsername(username_str);
            user.setPassword(password);
            user.login(new SaveListener<BmobUser>() {

                @Override
                public void done(BmobUser bmobUser, BmobException e) {
                    if (e==null){
                        toast("成功");
                        Intent intent=new Intent();
                        intent.setClass(LoginActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        toast("失败");
                    }
                }
            });
        }

    }
    private void JustL(){
        BmobUser  bmobuser=BmobUser.getCurrentUser();
        if (bmobuser!=null){
            toast("可直接登录");
            Intent intent=new Intent();
            intent.setClass(LoginActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
            //直接使用
        }
        else{
            //缓存用户对象为空时， 可打开用户注册界面…
        }
    }
    private void toast(String  message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
