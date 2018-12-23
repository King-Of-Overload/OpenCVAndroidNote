package zjut.alan.opencvdemo.c7;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import utils.ChapterUtils;
import zjut.alan.opencvdemo.R;

public class DisplayModeActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    private MyCvCameraView mOpenCvCameraView;
    private int option;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_mode);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ChapterUtils.initLoadOpenCV(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = findViewById(R.id.full_screen_camera_id);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        //后置摄像头开启预览
        mOpenCvCameraView.setCameraIndex(0);
        mOpenCvCameraView.enableView();
        try {
            initFaceDetector();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //初始化面部识别
    private void initFaceDetector() throws IOException{
        System.loadLibrary("zjut_face_detection");
        InputStream inputStream = getResources().openRawResource(R.raw.lbpcascade_frontalface);
        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
        File file = new File(cascadeDir.getAbsoluteFile(),"lbpcascade_frontalface.xml");
        FileOutputStream outputStream = new FileOutputStream(file);
        byte[] buff = new byte[1024];
        int len = 0;
        while((len = inputStream.read(buff)) != -1){
            outputStream.write(buff,0, len);
        }
        inputStream.close();
        outputStream.close();
        initLoad(file.getAbsolutePath());
        file.delete();
        cascadeDir.delete();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        if(getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            Core.rotate(frame, frame, Core.ROTATE_90_CLOCKWISE);//顺时针旋转90度
        }
        process(frame);
        return frame;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.invert:{
                option = 1;
                break;
            }
            case R.id.edge:{
                option = 2;
                break;
            }
            case R.id.sobel:{
                option = 3;
                break;
            }
            case R.id.boxBlur:{
                option = 4;
                break;
            }
            case R.id.faceDetection:{
                option = 5;
                break;
            }
            default:{
                option = 0;
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void process(Mat frame){
        switch (option){
            case 1:{
                Core.bitwise_not(frame,frame);
                break;
            }
            case 2:{
                Mat edges = new Mat();
                Imgproc.Canny(frame,edges,100,200,3,false);
                Mat result = Mat.zeros(frame.size(),frame.type());
                frame.copyTo(result,edges);
                result.copyTo(frame);
                edges.release();
                result.release();
                break;
            }
            case 3:{//边缘化
                Mat gradx = new Mat();
                Imgproc.Sobel(frame, gradx, CvType.CV_32F,1,0);
                Core.convertScaleAbs(frame,gradx);
                gradx.copyTo(frame);
                gradx.release();
                break;
            }
            case 4:{//梯度
                Mat temp = new Mat();
                Imgproc.blur(frame,temp,new Size(15,15));
                temp.copyTo(frame);
                temp.release();
                break;
            }
            case 5:{//面部检测
                faceDetection(frame.getNativeObjAddr());
                break;
            }
            default:break;
        }
    }

    //jni方法
    public native void faceDetection(long frameAddress);
    public native void initLoad(String haarFilePath);
}
