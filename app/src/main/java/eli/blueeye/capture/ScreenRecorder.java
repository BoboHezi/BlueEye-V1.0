package eli.blueeye.capture;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScreenRecorder extends Thread {

    private static final String TAG = "ScreenRecorder";
    private static final String videoPath = Environment.getExternalStorageDirectory().getPath() + "/blueeye/videos/";

    private int windowWidth;
    private int windowHeight;
    private int screenDensity;
    private int biteRate ;
    private int dpi;

    private Context context;
    private MediaProjection mediaProjection;
    public MediaProjectionManager mediaProjectionManager;

    private static final String MIME_TYPE = "video/avc";
    private static final int FRAME_RATE = 30;
    private static final int FRAME_INTERVAL = 2;
    private static final int TIMEOUT_US = 10000;

    private MediaCodec mediaCodec;
    private Surface surface;
    private MediaMuxer mediaMuxer;
    private boolean isMuxerStarted = false;
    private int videoTrackIndex = -1;
    private AtomicBoolean quit = new AtomicBoolean(false);
    private MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
    private VirtualDisplay virtualDisplay;

    public ScreenRecorder(Context context) {
        this.context = context;
        this.createEnvironment();

        this.biteRate = 6000000;
        this.dpi = 1;

        createEnvironment();
    }

    //初始化截屏、录屏
    private void createEnvironment() {
        mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        windowWidth = dm.widthPixels;
        windowHeight = dm.heightPixels;
        screenDensity = dm.densityDpi;

        Log.i(TAG, "ScreenWidth:" + windowWidth + "\nScreenHeight:" + windowHeight + "\nDensity:" + screenDensity);
    }

    //准备
    public void prepareCapture(int resultCode, Intent resultData) {
        setMediaProjection(resultCode, resultData);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int i = windowHeight;
            windowHeight = windowWidth;
            windowWidth = i;
        }
        setVirtualDisplay();

        try {
            File folder = new File(videoPath);
            if (!folder.exists())
                folder.mkdirs();
        } catch (Exception e) {
        }
    }

    //获取到MediaProjection
    public void setMediaProjection(int resultCode, Intent resultData) {
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, resultData);
    }

    //设置VirtualDisplay
    public void setVirtualDisplay() {
        mediaProjection.createVirtualDisplay("ScreenRecord",
                windowWidth, windowHeight, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null, null);
    }

    public final void quit() {
            quit.set(true);
    }

    @Override
    public void run() {

        try {
            try {
                prepareEncoder();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String name = "VID_" + format.format(new Date()) + ".mp4";
                mediaMuxer = new MediaMuxer(videoPath + name, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException e) {
                e.printStackTrace();
            }

            virtualDisplay = mediaProjection.createVirtualDisplay(TAG + "-display",
                    windowWidth, windowHeight, dpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    surface, null, null);
            recordVirtualDisplay();
        } finally {
            release();
        }

    }

    private void recordVirtualDisplay() {
        while (!quit.get()) {
            int index = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_US);

            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                resetOutputFormat();
            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            } else if (index >= 0) {
                if (!isMuxerStarted) {
                    throw new IllegalStateException("MediaMuxer dose not call addTrack(format)");
                }
                encodeToVideoTrack(index);
                mediaCodec.releaseOutputBuffer(index, false);
            }
        }
    }

    private void encodeToVideoTrack(int index) {

        ByteBuffer encodedData = mediaCodec.getOutputBuffer(index);

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            bufferInfo.size = 0;
        }
        if (bufferInfo.size == 0) {
            encodedData = null;
        }

        if (encodedData != null) {
            encodedData.position(bufferInfo.offset);
            encodedData.limit(bufferInfo.offset + bufferInfo.size);
            mediaMuxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo);
        }
    }

    private void resetOutputFormat() {

        if (isMuxerStarted) {
            throw new IllegalStateException("output format already changed!");
        }

        MediaFormat newFormat = mediaCodec.getOutputFormat();
        videoTrackIndex = mediaMuxer.addTrack(newFormat);
        mediaMuxer.start();
        isMuxerStarted = true;
    }

    private void prepareEncoder(){

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, windowWidth, windowHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, biteRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, FRAME_INTERVAL);

        try {
            mediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        } catch (IOException e) {
        }
        mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        surface = mediaCodec.createInputSurface();
        mediaCodec.start();
    }

    private void release() {
        if (mediaCodec != null) {
            mediaCodec.stop();
            mediaCodec.release();
            mediaCodec = null;
        }
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
        if (mediaProjection != null) {
            mediaProjection.stop();
        }
        if (mediaMuxer != null) {
            mediaMuxer.stop();
            mediaMuxer.release();
            mediaMuxer = null;
        }
    }
}
