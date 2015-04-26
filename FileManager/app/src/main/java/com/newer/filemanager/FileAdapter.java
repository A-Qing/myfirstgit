package com.newer.filemanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by dell on 2015/3/23.
 */
public class FileAdapter extends BaseAdapter {
    private Context context;
    private List<File> data;
    private LayoutInflater inflater;
    private WhileCopy whileCopy;
    private String type;


    public void setWhileCopy(WhileCopy whileCopy) {
        this.whileCopy = whileCopy;
    }

    public FileAdapter(Context context, List<File> data, String type) {
        this.context = context;
        this.data = data;
        inflater = LayoutInflater.from(context);
        this.type = type;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ImageButtonListener listener;
        if(convertView == null){
            holder = new ViewHolder();
            if (type.equals("listView")){
                convertView = inflater.inflate(R.layout.file_item,parent,false);
                holder.imageView = (ImageView) convertView.findViewById(R.id.imageView_fileImg);
                holder.fileName = (TextView) convertView.findViewById(R.id.textView_filename);
                holder.fileSize = (TextView) convertView.findViewById(R.id.textView_filesize);
                holder.lastModify = (TextView) convertView.findViewById(R.id.textView_lastmodify);
                holder.imageButton = (ImageButton) convertView.findViewById(R.id.imageButton);
            }
            if(type.equals("gridView")){
                convertView = inflater.inflate(R.layout.grid_file_item,parent,false);
                holder.imageView = (ImageView) convertView.findViewById(R.id.imageView_fileImg1);
                holder.fileName = (TextView) convertView.findViewById(R.id.textView_filename1);
                holder.fileSize = (TextView) convertView.findViewById(R.id.textView_filesize1);
                holder.lastModify = (TextView) convertView.findViewById(R.id.textView_lastmodify1);
                holder.imageButton = (ImageButton) convertView.findViewById(R.id.imageButton1);
            }

            listener = new ImageButtonListener();
            holder.imageButton.setOnClickListener(listener);
            convertView.setTag(holder);
            holder.imageButton.setTag(listener);
        }else{
            holder = (ViewHolder) convertView.getTag();
            listener = (ImageButtonListener) holder.imageButton.getTag();
        }
        listener.setPosition(position);
        File file = data.get(position);
        holder.fileName.setText(file.getName());
        if (file.isFile()){
            holder.imageView.setImageResource(R.drawable.ic_action_video);
            holder.fileSize.setText(FileUtil.getDataSize(file.length()));
        }else {
            holder.imageView.setImageResource(R.drawable.ic_action_collection);
            holder.fileSize.setText(file.listFiles().length==0?"空文件夹":""+file.listFiles().length+"个文件");
//          holder.fileSize.setText(""+file.length());
        }
        Date date = new Date(file.lastModified());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String show = simpleDateFormat.format(date);
        holder.lastModify.setText("-"+show);

        return convertView;
    }
    static class ViewHolder{
        ImageView imageView;
        TextView fileName;
        TextView fileSize;
        TextView lastModify;
        ImageButton imageButton;
    }
    class ImageButtonListener implements View.OnClickListener{
        private int position;

        public void setPosition(int position) {
            this.position = position;

        }

        @Override
        public void onClick(View v) {

            PopupMenu menu = new PopupMenu(context,v);
            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case R.id.action_copy_file:
                            Toast.makeText(context,"可进行粘贴操作",Toast.LENGTH_SHORT).show();
                            whileCopy.inPaste(position,"copy");
                            break;
                        case R.id.action_rename:
                            whileCopy.rename(position);
                            break;
                        case R.id.action_cut:
                            Toast.makeText(context,"可进行剪切操作",Toast.LENGTH_SHORT).show();
                            whileCopy.inPaste(position,"cut");
                            break;
                        case R.id.action_open_with:
                            openFileWith();
                            break;
                    }
                    return true;
                }

                private void openFileWith() {
                    final String[] methods = {"文本","音频","视频","图像","其他"};
                    new AlertDialog.Builder(context)
                            .setTitle("打开为")
                            .setItems(methods, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String method = methods[which];
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                }
                            })
                            .show();
                }
            });
            menu.inflate(R.menu.popup_menu);
            menu.show();
        }
    }

    public interface WhileCopy{
        void inPaste(int position,String action);
        void finishCopy();
        void rename(int position);
    }
}
