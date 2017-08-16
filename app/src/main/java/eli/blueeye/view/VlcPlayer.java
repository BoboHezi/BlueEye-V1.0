package eli.blueeye.view;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;
import org.videolan.libvlc.Util;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VlcPlayer implements SurfaceHolder.Callback{

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Activity activity;
    private String url;

    private Handler handler = new SurfaceHandler(this);

    private LibVLC libVLC;
    private int viewWidth;
    private int viewHeight;

    private static final int VideoSizeChanged = -1;
    private static final String TAG = "VlcPlayer";
    private static final String VID_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/blueeye/videos/";
    private static final String IMG_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/blueeye/photos/";

    public VlcPlayer(SurfaceView surfaceView, Activity activity, String url) {
        this.surfaceView = surfaceView;
        this.activity = activity;
        this.url = url;

        this.surfaceView.setKeepScreenOn(true);
        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceHolder.addCallback(this);
    }

    /**
     * 创建VLC显示
     */
    public void createPlayer() {
        releasePlayer();
        try {
            libVLC = Util.getLibVlcInstance();
            libVLC.setSubtitlesEncoding("");
            libVLC.setAout(LibVLC.AOUT_OPENSLES);
            libVLC.setTimeStretching(true);
            libVLC.setChroma("RV32");
            libVLC.setVerboseMode(true);
            libVLC.restart(activity);
            EventHandler.getInstance().addHandler(handler);
            surfaceHolder.setFormat(PixelFormat.RGBX_8888);
            surfaceHolder.setKeepScreenOn(true);
            MediaList list = libVLC.getMediaList();
            list.clear();
            Media media = new Media(libVLC, LibVLC.PathToURI(url));
            media.getWidth();
            media.getHeight();
            list.add(media, false);
            libVLC.playIndex(0);
        } catch (Exception e) {
        }
    }

    /**
     * 开始播放
     */
    public void play() {
        if (libVLC == null)
            createPlayer();
        libVLC.play();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (libVLC != null)
            libVLC.pause();
    }

    /**
     * 释放资源
     */
    public void releasePlayer() {
        if (libVLC == null)
            return;
        EventHandler.getInstance().removeHandler(handler);
        libVLC.stop();
        libVLC.detachSurface();
        surfaceHolder = null;
        libVLC.closeAout();
        libVLC.destroy();
        libVLC = null;

        viewWidth = 0;
        viewHeight = 0;
    }

    /**
     * 是否正在播放
     * @return
     */
    public boolean isPlaying() {
        if (libVLC == null) {
            return false;
        }
        return libVLC.isPlaying();
    }

    /**
     * 是否处于录屏状态
     * @return
     */
    public boolean isRecording() {
        if (libVLC == null) {
            return false;
        }
        return libVLC.videoIsRecording();
    }

    /**
     * 截图
     */
    public void snapShot() {
        new TakePhotoThread().start();
    }

    /**
     * 开始录像
     */
    public void startRecord() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String path = VID_FOLDER + "VID_" + sdf.format(new Date());
        while (true) {
            if (!libVLC.videoIsRecording()) {
                libVLC.videoRecordStart(path);
            }
            if (libVLC.videoIsRecording()) {
                return;
            }
        }
    }

    /**
     * 结束录像
     */
    public void stopRecord() {
        while (true) {
            if (libVLC.videoIsRecording()) {
                libVLC.videoRecordStop();
            }
            if (!libVLC.videoIsRecording()) {
                return;
            }
        }
    }

    /**
     * 重置画面
     * @param width     画面宽度
     * @param height    画面高度
     */
    private void setSize(int width, int height) {
        if (surfaceHolder == null || surfaceView == null) {
            return;
        }
        viewWidth = width;
        viewHeight = height;

        if (viewWidth * viewHeight <= 1)
            return;

        int screenWidth = activity.getWindow().getDecorView().getWidth();
        int screenHeight = activity.getWindow().getDecorView().getHeight();

        boolean isPortrait = activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        if ( (screenWidth > screenHeight && isPortrait) || (screenWidth < screenHeight && !isPortrait) ) {
            int i = screenWidth;
            screenWidth = screenHeight;
            screenHeight = i;
        }

        float videoAR = (float) viewWidth / (float) viewHeight;
        float screenAR = (float) screenWidth / (float) screenHeight;

        if (screenAR >= videoAR) {
            screenWidth = (int) (screenHeight / videoAR);
        }
        screenHeight = (screenWidth * 9) / 16;

        surfaceHolder.setFixedSize(viewWidth, viewHeight);

        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        lp.width = screenWidth;
        lp.height = screenHeight;
        surfaceView.setLayoutParams(lp);
        surfaceView.invalidate();
    }

    //定义播放器接口
    IVideoPlayer videoPlayer = new IVideoPlayer() {
        @Override
        public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
            Message msg = Message.obtain(handler, VideoSizeChanged, width, height);
            msg.sendToTarget();
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (libVLC != null) {
            libVLC.attachSurface(holder.getSurface(), videoPlayer);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    class SurfaceHandler extends Handler {

        private WeakReference<VlcPlayer> owner;

        public SurfaceHandler(VlcPlayer owner) {
            this.owner = new WeakReference<>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            VlcPlayer player = owner.get();

            if (msg.what == VideoSizeChanged) {
                player.setSize(msg.arg1, msg.arg2);
                return;
            }

            Bundle b = msg.getData();
            switch (b.getInt("event")) {
                case EventHandler.MediaPlayerEndReached:
                    player.releasePlayer();
                    break;
                case EventHandler.MediaPlayerPlaying:break;
                case EventHandler.MediaPlayerPaused:break;
                case EventHandler.MediaPlayerStopped:break;
                default:break;
            }
        }
    }

    /**
     * 截图的线程
     */
    private class TakePhotoThread extends Thread {
        @Override
        public void run() {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String path = IMG_FOLDER + "IMG_" + sdf.format(new Date()) + ".png";
                File file = new File(path);
                if (!file.exists()) {
                    file.createNewFile();
                }

                if (viewWidth > 0 && viewHeight > 0) {
                    if(!libVLC.takeSnapShot(path, viewWidth, viewHeight)) {
                        file.delete();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
