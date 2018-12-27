package zjut.alan.opencvdemo.c9;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import utils.ImageSelectUtils;
import zjut.alan.opencvdemo.R;

public class BeautyFaceActivity extends AppCompatActivity implements View.OnClickListener{
    private int option;
    private float sigma = 30.0f;
    private int REQUEST_CAPTURE_IMAGE = 1;
    private Uri fileUri;
    private Button selectBtn, processBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beauty_face);
        selectBtn = findViewById(R.id.select_image_btn);
        processBtn = findViewById(R.id.nine_process_btn);
        selectBtn.setOnClickListener(this);
        processBtn.setOnClickListener(this);
        option = getIntent().getIntExtra("TYPE", 0);
        System.loadLibrary("zjut_face_detection");
        if(option == 1){
            setTitle("积分图计算演示");
        }

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id){
            case R.id.select_image_btn:{
                pickUpImage();
                break;
            }
            case R.id.nine_process_btn:{
                //积分图实现快速模糊
                Integral_Image_Demo();
                break;
            }
            default:break;
        }
    }

    //计算积分图快速模糊
    private void Integral_Image_Demo() {
        Mat src = Imgcodecs.imread(fileUri.getPath());
        if(src.empty()){return;}
        Mat dst = new Mat(src.size(), src.type());
        //积分和
        Mat sum = new Mat();
        //计算积分
        Imgproc.integral(src, sum, CvType.CV_32S);
        int w = src.cols();
        int h = src.rows();
        int x2 = 0, y2 = 0;
        int x1 = 0, y1 = 0;
        int ksize = 15;
        int radius = ksize / 2;
        int ch = src.channels();
        byte[] data = new byte[w*h*ch];
        int[] tl = new int[3];
        int[] tr = new int[3];
        int[] bl = new int[3];
        int[] br = new int[3];
        int cx = 0;
        int cy = 0;
        for(int row = 0; row < h+radius; row++){
            
        }
    }


    private void pickUpImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "图像选择..."), REQUEST_CAPTURE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            if(data != null) {
                Uri uri = data.getData();
                File f = new File(ImageSelectUtils.getRealPath(uri, getApplicationContext()));
                fileUri = Uri.fromFile(f);
            }
        }
        // display it
        if(fileUri == null) return;
        ImageView imageView = this.findViewById(R.id.chapter9_imageView);
        Bitmap bm = BitmapFactory.decodeFile(fileUri.getPath());
        imageView.setImageBitmap(bm);
    }
}
