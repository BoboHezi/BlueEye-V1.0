package eli.blueeye.v1.entity;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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

import eli.blueeye.v1.activity.MainActivity;

/**
 * vlc-android实现类
 *
 * @author eli chang
 */
public class VlcPlayer implements SurfaceHolder.Callback {

    private SurfaceView eSurfaceView;
    private SurfaceHolder eSurfaceHolder;
    private Activity activity;
    private String eURL;
    MainActivity.SnapHandler ePhotoHandler;

    private Handler eHandler;

    private LibVLC eLibVLC;
    private int eViewWidth;
    private int eViewHeight;
    private String eScreenCapturePath;
    private String eScreenRecodePath;

    private static final int VideoSizeChanged = -1;
    private static final String TAG = "VlcPlayer";
    private static final String VID_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/blueeye/videos/";
    private static final String IMG_FOLDER = Environment.getExternalStorageDirectory().getPath() + "/blueeye/photos/";

    public VlcPlayer(SurfaceView eSurfaceView, Activity activity, String eURL, MainActivity.SnapHandler ePhotoHandler) {
        this.eSurfaceView = eSurfaceView;
        this.activity = activity;
        this.eURL = eURL;
        this.ePhotoHandler = ePhotoHandler;
        this.eSurfaceView.setKeepScreenOn(true);
        this.eSurfaceHolder = eSurfaceView.getHolder();
        this.eSurfaceHolder.addCallback(this);
        this.eHandler = new SurfaceHandler(this);
    }

    /**
     * 创建VLC显示
     */
    public void createPlayer() {
        releasePlayer();
        try {
            eLibVLC = Util.getLibVlcInstance();
            eLibVLC.setSubtitlesEncoding("");
            eLibVLC.setAout(LibVLC.AOUT_OPENSLES);
            eLibVLC.setTimeStretching(true);
            eLibVLC.setChroma("RV32");
            eLibVLC.setVerboseMode(true);
            eLibVLC.restart(activity);
            EventHandler.getInstance().addHandler(eHandler);
            eSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
            eSurfaceHolder.setKeepScreenOn(true);
            MediaList list = eLibVLC.getMediaList();
            list.clear();
            Media media = new Media(eLibVLC, LibVLC.PathToURI(eURL));
            media.getWidth();
            media.getHeight();
            list.add(media, false);
            eLibVLC.playIndex(0);
        } catch (Exception e) {
        }
    }

    /**
     * 开始播放
     */
    public void play() {
        if (eLibVLC == null)
            createPlayer();
        eLibVLC.play();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (eLibVLC != null)
            eLibVLC.pause();
    }

    /**
     * 释放资源
     */
    public void releasePlayer() {
        if (eLibVLC == null)
            return;
        EventHandler.getInstance().removeHandler(eHandler);
        eLibVLC.stop();
        eLibVLC.detachSurface();
        eSurfaceHolder = null;
        eLibVLC.closeAout();
        eLibVLC.destroy();
        eLibVLC = null;

        eViewWidth = 0;
        eViewHeight = 0;
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        if (eLibVLC == null) {
            return false;
        }
        return eLibVLC.isPlaying();
    }

    /**
     * 是否处于录屏状态
     *
     * @return
     */
    public boolean isRecording() {
        if (eLibVLC == null) {
            return false;
        }
        return eLibVLC.videoIsRecording();
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
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
        eScreenRecodePath = VID_FOLDER + "VID_" + format.format(new Date());
        while (true) {
            if (!eLibVLC.videoIsRecording()) {
                eLibVLC.videoRecordStart(eScreenRecodePath);
            }
            if (eLibVLC.videoIsRecording()) {
                return;
            }
        }
    }

    /**
     * 结束录像
     */
    public void stopRecord() {
        while (true) {
            if (eLibVLC.videoIsRecording()) {
                eLibVLC.videoRecordStop();
                if (eScreenRecodePath != null) {
                    sendSnapResult(MainActivity.CAPTURE_VIDEO, eScreenRecodePath);
                    eScreenRecodePath = null;
                }
            }
            if (!eLibVLC.videoIsRecording()) {
                return;
            }
        }
    }

    /**
     * 重置画面
     *
     * @param width  画面宽度
     * @param height 画面高度
     */
    private void setSize(int width, int height) {
        if (eSurfaceHolder == null || eSurfaceView == null) {
            return;
        }
        //设置视频宽高
        eViewWidth = width;
        eViewHeight = height;

        if (eViewWidth * eViewHeight <= 1)
            return;

        //获取屏幕宽高
        int screenWidth = activity.getWindow().getDecorView().getWidth();
        int screenHeight = activity.getWindow().getDecorView().getHeight();

        //屏幕方向
        boolean isPortrait = activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

        //重置屏幕宽高
        if ((screenWidth > screenHeight && isPortrait) || (screenWidth < screenHeight && !isPortrait)) {
            int i = screenWidth;
            screenWidth = screenHeight;
            screenHeight = i;
        }

        //计算比例
        float videoAR = (float) eViewWidth / (float) eViewHeight;
        float screenAR = (float) screenWidth / (float) screenHeight;

        if (screenAR >= videoAR) {
            screenWidth = (int) (screenHeight / videoAR);
        }
        //设置画面为16 : 9
        screenHeight = (screenWidth * 9) / 16;

        //设置画面宽高
        eSurfaceHolder.setFixedSize(eViewWidth, eViewHeight);

        //设置播放器宽高
        ViewGroup.LayoutParams lp = eSurfaceView.getLayoutParams();
        if (isPortrait) {
            lp.width = screenWidth;
            lp.height = screenHeight;
        } else {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        eSurfaceView.setLayoutParams(lp);
        eSurfaceView.invalidate();
    }

    //定义播放器接口
    IVideoPlayer videoPlayer = new IVideoPlayer() {
        @Override
        public void setSurfaceSize(int width, int height, int visible_width, int visible_height, int sar_num, int sar_den) {
            Message msg = Message.obtain(eHandler, VideoSizeChanged, width, height);
            msg.sendToTarget();
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (eLibVLC != null) {
            eLibVLC.attachSurface(holder.getSurface(), videoPlayer);
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
                case EventHandler.MediaPlayerPlaying:
                    break;
                case EventHandler.MediaPlayerPaused:
                    break;
                case EventHandler.MediaPlayerStopped:
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 发送截录屏结果和对应文件路径
     */
    private void sendSnapResult(int fileType, String path) {
        final Message msg = ePhotoHandler.obtainMessage();
        msg.what = fileType;
        Bundle bundle = new Bundle();
        bundle.putString("path", path);
        msg.setData(bundle);

        if (fileType == MainActivity.CAPTURE_VIDEO) {
            ePhotoHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ePhotoHandler.sendMessage(msg);
                }
            }, 2000);
        } else {
            ePhotoHandler.sendMessage(msg);
        }
    }

    /**
     * 截图的线程
     */
    private class TakePhotoThread extends Thread {
        @Override
        public void run() {
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
                eScreenCapturePath = IMG_FOLDER + "IMG_" + format.format(new Date()) + ".png";
                File file = new File(eScreenCapturePath);
                if (!file.exists()) {
                    file.createNewFile();
                }

                if (eViewWidth > 0 && eViewHeight > 0) {
                    if (!eLibVLC.takeSnapShot(eScreenCapturePath, eViewWidth, eViewHeight)) {
                        file.delete();
                    } else {
                        if (eScreenCapturePath != null) {
                            sendSnapResult(MainActivity.CAPTURE_PHOTO, eScreenCapturePath);
                            eScreenCapturePath = null;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}