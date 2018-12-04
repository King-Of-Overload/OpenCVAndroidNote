package zjut.alan.opencvdemo.c1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.BitmapCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import utils.ChapterUtils;
import utils.ImageSelectUtils;
import zjut.alan.opencvdemo.R;

/**
 * 拍照与图像选择
 */
public class TakePhotoActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int REQUEST_CAPTURE_IMAGE = 1;
    private Button takePhotoBtn, choosePhotoBtn, processBtn;
    private Uri fileUri;
    private static final String CV_TAG = "CV_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        takePhotoBtn = findViewById(R.id.takePhoto);
        choosePhotoBtn = findViewById(R.id.chosePhoto);
        processBtn = findViewById(R.id.process_btn);
        takePhotoBtn.setOnClickListener(this);
        choosePhotoBtn.setOnClickListener(this);
        processBtn.setOnClickListener(this);
        //Android 动态权限申请
        if(ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                1);
        }
        if(ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    2);
        }
        initLoadOpenCV();
    }

    private void initLoadOpenCV(){
        boolean success = OpenCVLoader.initDebug();//加载动态库
        if(success){
            Log.i(CV_TAG, "OpenCV加载完成");
        }else{
            Toast.makeText(getApplicationContext(),"无法加载OpenCV", Toast.LENGTH_LONG).show();
        }
    }


    //重写申请请求方法
    @Override
    public void onRequestPermissionsResult
            (int requestCode,String[] permissions, int[] grantResults) {
        if (requestCode == 1 || requestCode ==2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //权限获取成功
                Log.i("GRANT", "权限获取成功");
            } else {
                //权限被拒绝
                Log.i("GRANT", "权限获取失败");
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.takePhoto){
            //调用摄像头并返回数据
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri = Uri.fromFile(ImageSelectUtils.getSaveFilePath());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(Intent.createChooser(intent, "拍照"), REQUEST_CAPTURE_IMAGE);
        }else if(view.getId() == R.id.chosePhoto){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"图像选择..."), REQUEST_CAPTURE_IMAGE);
        }else if(view.getId() == R.id.process_btn){
            //进行灰度转换
            if(fileUri != null){
                Mat src = Imgcodecs.imread(fileUri.getPath());
                if(src.empty()){
                    return;
                }
                Mat dst = new Mat();
                Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
                Bitmap bitmap = grayMat2Bitmap(dst);
                ImageView iv = findViewById(R.id.photo_iv);
                iv.setImageBitmap(bitmap);
                src.release();
                dst.release();
            }
        }
    }

    //将灰度矩阵转换成位图对象
    private Bitmap grayMat2Bitmap(Mat result){
        Mat image;
        if(result.cols() > 1000 || result.rows() > 1000){
            image = new Mat();
            Imgproc.resize(result, image, new Size(result.cols() / 4, result.rows() / 4));
        }else{
            image = result;
        }
        Bitmap bitmap = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_GRAY2RGBA);
        Utils.matToBitmap(image, bitmap);
        image.release();
        return bitmap;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_CAPTURE_IMAGE){
            if(data != null){
                Uri uri = data.getData();
                File file = new File(ImageSelectUtils.getRealPath(uri, getApplicationContext()));
                fileUri = Uri.fromFile(file);
            }
        }
        displaySelectedImage();
    }

    //显示图片，降采样版本
    private void displaySelectedImage() {
        if(fileUri !=  null){
            ImageView imageView = findViewById(R.id.photo_iv);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fileUri.getPath(), options);
            int w = options.outWidth;
            int h = options.outHeight;
            int inSample = 1;
            if(w > 1000 || h > 1000){
                while(Math.max(w/inSample, h/inSample) > 1000){
                    inSample *= 2;
                }
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = inSample;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath(), options);
            imageView.setImageBitmap(bm);
        }
    }



}
