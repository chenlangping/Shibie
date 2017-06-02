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

    private static int MAXARRAY=100;
    //这个MAXARRAY是最大的数组，建议填写为 识别的方法数*4
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
        int subtractLine[] = new int[5];    //裁剪完数字的高度差
        int subtractColumn[] = new int[5];  //裁剪完后数字的宽度差


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

        String sum ="";
        for (int i = 1; i < height; i++) {
            for (int j = number10; j <= number11; j++) {
                sumLine[1][i] += test[j][i];
            }
           // Log.d("clp", "sumline[1]=" + String.valueOf(sumLine[1][i]));
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

        for(int i = number50; i <= number51; i++){
            sum = sum + String.valueOf(sumLine[1][i])+"\t";
        }
        sum += "\n";

        subtractColumn[1] = number11 - number10;
        subtractLine[1] = number51 - number50;



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
            //Log.d("clp", "sumline[2]=" + String.valueOf(sumLine[2][i]));
           // sum = sum + String.valueOf(sumLine[2][i])+"\t";

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
        for(int i = number60; i <= number61; i++){
            sum = sum + String.valueOf(sumLine[2][i])+"\t";
        }
        sum += "\n";

        subtractColumn[2] = number21 - number20;
        subtractLine[2] = number61 - number60;

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
           // Log.d("clp", "sumline[3]=" + String.valueOf(sumLine[3][i]));
            //sum = sum + String.valueOf(sumLine[3][i])+"\t";
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
        for(int i = number70; i <= number71; i++){
            sum = sum + String.valueOf(sumLine[3][i])+"\t";
        }
        sum += "\n";

        subtractColumn[3] = number31 - number30;
        subtractLine[3] = number71 - number70;

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

        for (int i = 1; i < height; i++) {
            for (int j = number40; j <= number41; j++) {
                sumLine[4][i] += test[j][i];
            }
           // Log.d("clp", "sumline[4]=" + String.valueOf(sumLine[4][i]));
            //sum = sum + String.valueOf(sumLine[4][i])+"\t";

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
        for(int i = number80; i <= number81; i++){
            sum = sum + String.valueOf(sumLine[4][i])+"\t";
        }
        sum += "\n";

        subtractColumn[4] = number41 - number40;
        subtractLine[4] = number81 - number80;

        Log.d("clp", "number1 " + String.valueOf(subtractColumn[1]));
        Log.d("clp", "number1 " + String.valueOf(subtractLine[1]));
        Log.d("clp", "number2 " + String.valueOf(subtractColumn[2]));
        Log.d("clp", "number2 " + String.valueOf(subtractLine[2]));
        Log.d("clp", "number3 " + String.valueOf(subtractColumn[3]));
        Log.d("clp", "number3 " + String.valueOf(subtractLine[3]));
        Log.d("clp", "number4 " + String.valueOf(subtractColumn[4]));
        Log.d("clp", "number4 " + String.valueOf(subtractLine[4]));

        Log.d("clp","sum = "+sum);



        if(number11!=1&&number21!=1&&number31!=1&&number41!=1){
            hasFourNums=true;
            Log.d("clp","四个数");
        }
        if(hasFourNums){
            //确保一定是四个数字
            String checkNumber="识别的数字=";

            int result[][]=new int[MAXARRAY][10];

            result[0]=identify(number10,test2);
            result[1]=identify(number20,test2);
            result[2]=identify(number30,test2);
            result[3]=identify(number40,test2);


            result[4]= identify2(1,number50,sumLine);
            result[5]= identify2(2,number60,sumLine);
            result[6]= identify2(3,number70,sumLine);
            result[7]= identify2(4,number80,sumLine);


            result[8]= identify3(subtractColumn,subtractLine,1);
            result[9]= identify3(subtractColumn,subtractLine,2);
            result[10]= identify3(subtractColumn,subtractLine,3);
            result[11]= identify3(subtractColumn,subtractLine,4);

            //result[12]= identify4(test,1,number10,number11,number50,number51);
            //result[13]= identify4(test,2,number20,number21,number60,number61);
            //result[14]= identify4(test,3,number30,number31,number70,number71);
            //result[15]= identify4(test,4,number40,number41,number80,number81);

            textView.setText(finalResult(result));

            showRusult(result);

            Log.d("clp",finalResult(result));

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

    private int[] identify(int i,int test2[]){
        String checkNumber="";
        int result[]=new int[10];
        if (
                ((test2[i]>=26&&test2[i]<=28)&&
                        (test2[i+1]>=26&&test2[i+1]<=27)&&
                        (test2[i+2]>=20&&test2[i+2]<=27)&&
                        (test2[i+3]>=15&&test2[i+3]<=26)&&
                        (test2[i+4]>=15&&test2[i+4]<=28))

                )   {
            checkNumber=checkNumber+"1";
            result[1]=1;
        }if(
                ( (test2[i]>=23&&test2[i]<=28)&&
                        (test2[i+1]==22||test2[i+1]==23)&&
                        (test2[i+2]==20||test2[i+2]==21||test2[i+2]==23||test2[i+2]==24)&&
                        (test2[i+3]==21||test2[i+3]==24||test2[i+3]==25)&&
                        (test2[i+4]==21||test2[i+4]==22||test2[i+4]==24)&&
                        (test2[i+5]==21||test2[i+5]==22||test2[i+5]==23||test2[i+5]==24)&&
                        (test2[i+6]>=18&&test2[i+6]<=23)&&
                        (test2[i+7]>=20&&test2[i+7]<=23) )

                ){
            result[2]=1;
        }if(
                ((test2[i]>=23&&test2[i]<=26)&&
                        (test2[i+1]>=22&&test2[i+1]<=25)&&
                        (test2[i+2]>=21&&test2[i+2]<=25)&&
                        (test2[i+3]>=23&&test2[i+3]<=26)&&
                        (test2[i+4]>=23&&test2[i+4]<=26)&&
                        (test2[i+5]>=18&&test2[i+5]<=26)&&
                        (test2[i+6]>=16&&test2[i+6]<=21))
                ){
            result[3]=1;
        }if(
                ((test2[i]>=26&&test2[i]<=28)&&
                        (test2[i+1]>=24&&test2[i+1]<=26)&&
                        (test2[i+2]>=24&&test2[i+2]<=26)&&
                        (test2[i+3]>=23&&test2[i+3]<=27)&&
                        (test2[i+4]>=23&&test2[i+4]<=27)&&
                        (test2[i+5]>=15&&test2[i+5]<=27)&&
                        (test2[i+6]>=15&&test2[i+6]<=16)&&
                        (test2[i+7]>=15&&test2[i+7]<=27))
                ){
            result[4]=1;
        }if(
                (test2[i]>=24&&test2[i]<=28)&&
                        (test2[i+1]>=18&&test2[i+1]<=24)&&
                        (test2[i+2]>=18&&test2[i+2]<=25)&&
                        (test2[i+3]>=22&&test2[i+3]<=24)&&
                        (test2[i+4]>=23&&test2[i+4]<=24)&&
                        (test2[i+5]>=22&&test2[i+5]<=24)&&
                        (test2[i+6]>=18&&test2[i+6]<=23)&&
                        (test2[i+7]>=19&&test2[i+7]<=21)
                ){
            result[5]=1;
        }if(
                (test2[i]>=21&&test2[i]<=27)&&
                        (test2[i+1]>=17&&test2[i+1]<=22)&&
                        (test2[i+2]>=15&&test2[i+2]<=25)&&
                        (test2[i+3]>=21&&test2[i+3]<=26)&&
                        (test2[i+4]>=23&&test2[i+4]<=27)&&
                        (test2[i+5]>=22&&test2[i+5]<=27)&&
                        (test2[i+6]>=17&&test2[i+6]<=26)&&
                        (test2[i+7]>=19&&test2[i+7]<=23)
                ){
            result[6]=1;
        }if(
                ((test2[i]>=24&&test2[i]<=28)&&
                        (test2[i+1]>=26&&test2[i+1]<=27)&&
                        (test2[i+2]>=21&&test2[i+2]<=25)&&
                        (test2[i+3]>=19&&test2[i+3]<=25)&&
                        (test2[i+4]>=20&&test2[i+4]<=25)&&
                        (test2[i+5]>=21&&test2[i+5]<=25)&&
                        (test2[i+6]>=23&&test2[i+6]<=25)&&
                        (test2[i+7]>=24&&test2[i+7]<=26))

                ){
            result[7]=1;
        }if(
                ((test2[i]>=21&&test2[i]<=28)&&
                        (test2[i+1]>=17&&test2[i+1]<=21)&&
                        (test2[i+2]>=17&&test2[i+2]<=19)&&
                        (test2[i+3]>=19&&test2[i+3]<=23)&&
                        (test2[i+4]>=23&&test2[i+4]<=25)&&
                        (test2[i+5]>=20&&test2[i+5]<=24)&&
                        (test2[i+6]>=15&&test2[i+6]<=24)&&
                        (test2[i+7]>=18&&test2[i+7]<=21))

                ){
            result[8]=1;
        }if(
                ((test2[i]>=19&&test2[i]<=27)&&
                        (test2[i+1]>=17&&test2[i+1]<=23)&&
                        (test2[i+2]>=19&&test2[i+2]<=27)&&
                        (test2[i+3]>=19&&test2[i+3]<=27)&&
                        (test2[i+4]>=25&&test2[i+4]<=27)&&
                        (test2[i+5]>=23&&test2[i+5]<=27)&&
                        (test2[i+6]>=17&&test2[i+6]<=27)&&
                        (test2[i+7]>=18&&test2[i+7]<=25))
                ){
            result[0]=1;
        }

        return result;
    }

    private int[] identify2(int flag,int i,int sumLine[][]){
        String checkNumber="";
        int result[]=new int[10];
        if(
                (sumLine[flag][i] >= 3 && sumLine[flag][i] <= 6)&&
                        (sumLine[flag][i+1] >= 2 && sumLine[flag][i+1] <= 6)&&
                        (sumLine[flag][i+2] >= 1 && sumLine[flag][i+2] <= 3)&&
                        (sumLine[flag][i+3] >= 0 && sumLine[flag][i+3] <= 6)&&
                        (sumLine[flag][i+4] >= 1 && sumLine[flag][i+4] <= 6)&&
                        (sumLine[flag][i+5] >= 2 && sumLine[flag][i+5] <= 6)&&
                        (sumLine[flag][i+6] >= 3 && sumLine[flag][i+6] <= 5)&&
                        (sumLine[flag][i+7] >= 3 && sumLine[flag][i+7] <= 6)&&
                        (sumLine[flag][i+8] >= 3 && sumLine[flag][i+8] <= 5)&&
                        (sumLine[flag][i+9] >= 0 && sumLine[flag][i+9] <= 5)
                ){
            result[1]=1;
        }
        if(
                (sumLine[flag][i] >= 4 && sumLine[flag][i] <= 5)&&
                        (sumLine[flag][i+1] >= 0 && sumLine[flag][i+1] <= 5)&&
                        (sumLine[flag][i+2] >= 2 && sumLine[flag][i+2] <= 5)&&
                        (sumLine[flag][i+3] >= 3 && sumLine[flag][i+3] <= 6)&&
                        (sumLine[flag][i+4] >= 6 && sumLine[flag][i+4] <= 8)&&
                        (sumLine[flag][i+5] >= 5 && sumLine[flag][i+5] <= 8)&&
                        (sumLine[flag][i+6] >= 5 && sumLine[flag][i+6] <= 9)&&
                        (sumLine[flag][i+7] >= 0 && sumLine[flag][i+7] <= 6)&&
                        (sumLine[flag][i+8] >= 0 && sumLine[flag][i+8] <= 7)
                ){
            //checkNumber=checkNumber+"2";
            result[2]=1;
        }
        if(
                (sumLine[flag][i] >= 4 && sumLine[flag][i] <= 6)&&
                        (sumLine[flag][i+1] >= 2 && sumLine[flag][i+1] <= 6)&&
                        (sumLine[flag][i+2] >= 2 && sumLine[flag][i+2] <= 5)&&
                        (sumLine[flag][i+3] >= 2 && sumLine[flag][i+3] <= 5)&&
                        (sumLine[flag][i+4] >= 5 && sumLine[flag][i+4] <= 8)&&
                        (sumLine[flag][i+5] >= 4 && sumLine[flag][i+5] <= 8)&&
                        (sumLine[flag][i+6] >= 4 && sumLine[flag][i+6] <= 7)&&
                        (sumLine[flag][i+7] >= 6 && sumLine[flag][i+7] <= 8)&&
                        (sumLine[flag][i+8] >= 6 && sumLine[flag][i+8] <= 7)&&
                        (sumLine[flag][i+9] >= 3 && sumLine[flag][i+9] <= 6)&&
                        (sumLine[flag][i+10] >= 3 && sumLine[flag][i+10] <= 5)&&
                        (sumLine[flag][i+11] >= 2 && sumLine[flag][i+11] <= 6)&&
                        (sumLine[flag][i+12] >= 2 && sumLine[flag][i+12] <= 5)
                ){
            //checkNumber=checkNumber+"3";
            result[3]=1;

        }
        if(
                (sumLine[flag][i] >= 7 && sumLine[flag][i] <= 10)&&
                        (sumLine[flag][i+1] >= 6 && sumLine[flag][i+1] <= 8)&&
                        (sumLine[flag][i+2] >= 5 && sumLine[flag][i+2] <= 7)&&
                        (sumLine[flag][i+3] >= 4 && sumLine[flag][i+3] <= 8)&&
                        (sumLine[flag][i+4] >= 4 && sumLine[flag][i+4] <= 10)&&
                        (sumLine[flag][i+5] >= 4 && sumLine[flag][i+5] <= 8)&&
                        (sumLine[flag][i+6] >= 4 && sumLine[flag][i+6] <= 8)&&
                        (sumLine[flag][i+7] >= 4 && sumLine[flag][i+7] <= 8)&&
                        (sumLine[flag][i+8] >= 0 && sumLine[flag][i+8] <= 5)&&
                        (sumLine[flag][i+9] >= 0 && sumLine[flag][i+9] <= 1)&&
                        (sumLine[flag][i+10] >= 0 && sumLine[flag][i+10] <= 9)&&
                        (sumLine[flag][i+11] >= 6 && sumLine[flag][i+11] <= 9)&&
                        (sumLine[flag][i+12] >= 7 && sumLine[flag][i+12] <= 10)

                ){
            //checkNumber=checkNumber+"4";
            result[4]=1;

        }
        if(
                (sumLine[flag][i] >= 1 && sumLine[flag][i] <= 3)&&
                        (sumLine[flag][i+1] >= 1 && sumLine[flag][i+1] <= 3)&&
                        (sumLine[flag][i+2] >= 1 && sumLine[flag][i+2] <= 7)&&
                        (sumLine[flag][i+3] >= 6 && sumLine[flag][i+3] <= 10)&&
                        (sumLine[flag][i+4] >= 2 && sumLine[flag][i+4] <= 10)&&
                        (sumLine[flag][i+5] >= 1 && sumLine[flag][i+5] <= 5)&&
                        (sumLine[flag][i+6] >= 2 && sumLine[flag][i+6] <= 6)&&
                        (sumLine[flag][i+7] >= 6 && sumLine[flag][i+7] <= 8)&&
                        (sumLine[flag][i+8] >= 6 && sumLine[flag][i+8] <= 7)&&
                        (sumLine[flag][i+9] >= 3 && sumLine[flag][i+9] <= 6)&&
                        (sumLine[flag][i+10] >= 3 && sumLine[flag][i+10] <= 5)&&
                        (sumLine[flag][i+11] >= 3 && sumLine[flag][i+11] <= 5)&&
                        (sumLine[flag][i+12] >= 2 && sumLine[flag][i+12] <= 5)

                ){
            //checkNumber=checkNumber+"5";
            result[5]=1;

        }
        if(
                (sumLine[flag][i] >= 3 && sumLine[flag][i] <= 8)&&
                        (sumLine[flag][i+1] >= 1 && sumLine[flag][i+1] <= 8)&&
                        (sumLine[flag][i+2] >= 4 && sumLine[flag][i+2] <= 9)&&
                        (sumLine[flag][i+3] >= 5 && sumLine[flag][i+3] <= 8)&&
                        (sumLine[flag][i+4] >= 2 && sumLine[flag][i+4] <= 8)&&
                        (sumLine[flag][i+5] >= 0 && sumLine[flag][i+5] <= 3)&&
                        (sumLine[flag][i+6] >= 2 && sumLine[flag][i+6] <= 4)&&
                        (sumLine[flag][i+7] >= 3 && sumLine[flag][i+7] <= 5)&&
                        (sumLine[flag][i+8] >= 3 && sumLine[flag][i+8] <= 5)&&
                        (sumLine[flag][i+9] >= 3 && sumLine[flag][i+9] <= 4)&&
                        (sumLine[flag][i+10] >= 3 && sumLine[flag][i+10] <= 5)&&
                        (sumLine[flag][i+11] >= 2 && sumLine[flag][i+11] <= 6)&&
                        (sumLine[flag][i+12] >= 1 && sumLine[flag][i+12] <= 6)

                ){
            //checkNumber=checkNumber+"6";
            result[6]=1;

        }
        if(
                (sumLine[flag][i] >= 0 && sumLine[flag][i] <= 1)&&
                        (sumLine[flag][i+1] >= 0 && sumLine[flag][i+1] <= 1)&&
                        (sumLine[flag][i+2] >= 0 && sumLine[flag][i+2] <= 7)&&
                        (sumLine[flag][i+3] >= 5 && sumLine[flag][i+3] <= 8)&&
                        (sumLine[flag][i+4] >= 6 && sumLine[flag][i+4] <= 8)&&
                        (sumLine[flag][i+5] >= 6 && sumLine[flag][i+5] <= 9)&&
                        (sumLine[flag][i+6] >= 6 && sumLine[flag][i+6] <= 9)&&
                        (sumLine[flag][i+7] >= 6 && sumLine[flag][i+7] <= 9)&&
                        (sumLine[flag][i+8] >= 6 && sumLine[flag][i+8] <= 9)&&
                        (sumLine[flag][i+9] >= 6 && sumLine[flag][i+9] <= 9)&&
                        (sumLine[flag][i+10] >= 6 && sumLine[flag][i+10] <= 9)&&
                        (sumLine[flag][i+11] >= 6 && sumLine[flag][i+11] <= 9)&&
                        (sumLine[flag][i+12] >= 6 && sumLine[flag][i+12] <= 9)

                ){
            //checkNumber=checkNumber+"7";
            result[7]=1;

        }
        if(
                (sumLine[flag][i] >= 3 && sumLine[flag][i] <= 6)&&
                        (sumLine[flag][i+1] >= 1 && sumLine[flag][i+1] <= 7)&&
                        (sumLine[flag][i+2] >= 2 && sumLine[flag][i+2] <= 6)&&
                        (sumLine[flag][i+3] >= 3 && sumLine[flag][i+3] <= 6)&&
                        (sumLine[flag][i+4] >= 3 && sumLine[flag][i+4] <= 6)&&
                        (sumLine[flag][i+5] >= 2 && sumLine[flag][i+5] <= 5)&&
                        (sumLine[flag][i+6] >= 2 && sumLine[flag][i+6] <= 5)&&
                        (sumLine[flag][i+7] >= 2 && sumLine[flag][i+7] <= 5)&&
                        (sumLine[flag][i+8] >= 2 && sumLine[flag][i+8] <= 6)&&
                        (sumLine[flag][i+9] >= 3 && sumLine[flag][i+9] <= 6)&&
                        (sumLine[flag][i+10] >= 3 && sumLine[flag][i+10] <= 6)&&
                        (sumLine[flag][i+11] >= 2 && sumLine[flag][i+11] <= 8)&&
                        (sumLine[flag][i+12] >= 0 && sumLine[flag][i+12] <= 6)

                ){
            result[8]=1;

        }
        if(
                (sumLine[flag][i] >= 4 && sumLine[flag][i] <= 6)&&
                        (sumLine[flag][i+1] >= 2 && sumLine[flag][i+1] <= 6)&&
                        (sumLine[flag][i+2] >= 2 && sumLine[flag][i+2] <= 5)&&
                        (sumLine[flag][i+3] >= 3 && sumLine[flag][i+3] <= 6)&&
                        (sumLine[flag][i+4] >= 3 && sumLine[flag][i+4] <= 5)&&
                        (sumLine[flag][i+5] >= 3 && sumLine[flag][i+5] <= 5)&&
                        (sumLine[flag][i+6] >= 3 && sumLine[flag][i+6] <= 6)&&
                        (sumLine[flag][i+7] >= 3 && sumLine[flag][i+7] <= 5)&&
                        (sumLine[flag][i+8] >= 4 && sumLine[flag][i+8] <= 6)&&
                        (sumLine[flag][i+9] >= 3 && sumLine[flag][i+9] <= 6)
                ){
            result[0]=1;
        }
        return result;
    }

    //identify3是根据裁剪完数字的长款比来确定的，但是只能确定部分，很多重合
    private int[] identify3(int subtractColumn[], int subtractLine[],int i){
        String checkNumber = "";
        int result[]=new int[10];

        if(
                (subtractColumn[i] == 6 && subtractLine[i] == 9)||
                        (subtractColumn[i] == 7 && subtractLine[i] == 9)
                ) {
            //checkNumber += "1";
            result[1]=1;
        }
        else if(
                //(subtractColumn[i] == 8 && subtractLine[i] == 13)||
                        (subtractColumn[i] == 9 && subtractLine[i] == 9)
                ) {
            result[2]=1;
            //checkNumber += "2";
        }
        else if(
                (subtractColumn[i] == 7 && subtractLine[i] == 12)||
                        (subtractColumn[i] == 7 && subtractLine[i] == 13)
                        //(subtractColumn[i] == 8 && subtractLine[i] == 13)||
                        //(subtractColumn[i] == 9 && subtractLine[i] == 12)

                ){
            //checkNumber += "3";
            result[3]=1;
        }
        /*else if(
                //(subtractColumn[i] == 8 && subtractLine[i] == 13)||
                        //(subtractColumn[i] == 9 && subtractLine[i] == 13)||
                        //(subtractColumn[i] == 10 && subtractLine[i] == 12)
                ){
            checkNumber += "4";
        }*/
        else if (
                (subtractColumn[i] == 8 && subtractLine[i] == 12)
                        //(subtractColumn[i] == 8 && subtractLine[i] == 13)||
                       // (subtractColumn[i] == 9 && subtractLine[i] == 12)
                ){
            //checkNumber += "5";
            result[5]=1;

        }
        /*else if(
               // (subtractColumn[i] == 8 && subtractLine[i] == 13)||
                       // (subtractColumn[i] == 9 && subtractLine[i] == 12)||
                        //(subtractColumn[i] == 10 && subtractLine[i] == 12)
                ){
            checkNumber += "6";
        }*/
        /*else if(
                //(subtractColumn[i] == 8 && subtractLine[i] == 13)||
                       // (subtractColumn[i] == 9 && subtractLine[i] == 12)||
                       // (subtractColumn[i] == 9 && subtractLine[i] == 13)
                ){
            checkNumber += "7";
        }*/
       /* else if(
                //(subtractColumn[i] == 8 && subtractLine[i] == 13)||
                        //(subtractColumn[i] == 10 && subtractLine[i] == 12)
                ){
            checkNumber += "8";
        }*/
        else if(
                (subtractColumn[i] == 10 && subtractLine[i] == 9)||
                        (subtractColumn[i] == 7 && subtractLine[i] == 13)
                ){
            result[0]=1;
            //checkNumber += "0";
        }
        //2,3,4,5,6,7,8 (8,13)
        else if (
                subtractColumn[i] == 8 && subtractLine[i] == 13
                ){
            //checkNumber += "2/3/4/5/6/7/8";
            result[2]=1;
            result[3]=1;
            result[4]=1;
            result[5]=1;
            result[6]=1;
            result[7]=1;
            result[8]=1;
        }
        //3,5,6,7 (9,12)
        else if (
                subtractColumn[i] == 9 && subtractLine[i] == 12
                ){
            //checkNumber += "3/5/6/7";
            result[3]=1;
            result[5]=1;
            result[6]=1;
            result[7]=1;
        }
        //4,7 (9,13)
        else if (
                subtractColumn[i] == 9 && subtractLine[i] == 13
                ){
            //checkNumber += "4/7";
            result[4]=1;
            result[7]=1;
        }
        //4,6,8 (10,12)
        else if (
                subtractColumn[i] == 10 && subtractLine[i] == 12
                ){
            //checkNumber += "4/6/8";
            result[4]=1;
            result[6]=1;
            result[8]=1;
        }
        //4,6,7,8无法识别
        else {
            //checkNumber = checkNumber + "无法匹配";
        }


        return result;
    }

    //identify4是根据数字特征来识别的，大致感觉是，如果是某个数字，那么test[m][n]一定是0/1。
    //number10 number11 数字的上行和下行。 number40 number41 数字的左列和右列。
    private int[] identify4(int test[][],int i,int number10, int number11, int number40, int number41){

        String checkNumber = "";

        int result[]=new int[10];

        if(
                (test[number11-3][number40] == 0 && test[number11-3][number40+1] == 0)&&
                        (test[number11-3][number40+2] == 0 && test[number11-3][number40+3] == 0)&&
                        (test[number11-3][number40+4] == 0 && test[number11-3][number40+5] == 0)&&
                        (test[number11-3][number40+6] == 0 && test[number11-3][number40+7] == 0)&&
                        (test[number11-3][number40+8] == 0 && test[number11-3][number40+9] == 0)
                ) {
            //checkNumber += "1";
            result[1]=1;
        }
        /*else if(
                ()
                )
        {
            checkNumber += "2";
        }
        else if(
                ()
                )
        {
            checkNumber += "3";
        }
        else if(
                ()
                )
        {
            checkNumber += "4";
        }*/
        else if(
                (test[number10+(number11 - number10)/2][number40] == 0 &&
                        test[number10+(number11 - number10)/2][number40+1] == 0)&&
                        (test[number10][number41] == 0 || test[number10][number41-1] == 0 ||
                                test[number10][number41-2] == 0)&&
                        (test[number11][number40+4] == 1 && test[number11][number40+5] == 1)&&
                        (test[number11-1][number40+4] == 1 && test[number11-1][number40+5] == 1)
                )
        {
            //checkNumber += "5";
            result[5]=1;
        }
        /*else if(
                ()
                )
        {
            checkNumber += "6";
        }*/
        else if(
                (test[number10+(number11 - number10)/2][number40] == 0 &&
                        test[number10+(number11 - number10)/2][number40+1] == 0)&&
                        (test[number10][number40] == 0 || test[number10][number40+1] == 0)&&
                        (test[number11][number40+(number41 - number40)/2] == 1)
                )
        {
            //checkNumber += "7";
            result[7]=1;
        }

        /*else if(
                ()
                )
        {
            checkNumber += "8";
        }*/
        else if(
                (test[number10+(number11 - number10)/2][number40] == 0 &&
                        test[number10+(number11 - number10)/2][number41] == 0)&&
                        (test[number10][number40+(number41 - number40)/2] == 0 ||
                                test[number11][number40+(number41 - number40)/2] == 0)&&
                        (test[number10+(number11-number10)/2][number40+(number41-number40)/2] == 1)
                )
        {
            //checkNumber += "0";
            result[0]=1;
        }
        else{
            //checkNumber += "无法识别";
        }

        return result;
    }

    private void showRusult(int result[][]){
        String a="";
        for(int i=0;i<MAXARRAY;i++){
            for(int j=0;j<10;j++){
                a=a+String.valueOf(result[i][j]);
            }
            Log.d("clp",a);
            a="";
        }
    }

    private String finalResult(int result[][]){
        String finalResult="";
        int array1[]=new int[10];
        int array2[]=new int[10];
        int array3[]=new int[10];
        int array4[]=new int[10];

        for(int i=0;i<MAXARRAY;i=i+4){
            for(int j=0;j<10;j++){
                array1[j]+=result[i][j];
            }
        }

        for(int i=1;i<MAXARRAY;i=i+4){
            for(int j=0;j<10;j++){
                array2[j]+=result[i][j];
            }
        }

        for(int i=2;i<MAXARRAY;i=i+4){
            for(int j=0;j<10;j++){
                array3[j]+=result[i][j];
            }
        }

        for(int i=3;i<MAXARRAY;i=i+4){
            for(int j=0;j<10;j++){
                array4[j]+=result[i][j];
            }
        }

        finalResult+=String.valueOf(maxArrayIndex(array1));
        finalResult+=String.valueOf(maxArrayIndex(array2));
        finalResult+=String.valueOf(maxArrayIndex(array3));
        finalResult+=String.valueOf(maxArrayIndex(array4));

        for(int i=0;i<10;i++){
            Log.d("clp",String.valueOf(i)+" "+String.valueOf(array1[i]));
        }

        for(int i=0;i<10;i++){
            Log.d("clp",String.valueOf(i)+" "+String.valueOf(array2[i]));
        }

        for(int i=0;i<10;i++){
            Log.d("clp",String.valueOf(i)+" "+String.valueOf(array3[i]));
        }

        for(int i=0;i<10;i++){
            Log.d("clp",String.valueOf(i)+" "+String.valueOf(array4[i]));
        }

        return finalResult;
    }

    private int maxArrayIndex(int array[]){
        //该函数返回数组最大值的下标
        //同时也获取到最大值,用maxNumber记录
        int maxIndex = 0;
        int maxNumber = array[0];
        for(int i=0; i<array.length; i++){
            if(array[i] > maxNumber){
                maxNumber = array[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}


