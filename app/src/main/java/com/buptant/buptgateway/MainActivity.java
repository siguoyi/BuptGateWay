package com.buptant.buptgateway;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText et_account;
    private EditText et_password;
    private Button bt_login;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static Handler handler;
    private String account;
    private String password;

    private String ip;
    private boolean isConnected = false;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_account = (EditText) findViewById(R.id.et_account);
        et_password = (EditText) findViewById(R.id.et_password);
        bt_login = (Button) findViewById(R.id.bt_login);

        sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        requestQueue = Volley.newRequestQueue(this);
        isConnected();

        String user = sharedPreferences.getString("account", "");
        String pwd = sharedPreferences.getString("password", "");
        if(!user.equals("") && !pwd.equals("")){
            et_account.setText(user);
            et_password.setText(pwd);
        }

        bt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                account = et_account.getText().toString();
                password = et_password.getText().toString();
                if(account.equals("")){
                    Toast.makeText(MainActivity.this, R.string.account_tips, Toast.LENGTH_SHORT).show();
                }else if(password.equals("")){
                    Toast.makeText(MainActivity.this, R.string.password_tips, Toast.LENGTH_SHORT).show();
                }else {
                    try {
                        if(isConnected){
                            logout();
                        }else {
                            login(account, password);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 0x123){
                    Toast.makeText(MainActivity.this, R.string.login_success_txt, Toast.LENGTH_SHORT).show();
                    editor.putString("account", account);
                    editor.putString("password", password);
                    Log.d(TAG, account + "  " + password);
                    editor.commit();
                    bt_login.setText(R.string.logout_txt);
                }else if(msg.what == 0x234){
                    Toast.makeText(MainActivity.this, R.string.logout_success_txt, Toast.LENGTH_SHORT).show();
                    bt_login.setText(R.string.login_txt);
                }else if(msg.what == 0x345){
                    isConnected = true;
                    bt_login.setText(R.string.logout_txt);
                }else if(msg.what == 0x456){
                    isConnected = false;
                    bt_login.setText(R.string.login_txt);
                }
            }
        };

    }

    public void isConnected() {

        new Thread(){
            @Override
            public void run() {
                boolean flag = false;
                try {
                    String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
                    Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);// ping网址3次
                    // 读取ping的内容，可以不加
                    InputStream input = p.getInputStream();
                    BufferedReader in = new BufferedReader(new InputStreamReader(input));
                    StringBuffer sb = new StringBuffer();
                    String content = "";
                    while ((content = in.readLine()) != null) {
                        sb.append(content);
                    }
                    // ping的状态
                    int status = p.waitFor();
                    if (status == 0) {
                        flag = true;
                        handler.sendEmptyMessage(0x345);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!flag){
                    handler.sendEmptyMessage(0x456);
                }
            }
        }.start();


    }

    private void login(final String account, final String password){
        String url = "http://10.3.8.211/";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                handler.sendEmptyMessage(0x123);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("DDDDD", account);
                map.put("upass", password);
                map.put("0MKKey", "");
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void logout() {
        String url = "http://10.3.8.211/F.html";
        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, response);
                        handler.sendEmptyMessage(0x234);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", error.getMessage(), error);
            }
        });
        requestQueue.add(stringRequest);
    }
}
