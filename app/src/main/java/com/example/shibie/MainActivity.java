package com.example.shibie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;



public class MainActivity extends AppCompatActivity {

    private String __VIEWSTATE="";
    private String __EVENTVALIDATION="";
    private String cookie="";

    private Bitmap bitmap=null;
    private ImageView imageView=null;
    private TextView textView=null;
    private Button button=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        getCookieAndPicture();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCookieAndPicture();
            }
        });
    }

    private void initView(){
        imageView=(ImageView)findViewById(R.id.picture);
        textView=(TextView)findViewById(R.id.textview);
        button=(Button)findViewById(R.id.button);

    }

    private void getCookieAndPicture() {//获取cookie
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();

                    Request request =new Request.Builder()
                            .url("http://ecard.neu.edu.cn/SelfSearch/Login.aspx")
                            .addHeader("Connection","keep-alive")

                            .build();

                    Response response=client.newCall(request).execute();

                    InputStream in=response.body().byteStream();
                    BufferedReader reader=new BufferedReader(new InputStreamReader(in));
                    String line="";
                    String __VIEWSTATEline=null;
                    String __EVENTVALIDATIONline=null;
                    while((line=reader.readLine())!=null){
                        Log.d("clp",line);
                        if(line.indexOf("__VIEWSTATE")!=-1){
                            __VIEWSTATEline=line;
                        }
                        if(line.indexOf("__EVENTVALIDATION")!=-1){
                            __EVENTVALIDATIONline=line;
                        }

                    }
                    Log.d("clp",__VIEWSTATEline);
                    Log.d("clp",__EVENTVALIDATIONline);

                    __VIEWSTATE=__VIEWSTATEline.substring(__VIEWSTATEline.indexOf("value=")+7,__VIEWSTATEline.length()-4);
                    Log.d("clp","处理过后的="+__VIEWSTATE);

                    __EVENTVALIDATION=__EVENTVALIDATIONline.substring(__EVENTVALIDATIONline.indexOf("value=")+7,__EVENTVALIDATIONline.length()-4);
                    Log.d("clp","处理过后的="+__EVENTVALIDATION);

                    String cookieAll = response.headers("Set-Cookie").get(0);
                    Log.d("clp","\n cookie字段="+cookieAll);
                    //在这一步获取到形如  ASP.NET_SessionId=jxlbbbmkih40pzd40htwvu5r; path=/; HttpOnly 这样的cookie，但是我们只需要分号之前的

                    String[] cookieList = cookieAll.split(";");
                    //用分号分割
                    cookie = cookieList[0];
                    Log.d("clp","\n"+cookie);


                    //下一步就是利用此cookie去获取图片啦

                    getPicture();

                } catch (Exception e) {

                    ToastShow("无法连接到校园卡查询中心");
                    //超时处理
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void ToastShow(final String string){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),string,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPicture() {//利用现有的cookie去获取图片
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    OkHttpClient client = new OkHttpClient();

                    Request request =new Request.Builder()
                            .url("http://ecard.neu.edu.cn/"+"SelfSearch/validateimage.ashx?0.6119085425159312")
                            //这里的0.0000000000000000数字随意取，但是要确保长度一样
                            .addHeader("Cookie",cookie+"; .NECEID=1; .NEWCAPEC1=$newcapec$:zh-CN_CAMPUS;")
                            .addHeader("Connection","keep-alive")
                            .addHeader("Referer","http://ecard.neu.edu.cn/SelfSearch/Login.aspx")
                            .build();

                    ResponseBody body = client.newCall(request).execute().body();
                    InputStream in = body.byteStream();
                    bitmap = BitmapFactory.decodeStream(in);
                    showPicture(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastShow("无法连接到校卡查询中心");
                }
            }
        }).start();
    }

    private void showPicture(final Bitmap bitmap) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bitmap);
            }
        });
    }

    private void func(Bitmap bitmap){
        //把bitmap识别一下，然后用Log.d 方法显示下就行
        //TODO FOR XUEYANG
    }
}
