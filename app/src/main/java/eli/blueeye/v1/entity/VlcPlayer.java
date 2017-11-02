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

/**
 * Created by eli chang on 2017/11/2.
 */

public class VlcPlayer implements SurfaceHolder.Callback {

    private static final String TAG = "VlcPlayer";

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
            options.add(":file-caching=1000");//文件缓存
            options.add(":network-caching=3000");//网络缓存
            options.add(":live-caching=1000");//直播缓存
            options.add(":sout-mux-caching=1000");//输出缓存
            //options.add(":codec=mediacodec,iomx,all");

            libvlc = new LibVLC(context, options);
            mediaPlayer = new MediaPlayer(libvlc);
            media = new Media(libvlc, Uri.parse(url));
            mediaPlayer.setMedia(media);

            iVlcVout = mediaPlayer.getVLCVout();
            iVlcVout.setVideoView(surfaceView);
            iVlcVout.attachViews();

            iVlcVout.addCallback(new IVLCVout.Callback() {
                @Override
                public void onSurfacesCreated(IVLCVout vlcVout) {
                    int width = surfaceView.getWidth();
                    int height = surfaceView.getHeight();

                    if (width * height == 0) {
                        return;
                    }

                    mediaPlayer.getVLCVout().setWindowSize(width, height);
                    mediaPlayer.setAspectRatio("16:9");
                    mediaPlayer.setScale(0);

                    play();
                }

                @Override
                public void onSurfacesDestroyed(IVLCVout vlcVout) {
                }
            });
        } catch (Exception e) {
        }
    }

    private void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    private void releasePlayer() {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mediaPlayer.getVLCVout().setWindowSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
