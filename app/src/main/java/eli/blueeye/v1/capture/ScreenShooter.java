package eli.blueeye.v1.capture;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 截屏
 *
 * @author eli chang
 */
public class ScreenShooter extends Thread {

    private static final String TAG = "ScreenShooter";
    private static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/blueeye/photos/";

    private Context context;
    private int eWindowWidth;
    private int eWindowHeight;
    private int eScreenDensity;
    private WindowManager eWindowManager;
    private ImageReader eImageReader;

    public MediaProjectionManager eMediaManager;
    private MediaProjection eMediaProjection;

    private Handler handler;

    public ScreenShooter(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
        this.createEnvironment();
    }

    @Override
    public void run() {
        startCapture();
    }

    private void createEnvironment() {
        eMediaManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        eWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        eWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
        eWindowWidth = displayMetrics.widthPixels;
        eWindowHeight = displayMetrics.heightPixels;
        eScreenDensity = displayMetrics.densityDpi;
        eImageReader = ImageReader.newInstance(eWindowWidth, eWindowHeight, 0x1, 2);
    }

    public void prepareCapture(int resultCode, Intent resultData) {
        setMediaProjection(resultCode, resultData);
        setVirtualDisplay();

        try {
            File folder = new File(PATH);
            if (!folder.exists())
                folder.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setMediaProjection(int resultCode, Intent resultData) {
        eMediaProjection = eMediaManager.getMediaProjection(resultCode, resultData);
    }

    private void setVirtualDisplay() {
        eMediaProjection.createVirtualDisplay("ScreenCapture", eWindowWidth, eWindowHeight,
                eScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                eImageReader.getSurface(), null, null);
    }

    public void startCapture() {

        SystemClock.sleep(100);
        Image image = eImageReader.acquireNextImage();
        if (image == null) {
            Log.e(TAG, "Image is NULL...");
            return;
        }

        //图片宽高
        int width = image.getWidth();
        int height = image.getHeight();

        final Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        int pixStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixStride * width;

        //获取Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);

        int offset = rowPadding / pixStride;
        width = width - offset * 8;
        height = (int) ((double) width / 1.795);

        //横屏状态下对图像进行截取
        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            int offsetHeight = bitmap.getHeight() / 2 - height / 2;
            if (offsetHeight > 0 && width > 0 && height > 0) {
                bitmap = Bitmap.createBitmap(bitmap, 0, offsetHeight, width, height);
                //保存
                saveToFile(bitmap);
                //提供至主视图
                postToActivity(bitmap);
                image.close();
            }
        }
    }

    public void saveToFile(Bitmap bitmap) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String name = "IMG_" + format.format(new Date()) + ".png";

            File file = new File(PATH, name);
            if (file.exists())
                file.delete();

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            return;
        }
    }

    private void postToActivity(Bitmap bitmap) {
        //传递Bitmap
        Bundle b = new Bundle();
        b.putParcelable("photo", bitmap);
        Message msg = Message.obtain();
        msg.setData(b);
        msg.what = 1;
        handler.sendMessage(msg);
    }
}