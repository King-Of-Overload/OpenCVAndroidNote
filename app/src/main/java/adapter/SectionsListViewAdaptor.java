package adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import model.ItemDto;
import zjut.alan.opencvdemo.R;

public class SectionsListViewAdaptor extends BaseAdapter {
    private Context appContext;
    private List<ItemDto> dataModel;

    public SectionsListViewAdaptor(Context context){
        this.appContext = context;
        dataModel = new ArrayList<>();
    }

    public List<ItemDto> getDataModel() {
        return this.dataModel;
    }

    @Override
    public int getCount() {
        return dataModel.size();
    }

    @Override
    public Object getItem(int i) {
        return dataModel.get(i);
    }

    @Override
    public long getItemId(int i) {
        return dataModel.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) appContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_layout, parent, false);
        TextView textView = rowView.findViewById(R.id.row_textView);
        textView.setText(dataModel.get(i).getDesc());
        return rowView;
    }
}
