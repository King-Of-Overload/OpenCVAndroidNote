package zjut.alan.opencvdemo.c7;

import android.Manifest;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;

import utils.ChapterUtils;
import zjut.alan.opencvdemo.R;

public class CameraViewActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2,View.OnClickListener,
        View.OnTouchListener{
    private MyCvCameraView mOpenCvCameraView;
    private int cameraIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);
        ChapterUtils.initLoadOpenCV(this);
        if(Build.VERSION.SDK_INT >= 23){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            },1);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = findViewById(R.id.cv_camera_id);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        RadioButton backOption = findViewById(R.id.backCameraBtn);
        RadioButton frontOption = findViewById(R.id.frontCameraBtn);
        backOption.setSelected(true);
        backOption.setOnClickListener(this);
        frontOption.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id == R.id.frontCameraBtn){
            cameraIndex = 1;
        }else if(id == R.id.backCameraBtn){
            cameraIndex = 0;
        }
        mOpenCvCameraView.setCameraIndex(cameraIndex);//设置前置还是后置
        if(mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
        mOpenCvCameraView.enableView();//开始预览
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.i("CVCamera", "竖屏显示");
        }
        Mat frame = inputFrame.rgba();
        if(cameraIndex == 0){
            return frame;
        }else{
            //若使用前置，需要进行镜像变化
            Core.flip(frame,frame,1);
            return frame;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        File fileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "myOcrImages");
        String name = String.valueOf(System.currentTimeMillis() + "_ocr.jpg");
        File tempFile = new File(fileDir.getAbsoluteFile()+File.separator,name);
        String fileName = tempFile.getAbsolutePath();
        Log.i("TAKE_PICTURE", fileName);
        mOpenCvCameraView.takePicture(fileName);
        Toast.makeText(this, fileName+"saved", Toast.LENGTH_LONG).show();
        return false;
    }

}
