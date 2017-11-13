package eli.blueeye.v1.entity;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.util.ArrayList;

public class VlcPlayer implements SurfaceHolder.Callback, IVLCVout.Callback {

    private static final String TAG = "VlcPlayer";

    private boolean couldPlayer = false;

    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    private MediaPlayer mediaPlayer;
    private IVLCVout iVlcVout;
    private Activity activity;
    private Context context;
    private LibVLC libvlc;
    private Media media;
    private String url;

    private ArrayList<String> options;

    public VlcPlayer(SurfaceView surfaceView, Context context, Activity activity, String url) {
        this.surfaceView = surfaceView;
        this.context = context;
        this.activity = activity;
        this.url = url;

        this.surfaceHolder = surfaceView.getHolder();
        this.surfaceView.setKeepScreenOn(true);
        this.surfaceHolder.setKeepScreenOn(true);
        this.surfaceHolder.addCallback(this);
    }

    public void createPlayer() {
        releasePlayer();
        try {
            options = new ArrayList<>();
            options.add("--aout=none");
            options.add("--swscale-mode=0");

            libvlc = new LibVLC(context, options);

            media = new Media(libvlc, Uri.parse(url));
            media.setHWDecoderEnabled(true, true);
            media.addOption(":network-caching=150");
            media.addOption(":clock-jitter=0");
            media.addOption(":clock-synchro=0");

            mediaPlayer = new MediaPlayer(libvlc);
            mediaPlayer.setMedia(media);

            iVlcVout = mediaPlayer.getVLCVout();
            iVlcVout.setVideoView(surfaceView);
            iVlcVout.attachViews();

            iVlcVout.addCallback(this);
        } catch (Exception e) {
        }
    }

    /**
     * 开始播放
     */
    public void play() {
        if (mediaPlayer != null && couldPlayer) {
            mediaPlayer.play();
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * 释放资源
     */
    public void releasePlayer() {
        if (libvlc == null) {
            return;
        }

        libvlc.release();
        mediaPlayer.stop();
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        if (libvlc == null) {
            return false;
        }
        return mediaPlayer.isPlaying();
    }

    /**
     * 是否处于录屏状态
     *
     * @return
     */
    public boolean isRecording() {
        if (libvlc == null) {
            return false;
        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        setViewSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {
        int width = surfaceView.getWidth();
        int height = surfaceView.getHeight();

        if (width * height == 0) {
            return;
        }

        setViewSize(width, height);

        couldPlayer = true;
        play();
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {
    }

    /**
     * 设置画面尺寸
     *
     * @param width
     * @param height
     */
    private void setViewSize(int width, int height) {
        if (mediaPlayer != null) {
            mediaPlayer.getVLCVout().setWindowSize(width, height);
            mediaPlayer.setAspectRatio(width + ":" + height);
            mediaPlayer.setScale(0);
        }
    }
}
