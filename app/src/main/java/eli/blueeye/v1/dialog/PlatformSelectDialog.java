package eli.blueeye.v1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import eli.blueeye.v1.R;
import eli.blueeye.v1.entity.ShareEntity;
import eli.blueeye.v1.inter.OnPlatformSelected;

/**
 * 选择分享平台的Dialog
 *
 * @author eli chang
 */
public class PlatformSelectDialog extends Dialog implements View.OnClickListener {

    private Context context;

    private ImageButton buttonShareSina;
    private ImageButton buttonShareCircleFriends;
    private ImageButton buttonShareWeChat;
    private ImageButton buttonShareQQ;
    private ImageButton buttonShareQZone;
    private Button buttonCancel;

    private OnPlatformSelected platformSelected;

    public PlatformSelectDialog(Context context) {
        super(context, R.style.dialog_share_platform);
        this.context = context;
        initView();
    }

    private void initView() {
        setContentView(R.layout.dialog_share_platform);
        getWindow().setWindowAnimations(R.style.animation_share_dialog);

        buttonShareSina = (ImageButton) findViewById(R.id.share_sina);
        buttonShareSina.setOnClickListener(this);
        buttonShareCircleFriends = (ImageButton) findViewById(R.id.share_circle_friends);
        buttonShareCircleFriends.setOnClickListener(this);
        buttonShareWeChat = (ImageButton) findViewById(R.id.share_we_chat);
        buttonShareWeChat.setOnClickListener(this);
        buttonShareQQ = (ImageButton) findViewById(R.id.share_qq);
        buttonShareQQ.setOnClickListener(this);
        buttonShareQZone = (ImageButton) findViewById(R.id.share_qq_zone);
        buttonShareQZone.setOnClickListener(this);
        buttonCancel = (Button) findViewById(R.id.share_cancel);
        buttonCancel.setOnClickListener(this);
    }

    /**
     * 实例化接口
     *
     * @param platformSelected
     */
    public void setOnPlatformSelected(OnPlatformSelected platformSelected) {
        this.platformSelected = platformSelected;
    }

    @Override
    public void onClick(View v) {
        int id = ShareEntity.PLATFORM_ID_CANCEL;
        switch (v.getId()) {
            case R.id.share_sina:
                id = ShareEntity.PLATFORM_ID_SINA;
                break;

            case R.id.share_circle_friends:
                id = ShareEntity.PLATFORM_ID_CIRCLEFRIENDS;
                break;

            case R.id.share_we_chat:
                id = ShareEntity.PLATFORM_ID_WENCAHT;
                break;

            case R.id.share_qq:
                id = ShareEntity.PLATFORM_ID_QQ;
                break;

            case R.id.share_qq_zone:
                id = ShareEntity.PLATFORM_ID_QZONE;
                break;

            case R.id.share_cancel:
                break;
        }
        if (platformSelected != null) {
            platformSelected.getSelectedPlatform(id);
        }
        dismiss();
    }

    @Override
    public void show() {
        super.show();

        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
    }
}