package zjut.alan.opencvdemo.c4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import utils.ChapterUtils;
import utils.ImageSelectUtils;
import zjut.alan.opencvdemo.R;

/**
 * 图像操作  卷积  均值模糊  最值
 */
public class ConvolutionActivity extends AppCompatActivity implements View.OnClickListener{
    private int REQUEST_CAPTURE_IMAGE = 1;
    private String TAG = "DEMO-OpenCV";
    private Uri fileUri;
    private Button selectedBtn, processBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convolution);
        ChapterUtils.getWritePermission(this);
        ChapterUtils.initLoadOpenCV(this);
        selectedBtn = findViewById(R.id.select_image_btn);
        processBtn = findViewById(R.id.convolution_btn);
        selectedBtn.setOnClickListener(this);
        processBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.select_image_btn){
            pickUpImage();
        }else if(view.getId() == R.id.convolution_btn){
            blurImage(10);
        }
    }


    //均值模糊，使用卷积操作
    private void blurImage(int type){
        //读取图片
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if(src.empty()){
            return;
        }
        Mat dst = new Mat();
        if(type == 0){//均值模糊
            Imgproc.blur(src,dst,new Size(1,50),new Point(-1,-1),
                    Core.BORDER_DEFAULT);
        }else if(type == 1){//高斯模糊,如果做抑制噪声操作，通常设置为5*5的卷积核
            Imgproc.GaussianBlur(src, dst, new Size(15,15), 15);
            //非线性，统计排序滤波
        }else if(type == 2){//中值滤波
            Imgproc.medianBlur(src, dst, 5);
        }else if(type == 3){//最大值滤波
            //生成卷积核
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));
            Imgproc.dilate(src,dst,kernel);
        }else if(type == 4){//最小值滤波  
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(3,3));
            Imgproc.erode(src,dst,kernel);
        }else if (type == 5){//高斯双边滤波
            Imgproc.bilateralFilter(src,dst,0,150,15);
        }else if(type == 6){//均值迁移滤波
            Imgproc.pyrMeanShiftFiltering(src,dst,10,50);
        }else if(type == 7){//自定义卷积核
            customerFilter(src,dst,3);
        }else if(type == 8){//形态学操作
            morphologyDemo(src,dst,6);
        }else if(type == 9){//阈值化操作
            thresholdDemo(src,dst);
        }else if(type == 10){//自适应阈值
            adpThresholdDemo(src,dst);
        }
        Bitmap bm = Bitmap.createBitmap(src.cols(),src.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst,result,Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bm);
        ImageView iv = findViewById(R.id.chapter4_imageView);
        iv.setImageBitmap(bm);
        src.release();
        dst.release();
        result.release();
    }

    //阈值化操作
    private void thresholdDemo(Mat src, Mat dst){
        int t = 127;
        int maxValue = 255;
        Mat gray = new Mat();
        Imgproc.cvtColor(src,dst, Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(gray,dst,t,maxValue, Imgproc.THRESH_OTSU);
        gray.release();
    }

    //适应性阈值
    private void adpThresholdDemo(Mat src, Mat dst) {
        int t = 127;
        int maxValue = 255;
        Mat gray = new Mat();
        Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.adaptiveThreshold(gray, dst, maxValue,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 15, 10);
        gray.release();
    }



    //形态学操作
    private void morphologyDemo(Mat src, Mat dst, int option){
        //创建结构元素
        Mat k = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,new Size(15,15),
                new Point(-1,-1));
        //形态学操作
        switch (option){
            case 0:{//膨胀
                Imgproc.morphologyEx(src, dst,Imgproc.MORPH_DILATE,k);
                break;
            }
            case 1:{//腐蚀
                Imgproc.morphologyEx(src,dst,Imgproc.MORPH_ERODE, k);
                break;
            }
            case 2:{//开操作
                Imgproc.morphologyEx(src,dst,Imgproc.MORPH_OPEN,k);
                break;
            }
            case 3:{//闭操作
                Imgproc.morphologyEx(src,dst,Imgproc.MORPH_CLOSE,k);
                break;
            }
            case 4:{//顶帽操作
                Imgproc.morphologyEx(src,dst,Imgproc.MORPH_TOPHAT,k);
                break;
            }
            case 5:{//黑帽操作
                Imgproc.morphologyEx(src,dst,Imgproc.MORPH_BLACKHAT,k);
                break;
            }
            case 6:{//基本梯度
                Imgproc.morphologyEx(src,dst,Imgproc.MORPH_GRADIENT,k);
                break;
            }
            default:break;
        }
    }

    //滤波
    private void customerFilter(Mat src, Mat dst, int type){
        if(type == 1){//自定义均值算子
            Mat k = new Mat(3,3,CvType.CV_32FC1);
            float[] data = new float[]{1.0f/9.0f,1.0f/9.0f,1.0f/9.0f
            ,1.0f/9.0f,1.0f/9.0f,1.0f/9.0f
            ,1.0f/9.0f,1.0f/9.0f,1.0f/9.0f};
            k.put(0,0,data);
        }else if(type == 2){//锐化
            Mat k = new Mat(3, 3, CvType.CV_32FC1);
            float[] data = new float[]{0,1.0f/8.0f,0,
                    1.0f/8.0f, 0.5f, 1.0f/8.0f,
                    0, 1.0f/8.0f, 0};
            k.put(0, 0, data);
        }else if(type == 3){//Robbert梯度
            Mat kx = new Mat(3,3,CvType.CV_32FC1);
            Mat ky = new Mat(3,3,CvType.CV_32FC1);
            float[] robert_x = new float[]{-1,0,0,1};
            float[] robert_y = new float[]{0,1,-1,0};
            kx.put(0,0,robert_x);
            ky.put(0,0,robert_y);
            Imgproc.filter2D(src,dst,-1,kx);
            Imgproc.filter2D(src,dst,-1,ky);
        }
    }





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
        if(fileUri == null) return;
        ImageView imageView = findViewById(R.id.chapter4_imageView);
        Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath());
        imageView.setImageBitmap(bitmap);
    }



}
