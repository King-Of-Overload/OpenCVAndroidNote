package zjut.alan.opencvdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import adapter.SectionsListViewAdaptor;
import model.ItemDto;
import utils.AppConstants;
import utils.ChapterUtils;

public class MainActivity extends AppCompatActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initListView();
    }





    private void initListView(){
        ListView listView = findViewById(R.id.chapter_listView);
        final SectionsListViewAdaptor commandAdapter = new SectionsListViewAdaptor(this);
        listView.setAdapter(commandAdapter);
        commandAdapter.getDataModel().addAll(ChapterUtils.getChapters());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ItemDto dot = commandAdapter.getDataModel().get(position);
                goSectionList(dot);//跳转到对应的页面
            }
        });
        commandAdapter.notifyDataSetChanged();
    }


    //跳转界面方法
    private void goSectionList(ItemDto dto){
        Intent intent = new Intent(getApplicationContext(), SectionsActivity.class);
        intent.putExtra(AppConstants.ITEM_KEY, dto);
        startActivity(intent);
    }

}
