package zjut.alan.opencvdemo.c7;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;

import java.io.FileOutputStream;

public class MyCvCameraView extends JavaCameraView implements Camera.PictureCallback {
    private String TAG = "MyCvCameraView";
    private String imageFileName;

    public MyCvCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void takePicture(final String fileName){
        this.imageFileName = fileName;
        //回收内存，因为camera是硬件资源
        System.gc();
        mCamera.setPreviewCallback(null);
        mCamera.takePicture(null,null,this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);
        try {
            FileOutputStream fos = new FileOutputStream(imageFileName);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
