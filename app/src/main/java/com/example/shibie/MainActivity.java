package com.example.shibie;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
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
    private Button button2=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        getCookieAndPicture();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //bitmap=func1(bitmap);
                bitmap=binarization(bitmap);
                showPicture(bitmap);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
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
        button2=(Button)findViewById(R.id.button2);
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
                        //Log.d("clp",line);
                        if(line.indexOf("__VIEWSTATE")!=-1){
                            __VIEWSTATEline=line;
                        }
                        if(line.indexOf("__EVENTVALIDATION")!=-1){
                            __EVENTVALIDATIONline=line;
                        }

                    }
                    //Log.d("clp",__VIEWSTATEline);
                    //Log.d("clp",__EVENTVALIDATIONline);

                    __VIEWSTATE=__VIEWSTATEline.substring(__VIEWSTATEline.indexOf("value=")+7,__VIEWSTATEline.length()-4);
                    //Log.d("clp","处理过后的="+__VIEWSTATE);

                    __EVENTVALIDATION=__EVENTVALIDATIONline.substring(__EVENTVALIDATIONline.indexOf("value=")+7,__EVENTVALIDATIONline.length()-4);
                    //Log.d("clp","处理过后的="+__EVENTVALIDATION);

                    String cookieAll = response.headers("Set-Cookie").get(0);
                    //Log.d("clp","\n cookie字段="+cookieAll);
                    //在这一步获取到形如  ASP.NET_SessionId=jxlbbbmkih40pzd40htwvu5r; path=/; HttpOnly 这样的cookie，但是我们只需要分号之前的

                    String[] cookieList = cookieAll.split(";");
                    //用分号分割
                    cookie = cookieList[0];
                    //Log.d("clp","\n"+cookie);


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
                            .url("http://ecard.neu.edu.cn/SelfSearch/validateimage.ashx?0.6119085425159312")
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
                func2(bitmap);
            }
        });
    }

    private Bitmap func1(Bitmap bitmap){
        //把bitmap识别一下，然后用Log.d 方法显示下就行
        //TODO FOR XUEYANG
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        Paint paint = new Paint();
        ColorMatrixColorFilter cmcf = new ColorMatrixColorFilter(colorMatrix);
        paint.setColorFilter(cmcf);

        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.RGB_565);

        Canvas drawingCanvas = new Canvas(result);
        Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect dst = new Rect(src);
        drawingCanvas.drawBitmap(bitmap, src, dst, paint);

        return result;
    }

    private void func2(Bitmap bitmap){

        int width=0;
        int height=0;
        width=bitmap.getWidth();
        height=bitmap.getHeight();

        int test[][]=new int[width][height];
        for (int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                test[i][j]=-1;
            }
        }

        Log.d("clp","宽度："+String.valueOf(width)+"\n高度："+String.valueOf(height));

        for (int i=0;i<width;i++){
            for(int j=0;j<height;j++){
                int pixel=bitmap.getPixel(i,j);
                //Log.d("clp","像素值"+"宽度："+String.valueOf(i)+"高度："+String.valueOf(j)+"  "+String.valueOf(pixel));
                if(pixel==-1){
                    test[i][j]=1;
                }else{
                    test[i][j]=0;
                }
            }
        }
        Log.d("clp","结束");

        String line="";
        for (int i=1;i<height;i++){
            for(int j=1;j<width;j++) {
                line=line+String.valueOf(test[j][i]);
            }
            Log.d("clp",line);
            line="";
        }


        int test2[]=new int[width];
        String str="";
        int count=0;

        for (int i=1;i<height;i++){
            for(int j=1;j<width;j++) {
                test2[j]= test2[j]+test[j][i];
            }

        }

        line="";
        for (int i=1;i<width;i++){
            if(test2[i]==29){
                line=line+" ";
                str=str+"0";
                continue;
            }
            str=str+"#";
            count++;
            line=line+String.valueOf(test2[i])+"\t";
        }
        Log.d("clp",line);

        Log.d("clp",str);

        Log.d("clp",String.valueOf(str.split("#").length));
        Log.d("clp",String.valueOf(count));

        int number10=1;
        int number11=1;
        int number20=1;
        int number21=1;
        int number30=1;
        int number31=1;
        int number40=1;
        int number41=1;

        int number50 = 1;
        int number51 = 1;
        int number60 = 1;
        int number61 = 1;
        int number70 = 1;
        int number71 = 1;
        int number80 = 1;
        int number81 = 1;
        boolean hasFourNums=false;
        int sumLine[][] = new int[5][height];



        /*---------------------------1-------------------------------*/
        for(int i=1;i<width;i++){
            if(test2[i]!=29){
                Log.d("clp",String.valueOf(test2[i])+" "+ String.valueOf(i));
                number10=i;
                Log.d("clp","number10="+String.valueOf(number10));
                break;
            }
        }

        for(int i=number10+1;i<width;i++){
            if(test2[i]==29){
                number11=i-1;
                Log.d("clp","number10="+String.valueOf(number11));
                break;
            }
        }
        for (int i = 1; i < height; i++) {
            for (int j = number10; j <= number11; j++) {
                sumLine[1][i] += test[j][i];
            }
            Log.d("clp", "sumline[1]=" + String.valueOf(sumLine[1][i]));
        }

        for (int j = 1; j < height; j++) {
            if (sumLine[1][j] != (number11 - number10 + 1)) {
                number50 = j;
                Log.d("clp", "number50=" + String.valueOf(number50));
                break;
            }
        }

        for (int j = number50 + 1; j < height; j++) {
            if (sumLine[1][j] == (number11 - number10 + 1)) {
                number51 = j - 1;
                Log.d("clp", "number51=" + String.valueOf(number51));
                break;
            }
        }
        /*---------------------------2-------------------------------*/
        for(int i=number11+1;i<width;i++){
            if(test2[i]!=29){
                number20=i;
                Log.d("clp","number20="+String.valueOf(number20));
                break;
            }
        }

        for(int i=number20+1;i<width;i++){
            if(test2[i]==29){
                number21=i-1;
                Log.d("clp","number21="+String.valueOf(number21));
                break;
            }
        }
        for (int i = 1; i < height; i++) {
            for (int j = number20; j <= number21; j++) {
                sumLine[2][i] += test[j][i];
            }
            Log.d("clp", "sumline[2]=" + String.valueOf(sumLine[2][i]));
        }

        for (int j = 1; j < height; j++) {
            if (sumLine[2][j] != (number21 - number20 + 1)) {
                number60 = j;
                Log.d("clp", "number60=" + String.valueOf(number60));
                break;
            }
        }

        for (int j = number60 + 1; j < height; j++) {
            if (sumLine[2][j] == (number21 - number20 + 1)) {
                number61 = j - 1;
                Log.d("clp", "number61=" + String.valueOf(number61));
                break;
            }
        }

        /*---------------------------3-------------------------------*/
        for(int i=number21+1;i<width;i++){
            if(test2[i]!=29){
                number30=i;
                Log.d("clp","number30="+String.valueOf(number30));
                break;
            }
        }

        for(int i=number30+1;i<width;i++){
            if(test2[i]==29){
                number31=i-1;
                Log.d("clp","number31="+String.valueOf(number31));
                break;
            }
        }
        for (int i = 1; i < height; i++) {
            for (int j = number30; j <= number31; j++) {
                sumLine[3][i] += test[j][i];
            }
            Log.d("clp", "sumline[3]=" + String.valueOf(sumLine[3][i]));
        }

        for (int j = 1; j < height; j++) {
            if (sumLine[3][j] != (number31 - number30 + 1)) {
                number70 = j;
                Log.d("clp", "number70=" + String.valueOf(number70));
                break;
            }
        }

        for (int j = number70 + 1; j < height; j++) {
            if (sumLine[3][j] == (number31 - number30 + 1)) {
                number71 = j - 1;
                Log.d("clp", "number71=" + String.valueOf(number71));
                break;
            }
        }
        /*---------------------------4-------------------------------*/
        for(int i=number31+1;i<width;i++){
            if(test2[i]!=29){
                number40=i;
                Log.d("clp","number40="+String.valueOf(number40));
                break;
            }
        }

        for(int i=number40+1;i<width;i++){
            if(test2[i]==29){
                number41=i-1;
                Log.d("clp","number41="+String.valueOf(number41));
                break;
            }
        }

        if(number11!=1&&number21!=1&&number31!=1&&number41!=1){
            hasFourNums=true;
            Log.d("clp","四个数");
        }
        for (int i = 1; i < height; i++) {
            for (int j = number40; j <= number41; j++) {
                sumLine[4][i] += test[j][i];
            }
            Log.d("clp", "sumline[4]=" + String.valueOf(sumLine[4][i]));
        }

        for (int j = 1; j < height; j++) {
            if (sumLine[4][j] != (number41 - number40 + 1)) {
                number80 = j;
                Log.d("clp", "number80=" + String.valueOf(number80));
                break;
            }
        }

        for (int j = number80 + 1; j < height; j++) {
            if (sumLine[4][j] == (number41 - number40 + 1)) {
                number81 = j - 1;
                Log.d("clp", "number81=" + String.valueOf(number81));
                break;
            }
        }

        if(hasFourNums){
            //确保一定是四个数字
            String checkNumber="识别的数字=";
            checkNumber=checkNumber+identify(1,number10,number50,number51,test2,sumLine);
            checkNumber=checkNumber+identify(2,number20,number60,number61,test2,sumLine);
            checkNumber=checkNumber+identify(3,number30,number70,number71,test2,sumLine);
            checkNumber=checkNumber+identify(4,number40,number80,number81,test2,sumLine);
            Log.d("clp",checkNumber);
            textView.setText(checkNumber);
        }

    }

    public Bitmap binarization(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int area = width * height;
        int gray[][] = new int[width][height];
        int average = 0;// 灰度平均值
        int graysum = 0;
        int graymean = 0;
        int grayfrontmean = 0;
        int graybackmean = 0;
        int pixelGray;
        int front = 0;
        int back = 0;
        int[] pix = new int[width * height];
        img.getPixels(pix, 0, width, 0, 0, width, height);
        for (int i = 0; i < width; i++) { // 不算边界行和列，为避免越界
            for (int j = 0; j < height; j++) {
                int x = j * width + i;
                int r = (pix[x] >> 16) & 0xff;
                int g = (pix[x] >> 8) & 0xff;
                int b = pix[x] & 0xff;
                pixelGray = (int) (0.33 * r + 0.33 * g + 0.33 * b);// 计算每个坐标点的灰度
                gray[i][j] = (pixelGray << 16) + (pixelGray << 8) + (pixelGray);
                graysum += pixelGray;
            }
        }
        graymean = (int) (graysum / area);// 整个图的灰度平均值
        average = graymean;
        Log.d("clp","Average:"+average);
        for (int i = 0; i < width; i++) // 计算整个图的二值化阈值
        {
            for (int j = 0; j < height; j++) {
                if (((gray[i][j]) & (0x0000ff)) < graymean) {
                    graybackmean += ((gray[i][j]) & (0x0000ff));
                    back++;
                } else {
                    grayfrontmean += ((gray[i][j]) & (0x0000ff));
                    front++;
                }
            }
        }
        int frontvalue = (int) (grayfrontmean / front);// 前景中心
        int backvalue = (int) (graybackmean / back);// 背景中心
        float G[] = new float[frontvalue - backvalue + 1];// 方差数组
        int s = 0;
        Log.i("clp","Front:"+front+"**Frontvalue:"+frontvalue+"**Backvalue:"+backvalue);
        for (int i1 = backvalue; i1 < frontvalue + 1; i1++)// 以前景中心和背景中心为区间采用大津法算法（OTSU算法）
        {
            back = 0;
            front = 0;
            grayfrontmean = 0;
            graybackmean = 0;
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    if (((gray[i][j]) & (0x0000ff)) < (i1 + 1)) {
                        graybackmean += ((gray[i][j]) & (0x0000ff));
                        back++;
                    } else {
                        grayfrontmean += ((gray[i][j]) & (0x0000ff));
                        front++;
                    }
                }
            }
            grayfrontmean = (int) (grayfrontmean / front);
            graybackmean = (int) (graybackmean / back);
            G[s] = (((float) back / area) * (graybackmean - average)
                    * (graybackmean - average) + ((float) front / area)
                    * (grayfrontmean - average) * (grayfrontmean - average));
            s++;
        }
        float max = G[0];
        int index = 0;
        for (int i = 1; i < frontvalue - backvalue + 1; i++) {
            if (max < G[i]) {
                max = G[i];
                index = i;
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int in = j * width + i;
                if (((gray[i][j]) & (0x0000ff)) < (index + backvalue)) {
                    pix[in] = Color.rgb(0, 0, 0);
                } else {
                    pix[in] = Color.rgb(255, 255, 255);
                }
            }
        }

        Bitmap temp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        temp.setPixels(pix, 0, width, 0, 0, width, height);
        return temp;
    }

    private String identify(int flag,int i,int number1, int number2,int test2[],int sumLine[][]){
        String checkNumber="";
        if (
                /*((test2[i]>=26&&test2[i]<=28)&&
                        (test2[i+1]>=26&&test2[i+1]<=27)&&
                        (test2[i+2]>=20&&test2[i+2]<=27)&&
                        (test2[i+3]>=15&&test2[i+3]<=26)&&
                        (test2[i+4]>=15&&test2[i+4]<=28))||*/
                        ( (sumLine[flag][number1] >= 3&&sumLine[flag][number1] <= 4)&&
                                (sumLine[flag][number1+1] >= 2&& sumLine[flag][number1+1] <=3)&&
                                (sumLine[flag][number2] == sumLine[flag][number2 - 1])&&
                                (sumLine[flag][number2 - 1] == sumLine[flag][number2 - 2])&&
                                (sumLine[flag][number2 - 2] == sumLine[flag][number2 - 3])&&
                                (sumLine[flag][number2 - 3] == sumLine[flag][number2 - 4])&&
                                (sumLine[flag][number2 - 4] == sumLine[flag][number2 - 5])&&
                                (sumLine[flag][number2 - 5] == sumLine[flag][number2 - 6])
                        )

                )   {
            checkNumber=checkNumber+"1";
        }else if(
                ( (test2[i]>=23&&test2[i]<=28)&&
                        (test2[i+1]==22||test2[i+1]==23)&&
                        (test2[i+2]==20||test2[i+2]==21||test2[i+2]==23||test2[i+2]==24)&&
                        (test2[i+3]==21||test2[i+3]==24||test2[i+3]==25)&&
                        (test2[i+4]==21||test2[i+4]==22||test2[i+4]==24)&&
                        (test2[i+5]==21||test2[i+5]==22||test2[i+5]==23||test2[i+5]==24)&&
                        (test2[i+6]>=18&&test2[i+6]<=23)&&
                        (test2[i+7]>=20&&test2[i+7]<=23) )||
                        (sumLine[flag][number2] == 0 || sumLine[flag][number2 - 1] == 0

                        )
                ){
            checkNumber=checkNumber+"2";
        }else if(
                ((test2[i]>=23&&test2[i]<=26)&&
                        (test2[i+1]>=22&&test2[i+1]<=25)&&
                        (test2[i+2]>=21&&test2[i+2]<=25)&&
                        (test2[i+3]>=23&&test2[i+3]<=26)&&
                        (test2[i+4]>=23&&test2[i+4]<=26)&&
                        (test2[i+5]>=18&&test2[i+5]<=26)&&
                        (test2[i+6]>=16&&test2[i+6]<=21))
                ){
            checkNumber=checkNumber+"3";
        }else if(
                ((test2[i]>=26&&test2[i]<=28)&&
                        (test2[i+1]>=24&&test2[i+1]<=26)&&
                        (test2[i+2]>=24&&test2[i+2]<=26)&&
                        (test2[i+3]>=23&&test2[i+3]<=27)&&
                        (test2[i+4]>=23&&test2[i+4]<=27)&&
                        (test2[i+5]>=15&&test2[i+5]<=27)&&
                        (test2[i+6]>=15&&test2[i+6]<=16)&&
                        (test2[i+7]>=15&&test2[i+7]<=27))||
                        (
                            sumLine[flag][number2 - 3] == 0 || sumLine[flag][number2 - 4] == 0
                        )
                ){
            checkNumber=checkNumber+"4";
        }else if(
                (test2[i]>=24&&test2[i]<=28)&&
                        (test2[i+1]>=18&&test2[i+1]<=24)&&
                        (test2[i+2]>=18&&test2[i+2]<=25)&&
                        (test2[i+3]>=22&&test2[i+3]<=24)&&
                        (test2[i+4]>=23&&test2[i+4]<=24)&&
                        (test2[i+5]>=22&&test2[i+5]<=24)&&
                        (test2[i+6]>=18&&test2[i+6]<=23)&&
                        (test2[i+7]>=19&&test2[i+7]<=21)
                ){
            checkNumber=checkNumber+"5";
        }else if(
                (test2[i]>=21&&test2[i]<=27)&&
                        (test2[i+1]>=17&&test2[i+1]<=22)&&
                        (test2[i+2]>=15&&test2[i+2]<=25)&&
                        (test2[i+3]>=21&&test2[i+3]<=26)&&
                        (test2[i+4]>=23&&test2[i+4]<=27)&&
                        (test2[i+5]>=22&&test2[i+5]<=27)&&
                        (test2[i+6]>=17&&test2[i+6]<=26)&&
                        (test2[i+7]>=19&&test2[i+7]<=23)
                ){
            checkNumber=checkNumber+"6";
        }else if(
                ((test2[i]>=24&&test2[i]<=28)&&
                        (test2[i+1]>=26&&test2[i+1]<=27)&&
                        (test2[i+2]>=21&&test2[i+2]<=25)&&
                        (test2[i+3]>=19&&test2[i+3]<=25)&&
                        (test2[i+4]>=20&&test2[i+4]<=25)&&
                        (test2[i+5]>=21&&test2[i+5]<=25)&&
                        (test2[i+6]>=23&&test2[i+6]<=25)&&
                        (test2[i+7]>=24&&test2[i+7]<=26))||
                        (
                                sumLine[flag][number1] == 0 || sumLine[flag][number1 + 1] == 0
//
                        )
                ){
            checkNumber=checkNumber+"7";
        }else if(
                ((test2[i]>=21&&test2[i]<=28)&&
                        (test2[i+1]>=17&&test2[i+1]<=21)&&
                        (test2[i+2]>=17&&test2[i+2]<=19)&&
                        (test2[i+3]>=19&&test2[i+3]<=23)&&
                        (test2[i+4]>=23&&test2[i+4]<=25)&&
                        (test2[i+5]>=20&&test2[i+5]<=24)&&
                        (test2[i+6]>=15&&test2[i+6]<=24)&&
                        (test2[i+7]>=18&&test2[i+7]<=21))&&
                        ((Math.abs(sumLine[flag][number1]-sumLine[flag][number2]) <= 2 )||
                                (Math.abs(sumLine[flag][number1 + 1]-sumLine[flag][number2 - 1]) <= 2) &&
                                (Math.abs(sumLine[flag][number1 + 2]-sumLine[flag][number2 - 2]) <= 2) &&
                                (Math.abs(sumLine[flag][number1 + 3]-sumLine[flag][number2 - 3]) <= 2) &&
                                (Math.abs(sumLine[flag][number1 + 4]-sumLine[flag][number2 - 4]) <= 2) &&
                                (Math.abs(sumLine[flag][number1 + 5]-sumLine[flag][number2 - 5]) <= 2) &&
                                (Math.abs(sumLine[flag][number1 + 6]-sumLine[flag][number2 - 6]) <= 2) &&
                                (sumLine[flag][number1] >= 4 && sumLine[flag][number1] <= 5)&&
                                (sumLine[flag][number1 + 6] <= 5 && sumLine[flag][number1 + 7] <= 5)
                        )
                ){
            checkNumber=checkNumber+"8";
        }else if(
                ((test2[i]>=19&&test2[i]<=27)&&
                        (test2[i+1]>=17&&test2[i+1]<=23)&&
                        (test2[i+2]>=19&&test2[i+2]<=27)&&
                        (test2[i+3]>=19&&test2[i+3]<=27)&&
                        (test2[i+4]>=25&&test2[i+4]<=27)&&
                        (test2[i+5]>=23&&test2[i+5]<=27)&&
                        (test2[i+6]>=17&&test2[i+6]<=27)&&
                        (test2[i+7]>=18&&test2[i+7]<=25))
                ){
            checkNumber=checkNumber+"0";
        }else{
            checkNumber=checkNumber+"无法匹配";
        }
        return checkNumber;
    }
}
