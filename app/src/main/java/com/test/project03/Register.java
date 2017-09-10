package com.test.project03;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by thinkpad on 2017/9/7.
 */

public class Register extends AppCompatActivity {
    private EditText et_smscode;
    private EditText et_name ;
    private EditText et_pwd;
    private EditText et_pwd_two;
    private EditText et_phone;
    private Button btn_getCode;
    private Button btn_register;
    private String  phone;
    private String Smscode;
    private String  username;
    private String password;
    private String password_two;
//    BmobUser newuser;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regist);
        Bmob.initialize(this,"8a080bcc6df5e6fbbd0ee96ef18c4b1c");

        getSupportActionBar().hide();
        et_name=(EditText)findViewById(R.id.et_name);
        et_pwd=(EditText)findViewById(R.id.et_pwd);
        et_pwd_two=(EditText)findViewById(R.id.et_pwd_two);
//        手机短信注册部分
//        et_phone=(EditText)findViewById(R.id.btn_phone);
//        et_smscode=(EditText)findViewById(R.id.et_smscode);
//        btn_getCode=(Button)findViewById(R.id.btn_getcode);
        btn_register=(Button)findViewById(R.id.btn_register);
        btn_register.setOnClickListener(onclickListener);

//        Phone();//手机短信验证码注册
    }

    /**
     * 注册按钮
     */
    View.OnClickListener onclickListener=new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(et_name.getText()!=null&&et_pwd.getText()!=null&&et_pwd_two.getText()!=null){//&&et_phone.getText()!=null&&et_smscode.getText()!=null
                username=et_name.getText().toString();
                password=et_pwd.getText().toString();
                password_two=et_pwd_two.getText().toString();
//                phone=et_phone.getText().toString();
//                Smscode=et_smscode.getText().toString();
                if (password.equals(password_two)){
                    RegisterPart();//无短信方式
//                    RegisterPart2();//有短息方式
                }else{
                    toast("密码错误");
                }
            }else{
                toast("错误");
            }
        }
    };

    /**
     * 用户注册
     * 无手机验证
     * 2017/2/28 取消使用
     */
    private void RegisterPart(){
        BmobUser  user=new User();
        user.setUsername(username);
        user.setPassword(password);
        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User s, BmobException e) {
                if(e==null){
                    toast("注册成功:" +s.toString());
                    Intent intent=new Intent();
                    intent.setClass(Register.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    toast("注册失败"+s.toString());
                }
            }
        });
    }


    /**
     * 请求登录操作发送短信验证码
     * 待改进：
     * 接接收验证码无倒计时
     */
    private void Phone(){
        btn_getCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toast("短信已发送成功，请注意查收！");
                BmobSMS.requestSMSCode(phone,"默认短信", new QueryListener<Integer>() {
                    @Override
                    public void done(Integer smsId,BmobException ex) {
                        if(ex==null){//验证码发送成功
                            toast("短信发送成功");
                            Log.i("smile", "短信id："+smsId);//用于后续的查询本次短信发送状态
                        }
                    }
                });
            }
        });
    }

    /**
     * 注册第二版
     * 手机号收验证码
     * 注册的同时保存username和password
     *
     */
    private void RegisterPart2(){
        User user = new User();
        user.setMobilePhoneNumber(phone);         //设置手机号码（必填）
        user.setUsername(username);                //设置用户名，如果没有传用户名，则默认为手机号码
        user.setPassword(password);                  //设置用户密码
//        Phone();
        user.signOrLogin(Smscode, new SaveListener<User>() {

            @Override
            public void done(User user,BmobException e) {
                if(e==null){
                    toast("注册或登录成功");
                    Intent intent=new Intent();
                    intent.setClass(Register.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                    Log.i("smile", ""+user.getUsername()+"-"+"-"+user.getObjectId());
                }else{
                    toast("失败:" + e.getMessage());
                }

            }

        });
    }


    private void toast(String  message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

