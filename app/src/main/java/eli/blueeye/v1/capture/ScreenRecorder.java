package eli.blueeye.v1.capture;

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
    private static final String eVideoPath = Environment.getExternalStorageDirectory().getPath() + "/blueeye/videos/";

    private int eWindowWidth;
    private int eWindowHeight;
    private int eScreenDensity;
    private int eBiteRate;
    private int eDpi;

    private Context context;
    private MediaProjection eMediaProjection;
    public MediaProjectionManager eMediaProjectionManager;

    private static final String MIME_TYPE = "video/avc";
    private static final int FRAME_RATE = 30;
    private static final int FRAME_INTERVAL = 2;
    private static final int TIMEOUT_US = 10000;

    private MediaCodec eMediaCodec;
    private Surface surface;
    private MediaMuxer eMediaMixer;
    private boolean isMixerStarted = false;
    private int eVideoTrackIndex = -1;
    private AtomicBoolean eQuit = new AtomicBoolean(false);
    private MediaCodec.BufferInfo eBufferInfo = new MediaCodec.BufferInfo();
    private VirtualDisplay eVirtualDisplay;

    public ScreenRecorder(Context context) {
        this.context = context;
        this.createEnvironment();

        this.eBiteRate = 6000000;
        this.eDpi = 1;

        createEnvironment();
    }

    //初始化截屏、录屏
    private void createEnvironment() {
        eMediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        eWindowWidth = displayMetrics.widthPixels;
        eWindowHeight = displayMetrics.heightPixels;
        eScreenDensity = displayMetrics.densityDpi;

        Log.i(TAG, "ScreenWidth:" + eWindowWidth + "\nScreenHeight:" + eWindowHeight + "\nDensity:" + eScreenDensity);
    }

    //准备
    public void prepareCapture(int resultCode, Intent resultData) {
        setMediaProjection(resultCode, resultData);
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int i = eWindowHeight;
            eWindowHeight = eWindowWidth;
            eWindowWidth = i;
        }
        setVirtualDisplay();

        try {
            File folder = new File(eVideoPath);
            if (!folder.exists())
                folder.mkdirs();
        } catch (Exception e) {
        }
    }

    //获取到MediaProjection
    public void setMediaProjection(int resultCode, Intent resultData) {
        eMediaProjection = eMediaProjectionManager.getMediaProjection(resultCode, resultData);
    }

    //设置VirtualDisplay
    public void setVirtualDisplay() {
        eMediaProjection.createVirtualDisplay("ScreenRecord",
                eWindowWidth, eWindowHeight, eScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface, null, null);
    }

    public final void quit() {
            eQuit.set(true);
    }

    @Override
    public void run() {

        try {
            try {
                prepareEncoder();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String name = "VID_" + format.format(new Date()) + ".mp4";
                eMediaMixer = new MediaMuxer(eVideoPath + name, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException e) {
                e.printStackTrace();
            }

            eVirtualDisplay = eMediaProjection.createVirtualDisplay(TAG + "-display",
                    eWindowWidth, eWindowHeight, eDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    surface, null, null);
            recordVirtualDisplay();
        } finally {
            release();
        }
    }

    private void recordVirtualDisplay() {
        while (!eQuit.get()) {
            int index = eMediaCodec.dequeueOutputBuffer(eBufferInfo, TIMEOUT_US);

            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                resetOutputFormat();
            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            } else if (index >= 0) {
                if (!isMixerStarted) {
                    throw new IllegalStateException("MediaMixer dose not call addTrack(format)");
                }
                encodeToVideoTrack(index);
                eMediaCodec.releaseOutputBuffer(index, false);
            }
        }
    }

    private void encodeToVideoTrack(int index) {

        ByteBuffer encodedData = eMediaCodec.getOutputBuffer(index);

        if ((eBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            eBufferInfo.size = 0;
        }
        if (eBufferInfo.size == 0) {
            encodedData = null;
        }

        if (encodedData != null) {
            encodedData.position(eBufferInfo.offset);
            encodedData.limit(eBufferInfo.offset + eBufferInfo.size);
            eMediaMixer.writeSampleData(eVideoTrackIndex, encodedData, eBufferInfo);
        }
    }

    private void resetOutputFormat() {

        if (isMixerStarted) {
            throw new IllegalStateException("output format already changed!");
        }

        MediaFormat newFormat = eMediaCodec.getOutputFormat();
        eVideoTrackIndex = eMediaMixer.addTrack(newFormat);
        eMediaMixer.start();
        isMixerStarted = true;
    }

    private void prepareEncoder(){

        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, eWindowWidth, eWindowHeight);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, eBiteRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, FRAME_INTERVAL);

        try {
            eMediaCodec = MediaCodec.createEncoderByType(MIME_TYPE);
        } catch (IOException e) {
        }
        eMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        surface = eMediaCodec.createInputSurface();
        eMediaCodec.start();
    }

    private void release() {
        if (eMediaCodec != null) {
            eMediaCodec.stop();
            eMediaCodec.release();
            eMediaCodec = null;
        }
        if (eVirtualDisplay != null) {
            eVirtualDisplay.release();
        }
        if (eMediaProjection != null) {
            eMediaProjection.stop();
        }
        if (eMediaMixer != null) {
            eMediaMixer.stop();
            eMediaMixer.release();
            eMediaMixer = null;
        }
    }
}