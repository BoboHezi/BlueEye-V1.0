package eli.blueeye.v1.activity;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import eli.blueeye.v1.R;
import eli.blueeye.v1.entity.LoadListView;
import eli.blueeye.v1.entity.ShareEntity;
import eli.blueeye.v1.inter.LongTouchListener;
import eli.blueeye.v1.server.GravitySensorListener;
import eli.blueeye.v1.util.Util;
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
     * rtsp://10.42.0.1/
     * rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov
     * sdcard/1/video.mov
     */

    private static final String TAG = "MainActivity";
    //两种文件类型
    public static final int CAPTURE_PHOTO = 1;
    public static final int CAPTURE_VIDEO = 2;
    //路径
    private static final String eUrl = "rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov";
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
    private ImageButton eSetUpButton;
    //分享按钮
    private ImageButton eShareButton;
    //删除按钮
    private ImageButton eDeleteButton;

    //播放状态
    private boolean isPlayer = true;
    //截录屏按钮显示状态
    private boolean isShowCamera = false;
    //控制区域显示状态
    private boolean isShowSetup = false;

    //截屏结果的回调方法
    private SnapHandler eSnapHandler;
    //隐藏按钮区域的Handler
    private Handler eHiddenHandler;
    //隐藏按钮区的线程
    private HiddenRunnable eHiddenRunnable;
    //重力传感器实现类
    private GravitySensorListener eSensorListener;
    //文件管理实现类
    private LoadListView eLoadListView;
    //分享实现类
    private ShareEntity eShareEntity;

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
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume: ");
        if (eVlcPlayer != null) {
            eVlcPlayer.play();
        }
        super.onResume();
        //设置视频尺寸
        resetViewSize();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop: ");
        //应用失去焦点时，暂停播放
        /*if (eVlcPlayer != null) {
            eVlcPlayer.pause();
        }*/
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
        eSetUpButton = (ImageButton) findViewById(R.id.main_button_setup);
        eSetUpButton.setOnClickListener(this);

        //分享按钮
        eShareButton = (ImageButton) findViewById(R.id.main_button_share);
        eShareButton.setOnClickListener(this);
        //删除按钮
        eDeleteButton = (ImageButton) findViewById(R.id.main_button_delete);
        eDeleteButton.setOnClickListener(this);
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
        setToolBarPosition(width, height);
    }

    /**
     * 初始化隐藏Handler和线程
     */
    private void initHandlerThread() {
        if (eSnapHandler == null)
            eSnapHandler = new SnapHandler();
        if (eHiddenHandler == null)
            eHiddenHandler = new Handler();
        addHiddenThread();
    }

    /**
     * 初始化播放器
     */
    private void initSurface() {
        eVlcPlayer = new VlcPlayer(eSurfaceView, this, eUrl, eSnapHandler);
        eVlcPlayer.createPlayer();
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

            case R.id.main_button_setup:
                //设置
                switchShowSetup(view);
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
            setToolBarPosition(screenWidth, screenHeight);
        }
        //竖屏
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            int height = (screenWidth * 9) / 16;
            layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            eSurfaceView.setLayoutParams(layoutParams);
            //设置工具条布局
            setToolBarPosition(screenWidth, height);
        }
    }

    /**
     * 开始截屏
     */
    private void startCapture() {
        if (eVlcPlayer == null || !eVlcPlayer.isPlaying()) {
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
        if (eVlcPlayer == null || !eVlcPlayer.isPlaying()) {
            //当视频停止时，不进行录像
            return;
        }
        if (eVlcPlayer.isRecording()) {
            //结束录屏
            eVlcPlayer.stopRecord();
        }
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
    private void switchShowSetup(View view) {
        //当处于竖屏状态下，则不进行更新
        if (!Util.isLandscape(context)) {
            return;
        }

        if (!isShowSetup) {
            view.animate().rotation(90f).setDuration(100).start();
        } else {
            view.animate().rotation(0).setDuration(100).start();
        }
        isShowSetup = !isShowSetup;
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
    private void setToolBarPosition(int width, int height) {
        //判断屏幕方向
        final boolean isLand = Util.isLandscape(context);

        final int dip30 = Util.dip2px(context, 30);
        final int dip60 = Util.dip2px(context, 60);
        final int dip20 = Util.dip2px(context, 20);
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
        LinearLayout.LayoutParams toolBarLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dip20);
        toolBarLP.setMargins(0, height / 2 - dip60 / 2 - dip20 - offset, 0, 0);
        toolBar.setLayoutParams(toolBarLP);

        if (isShowSetup) {
            //当设置按钮处于旋转90度的状态，将其动画复位
            eSetUpButton.animate().rotation(0).setDuration(1).start();
        }
        isShowSetup = false;
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
        if (eHiddenRunnable == null)
            eHiddenRunnable = new HiddenRunnable();
        eHiddenHandler.postDelayed(eHiddenRunnable, 5000);
    }

    /**
     * 取消隐藏按钮区的线程
     */
    private void removeHiddenThread() {
        if (eHiddenRunnable == null)
            return;
        eHiddenHandler.removeCallbacks(eHiddenRunnable);
        eHiddenRunnable = null;
    }

    /**
     * 隐藏按钮区的线程
     */
    class HiddenRunnable implements Runnable {

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
}