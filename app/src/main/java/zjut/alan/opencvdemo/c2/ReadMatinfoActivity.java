package zjut.alan.opencvdemo.c2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import utils.ChapterUtils;
import utils.ImageSelectUtils;
import zjut.alan.opencvdemo.R;

/**
 * Mat对象
 */
public class ReadMatinfoActivity extends AppCompatActivity implements View.OnClickListener{
    private Button selectBtn,infoBtn;
    private static int REQUEST_CAPTURE_IMAGE = 1;
    private Uri fileUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_matinfo);
        //申请权限
        ChapterUtils.getWritePermission(this);
        selectBtn = findViewById(R.id.select_image_btn);
        infoBtn = findViewById(R.id.get_matInfo_btn);
        selectBtn.setOnClickListener(this);
        infoBtn.setOnClickListener(this);
        ChapterUtils.initLoadOpenCV(getApplicationContext());
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.select_image_btn){
            pickUpImage();
        }else if(view.getId() == R.id.get_matInfo_btn){
            //bitmap2MatDemo();
            //matToBitmapDemo();
            //basicDrawOnCanvas();
            //basicDrawOnMat();
            scanPixelsDemo();
        }
    }

    //选择照片方法
    private void pickUpImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"图像选择..."),REQUEST_CAPTURE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri uri = data.getData();
                File f = new File(ImageSelectUtils.getRealPath(uri,getApplicationContext()));
                fileUri = Uri.fromFile(f);
            }
        }
        //显示
        if(fileUri == null) return;
        ImageView imageView = findViewById(R.id.matInfo_imageView);
        Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath());
        imageView.setImageBitmap(bm);
    }


    //bitmap转mat
    private void bitmap2MatDemo(){
        Bitmap bitmap = Bitmap.createBitmap(500,500, Bitmap.Config.ARGB_8888);
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap,mat);
        //变成圆形
        Imgproc.circle(mat,new Point(mat.cols() / 2,mat.rows() / 2),50,
                new Scalar(28,0,0,255));
        Utils.matToBitmap(mat,bitmap);
        ImageView iv = findViewById(R.id.matInfo_imageView);
        iv.setImageBitmap(bitmap);
    }

    //mat转bitmap
    private void matToBitmapDemo(){
        Mat src = Imgcodecs.imread(fileUri.getPath());
        int width = src.cols();
        int height = src.rows();
        Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        Mat dst = new Mat();
        Imgproc.cvtColor(src,dst,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(dst,bitmap);
        dst.release();
        ImageView iv = findViewById(R.id.matInfo_imageView);
        iv.setImageBitmap(bitmap);
    }

    //在画布上绘图
    private void basicDrawOnCanvas(){
        Bitmap bitmap = Bitmap.createBitmap(500,500, Bitmap.Config.ARGB_8888);
        //创建画布与画笔风格
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        //绘制直线
        canvas.drawLine(10,10,490,490,paint);//起始点
        canvas.drawLine(10,490,490,10,paint);
        //绘制矩形
        Rect rect = new Rect();
        rect.set(50,50,150,150);//左上角点右下角点
        canvas.drawRect(rect,paint);
        //绘制圆
        paint.setColor(Color.GREEN);
        canvas.drawCircle(400,400,50,paint);
        //绘制文本
        paint.setColor(Color.RED);
        canvas.drawText("渡边麻美",40,40,paint);//起始点
        //显示结果
        ImageView iv = findViewById(R.id.matInfo_imageView);
        iv.setImageBitmap(bitmap);
        //bitmap.recycle();
    }

    //在Mat上画图
    private void basicDrawOnMat(){
        Mat src = Mat.zeros(500,500, CvType.CV_8UC3);
        Imgproc.ellipse(src,new Point(250,250),new Size(100,50),
                360,0,360,new Scalar(0,0,255),2,8,0);
        //长轴与短轴
        Imgproc.putText(src,"幼儿缘",new Point(20,20),
                Core.FONT_HERSHEY_PLAIN,1.0,new Scalar(0,255,0),1);
        org.opencv.core.Rect rect = new org.opencv.core.Rect();
        rect.x = 50;
        rect.y = 50;
        rect.width = 100;
        rect.height = 100;
        Imgproc.rectangle(src,rect.tl(),rect.br(),
                new Scalar(255,0,0),2,8,0);//tl左上 br右下
        Imgproc.circle(src,new Point(400,400),50,
                new Scalar(0,255,0),2,8,0);
        Bitmap bitmap = Bitmap.createBitmap(src.cols(),src.rows(), Bitmap.Config.ARGB_8888);
        Mat dst = new Mat();
        Imgproc.cvtColor(src,dst,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(dst,bitmap);
        ImageView iv = findViewById(R.id.matInfo_imageView);
        iv.setImageBitmap(bitmap);

    }

    //读入缓冲区，修改像素值
    private void scanPixelsDemo(){
        //一定要设置为true,可变
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bac).copy(Bitmap.Config.ARGB_8888,true);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap.Config config = bitmap.getConfig();
        //创建一个像素数组缓冲区
        int[] pixels = new int[width*height];
        bitmap.getPixels(pixels,0,width,0,0,width,height);
        int a = 0, r = 0, g = 0, b = 0;
        int index = 0;
        for(int row = 0; row < height; row++){
            for(int col = 0; col < width; col++){
                //读取像素
                index = width*row + col;
                a = (pixels[index] >> 24) & 0xff;
                r = (pixels[index] >> 16) & 0xff;
                g = (pixels[index] >> 8) & 0xff;
                b= pixels[index] & 0xff;
                //修改像素
                r = 255 - r;
                g = 255 - g;
                b = 255 - b;
                //保存到bitmap
                pixels[index] = (a << 24) | (g << 16) | (b << 8) | b;
            }
        }
        bitmap.setPixels(pixels,0, width,0,0,width,height);
        ImageView iv = findViewById(R.id.matInfo_imageView);
        iv.setImageBitmap(bitmap);
    }




}
