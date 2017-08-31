package eli.blueeye.v1.dialog;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import eli.blueeye.v1.R;
import eli.blueeye.v1.entity.LoadListView;
import eli.blueeye.v1.view.CustomSeekBar;

/**
 * 播放视频的Dialog
 *
 * @author eli chang
 */
public class CustomVideoDialog extends BaseDialog implements SurfaceHolder.Callback, View.OnClickListener {

    private SurfaceView eSurfaceView;
    private SurfaceHolder eSurfaceHolder;
    private MediaPlayer eMediaPlayer;
    private ImageButton eButtonMore;
    private File[] files;
    private GestureDetector eGestureDetector;

    private CustomSeekBar eSeekBar;
    private UpdateThread eUpdateThread;

    public CustomVideoDialog(Context context, Activity activity, File[] files, LoadListView.RefreshHandler refreshHandler) {
        super(context, activity, files, refreshHandler, R.style.style_image_dialog);
        this.files = files;
        initView();
        initVideo();

        eGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            /**
             * 单击屏幕 退出
             * @param e
             * @return
             */
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                dismiss();
                return super.onSingleTapConfirmed(e);
            }

            /**
             * 长按屏幕 显示更多操作
             * @param e
             */
            @Override
            public void onLongPress(MotionEvent e) {
                showActionDialog();
                super.onLongPress(e);
            }
        });
    }

    /**
     * 初始化窗口和视图
     */
    @Override
    public void initView() {
        setContentView(R.layout.dialog_video);
        eButtonMore = (ImageButton) findViewById(R.id.dialog_video_button_more);
        eButtonMore.setOnClickListener(this);
        eTimeTextView = (TextView) findViewById(R.id.dialog_video_text_time);
        showTime();
        eSeekBar = (CustomSeekBar) findViewById(R.id.dialog_video_seek_bar);
        eUpdateThread = new UpdateThread();
        setWindowAnimation();
    }

    /**
     * 初始化视频播放器
     */
    private void initVideo() {
        eSurfaceView = (SurfaceView) findViewById(R.id.video_surface);
        eSurfaceHolder = eSurfaceView.getHolder();
        eSurfaceHolder.setKeepScreenOn(true);
        eSurfaceView.setKeepScreenOn(true);
        eSurfaceHolder.addCallback(this);
    }

    /**
     * 设置窗体动画
     */
    @Override
    public void setWindowAnimation() {
        getWindow().setWindowAnimations(R.style.animation_image_dialog);
    }

    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        if (eMediaPlayer == null) {
            eMediaPlayer = new MediaPlayer();
        }
        eMediaPlayer.reset();
        eMediaPlayer.setLooping(true);
        try {
            eMediaPlayer.setDataSource(files[0].getPath());
            eMediaPlayer.prepareAsync();
            eMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setDisplay(surfaceHolder);
                    mediaPlayer.start();
                    //设置进度条全部的时间
                    eSeekBar.setTime(mediaPlayer.getDuration());
                    //启动更新进度条的线程
                    eUpdateThread = new UpdateThread();
                    eUpdateThread.start();
                }
            });
        } catch (IOException e) {
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        eMediaPlayer.stop();
        eMediaPlayer.release();
        eMediaPlayer = null;
        eUpdateThread.interrupt();
    }

    /**
     * 按钮点击
     *
     * @param view
     */
    @Override
    public void onClick(View view) {
        showActionDialog();
    }

    /**
     * 重写触摸方法
     * 将数据传给GestureDetector判断事件
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        eGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    /**
     * 更新进度条和播放进度的线程
     */
    class UpdateThread extends Thread {
        @Override
        public void run() {
            while (eMediaPlayer != null && !this.isInterrupted()) {
                try {
                    //当进度条被重新定位时，更新播放的位置
                    if (eSeekBar.isSeek()) {
                        int seekTime = eSeekBar.getSeekTime();
                        eSeekBar.cancelSeek();
                        eMediaPlayer.seekTo(seekTime);
                        continue;
                    }
                    //获取目前的播放位置，更新进度条
                    int position = eMediaPlayer.getCurrentPosition();
                    eSeekBar.setCurrentTime(position);
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    break;
                } catch (IllegalStateException e) {
                    break;
                }
            }
        }
    }
}