package zjut.alan.opencvdemo.c8;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import utils.ChapterUtils;
import utils.ImageSelectUtils;
import zjut.alan.opencvdemo.R;

public class OcrDemoActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String DEFAULT_LANGUAGE = "nums";
    private String TAG = "OcrDemoActivity";
    private int REQUEST_CAPTURE_IMAGE = 1;
    private int option;
    private Uri fileUri;
    private TessBaseAPI baseApi;
    private Button selectBtn,ocrRecogBtn;
    private Bitmap sampleBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_demo);
        ChapterUtils.initLoadOpenCV(this);
        ChapterUtils.getWritePermission(this);
        selectBtn = findViewById(R.id.select_image_btn);
        ocrRecogBtn = findViewById(R.id.ocr_recognize_btn);
        selectBtn.setOnClickListener(this);
        ocrRecogBtn.setOnClickListener(this);
        option = getIntent().getIntExtra("TYPE", 0);
        try {
            initTessBaseAPI();
            if(option == 2){
                setTitle("身份证号码识别");
            }else if(option == 3){
                setTitle("偏斜校正");
                ocrRecogBtn.setText("校正");
            }else{
                setTitle("Tesseract OCR文本识别演示");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.select_image_btn:
                pickUpImage();
                break;
            case R.id.ocr_recognize_btn:
                if(option == 2) {
                    recognizeCardId();
                } else if(option == 3) {
                    //deSkewTextImage();
                }else {
                    recognizeTextImage();
                }
                break;
            default:
                break;
        }
    }

    //识别身份证
    private void recognizeCardId(){
        Bitmap template = BitmapFactory.decodeResource(getResources(),R.drawable.card_template);
        Bitmap cardImage = BitmapFactory.decodeFile(fileUri.getPath());
        Bitmap temp = CardNumberROIFinder.extractNumberROI(cardImage.copy(Bitmap.Config.ARGB_8888,true),
                template);
        baseApi.setImage(temp);
        String myIdNumber = baseApi.getUTF8Text();
        TextView txtView = findViewById(R.id.text_result_id);
        txtView.setText("身份证号码为:" + myIdNumber);
        ImageView imageView = findViewById(R.id.chapter8_imageView);
        imageView.setImageBitmap(temp);
    }

    //识别字符OCR
    private void recognizeTextImage(){
        if(fileUri == null) return;
        Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath());
        baseApi.setImage(bitmap);
        String recognizedText = baseApi.getUTF8Text();
        TextView textView = findViewById(R.id.text_result_id);
        if(!recognizedText.isEmpty()){
            textView.append("识别结果:\n"+recognizedText);
        }
    }



    private void pickUpImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "图像选择..."), REQUEST_CAPTURE_IMAGE);
    }

    //实例化Tesseract
    private void initTessBaseAPI() throws IOException{
        baseApi = new TessBaseAPI();
        String datapath = Environment.getExternalStorageDirectory()+"/tesseract/";
        File dir = new File(datapath + "tessdata/");
        if(!dir.exists()){
            dir.mkdirs();
            //加载数据集
            InputStream inputStream = getResources().openRawResource(R.raw.nums);
            File file = new File(dir, "nums.traineddata");
            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buff = new byte[1024];
            int len = 0;
            while((len = inputStream.read(buff)) != -1){
                outputStream.write(buff, 0, len);
            }
            inputStream.close();
            outputStream.close();
        }
        //加载数据集
        boolean success = baseApi.init(datapath, DEFAULT_LANGUAGE);
        if(success){
            Log.i(TAG, "成功加载Tesseract OCR引擎");
        }else{
            Log.i(TAG, "无法实例化数据集");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri uri = data.getData();
                File f = new File(ImageSelectUtils.getRealPath(uri, getApplicationContext()));
                fileUri = Uri.fromFile(f);
            }
        }
        displaySelectedImage();
    }

    private void displaySelectedImage(){
        if(fileUri == null) return;
        ImageView imageView = this.findViewById(R.id.chapter8_imageView);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileUri.getPath(), options);
        int w = options.outWidth;
        int h = options.outHeight;
        int inSample = 1;
        if(w > 1000 || h > 1000){
            while (Math.max(w / inSample, h / inSample) > 1000){
                inSample *= 2;
            }
        }
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSample;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        sampleBitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);
        imageView.setImageBitmap(sampleBitmap);
    }

}
