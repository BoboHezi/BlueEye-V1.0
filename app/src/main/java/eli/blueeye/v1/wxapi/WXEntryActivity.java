package eli.blueeye.v1.wxapi;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import eli.blueeye.v1.inter.Constants;

/**
 * 接收微信的请求及返回值
 *
 * @author eli chang
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXEntryActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IWXAPI api = WXAPIFactory.createWXAPI(this, Constants.APP_ID_WE_CHAT, false);
        api.handleIntent(getIntent(), this);
    }

    @Override
    public void onReq(BaseReq baseReq) {
        Log.i(TAG, "BaseReq.ErrCode: " + baseReq.getType());
    }

    @Override
    public void onResp(BaseResp baseResp) {
        String result;
        Log.i(TAG, "BaseResp.ErrCode: " + baseResp.errCode);
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = "分享成功";
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = null;
                break;
            default:
                result = "分享失败";
                break;
        }
        if (result != null) {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}