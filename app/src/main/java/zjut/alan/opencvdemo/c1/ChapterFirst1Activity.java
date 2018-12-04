package zjut.alan.opencvdemo.c1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import org.opencv.imgproc.Imgproc;

import zjut.alan.opencvdemo.R;

public class ChapterFirst1Activity extends AppCompatActivity implements View.OnClickListener{

    private Button processBtn;

    private static final String CV_TAG = "CV_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_first1);
        processBtn = findViewById(R.id.process_btn);
        processBtn.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bac);
        Mat src = new Mat();
        Mat dst = new Mat();
        Utils.bitmapToMat(bitmap,src);
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);//灰度
        ImageView iv = findViewById(R.id.sample_img);
        iv.setImageBitmap(bitmap);
        src.release();
        dst.release();
    }
}
