package eli.blueeye.v1.activity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import eli.blueeye.v1.R;
import eli.blueeye.v1.broadcast.NetStatusReceiver;
import eli.blueeye.v1.dialog.ControlDialog;
import eli.blueeye.v1.entity.LoadListView;
import eli.blueeye.v1.inter.LongTouchListener;
import eli.blueeye.v1.server.GravitySensorListener;
import eli.blueeye.v1.server.ReadInfoThread;
import eli.blueeye.v1.util.Util;
import eli.blueeye.v1.view.NetWorkSpeedView;
import eli.blueeye.v1.view.RSSIView;
import eli.blueeye.v1.view.TakePhotoView;
import eli.blueeye.v1.entity.VlcPlayer;

/**
 * Activity
 *
 * @author eli chang
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, LongTouchListener {

    /**
     * http://img95.699pic.com/videos/2016/09/05/65b0f4fc-c8da-4287-bdae-603a492c519f.mp4
     * http://img95.699pic.com/videos/2016/09/19/eb3b9233-d919-46ce-b2d5-a30e4dd9fcdb.mp4
     * http://img95.699pic.com/videos/2016/09/18/38d63eab-796a-43be-998f-c00308d186f0.mp4
     * rtsp://10.42.0.1/test.h264
     * rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov
     * sdcard/1/video.mov
     */

    private static final String TAG = "MainActivity";
    //两种截取文件类型
    public static final int CAPTURE_PHOTO = 1;
    public static final int CAPTURE_VIDEO = 2;
    public static final int HANDLER_INFO = 3;
    public static final int HANDLER_RECORD_TIME = 4;
    //最长录制时长
    private static final int LONGEST_RECORD_TIME = 60;
    //路径
    private static final String eUrl = "http://img95.699pic.com/videos/2016/09/05/65b0f4fc-c8da-4287-bdae-603a492c519f.mp4";
    //上下文
    private Context context;
    //按键管理
    private KeyguardManager eKeyguardManager;
    //视频播放界面
    private SurfaceView eSurfaceView;
    //rtsp实体
    private VlcPlayer eVlcPlayer;
    //按钮区域
    private LinearLayout eButtonArea;
    //播放/暂停按钮
    private ImageButton ePlayerControlButton;
    //全屏切换按钮
    private ImageButton eFullScreenButton;
    //截录屏按钮
    private TakePhotoView eTakePhotoView;
    //摄像头
    private ImageButton eCameraButton;
    //设置按钮
    private ImageButton eConsoleButton;
    //分享按钮
    private ImageButton eShareButton;
    //删除按钮
    private ImageButton eDeleteButton;
    //网速信息
    private NetWorkSpeedView eNetWorkSpeedView;
    //信号强度
    private RSSIView eRssiView;
    //录制时间
    private TextView eRecordTime;

    //播放状态
    private boolean isPlayer = true;
    //截录屏按钮显示状态
    private boolean isShowCamera = false;
    //录屏时间溢出标志
    private boolean isTimeOverFlow = false;

    //截屏结果的回调方法
    private SnapHandler eSnapHandler;
    //信息更新的Handler
    private RefreshInfoHandler eRefreshInfoHandler;
    //隐藏按钮区域的Handler
    private Handler eHiddenHandler;
    //隐藏按钮区的线程
    private HiddenButtonThread eHiddenButtonThread;
    //重力传感器实现类
    private GravitySensorListener eSensorListener;
    //文件管理实现类
    private LoadListView eLoadListView;
    //控制面板
    private ControlDialog eControlDialog;
    //网络状态接收
    private NetStatusReceiver eNetStatusReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        eKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        //初始化视图
        initView();
        //初始化传感器
        eSensorListener = new GravitySensorListener(context, eFullScreenButton, eKeyguardManager);
        //实例化文件管理部分
        eLoadListView = new LoadListView(this, this);
        eLoadListView.loadFiles();
        //初始化Handler和线程
        initHandlerThread();
        //初始化播放器
        initSurface();
        //初始化广播
        eNetStatusReceiver = new NetStatusReceiver();
        registerNetReceiver();
    }

    @Override
    protected void onResume() {
        if (eVlcPlayer != null) {
            eVlcPlayer.play();
        }
        super.onResume();
        //设置视频尺寸
        resetViewSize();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //显示按钮区域
        setAreaVisibility(true);
        removeHiddenThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放资源
        eVlcPlayer.releasePlayer();
        //注销广播
        unRegisterNetReceiver();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        //播放界面
        eSurfaceView = (SurfaceView) findViewById(R.id.main_surface);
        eSurfaceView.setOnClickListener(this);

        //播放控制按钮
        ePlayerControlButton = (ImageButton) findViewById(R.id.main_button_play_control);
        ePlayerControlButton.setOnClickListener(this);

        //全屏切换按钮
        eFullScreenButton = (ImageButton) findViewById(R.id.main_button_full_screen);
        eFullScreenButton.setOnClickListener(this);

        //按钮区域
        eButtonArea = (LinearLayout) findViewById(R.id.main_button_area);

        //摄像头按钮
        eCameraButton = (ImageButton) findViewById(R.id.main_button_camera);
        eCameraButton.setOnClickListener(this);

        //截录屏按钮
        eTakePhotoView = (TakePhotoView) findViewById(R.id.main_button_take_photo);
        eTakePhotoView.setOnClickListener(this);
        eTakePhotoView.setOnLongTouchListener(this, 500);

        //设置按钮
        eConsoleButton = (ImageButton) findViewById(R.id.main_button_console);
        eConsoleButton.setOnClickListener(this);

        //分享按钮
        eShareButton = (ImageButton) findViewById(R.id.main_button_share);
        eShareButton.setOnClickListener(this);
        //删除按钮
        eDeleteButton = (ImageButton) findViewById(R.id.main_button_delete);
        eDeleteButton.setOnClickListener(this);

        //网速信息
        eNetWorkSpeedView = (NetWorkSpeedView) findViewById(R.id.main_view_rate);
        //信号强度
        eRssiView = (RSSIView) findViewById(R.id.main_view_rssi);

        //录制时间
        eRecordTime = (TextView) findViewById(R.id.main_text_record_time);
    }

    /**
     * 将播放器重置为16:9
     */
    private void resetViewSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //获取屏幕宽度，计算屏幕高度
        int width = displayMetrics.widthPixels;
        int height = (width * 9) / 16;
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        eSurfaceView.setLayoutParams(layoutParams);
        //设置工具条布局
        resetLayout(width, height);
    }

    /**
     * 初始化隐藏Handler和线程
     */
    private void initHandlerThread() {
        if (eSnapHandler == null)
            eSnapHandler = new SnapHandler();
        if (eHiddenHandler == null)
            eHiddenHandler = new Handler();
        if (eRefreshInfoHandler == null)
            eRefreshInfoHandler = new RefreshInfoHandler();

        new ReadInfoThread(this, eRefreshInfoHandler).start();
        addHiddenThread();

        new RecordStateRefreshThread().start();
    }

    /**
     * 初始化播放器
     */
    private void initSurface() {
        eVlcPlayer = new VlcPlayer(eSurfaceView, this, eUrl, eSnapHandler);
        eVlcPlayer.createPlayer();
    }

    /**
     * 为广播注册过滤器
     */
    private void registerNetReceiver() {
        if (eNetStatusReceiver != null) {
            IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
            this.registerReceiver(eNetStatusReceiver, filter);
        }
    }

    /**
     * 注销广播过滤器
     */
    private void unRegisterNetReceiver() {
        if (eNetStatusReceiver != null) {
            this.unregisterReceiver(eNetStatusReceiver);
        }
    }

    /**
     * 按键监听事件
     *
     * @param keyCode 按键代码
     * @param event   事件
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && Util.isLandscape(context)) {
            //横屏状态下，返回竖屏状态
            eFullScreenButton.performClick();
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_BACK && eLoadListView != null && eLoadListView.isMultiSelect()) {
            //多选模式下，取消全部选择
            eLoadListView.cancelAllSelectItem();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 点击事件
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_button_play_control:
                //播放、暂停
                switchControlButton(view);
                break;

            case R.id.main_surface:
                //显示、隐藏功能按钮
                switchShowArea();
                break;

            case R.id.main_button_full_screen:
                //切换横竖屏
                switchScreenOrientation();
                break;

            case R.id.main_button_take_photo:
                //截图
                startCapture();
                break;

            case R.id.main_button_camera:
                //切换摄像头
                switchShowCamera(view);
                break;

            case R.id.main_button_console:
                //设置
                switchShowConsole(view);
                break;

            case R.id.main_button_share:
                //分享
                if (eLoadListView != null) {
                    eLoadListView.shareSelectedItems();
                }
                break;

            case R.id.main_button_delete:
                //删除
                if (eLoadListView != null) {
                    eLoadListView.deleteSelectedItems();
                }
                break;
        }
    }

    /**
     * 长按触发事件
     */
    @Override
    public void onLongTouch() {
        //停止隐藏线程
        removeHiddenThread();
        //开始录屏
        if (!eVlcPlayer.isRecording())
            startRecord();
    }

    /**
     * 结束长按触发事件
     */
    @Override
    public void onTouchStop() {
        if (eVlcPlayer.isRecording()) {
            //结束录屏
            stopRecord();
            //重新开始隐藏线程
            addHiddenThread();
        }
    }

    /**
     * 切换横竖屏
     *
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //定义方向传感器
        eSensorListener.setOrientation(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
        //切换UI
        switchFullImage(eFullScreenButton);
        //获取屏幕宽高
        int screenWidth = Util.getScreenWidth(this);
        int screenHeight = Util.getScreenHeight(this);
        FrameLayout.LayoutParams layoutParams;
        //横屏
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutParams = new FrameLayout.LayoutParams(screenWidth, screenHeight);
            eSurfaceView.setLayoutParams(layoutParams);
            //设置工具条布局
            resetLayout(screenWidth, screenHeight);
        }
        //竖屏
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            int height = (screenWidth * 9) / 16;
            layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            eSurfaceView.setLayoutParams(layoutParams);
            //设置工具条布局
            resetLayout(screenWidth, height);
        }
    }

    /**
     * 开始截屏
     */
    private void startCapture() {
        if (eVlcPlayer == null || !eVlcPlayer.isPlaying() || isTimeOverFlow) {
            isTimeOverFlow = false;
            //当视频停止时，不进行截图
            return;
        }
        if (!eVlcPlayer.isRecording()) {
            //截图
            eVlcPlayer.snapShot();
        }
    }

    /**
     * 开始录屏
     */
    private void startRecord() {
        if (eVlcPlayer == null || !eVlcPlayer.isPlaying()) {
            eTakePhotoView.cancelTouchState();
            //当视频停止时，不进行录像
            return;
        }
        if (!eVlcPlayer.isRecording()) {
            //录屏
            eVlcPlayer.startRecord();
        }
    }

    /**
     * 结束录屏
     */
    private void stopRecord() {
        if (eVlcPlayer == null) {
            //当视频停止时，不进行录像
            return;
        }
        //结束录屏
        eVlcPlayer.stopRecord();
    }

    /**
     * 实现播放暂停的切换，图标的切换
     *
     * @param view 点击事件的View对象
     */
    private void switchControlButton(View view) {
        if (isPlayer) {
            //播放暂停
            Util.setBackImage(context, view, R.drawable.start);
            eVlcPlayer.pause();
        } else {
            //播放开始
            Util.setBackImage(context, view, R.drawable.stop);
            eVlcPlayer.play();
        }
        isPlayer = !isPlayer;
    }

    /**
     * 切换控制区域的显示
     */
    private void switchShowConsole(View view) {
        //当处于竖屏状态下，则不进行更新
        if (!Util.isLandscape(context)) {
            return;
        }
        //显示控制面板
        eControlDialog = new ControlDialog(this);
        eControlDialog.show();
        //隐藏按钮区
        setAreaVisibility(false);
    }

    /**
     * 全屏/非全屏、横屏/竖屏的切换
     */
    private void switchScreenOrientation() {
        //显示按钮区
        setAreaVisibility(true);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //取消全屏
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //设置竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            //当传感器处于横屏时
            if (eSensorListener.isSensorLAND()) {
                eSensorListener.disableLAND();
            }
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //当处于显示文件的状态下，不转为横屏
            if (eLoadListView != null && eLoadListView.isSelectedState()) {
                return;
            }
            //设置全屏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //设置横屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            //当传感器处于竖屏时
            if (!eSensorListener.isSensorLAND()) {
                eSensorListener.disablePORI();
            }
        }
    }

    /**
     * 切换全屏按钮的图标
     *
     * @param view 点击事件的View对象
     */
    private void switchFullImage(View view) {
        if (Util.isLandscape(context)) {
            Util.setBackImage(context, view, R.drawable.small);
        } else {
            Util.setBackImage(context, view, R.drawable.full);
        }
    }

    /**
     * 切换截图按钮的显示
     *
     * @param view 点击事件的View对象
     */
    private void switchShowCamera(View view) {
        if (!Util.isLandscape(context)) {
            //竖屏下不进行切换操作
            return;
        }
        if (isShowCamera) {
            //隐藏截屏按钮
            Util.setBackImage(context, view, R.drawable.camera);
            eTakePhotoView.setInVisible();
        } else {
            //显示截屏按钮
            Util.setBackImage(context, view, R.drawable.camera_);
            eTakePhotoView.setVisible();
        }
        isShowCamera = !isShowCamera;
    }

    /**
     * 更改功能区组件布局
     *
     * @param width  总体宽度
     * @param height 总体高度
     */
    private void resetLayout(int width, int height) {
        //判断屏幕方向
        final boolean isLand = Util.isLandscape(context);

        final int dip30 = Util.dip2px(context, 30);
        final int dip60 = Util.dip2px(context, 60);
        final int dip22 = Util.dip2px(context, 22);
        final int offset = Util.dip2px(context, 5);

        //设置功能区域宽高
        FrameLayout.LayoutParams buttonAreaLP = new FrameLayout.LayoutParams(width, height);
        eButtonArea.setLayoutParams(buttonAreaLP);

        //设置中间按钮位置
        RelativeLayout centerArea = (RelativeLayout) findViewById(R.id.main_center_button_area);
        LinearLayout.LayoutParams centerAreaLP = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dip60);
        centerAreaLP.setMargins(0, height / 2 - dip60 / 2, 0, 0);
        centerArea.setLayoutParams(centerAreaLP);

        //设置播放按钮位置
        RelativeLayout.LayoutParams playControlLP = new RelativeLayout.LayoutParams(dip30, dip30);
        playControlLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
        playControlLP.addRule(RelativeLayout.CENTER_VERTICAL);
        ePlayerControlButton.setLayoutParams(playControlLP);

        //设置截图按钮位置
        RelativeLayout.LayoutParams takePhotoLP;
        int takePhotoSideLength = dip60;
        takePhotoLP = new RelativeLayout.LayoutParams(takePhotoSideLength, takePhotoSideLength);
        takePhotoLP.addRule(RelativeLayout.CENTER_VERTICAL);
        takePhotoLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        takePhotoLP.setMargins(0, 0, offset * 2, 0);
        eTakePhotoView.setLayoutParams(takePhotoLP);

        //竖屏下关闭截图按钮的显示
        if (!isLand) {
            Util.setBackImage(context, eCameraButton, R.drawable.camera);
            eTakePhotoView.setInVisible();
        }

        //设置工具条位置
        LinearLayout toolBar = (LinearLayout) findViewById(R.id.main_tool_bar_area);
        LinearLayout.LayoutParams toolBarLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip22);
        toolBarLP.setMargins(0, height / 2 - dip60 / 2 - dip22 - offset, 0, 0);
        toolBar.setLayoutParams(toolBarLP);

        //当设置按钮处于旋转90度的状态，将其动画复位
        eConsoleButton.animate().rotation(0).setDuration(1).start();

        if (isLand) {
            //重置控制面板
        }

        //文件管理区域位置
        FrameLayout fileManagerArea = (FrameLayout) findViewById(R.id.main_file_area);
        FrameLayout.LayoutParams fileManagerAreaLP = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fileManagerAreaLP.setMargins(0, height, 0, 0);
        fileManagerArea.setLayoutParams(fileManagerAreaLP);

        //取消文件列表中的所有标记
        if (isLand && eLoadListView != null) {
            eLoadListView.cancelAllSelectItem();
        }

        //隐藏控制面板
        if (eControlDialog != null) {
            eControlDialog.dismiss();
        }
    }

    /**
     * 切换按钮区的隐藏和显示
     */
    private void switchShowArea() {
        if (eButtonArea.getVisibility() == View.VISIBLE) {
            setAreaVisibility(false);
        } else if (eButtonArea.getVisibility() == View.INVISIBLE) {
            setAreaVisibility(true);
        }
    }

    /**
     * 设置按钮区是否可见
     *
     * @param isVisibility 可见状态
     */
    private void setAreaVisibility(boolean isVisibility) {
        if (isVisibility) {
            eButtonArea.setVisibility(View.VISIBLE);
            removeHiddenThread();
            addHiddenThread();
        } else {
            eButtonArea.setVisibility(View.INVISIBLE);
            removeHiddenThread();
        }
    }

    /**
     * 启动隐藏按钮区的线程
     */
    private void addHiddenThread() {
        if (eHiddenButtonThread == null)
            eHiddenButtonThread = new HiddenButtonThread();
        eHiddenHandler.postDelayed(eHiddenButtonThread, 5000);
    }

    /**
     * 取消隐藏按钮区的线程
     */
    private void removeHiddenThread() {
        if (eHiddenButtonThread == null)
            return;
        eHiddenHandler.removeCallbacks(eHiddenButtonThread);
        eHiddenButtonThread = null;
    }

    /**
     * 隐藏按钮区的线程
     */
    private class HiddenButtonThread extends Thread {
        @Override
        public void run() {
            if (eButtonArea.getVisibility() == View.VISIBLE) {
                //当处于锁屏且横屏的状态下，不隐藏按钮区
                if (Util.isLandscape(context) && (eKeyguardManager.inKeyguardRestrictedInputMode()))
                    return;
                eButtonArea.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 录屏时间更新
     */
    private class RecordStateRefreshThread extends Thread {
        @Override
        public void run() {

            long lastTime = System.currentTimeMillis();
            int time;

            while (true) {
                if (eVlcPlayer != null) {
                    if (eVlcPlayer.isRecording()) {
                        time = (int) ((System.currentTimeMillis() - lastTime) / 1000);
                        if (time <= 60) {
                            postRecordTime(time);
                            isTimeOverFlow = true;
                        } else {
                            eTakePhotoView.cancelTouchState();
                        }
                    } else {
                        lastTime = System.currentTimeMillis();
                        postRecordTime(-1);
                        time = 0;
                    }
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
            }
        }

        /**
         * 发送更新数据
         * @param time
         */
        private void postRecordTime(int time) {
            if (eRefreshInfoHandler != null) {
                Message msg = eRefreshInfoHandler.obtainMessage();
                msg.what = HANDLER_RECORD_TIME;
                Bundle bundle = new Bundle();
                bundle.putInt("time", time);
                msg.setData(bundle);
                eRefreshInfoHandler.sendMessage(msg);
            }
        }
    }

    /**
     * 用于获取异步截图的Handler
     */
    public class SnapHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //获取到截取的文件路径
            String filePath = msg.getData().getString("path");
            if (msg.what == CAPTURE_VIDEO) {
                //获取到截取的视频路径
                filePath = filePath + ".mp4";
            }
            //更新ListView最新数据
            if (eLoadListView != null && filePath != null) {
                eLoadListView.addFile(filePath);
            }
        }
    }

    /**
     * 更新显示信息的Handler
     */
    public class RefreshInfoHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HANDLER_INFO) {
                float rate = msg.getData().getFloat("RATE");
                int rssi = msg.getData().getInt("RSSI");
                if (eNetWorkSpeedView != null) {
                    eNetWorkSpeedView.setSpeed(rate);
                }
                if (eRssiView != null) {
                    eRssiView.setRssi(rssi);
                }
            } else if (msg.what == HANDLER_RECORD_TIME) {
                //获取录制时间
                int time = msg.getData().getInt("time");
                //更新文字
                if (eRecordTime != null && eTakePhotoView != null) {
                    if (time >= 0 && time <= LONGEST_RECORD_TIME) {
                        String timeText = Util.formatTime(time);
                        eRecordTime.setText(timeText);
                    } else {
                        eRecordTime.setText("");
                    }
                }
            }
        }
    }
}