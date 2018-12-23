package zjut.alan.opencvdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import adapter.SectionsListViewAdaptor;
import model.ItemDto;
import utils.AppConstants;
import utils.ChapterUtils;
import zjut.alan.opencvdemo.c1.ChapterFirst1Activity;
import zjut.alan.opencvdemo.c1.TakePhotoActivity;
import zjut.alan.opencvdemo.c2.ReadMatinfoActivity;
import zjut.alan.opencvdemo.c3.MatOperationsActivity;
import zjut.alan.opencvdemo.c4.ConvolutionActivity;
import zjut.alan.opencvdemo.c5.ImageAnalysisActivity;
import zjut.alan.opencvdemo.c7.CameraViewActivity;
import zjut.alan.opencvdemo.c7.DisplayModeActivity;
import zjut.alan.opencvdemo.c8.OcrDemoActivity;

public class SectionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sections);
        ItemDto dto = (ItemDto) getIntent().getSerializableExtra(AppConstants.ITEM_KEY);
        if(null != dto){
            initListView(dto);
        }
    }

    private void initListView(ItemDto dto){
        ListView listView = findViewById(R.id.section_listView);
        final SectionsListViewAdaptor commandAdapter = new SectionsListViewAdaptor(this);
        listView.setAdapter(commandAdapter);
        commandAdapter.getDataModel().addAll(ChapterUtils.getSections((int) dto.getId()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String command = commandAdapter.getDataModel().get(position).getName();
                goDemoView(command);//跳转到模块部分
            }
        });
        commandAdapter.notifyDataSetChanged();
    }

    private void goDemoView(String command){
        if(command.equals(AppConstants.CHAPTER_1TH_PGM_01)){
            Intent intent = new Intent(getApplicationContext(), ChapterFirst1Activity.class);
            startActivity(intent);
        }else if(command.equals(AppConstants.CHAPTER_1TH_PGM_02)){
            Intent intent = new Intent(getApplicationContext(), TakePhotoActivity.class);
            startActivity(intent);
        }else if(command.equals(AppConstants.CHAPTER_2TH_PGM_01)){
            Intent intent = new Intent(getApplicationContext(), ReadMatinfoActivity.class);
            startActivity(intent);
        }else if(command.equals(AppConstants.CHAPTER_3TH_PGM)){
            Intent intent = new Intent(getApplicationContext(), MatOperationsActivity.class);
            startActivity(intent);
        }else if(command.equals(AppConstants.CHAPTER_4TH_PGM)){
            Intent intent = new Intent(getApplicationContext(), ConvolutionActivity.class);
            startActivity(intent);
        }else if(command.equals(AppConstants.CHAPTER_5TH_PGM)){
            Intent intent = new Intent(getApplicationContext(), ImageAnalysisActivity.class);
            startActivity(intent);
        }else if(command.equals(AppConstants.CHAPTER_7TH_PGM)){
            Intent intent = new Intent(getApplicationContext(), CameraViewActivity.class);
            startActivity(intent);
        }else if(command.equals(AppConstants.CHAPTER_7TH_PGM_VIEW_MODE)){
            Intent intent = new Intent(getApplicationContext(), DisplayModeActivity.class);
            startActivity(intent);
        }else if(command.equals(AppConstants.CHAPTER_8TH_PGM_OCR)){
            Intent intent = new Intent(getApplicationContext(), OcrDemoActivity.class);
            intent.putExtra("TYPE", 1);
            startActivity(intent);
        }else if(command.equals(AppConstants.CHAPTER_8TH_PGM_ID_NUM)){
            Intent intent = new Intent(getApplicationContext(), OcrDemoActivity.class);
            intent.putExtra("TYPE", 2);
            startActivity(intent);
        }else if(command.equals(AppConstants.CHAPTER_8TH_PGM_DESKEW)){
            Intent intent = new Intent(getApplicationContext(), OcrDemoActivity.class);
            intent.putExtra("TYPE", 3);
            startActivity(intent);
        }
    }
}
