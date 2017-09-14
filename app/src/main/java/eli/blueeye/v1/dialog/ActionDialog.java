package eli.blueeye.v1.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.File;

import eli.blueeye.v1.R;
import eli.blueeye.v1.data.FileType;
import eli.blueeye.v1.entity.LoadListView;
import eli.blueeye.v1.entity.ShareEntity;
import eli.blueeye.v1.util.Util;

/**
 * 选择操作的Dialog
 *
 * @author eli chang
 */
public class ActionDialog extends BaseDialog implements View.OnClickListener {

    private Context context;
    private Activity activity;
    //分享按钮
    private Button eShareButton;
    //删除按钮
    private Button eDeleteButton;
    //取消按钮
    private Button eCancelButton;
    //当前文件
    private File[] files;
    //分享实体
    private ShareEntity eShareEntity;
    //刷新的Handler
    private LoadListView.RefreshHandler eRefreshHandler;

    public ActionDialog(Context context, Activity activity, File[] files, LoadListView.RefreshHandler eRefreshHandler) {
        super(context, activity, null, null, R.style.style_dialog_action);
        this.context = context;
        this.activity = activity;
        this.files = files;
        this.eRefreshHandler = eRefreshHandler;
        initView();
    }

    /**
     * 初始化视图组件
     */
    @Override
    public void initView() {
        setContentView(R.layout.dialog_more_action);
        setWindowAnimation();

        eShareButton = (Button) findViewById(R.id.dialog_action_button_share);
        eShareButton.setOnClickListener(this);
        eDeleteButton = (Button) findViewById(R.id.dialog_action_button_delete);
        eDeleteButton.setOnClickListener(this);
        eCancelButton = (Button) findViewById(R.id.dialog_action_button_cancel);
        eCancelButton.setOnClickListener(this);
    }

    /**
     * 重写show方法，定义窗口样式和动画
     */
    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        window.setAttributes(layoutParams);
    }

    /**
     * 设置窗口动画
     */
    @Override
    public void setWindowAnimation() {
        getWindow().setWindowAnimations(R.style.animation_dialog_action);
    }

    @Override
    public void onClick(View view) {
        dismiss();
        switch (view.getId()) {
            case R.id.dialog_action_button_share:
                //实例化分享类
                eShareEntity = new ShareEntity(context, activity, files);
                if (Util.checkFileType(files[0]) == FileType.PHOTO) {
                    eShareEntity.setShareType(ShareEntity.SHARE_TYPE_PHOTO);
                } else if (Util.checkFileType(files[0]) == FileType.VIDEO) {
                    eShareEntity.setShareType(ShareEntity.SHARE_TYPE_VIDEO);
                }
                break;

            case R.id.dialog_action_button_delete:
                //显示删除界面
                DeleteDialog deleteDialog = new DeleteDialog(context, activity, eRefreshHandler);
                deleteDialog.show();
                break;

            default:
                //取消
                break;
        }
    }
}