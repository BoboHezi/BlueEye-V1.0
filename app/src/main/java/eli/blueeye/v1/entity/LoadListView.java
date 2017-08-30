package eli.blueeye.v1.entity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import eli.blueeye.v1.R;
import eli.blueeye.v1.data.FileType;
import eli.blueeye.v1.util.Util;
import eli.blueeye.v1.view.CustomListAdapter;
import eli.blueeye.v1.view.CustomListView;
import eli.blueeye.v1.dialog.CustomPhotoDialog;
import eli.blueeye.v1.dialog.CustomVideoDialog;

public class LoadListView implements CustomListView.OnLoadMoreListener, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String TAG = "LoadListView";
    public static final int HANDLER_STATE_REFRESH = 1;
    public static final int HANDLER_STATE_LOAD_ALL = 2;
    public static final int HANDLER_STATE_DELETE = 3;

    private Context context;
    private RefreshHandler eRefreshHandler;
    private CustomListView eFileListView;
    private AsyncReadFile eAsyncReadFile;
    private LinearLayout eBottomBar;
    private CustomListAdapter eFileListAdapter;
    private List<Map<String, Object>> eListItems;
    private List<File> eFiles;
    private File eSelectedFile;

    private CustomPhotoDialog ePhotoDialog;
    private CustomVideoDialog eVideoDialog;
    private List<Integer> eSelectedItems;

    private boolean isLongClick = false;
    private static final int ePageCount = 5;

    public LoadListView(Context context, Activity activity) {
        this.context = context;

        this.eFiles = new ArrayList<>();
        this.eFileListView = (CustomListView) activity.findViewById(R.id.main_list_files);
        this.eBottomBar = (LinearLayout) activity.findViewById(R.id.main_file_bottom_bar);

        this.eFileListView.setOnItemClickListener(this);
        this.eFileListView.setOnItemLongClickListener(this);
        this.eFileListView.setLoadMoreListener(this);

        initList();
    }

    /**
     * 初始化ListView
     */
    private void initList() {
        eListItems = new ArrayList<>();
        eFileListAdapter = new CustomListAdapter(context, eListItems);
        //为ListView设置适配器
        eFileListView.setAdapter(eFileListAdapter);
        eFiles = new ReadFiles().readFiles();
        eRefreshHandler = new RefreshHandler();
    }

    /**
     * 开启线程进行数据的读取
     */
    public void loadFiles() {
        if (eAsyncReadFile != null) {
            eAsyncReadFile.interrupt();
            eAsyncReadFile = null;
        }
        eAsyncReadFile = new AsyncReadFile(eRefreshHandler);
        eAsyncReadFile.start();
    }

    /**
     * 载入更多
     */
    @Override
    public void loadMore() {
        loadFiles();
    }

    /**
     * 点击某一条记录触发事件
     * @param adapterView
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        //当目前处于长按状态，或者有被选中的状态，则不响应单击事件
        if ((eSelectedItems != null && eSelectedItems.size() > 0) || isLongClick)
            return;

        eSelectedFile = eFiles.get(position);

        if (checkFileType(eSelectedFile) == FileType.PHOTO) {
            //点击图片文件
            ePhotoDialog = new CustomPhotoDialog(context, eSelectedFile, eRefreshHandler);
            ePhotoDialog.show();
        }else if (checkFileType(eSelectedFile) == FileType.VIDEO) {
            //点击视频文件
            eVideoDialog = new CustomVideoDialog(context, eSelectedFile, eRefreshHandler);
            eVideoDialog.show();
        }
    }

    /**
     * 长按事件
     * @param adapterView
     * @param view
     * @param position
     * @param id
     * @return
     */
    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (eSelectedItems == null) {
            eSelectedItems = new ArrayList<>();
        }

        if (eSelectedItems.contains(position)) {
            //当列表中存在该Item，移除
            eSelectedItems.remove(eSelectedItems.indexOf(position));
        } else {
            //当列表中不存在该Item，添加
            eSelectedItems.add(position);
        }

        //更新选中的Item
        notifySelected();
        return true;
    }

    /**
     * 添加文件到列表的头部
     * @param filePath
     */
    public void addFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists() && file.isFile()) {
                Map<String, Object> map = getItemMap(file);
                if (map != null) {
                    //添加数据至数据集合
                    eListItems.add(0, map);
                    eRefreshHandler.sendEmptyMessage(HANDLER_STATE_REFRESH);
                    eFiles.add(0, file);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 更新底部按钮的显示
     */
    private void switchShowBottomBar() {
        //当目前存在被选中的Item，则显示底部的按钮框，否则隐藏
        float dip40 = Util.dip2px(context, 40) - 2;
        if (eSelectedItems != null && eSelectedItems.size() > 0) {
            eBottomBar.animate().translationY(-dip40).setDuration(300).start();
        } else {
            eBottomBar.animate().translationY(2).setDuration(300).start();
        }
    }

    /**
     * 判断是否是多选状态
     * @return
     */
    public boolean isMultiSelect() {
        if (eSelectedItems == null || eSelectedItems.size() == 0)
            return false;
        else
            return true;
    }

    /**
     * 取消所有的标记
     */
    public void cancelAllSelectItem() {
        if (eSelectedItems != null) {
            eSelectedItems.clear();
            notifySelected();
        }
    }

    /**
     * 删除文件对应记录
     * @param file
     */
    private void deleteFileFromView(File file) {
        if (file != null) {
            int index = eFiles.indexOf(file);
            if (checkFileType(file) == FileType.PHOTO && ePhotoDialog != null && ePhotoDialog.isShowing()) {
                ePhotoDialog.dismiss();
            } else if (checkFileType(file) == FileType.VIDEO && eVideoDialog != null && eVideoDialog.isShowing()) {
                eVideoDialog.dismiss();
            }
            //从ListView中删除记录
            eFileListAdapter.removeItem(index);
            //从文件列表中删除记录
            eFiles.remove(index);
            //如果该文件处于选中的状态，则需要更新
            if (eSelectedItems != null && eSelectedItems.contains(index)) {
                eSelectedItems.remove(eSelectedItems.indexOf(index));
                notifySelected();
            }
            eSelectedFile = null;
        }
    }

    /**
     * 删除对应文件
     * @param file 需要删除的文件
     */
    private void deleteFileFromDisk(File file) {
        if (file != null) {
            if (file.exists() && file.isFile())
                file.delete();
        }
    }

    /**
     * 检查文件的类型
     * @param file
     * @return
     */
    public static FileType checkFileType(File file) {
        if (file.getName().contains("IMG")) {
            return FileType.PHOTO;
        } else if (file.getName().contains("VID")) {
            return FileType.VIDEO;
        } else {
            return FileType.OTHER;
        }
    }

    /**
     * 更新被选中的Item
     */
    private void notifySelected() {
        eFileListAdapter.setSelectedItem(eSelectedItems);
        eFileListAdapter.notifyDataSetChanged();
        //更新底部按钮的显示
        switchShowBottomBar();
    }

    /**
     * 更新视图的Handler
     */
    public class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            //刷新ListView
            if (msg.what == HANDLER_STATE_REFRESH) {
                //提醒ListView刷新页面
                eFileListAdapter.notifyDataSetChanged();
            } else if (msg.what == HANDLER_STATE_LOAD_ALL) {
                Toast.makeText(context, "没有更多了", Toast.LENGTH_SHORT).show();
            } else if (msg.what == HANDLER_STATE_DELETE) {
                deleteFileFromView(eSelectedFile);
            }
        }
    }

    /**
     * 读取数据的异步线程
     */
    class AsyncReadFile extends Thread {

        private RefreshHandler refreshHandler;
        public AsyncReadFile(RefreshHandler refreshHandler) {
            this.refreshHandler = refreshHandler;
        }

        @Override
        public void run() {
            int start = eFileListView.getCount();
            int end = (start + ePageCount) > eFiles.size() ? eFiles.size() : (start + ePageCount);

            //当文件全部载入结束后，则不再进行更新
            if (start >= end) {
                refreshHandler.sendEmptyMessage(HANDLER_STATE_LOAD_ALL);
                return;
            }

            eFileListView.setLoadState(CustomListView.LOAD_STATE_LOADING);
            for (int i = start; i < end; i ++) {
                File file = eFiles.get(i);
                Map<String, Object> map = getItemMap(file);

                if (map == null) {
                    //文件出错，移除文件和视图
                    eFiles.remove(i);
                    end --;
                    i -- ;
                } else {
                    //添加数据至数据集合
                    eListItems.add(map);
                    refreshHandler.sendEmptyMessage(HANDLER_STATE_REFRESH);
                }
            }
            eFileListView.setLoadState(CustomListView.LOAD_STATE_NON_LOADING);
        }
    }

    /**
     * 获取文件对应的Map对象
     * @param file
     * @return
     */
    private Map<String, Object> getItemMap(File file) {
        Map<String, Object> map = new HashMap<>();

        //设置时间
        Date date = new Date(file.lastModified());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String timeText = format.format(date);

        //获取对应缩略图
        Bitmap thumbnail = null;
        if (checkFileType(file) == FileType.PHOTO) {
            timeText = "Photo    " + timeText;
            thumbnail = Util.getImageThumbnail(file.getPath(), 500, 350);
        } else if (checkFileType(file) == FileType.VIDEO) {
            timeText = "Video    " + timeText;
            thumbnail = Util.getVideoThumbnail(file.getPath(), 500, 350, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
        }

        if (thumbnail != null) {
            map.put("time", timeText);
            map.put("image", thumbnail);
        } else {
            return null;
        }

        return map;
    }
}