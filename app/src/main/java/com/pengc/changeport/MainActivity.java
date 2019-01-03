package com.pengc.changeport;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    private int count = 3;

    Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if(msg.what == 2){
                message.setText((CharSequence) msg.obj);
            }else if(msg.what == 1){
                try{
                    p.destroy();
                }catch (Exception e){
                    e.printStackTrace();
                }
                count--;
                mHandler.sendMessage(mHandler.obtainMessage(2,"修改成功，"+count+"秒后退出"));
                if(count == 0) {
                    MainActivity.this.finish();
                }else{
                    mHandler.sendEmptyMessageDelayed(1,1000);
                }
            }
            return true;
        }
    });

    Button btn;
    private Button getRoot;
    private Process p;
    private EditText input;
    private TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.button);
        getRoot = findViewById(R.id.getRoot);
        input = findViewById(R.id.input);
        message = findViewById(R.id.message);
        input.setSelection(input.getText().toString().trim().length());
        input.setEnabled(false);
        getRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                execRoot();
            }
        });
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exec();
            }
        });
        execRoot();
    }

    private void execRoot() {
        try{
//            String cmd = "adb tcpip 5555";
            //权限设置
            try{
                p.destroy();
            }catch (Exception e){}
            p = Runtime.getRuntime().exec("su");  //su为root用户,sh普通用户
            input.setEnabled(true);
            Toast.makeText(this,"获取root权限成功",Toast.LENGTH_SHORT).show();
        }
        catch(Throwable t)
        {
            t.printStackTrace();
            Toast.makeText(this,"获取root权限失败"+t.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void exec() {
        new AsyncTask<Void,Void,Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                try{
                    //获取输出流
                    OutputStream outputStream = p.getOutputStream();
                    PrintWriter pw=new PrintWriter(outputStream);
                    //将命令写入
                    mHandler.sendMessage(mHandler.obtainMessage(2,"开始更改端口"));
                    pw.println("setprop service.adb.tcp.port "+input.getText().toString().trim()+"\n");
                    Thread.sleep(1000);
                    mHandler.sendMessage(mHandler.obtainMessage(2,"重新启动adb服务"));
                    pw.println("stop adbd\n");
                    pw.println("start adbd\n");
                    Thread.sleep(1000);
                    mHandler.sendMessage(mHandler.obtainMessage(2,"修改成功，3秒后退出"));
                    //提交命令
                    pw.flush();
                    //关闭流操作
                    pw.close();
                    outputStream.close();
                    mHandler.sendEmptyMessageDelayed(1,1000);
                }
                catch(Throwable t)
                {
                    t.printStackTrace();
                }
                return null;
            }


        }.execute();

    }

}
