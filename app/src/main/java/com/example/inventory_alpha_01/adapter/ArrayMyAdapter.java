package com.example.inventory_alpha_01.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.inventory_alpha_01.R;

import java.util.ArrayList;

public class ArrayMyAdapter extends ArrayAdapter<MyAdapter>{
    private ArrayList<MyAdapter> dataList;
    private Context context;
    private int maxSelected=0;
    private int countSelected=0;
    private Event event;
    public ArrayMyAdapter(Context context, ArrayList<MyAdapter> dataList){
        super(context,0,dataList);
        this.context=context;
        if(dataList==null){
            this.dataList=new ArrayList<MyAdapter>();
        }
        this.event=new Event(){
        };
        this.dataList=dataList;
    }
    public ArrayMyAdapter(Context context, ArrayList<MyAdapter> dataList, Event event){
        super(context,0,dataList);
        this.context=context;
        if(dataList==null){
            this.dataList=new ArrayList<MyAdapter>();
        }
        this.event=event;
        this.dataList=dataList;
    }
    public void reset(){
        MyAdapter adapter=null;
        for(int i=0, iLen=this.dataList.size();i<iLen;i++){
            adapter=this.dataList.get(i);
            adapter.setShow(false);
            adapter.setChecked(false);
        }
        this.notifyDataSetChanged();
    }
    public void clearNotChecked(){
        for(int j=0, jLen=getDataList().size();j<jLen;j++){
            if(getDataList().get(j).isChecked()==false){
                remove(getDataList().get(j));
                j--;
                jLen--;
            }
        }
        this.notifyDataSetChanged();
    }
    public void checkDouble(){
        for(int j=0, jLen=getDataList().size();j<jLen;j++){
            for(int k=0, kLen=getDataList().size();k<kLen;k++){
                if(j!=k){
                    if(getDataList().get(j).getStrKey().equals(getDataList().get(k).getStrKey()) && getDataList().get(j).isChecked()==false){
                        remove(getDataList().get(j));
                        j--;
                        jLen--;
                        k--;
                        kLen--;
                        break;
                    }
                }
            }
        }
        this.notifyDataSetChanged();
    }
    public interface Event{
        default void show(MyAdapter myAdapter){
        }
    }
    public ArrayList<MyAdapter> getSelected(){
        ArrayList<MyAdapter> list=new ArrayList<>();
        MyAdapter adapter=null;
        for(int i=0, iLen=this.dataList.size();i<iLen;i++){
            adapter=this.dataList.get(i);
            if(adapter.isChecked()==true){
                list.add(adapter);
            }
        }
        return list;
    }
    private class ViewHolder{
        public TextView tvDisplay;
        public TextView tvListBottom;
        public TextView tvListTop;
        public TextView tvNo;
        //public ProgressBar pb;
        public ImageView img;
    }
    @Override
    public View getView(int position,View convertView,ViewGroup parent){
        ViewHolder holder=null;
        Log.v("ConvertView",String.valueOf(position));
        if(convertView==null){
            LayoutInflater vi=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView=vi.inflate(R.layout.list_item_checkbox,null);
            holder=new ViewHolder();
            //holder.pb=convertView.findViewById(R.id.pb_choose);
            holder.tvDisplay=convertView.findViewById(R.id.tv_display);
            holder.tvListBottom=convertView.findViewById(R.id.tv_list_bottom);
            holder.tvListTop=convertView.findViewById(R.id.tv_list_top);
            holder.tvNo = convertView.findViewById(R.id.tv_no);
            holder.img = convertView.findViewById(R.id.img_type);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        MyAdapter da=dataList.get(position);
        if(da.isShow()==false){
            event.show(da);
            da.setShow(true);
        }
        holder.tvNo.setText(String.valueOf(position+1));
        convertView.setBackgroundColor(da.getBackgroundColor());
        holder.tvDisplay.setText(da.getStrDisplay());
//        if (da.getStrListRight()!=null){
//            holder.pb.setVisibility(View.GONE);
//        }
//        else {
//            holder.pb.setVisibility(View.VISIBLE);
//        }
        if(da.getStrListTop()!=null && !da.getStrListTop().trim().equals("")){
            holder.tvListTop.setVisibility(View.VISIBLE);
            holder.tvListTop.setText(da.getStrListTop());
        }else{
            holder.tvListTop.setVisibility(View.GONE);
            holder.tvListTop.setText("");
        }
        if(da.getStrListBottom()!=null && !da.getStrListBottom().trim().equals("")){
            holder.tvListBottom.setVisibility(View.VISIBLE);
            holder.tvListBottom.setText(da.getStrListBottom());
        }else{
            holder.tvListBottom.setVisibility(View.GONE);
            holder.tvListBottom.setText("");
        }
        return convertView;
    }
    public int getMaxSelected(){
        return maxSelected;
    }
    public void setMaxSelected(int maxSelected){
        this.maxSelected=maxSelected;
    }
    public int getCountSelected(){
        return countSelected;
    }
    public void setCountSelected(int countSelected){
        this.countSelected=countSelected;
    }
    public ArrayList<MyAdapter> getDataList(){
        return dataList;
    }
    public void setDataList(ArrayList<MyAdapter> dataList){
        this.dataList=dataList;
    }
}