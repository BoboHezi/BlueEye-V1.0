package eli.blueeye.v1.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.util.DisplayMetrics;
import android.view.View;

public class Util {

    /**
     * Dip转像素
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
     * @param context
     * @return
     */
    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 获取屏幕宽度
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
     * @param imagePath 图片路径
     * @param width     缩略图宽度
     * @param height    缩略图高度
     * @return          缩略图
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
}
