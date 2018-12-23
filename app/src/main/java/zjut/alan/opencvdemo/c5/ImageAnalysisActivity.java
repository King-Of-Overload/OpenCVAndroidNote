package zjut.alan.opencvdemo.c5;

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
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import utils.ChapterUtils;
import utils.ImageSelectUtils;
import zjut.alan.opencvdemo.R;

public class ImageAnalysisActivity extends AppCompatActivity implements View.OnClickListener{
    private Button selectBtn,processBtn;
    private Uri fileUri;
    private int REQUEST_CAPTURE_IMAGE = 1;
    private String TAG = "DEMO-OpenCV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_analysis);
        ChapterUtils.initLoadOpenCV(getApplicationContext());
        ChapterUtils.getWritePermission(this);
        selectBtn = findViewById(R.id.select_image_btn);
        processBtn = findViewById(R.id.analysis_measure_btn);
        selectBtn.setOnClickListener(this);
        processBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.select_image_btn){
            pickUpImage();
        }else if(view.getId() == R.id.analysis_measure_btn){
            analysisImage(7);
        }
    }

    //Sobel梯度操作
    private void sobelDemo(Mat src, Mat dst){
        //X方向梯度
        Mat gradx = new Mat();
        Imgproc.Sobel(src, gradx, CvType.CV_32F, 1,0);
        Core.convertScaleAbs(gradx, gradx);
        Log.i(TAG, "X方向梯度");
        Mat grady = new Mat();
        Imgproc.Sobel(src, grady, CvType.CV_32F, 0, 1);
        Core.convertScaleAbs(grady, grady);
        Log.i(TAG, "Y方向梯度");
        Core.addWeighted(gradx, 0.5, grady, 0.5,0,dst);
        gradx.release();
        grady.release();
    }

    //scharr梯度操作
    private void scharrDemo(Mat src, Mat dst){
        //X方向梯度
        Mat gradx = new Mat();
        Imgproc.Scharr(src,gradx,CvType.CV_32F,1,0);
        Core.convertScaleAbs(gradx,gradx);
        Log.i(TAG, "X方向梯度");
        Mat grady = new Mat();
        Imgproc.Scharr(src,grady,CvType.CV_32F,0,1);
        Core.convertScaleAbs(grady,grady);
        Log.i(TAG, "Y方向梯度");
    }

    private void analysisImage(int section){
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if(src.empty()){
            return;
        }
        Mat dst = new Mat();
        if(section == 0){
            sobelDemo(src, dst);
        }else if(section == 1){//梯度计算
            scharrDemo(src,dst);
        }else if(section == 2){//直方图数据
            Mat gray = new Mat();
            Imgproc.cvtColor(src,gray, Imgproc.COLOR_BGR2GRAY);
            //计算直方图数据并归一化
            List<Mat> images = new ArrayList<>();
            images.add(gray);
            //定义遮罩层
            Mat mask = Mat.ones(src.size(), CvType.CV_8UC1);
            Mat hist = new Mat();//保存直方图数据
            Imgproc.calcHist(images, new MatOfInt(0),mask,hist,new MatOfInt(256),
                    new MatOfFloat(0,255));
            Core.normalize(hist,hist,0,255,Core.NORM_MINMAX);//归一化
            int height = hist.rows();
            dst.create(400,400,src.type());
            dst.setTo(new Scalar(200,200,200));
            float[] histData = new float[256];//与BIns个数一致
            hist.get(0,0, histData);//读取第一个像素的数据
            int offsetX = 50;
            int offsetY = 350;
            //绘制直方图
            Imgproc.line(dst, new Point(offsetX,0), new Point(offsetX, offsetY),
                    new Scalar(0,0,0));//y轴
            Imgproc.line(dst, new Point(offsetX,offsetY), new Point(400, offsetY),
                    new Scalar(0,0,0));//X轴
            for(int i = 0; i < height - 1; i++){
                int y1 = (int) histData[i];
                Rect rect = new Rect();
                rect.x = offsetX + i;
                rect.y = offsetY - y1;
                rect.width = 1;
                rect.height = y1;
                Imgproc.rectangle(dst, rect.tl(),rect.br(),new Scalar(15,15,15));
            }
            //释放内存
            gray.release();
        }else if(section == 3){//直方图均衡化
            Mat gray = new Mat();
            Imgproc.cvtColor(src,gray, Imgproc.COLOR_BGR2GRAY);
            Imgproc.equalizeHist(gray,dst);
            Imgproc.cvtColor(gray,dst, Imgproc.COLOR_GRAY2BGR);
            gray.release();
        }else if(section == 4){//直方图反向投影
            Mat hsv = new Mat();
            String sampleFilePath = fileUri.getPath().replaceAll("target", "sample");
            Mat sample = Imgcodecs.imread(sampleFilePath);
            Imgproc.cvtColor(sample,hsv,Imgproc.COLOR_BGR2HSV);
            Mat mask = Mat.ones(sample.size(),CvType.CV_8UC1);
            Mat mHist = new Mat();
            Imgproc.calcHist(Arrays.asList(hsv), new MatOfInt(0,1),mask,mHist,
                    new MatOfInt(30,32),new MatOfFloat(0,179,0,255));
            System.out.println(mHist.rows());
            System.out.println(mHist.cols());

            Mat srcHSV = new Mat();
            Imgproc.cvtColor(src,srcHSV, Imgproc.COLOR_BGR2HSV);
            Imgproc.calcBackProject(Arrays.asList(srcHSV),new MatOfInt(0,1),
                    mHist,dst, new MatOfFloat(0,179,0,255),1);
            Core.normalize(dst,dst,0,255,Core.NORM_MINMAX);
            Imgproc.cvtColor(dst,dst,Imgproc.COLOR_GRAY2BGR);
        }else if(section == 5){//模板匹配
            String tmpFilePath = fileUri.getPath().replaceAll("lena","tmpl");
            Mat tpl = Imgcodecs.imread(tmpFilePath);
            //获取结果模板大小
            int height = src.rows() - tpl.height() + 1;
            int width = src.cols() - tpl.cols() + 1;
            Mat result = new Mat(height,width,CvType.CV_32FC1);
            //模板匹配
            int method = Imgproc.TM_CCOEFF_NORMED;//归一化相关因子
            Imgproc.matchTemplate(src, tpl, result, method);
            Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result);
            Point maxloc = minMaxLocResult.maxLoc;
            Point minloc = minMaxLocResult.minLoc;

            Point mathcloc = null;
            if(method == Imgproc.TM_SQDIFF || method == Imgproc.TM_SQDIFF_NORMED){
                mathcloc = minloc;
            }else{
                mathcloc = maxloc;
            }
            //绘制
            src.copyTo(dst);
            Imgproc.rectangle(dst,mathcloc,new Point(mathcloc.x + tpl.cols(),
                    mathcloc.y + tpl.rows()),new Scalar(0,0,255),2,8,0);
            tpl.release();
            result.release();
        }else if(section == 6){//Harris角点检测
            //定义阈值T
            int threshold = 100;
            Mat gray = new Mat();
            Mat response = new Mat();
            Mat response_norm = new Mat();
            //角点检测
            Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
            Imgproc.cornerHarris(gray,response,2,3,0.04);
            //归一化
            Core.normalize(response,response_norm,0,255,Core.NORM_MINMAX,
                    CvType.CV_32F);
            //绘制角点
            dst.create(src.size(),src.type());
            src.copyTo(dst);
            float[] data = new float[1];
            for(int j = 0; j < response_norm.rows(); j++){
                for(int i = 0; i < response_norm.cols(); i++){
                    response_norm.get(j,i,data);//获取所有的角点值
                    //通过阈值过滤角点
                    if((int)data[0] > threshold){
                        Imgproc.circle(dst,new Point(i,j),5,
                                new Scalar(0,0,255),2,8,0);
                        Log.i("Harris Corner","找到了角点");
                    }
                }
            }
            gray.release();
            response.release();
        }else if(section == 7){//Shi-Tomasi角点检测
            double k = 0.04;
            int blockSize = 3;
            double qualityLevel = 0.01;
            boolean useHarrisCorner = false;//不使用Harris角点检测
            //角点检测
            Mat gray = new Mat();
            Imgproc.cvtColor(src,gray,Imgproc.COLOR_BGR2GRAY);
            MatOfPoint corners = new MatOfPoint();
            //该API直接返回点
            Imgproc.goodFeaturesToTrack(gray,corners,100,qualityLevel,
                    10,new Mat(),blockSize,useHarrisCorner,k);
            //绘制角点
            dst.create(src.size(),src.type());
            src.copyTo(dst);
            Point[] points = corners.toArray();
            for (int i = 0; i < points.length; i++){
                Imgproc.circle(dst,points[i],5,new Scalar(0,0,255),2,8,0);
            }
            gray.release();
        }

        Bitmap bm = Bitmap.createBitmap(dst.cols(),dst.rows(), Bitmap.Config.ARGB_8888);
        Mat result = new Mat();
        Imgproc.cvtColor(dst, result, Imgproc.COLOR_BGR2RGBA);
        Utils.matToBitmap(result, bm);
        ImageView iv = findViewById(R.id.chapter5_imageView);
        iv.setImageBitmap(bm);
        src.release();
        dst.release();
        result.release();
    }




    /*
    private void (int section) {
        // read image
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if(src.empty()){
            return;
        }
        Mat dst = new Mat();

        // 演示程序部分
        if(section == 0) {
            sobelDemo(src, dst);
        } else if(section == 1) {
            scharrDemo(src, dst);
        } else if(section == 2) {
            laplianDemo(src,dst);
        } else if(section == 3) {
            edge2Demo(src, dst);
        } else if(section == 4) {
            houghLinePDemo(src, dst);
        } else if(section == 5) {
            houghLinesDemo(src, dst);
        } else if(section == 6) {
            houghCircleDemo(src, dst);
        } else if(section == 7) {
            findContoursDemo(src, dst);
        } else if(section == 8) {
            measureContours(src, dst);
        } else if(section == 9) {
            displayHistogram(src, dst);
        } else if(section == 10) {
            equalizeHistogram(src, dst);
        } else if(section == 11) {
            compareHistogram(src, dst);
        } else if(section == 12) {
            backProjectionHistogram(src, dst);
        } else if(section == 13) {
            matchTemplateDemo(src, dst);
        }
     */




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
        ImageView imageView = findViewById(R.id.chapter5_imageView);
        Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath());
        imageView.setImageBitmap(bitmap);
    }
}
