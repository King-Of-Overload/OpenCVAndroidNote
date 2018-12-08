package zjut.alan.opencvdemo.c3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import utils.ChapterUtils;
import utils.ImageSelectUtils;
import zjut.alan.opencvdemo.R;

public class MatOperationsActivity extends AppCompatActivity implements View.OnClickListener{
    private int REQUEST_CAPTURE_IMAGE = 1;
    private String TAG = "DEMO-OpenCV";
    private Uri fileUri;
    private Button chooseBtn, processBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mat_operations);
        chooseBtn = findViewById(R.id.select_image_btn);
        processBtn = findViewById(R.id.operation_btn);
        chooseBtn.setOnClickListener(this);
        processBtn.setOnClickListener(this);
        ChapterUtils.initLoadOpenCV(getApplicationContext());
        ChapterUtils.getWritePermission(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.select_image_btn){
            pickUpImage();
        }else if(view.getId() == R.id.operation_btn){
            //readAndWritePixels();
            //channelsAndPixels();
            //meanAndDev();
            //blendMat(1.5, 30);
            //adjustBrightAndContrast(60,60);
            //normAndAbs();
        }
    }

    //创建随机浮点数图像，归一化
    private void normAndAbs(){
        //创建随机浮点数图像
        Mat src = Mat.zeros(400,400, CvType.CV_32FC3);
        float[] data = new float[400 *400 *3];
        Random random = new Random();
        for(int i = 0; i < data.length; i++){
            //生成高斯正态分布
            data[i] = (float) random.nextGaussian();
        }
        src.put(0,0,data);
        //归一化值到0到255之间
        Mat dst = new Mat();
        Core.normalize(src,dst,0,255,Core.NORM_MINMAX, -1, new Mat());
        //类型转换
        Mat dst8u = new Mat();
        dst.convertTo(dst8u, CvType.CV_8UC3);
        Bitmap bitmap = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst8u, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ImageView iv = findViewById(R.id.chapter3_imageView);
        iv.setImageBitmap(bitmap);
    }


    //增加亮度
    private void adjustBrightAndContrast(int b, float c){
        //输入图像src1
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if(src.empty()){return;}
        //调整亮度
        Mat dst1 = new Mat();
        Core.add(src, new Scalar(b,b,b), dst1);
        //调整对比度
        Mat dst2 = new Mat();
        Core.multiply(dst1, new Scalar(c,c,c), dst2);
        //转换为bitmap
        Bitmap bitmap = Bitmap.createBitmap(src.cols(),src.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst2,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ImageView iv = findViewById(R.id.chapter3_imageView);
        iv.setImageBitmap(bitmap);
    }




    //图像权重叠加
    private void blendMat(double alpha, double gamma){
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if(src.empty()){return;}
        Mat black = Mat.zeros(src.size(), src.type());
        Mat dst = new Mat();
        //像素混合  基于权重
        Core.addWeighted(src, alpha, black, 1.0-alpha, gamma, dst);
        //转换为bitmap
        Bitmap bitmap = Bitmap.createBitmap(src.cols(),src.rows(),
                Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bitmap);
        ImageView iv = findViewById(R.id.chapter3_imageView);
        iv.setImageBitmap(bitmap);
    }



    //均值与标准差的二值分割
    private void meanAndDev(){
        //加载图像
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if(src.empty()){return;}
        //转为灰度图像
        Mat gray = new Mat();
        Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
        //计算均值与标准方差
        MatOfDouble means = new MatOfDouble();
        MatOfDouble stddevs = new MatOfDouble();
        Core.meanStdDev(gray,means,stddevs);
        //显示均值与标准方差
        double[] mean = means.toArray();
        double[] stddev = stddevs.toArray();
        Log.i(TAG, "灰度图像的均值："+ mean[0]);
        Log.i(TAG,"灰度图像标准差："+stddev[0]);
        //读取像素数组
        int width = gray.cols();
        int height = gray.rows();
        byte[] data = new byte[width*height];//单通道
        gray.get(0,0,data);
        int pv = 0;
        //根据均值，二值分割
        int t = (int)mean[0];
        for(int i = 0; i < data.length; i++){
            pv = data[i] & 0xff;
            if(pv > t){
                data[i] = (byte)255;
            }else{
                data[i] = 0;
            }
        }
        gray.put(0,0,data);
        Bitmap bm = Bitmap.createBitmap(gray.cols(), gray.rows(), Bitmap.Config.ARGB_8888);
        Mat dst = new Mat();
        Imgproc.cvtColor(gray, dst, Imgproc.COLOR_GRAY2RGBA);
        Utils.matToBitmap(dst, bm);
        ImageView iv = this.findViewById(R.id.chapter3_imageView);
        iv.setImageBitmap(bm);
        dst.release();
        gray.release();
        src.release();
    }



    //通道分离与合并
    private void channelsAndPixels(){
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if(src.empty()){
            return;
        }
        List<Mat> mv = new ArrayList<>();
        //分离
        Core.split(src,mv);
        for(Mat m : mv){
            int pv = 0;
            int channels = m.channels();
            int width = m.cols();
            int height = m.rows();
            byte[] data = new byte[channels*width*height];
            m.get(0,0,data);
            for(int i = 0; i < data.length; i++){
                pv = data[i] & 0xff;
                pv = 255 - pv;
                data[i] = (byte)pv;
            }
            m.put(0,0,data);
        }
        Core.merge(mv, src);
        Bitmap bitmap = Bitmap.createBitmap(src.cols(),src.rows(), Bitmap.Config.ARGB_8888);
        Mat dst = new Mat();
        Imgproc.cvtColor(src,dst,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(dst,bitmap);
        ImageView iv = findViewById(R.id.chapter3_imageView);
        iv.setImageBitmap(bitmap);
        dst.release();
        src.release();
    }



    //读写像素
    private void readAndWritePixels(){
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if(src.empty()){
            return;
        }
        int channels = src.channels();
        int width = src.cols();
        int height = src.rows();
        //读取一行的像素数据
        /*byte[] data = new byte[channels*width];
        int b = 0, g = 0, r = 0;
        int pv = 0;
        for(int row = 0; row < height; row++){
            src.get(row, 0, data);
            for(int col = 0; col < width; col++){
                //读取
                pv = data[col] & 0xff;
                //修改
                pv = 255 - pv;
                data[col] = (byte)pv;
            }
            src.put(row, 0, data);
        }*/

        //读取所有的像素
        int pv = 0;
        byte[] data = new byte[channels*width*height];
        src.get(0,0,data);
        for(int i = 0; i < data.length; i++){
            pv = data[i] & 0xff;
            pv = 255 - pv;
            data[i] = (byte)pv;
        }
        src.put(0,0,data);

        //显示
        Bitmap bitmap = Bitmap.createBitmap(src.cols(), src.rows(), Bitmap.Config.ARGB_8888);
        Mat dst = new Mat();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(dst,bitmap);
        ImageView iv = findViewById(R.id.chapter3_imageView);
        iv.setImageBitmap(bitmap);
    }




    private void pickUpImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent,"图像选择..."),REQUEST_CAPTURE_IMAGE);
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
        if(fileUri == null) return;
        ImageView imageView = findViewById(R.id.chapter3_imageView);
        Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath());
        imageView.setImageBitmap(bm);
    }



}
