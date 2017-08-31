package eli.blueeye.v1.entity;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import eli.blueeye.v1.dialog.PlatformSelectDialog;
import eli.blueeye.v1.inter.OnPlatformSelected;
import eli.blueeye.v1.share.ShareCircleFriends;
import eli.blueeye.v1.share.ShareQQ;
import eli.blueeye.v1.share.ShareQZone;
import eli.blueeye.v1.share.ShareSina;
import eli.blueeye.v1.share.ShareWeChat;

/**
 * 分享到社交网络实现类
 *
 * @author eli chang
 */
public class ShareEntity implements OnPlatformSelected {

    public static final int PLATFORM_ID_SINA = 1;
    public static final int PLATFORM_ID_CIRCLEFRIENDS = 2;
    public static final int PLATFORM_ID_WENCAHT = 3;
    public static final int PLATFORM_ID_QQ = 4;
    public static final int PLATFORM_ID_QZONE = 5;
    public static final int PLATFORM_ID_CANCEL = 6;

    public static final int SHARE_TYPE_TEXT = 1;
    public static final int SHARE_TYPE_PHOTO = 2;
    public static final int SHARE_TYPE_VIDEO = 3;
    public static final int SHARE_TYPE_MULTIIMAGE = 4;
    public int shareType = SHARE_TYPE_TEXT;

    private PlatformSelectDialog ePlatformDialog;

    private Context context;
    private Activity activity;
    private File[] files;

    private ShareSina eShareToSina;
    private ShareQQ eShareToQQ;
    private ShareQZone eShareToQZone;
    private ShareWeChat eShareToWeChat;

    public ShareEntity(Context context, Activity activity, File[] files) {
        this.context = context;
        this.activity = activity;
        this.files = files;

        ePlatformDialog = new PlatformSelectDialog(context);
        ePlatformDialog.setOnPlatformSelected(this);
        ePlatformDialog.show();
    }

    /**
     * 设置分享内容的类别
     *
     * @param shareType
     */
    public void setShareType(int shareType) {
        this.shareType = shareType;
    }

    /**
     * 获取实体
     *
     * @return
     */
    public ShareQQ geteShareToQQ() {
        return this.eShareToQQ;
    }

    /**
     * 获取实体
     *
     * @return
     */
    public ShareSina geteShareToSina() {
        return this.eShareToSina;
    }

    /**
     * 获取实体
     *
     * @return
     */
    public ShareQZone geteShareToQZone() {
        return this.eShareToQZone;
    }

    /**
     * 分享到新浪微博
     */
    private void shareSina() {
        if (shareType == SHARE_TYPE_TEXT)
            return;

        eShareToSina = new ShareSina(context, activity);
        switch (shareType) {
            case SHARE_TYPE_PHOTO:
                //分享单张图片
                eShareToSina.shareImage(files[0]);
                break;
            case SHARE_TYPE_VIDEO:
                //分享视频
                eShareToSina.shareVideo(files[0]);
                break;
            case SHARE_TYPE_MULTIIMAGE:
                //分享多张图片
                eShareToSina.shareMultiImage(files);
                break;
        }
        shareType = SHARE_TYPE_TEXT;
    }

    /**
     * 分享到QQ好友
     */
    private void shareQQ() {
        if (shareType == SHARE_TYPE_TEXT)
            return;

        eShareToQQ = new ShareQQ(context, activity);
        switch (shareType) {
            case SHARE_TYPE_PHOTO:
                //分享单张图片
                eShareToQQ.shareImage(files[0]);
                break;
            default:
                Toast.makeText(context, "No Way", Toast.LENGTH_SHORT).show();
                break;
        }
        shareType = SHARE_TYPE_TEXT;
    }

    /**
     * 分享到QQ空间
     */
    private void shareQZone() {
        if (shareType == SHARE_TYPE_TEXT)
            return;

        eShareToQZone = new ShareQZone(context, activity);
        switch (shareType) {
            case SHARE_TYPE_PHOTO:
                //分享单张图片
                eShareToQZone.shareImage(files[0]);
                break;
            case SHARE_TYPE_VIDEO:
                //分享视频
                eShareToQZone.shareVideo(files[0]);
                break;
            case SHARE_TYPE_MULTIIMAGE:
                //分享多张图片
                eShareToQZone.shareMultiImage(files);
                break;
        }
        shareType = SHARE_TYPE_TEXT;
    }

    /**
     * 发送至微信
     */
    private void shareWeChat() {
        if (shareType == SHARE_TYPE_TEXT)
            return;

        eShareToWeChat = new ShareWeChat(context, activity);
        switch (shareType) {
            case SHARE_TYPE_PHOTO:
                //分享图片
                eShareToWeChat.shareImage(files[0]);
                break;
            case SHARE_TYPE_MULTIIMAGE:
                //分享多图
                eShareToWeChat.shareMultiImage(files);
                break;
            default:
                Toast.makeText(context, "No Way", Toast.LENGTH_SHORT).show();
        }
        shareType = SHARE_TYPE_TEXT;
    }

    /**
     * 分享到朋友圈
     */
    private void shareCircleFriends() {
        if (shareType == SHARE_TYPE_TEXT)
            return;

        ShareCircleFriends shareCircleFriends = new ShareCircleFriends(context, activity);
        switch (shareType) {
            case SHARE_TYPE_PHOTO:
                //分享图片
                shareCircleFriends.shareImage(files[0]);
                break;
            case SHARE_TYPE_MULTIIMAGE:
                //分享多图
                shareCircleFriends.shareMultiImage(files);
                break;
            default:
                Toast.makeText(context, "No Way", Toast.LENGTH_SHORT).show();
        }
        shareType = SHARE_TYPE_TEXT;
    }

    /**
     * 平台选择接口
     *
     * @param platformID 对应平台的ID
     */
    @Override
    public void getSelectedPlatform(int platformID) {
        switch (platformID) {
            case PLATFORM_ID_SINA:
                shareSina();
                break;

            case PLATFORM_ID_QQ:
                shareQQ();
                break;

            case PLATFORM_ID_QZONE:
                shareQZone();
                break;

            case PLATFORM_ID_WENCAHT:
                shareWeChat();
                break;

            case PLATFORM_ID_CIRCLEFRIENDS:
                shareCircleFriends();
                break;
        }
    }
}