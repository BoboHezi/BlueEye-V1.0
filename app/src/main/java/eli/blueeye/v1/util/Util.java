package eli.blueeye.v1.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.util.DisplayMetrics;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;

import eli.blueeye.v1.data.FileType;

/**
 * 工具类
 *
 * @author eli chang
 */
public class Util {

    /**
     * Dip转像素
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 像素转Dip
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 设置组件的背景
     *
     * @param context
     * @param view
     * @param ID
     */
    public static void setBackImage(Context context, View view, int ID) {
        Resources res = context.getResources();
        Drawable drawable = res.getDrawable(ID, null);
        view.setBackground(drawable);
    }

    /**
     * 判断屏幕方向
     *
     * @param context
     * @return
     */
    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 获取屏幕宽度
     *
     * @param activity
     * @return
     */
    public static int getScreenWidth(Activity activity) {
        try {
            //获取屏幕宽度
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取屏幕宽度
     *
     * @param activity
     * @return
     */
    public static int getScreenHeight(Activity activity) {
        try {
            //获取屏幕宽度
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.heightPixels;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获取图片的缩略图
     *
     * @param imagePath 图片路径
     * @param width     缩略图宽度
     * @param height    缩略图高度
     * @return 缩略图
     */
    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false;

        int w = options.outWidth;
        int h = options.outHeight;
        int beWidth = w / width;
        int beHeight = h / height;
        int be;

        if (beWidth < beHeight)
            be = beWidth;
        else
            be = beHeight;
        if (be <= 0)
            be = 1;

        options.inSampleSize = be;
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

        return bitmap;
    }

    /**
     * 获取视频的缩略图
     *
     * @param videoPath 视频路径
     * @param width     缩略图宽度
     * @param height    缩略图高度
     * @param kind
     * @return
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap;
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /**
     * 将Bitmap对象转为字节数组
     *
     * @param bitmap
     * @param needRecycle
     * @return
     */
    public static byte[] bitmap2ByteArray(final Bitmap bitmap, final boolean needRecycle) {
        int width;

        if (bitmap.getHeight() > bitmap.getWidth()) {
            width = bitmap.getWidth();
        } else {
            width = bitmap.getHeight();
        }

        Bitmap localBitmap = Bitmap.createBitmap(width, width, Bitmap.Config.RGB_565);
        Canvas localCanvas = new Canvas(localBitmap);

        while (true) {
            localCanvas.drawBitmap(bitmap, new Rect(0, 0, width, width), new Rect(0, 0, width, width), null);

            if (needRecycle)
                bitmap.recycle();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            localBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

            localBitmap.recycle();
            byte[] result = baos.toByteArray();
            try {
                baos.close();
                return result;
            } catch (Exception e) {
            }
            width = bitmap.getHeight();
        }
    }

    /**
     * @param type
     * @return
     */
    public static String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    /**
     * 检查文件的类型
     *
     * @param file
     * @return
     */
    public static FileType checkFileType(File file) {
        if (file.getName().contains("IMG")) {
            return FileType.PHOTO;
        } else if (file.getName().contains("VID")) {
            return FileType.VIDEO;
        } else {
            return FileType.OTHER;
        }
    }
}