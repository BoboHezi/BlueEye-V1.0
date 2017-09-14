package eli.blueeye.v1.dialog;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import eli.blueeye.v1.R;
import eli.blueeye.v1.entity.LoadListView;

/**
 * 删除确定的Dialog
 *
 * @author eli chang
 */
public class DeleteDialog extends BaseDialog implements View.OnClickListener {

    private final String TAG = "DeleteDialog";

    private Context context;
    //多图标记
    private boolean isMultiImage = false;
    //取消按钮
    private Button eButtonCancel;
    //删除按钮
    private Button eButtonDelete;
    //更新的Handler
    private LoadListView.RefreshHandler eRefreshHandler;

    public DeleteDialog(Context context, Activity activity, LoadListView.RefreshHandler eRefreshHandler) {
        super(context, activity, null, null, R.style.style_dialog_action);
        this.context = context;
        this.eRefreshHandler = eRefreshHandler;
        initView();
    }

    /**
     * 初始化视图组件
     */
    @Override
    public void initView() {
        setContentView(R.layout.dialog_delete);
        setWindowAnimation();

        eButtonCancel = (Button) findViewById(R.id.dialog_delete_button_cancel);
        eButtonDelete = (Button) findViewById(R.id.dialog_delete_button_confirm);
        eButtonDelete.setOnClickListener(this);
        eButtonCancel.setOnClickListener(this);
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
        getWindow().setWindowAnimations(R.style.animation_dialog_delete);
    }

    /**
     * 标记为删除多张图片
     *
     * @param isMultiImage
     */
    public void setIsMultiImage(boolean isMultiImage) {
        this.isMultiImage = isMultiImage;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.dialog_delete_button_cancel:
                dismiss();
                break;

            case R.id.dialog_delete_button_confirm:
                dismiss();
                if (isMultiImage) {
                    //删除多张
                    eRefreshHandler.sendEmptyMessage(LoadListView.HANDLER_STATE_DELETE_MULTI);
                } else {
                    //删除一张
                    eRefreshHandler.sendEmptyMessage(LoadListView.HANDLER_STATE_DELETE);
                }
                break;
        }
    }
}