package eli.blueeye.v1.share;

import android.app.Activity;
import android.content.Context;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;

/**
 * 分享到朋友圈的实体
 *
 * @author eli chang
 * @see BaseShareWeXin
 */
public class ShareCircleFriends extends BaseShareWeXin {

    public ShareCircleFriends(Context context, Activity activity) {
        super(context, activity);
        super.setScene(SendMessageToWX.Req.WXSceneTimeline);
    }
}
