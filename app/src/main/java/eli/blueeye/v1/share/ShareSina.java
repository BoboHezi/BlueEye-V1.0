package eli.blueeye.v1.share;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import com.sina.weibo.sdk.WbSdk;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.MultiImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.VideoSourceObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.share.WbShareCallback;
import com.sina.weibo.sdk.share.WbShareHandler;

import java.io.File;
import java.util.ArrayList;

import eli.blueeye.v1.inter.Constants;

/**
 * 分享到新浪微博的实体
 *
 * @author eli chang
 * @see BaseShare
 */
public class ShareSina extends BaseShare implements WbShareCallback {

    //微博分享实体
    private WbShareHandler shareHandler;

    public ShareSina(Context context, Activity activity) {
        super(context, activity);
        initSDK();
    }

    /**
     * 初始化微博分享
     */
    @Override
    public void initSDK() {
        WbSdk.install(context, new AuthInfo(context, Constants.APP_KEY, Constants.REDIRECT_URL, Constants.SCOPE));
        shareHandler = new WbShareHandler(activity);
        shareHandler.registerApp();
        shareHandler.setProgressColor(0xff33b5e5);
    }

    /**
     * 获取分享实体
     *
     * @return
     */
    public WbShareHandler getShareHandler() {
        return this.shareHandler;
    }

    /**
     * 分享图片
     *
     * @param imageFile 图片文件
     */
    public void shareImage(File imageFile) {
        WeiboMultiMessage weiboMultiMessage = new WeiboMultiMessage();
        weiboMultiMessage.textObject = getTextObject("This Is A Photo.", "", "");
        weiboMultiMessage.imageObject = getImageObject(imageFile);
        shareHandler.shareMessage(weiboMultiMessage, false);
    }

    /**
     * 分享视频
     *
     * @param videoFile 视频文件
     */
    public void shareVideo(File videoFile) {
        WeiboMultiMessage weiboMultiMessage = new WeiboMultiMessage();
        weiboMultiMessage.textObject = getTextObject("This Is A Video.", "", "");
        weiboMultiMessage.videoSourceObject = getVideoObject(videoFile);
        shareHandler.shareMessage(weiboMultiMessage, false);
    }

    /**
     * 分享多图
     *
     * @param imageFiles 文件数组
     */
    public void shareMultiImage(File imageFiles[]) {
        WeiboMultiMessage weiboMultiMessage = new WeiboMultiMessage();
        weiboMultiMessage.textObject = getTextObject("There are Many Photos.", "", "");
        weiboMultiMessage.multiImageObject = getMultiImageObject(imageFiles);
        shareHandler.shareMessage(weiboMultiMessage, false);
    }

    /**
     * 创建文字分享
     *
     * @param text      文字
     * @param title     标题
     * @param actionURL 链接
     * @return
     */
    private TextObject getTextObject(String text, String title, String actionURL) {
        TextObject object = new TextObject();
        object.text = text;
        object.title = title;
        object.actionUrl = actionURL;
        return object;
    }

    /**
     * 创建图片分享
     *
     * @param imageFile 图片文件
     * @return
     */
    private ImageObject getImageObject(File imageFile) {
        ImageObject imageObject = new ImageObject();

        if (imageFile == null) {
            imageFile = new File(context.getExternalFilesDir(null) + "/photo/photo.jpg");
        }
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getPath(), null);
        imageObject.setImageObject(bitmap);
        return imageObject;
    }

    /**
     * 创建视频分享
     *
     * @param videoFile 视频文件
     * @return
     */
    private VideoSourceObject getVideoObject(File videoFile) {
        VideoSourceObject videoObject = new VideoSourceObject();

        if (videoFile == null) {
            videoFile = new File(context.getExternalFilesDir(null) + "/video/VID_20170811_105225.mp4");
        }
        videoObject.videoPath = Uri.fromFile(videoFile);
        return videoObject;
    }

    /**
     * 创建多张图分享
     *
     * @return
     */
    private MultiImageObject getMultiImageObject(File files[]) {
        MultiImageObject multiImageObject = new MultiImageObject();

        if (files == null) {
            files = new File(context.getExternalFilesDir(null) + "/photo/").listFiles();
        }
        ArrayList<Uri> fileList = new ArrayList<>();
        for (File file : files) {
            fileList.add(Uri.fromFile(file));
        }
        multiImageObject.setImageList(fileList);
        return multiImageObject;
    }

    @Override
    public void onWbShareSuccess() {
        Toast.makeText(context, "分享成功", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onWbShareCancel() {
        Toast.makeText(context, "分享取消", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onWbShareFail() {
        Toast.makeText(context, "分享失败", Toast.LENGTH_LONG).show();
    }
}