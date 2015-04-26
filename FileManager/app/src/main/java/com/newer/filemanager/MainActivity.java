package com.newer.filemanager;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends Activity implements FileAdapter.WhileCopy {
    private static final String TAG ="MainActivity" ;
    private ListView listView;
    private FrameLayout frameLayout;
    private FrameLayout frameLayout2;
    private GridView gridView;
    private List<File> data;
    private FileAdapter adapter;
    private File currentFile;
    private String title;
    private ActionBar actionBar;
    private int counter;
    private int counterCopy;
    private boolean isRoot;
    private int position;
    private boolean isCopy;
    private File src;
    private String action;
    private Info info;
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        isRoot = true;
        info = new Info();
        frameLayout = (FrameLayout) findViewById(R.id.frame_layout);
        frameLayout2 = (FrameLayout) findViewById(R.id.frame_layout2);
        gridView = (GridView) findViewById(R.id.gridView);
        listView = (ListView) findViewById(R.id.listView);
        data = new ArrayList<>();
        File sd = Environment.getExternalStorageDirectory();
        title = new String();
        title += "SDcard";
        actionBar = getActionBar();
        showListView(sd, title);
        adapter = new FileAdapter(getApplicationContext(), data,"listView");
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new McmListener());
        listView.setOnItemClickListener(new ICListener());
        listView.setAdapter(adapter);
        adapter.setWhileCopy(this);

    }

    private void showListView(File file, String title) {
        currentFile = file;
        actionBar.setTitle(title);
        data.clear();
        File[] files = file.listFiles();

        Arrays.sort(files, info);

        for (File f : files) {
            data.add(f);
        }
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
        isRoot = title.equals("SDcard") ? true : false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            String path = currentFile.getPath().substring(0, (int) (currentFile.getPath().length()-currentFile.getName().length()));
//            File file = new File(path);
            title = (String) actionBar.getTitle().subSequence(0, (actionBar.getTitle().length() - currentFile.getName().length() - 1));
            if (isRoot) {
                counter++;
                if (counter == 2) {
                    return super.onKeyDown(keyCode, event);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "再按一次退出文件管理器", Toast.LENGTH_SHORT).show();
                }
                return false;
            } else {
                counter = 0;
            }
            showListView(currentFile.getParentFile(), title);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void inPaste(int position, String action) {
        isCopy = true;
        this.position = position;
        src = data.get(position);
        this.action = action;
    }

    @Override
    public void finishCopy() {
        isCopy = false;
        counterCopy = 0;
    }

    /**
     * 接口回调方法
     */
    @Override
    public void rename(int position) {
        this.position = position;
        newAlertDialog("重命名");
    }

    //------------------Action mode
    class McmListener implements AbsListView.MultiChoiceModeListener {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            int count = listView.getCheckedItemCount();
            int count2 = gridView.getCheckedItemCount();
            mode.setTitle((count>count2?count:count2 )+ "");
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.mcm_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            if (item.getItemId() == R.id.action_remove) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("删除项目？")
                        .setMessage("是否确认删除" + listView.getCheckedItemCount() + "个项目？此项操作不可撤销")
                        .setPositiveButton("狠心删除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                removeFolder();
                                showListView(currentFile, title);
                                mode.finish();
                            }
                        })
                        .setNegativeButton("不,再考虑一下", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mode.finish();
                            }
                        })
                        .show();
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }

    /**
     * 删除Action mode中 多选的文件
     */
    private void removeFolder() {
        List<File> removeFile = new ArrayList<>();
        SparseBooleanArray array = listView.getCheckedItemPositions();
        for (int i = 0; i < array.size(); i++) {
            int index = array.keyAt(i);
            if (array.get(index)) {
                removeFile.add(data.get(index));
            }
        }
        data.removeAll(removeFile);
        for (File f : removeFile) {
            FileUtil.deleteFolder(f);
        }
    }

    //---------------------OnclickListener
    class ICListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            File file = data.get(position);
            if (file.isDirectory()) {
                title += "/" + file.getName();
                showListView(file, title);
            } else {
                openFile(file);
            }
        }

        private void openFile(File file) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            MimeTypeMap map = MimeTypeMap.getSingleton();
            Uri data = Uri.fromFile(file);
            String name = file.getName();
            String typeName = name.substring(name.lastIndexOf(".") + 1);
            String type = map.getMimeTypeFromExtension(typeName);
            if(type!=null){
               intent.setDataAndType(data, type);
               startActivity(intent);
           }

        }
    }

    //----------------OptionsMenu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        this.menu = menu;
        menu.removeItem(R.menu.copy_menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.removeItem(R.menu.copy_menu);
        if (isCopy&&counterCopy==0) {
            getMenuInflater().inflate(R.menu.copy_menu, menu);
            counterCopy++;
        }
        if (!isCopy) {
            invalidateOptionsMenu();
            counterCopy = 0;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        item.setChecked(true);
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        switch (id) {
            case R.id.action_newProject:
                createNewProject();
                break;
            case R.id.show_as_grid:
                frameLayout.setVisibility(View.GONE);
                switchShowModel("gridView");
                break;
            case R.id.show_as_list:
                frameLayout.setVisibility(View.VISIBLE);
                switchShowModel("listView");
                break;
            case R.id.paste:
                try {
                    File dest = new File(currentFile.getPath() + "/" + src.getName());
                    if (src.isDirectory()) {
                        FileUtil.copyFolder(src, dest);
                    } else {
                        FileUtil.copyFile(src, dest);
                    }
                    if (action.equals("cut")) {
                        FileUtil.deleteFolder(src);
                    }
                    finishCopy();
                    showListView(currentFile, title);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        chooseSortModle(id);
        return super.onOptionsItemSelected(item);
    }

    private void switchShowModel(String type) {
        adapter = new FileAdapter(this, data, type);
        if(type.equals("listView")){
            listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new McmListener());
            listView.setOnItemClickListener(new ICListener());
            listView.setAdapter(adapter);
        }
        if(type.equals("gridView")){
            gridView.setAdapter(adapter);
            gridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            gridView.setMultiChoiceModeListener(new McmListener());
            gridView.setOnItemClickListener(new ICListener());
        }
        showListView(currentFile,title);
    }

    private void createNewProject() {
        String type = "创建新项目";
        newAlertDialog(type);
    }

    private void newAlertDialog(final String type) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.create_file_alert, null);
        final EditText editText = (EditText) view.findViewById(R.id.editText_file_name);
        if (type.equals("重命名")) {
            editText.setText(data.get(position).getName());
        }

        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        new AlertDialog.Builder(this).setTitle(type)
                .setView(view)
//                .setCancelable(false)
                .setMessage("请输入项目名称")
//                .setNeutralButton()
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    /**
                     *
                     * @param dialog 事件源所在的对话框
                     * @param which 点击的按钮
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String name = editText.getText().toString();
                            if (name.length() > 0) {
                                if (type.equals("创建新项目")) {
                                    FileUtil.createFolder(currentFile, name, data);
                                }
                                if (type.equals("重命名")) {
                                    File f = FileUtil.rename(data.get(position), name);
                                    data.set(position, f);
                                }
                            } else {
                                return;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        showListView(currentFile, title);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void chooseSortModle(int id) {
        switch (id) {
            case R.id.action_sort_by_abc:
                info = new Info();
                info.setAction("name");

                break;

            case R.id.action_sort_by_size:
                info = new Info();
                info.setAction("asc");
                break;
            case R.id.action_sort_by_desc:
                info = new Info();
                info.setAction("desc");
                break;
            case R.id.action_sort_by_directory:
                info = new Info();
                info.setAction("folder");
                break;

            case R.id.action_sort_by_last_modify:
                info = new Info();
                info.setAction("lastModify");
                break;
        }
        showListView(currentFile, title);
    }
}
