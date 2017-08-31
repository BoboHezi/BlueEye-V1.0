package eli.blueeye.v1.share;

import android.app.Activity;
import android.content.Context;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;

/**
 * 分享到微信的实体
 *
 * @author eli chang
 * @see BaseShareWeXin
 */
public class ShareWeChat extends BaseShareWeXin {

    public ShareWeChat(Context context, Activity activity) {
        super(context, activity);
        super.setScene(SendMessageToWX.Req.WXSceneSession);
    }
}