package eli.blueeye.v1.dialog;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import java.io.File;
import eli.blueeye.v1.R;
import eli.blueeye.v1.entity.LoadListView;

public class CustomActionDialog extends BaseDialog implements View.OnClickListener {

    private Context context;
    private Button eButtonShare;
    private Button eButtonDelete;
    private Button eButtonCancel;
    private File file;

    private LoadListView.RefreshHandler eRefreshHandler;

    public CustomActionDialog(Context context, File file, LoadListView.RefreshHandler eRefreshHandler) {
        super(context, null, null, R.style.style_dialog_action);
        this.context = context;
        this.file = file;
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

        eButtonShare = (Button) findViewById(R.id.dialog_action_button_share);
        eButtonShare.setOnClickListener(this);
        eButtonDelete = (Button) findViewById(R.id.dialog_action_button_delete);
        eButtonDelete.setOnClickListener(this);
        eButtonCancel = (Button) findViewById(R.id.dialog_action_button_cancel);
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
        getWindow().setWindowAnimations(R.style.animation_dialog_action);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.dialog_action_button_share:
                Toast.makeText(context, "Sharing", Toast.LENGTH_SHORT).show();
                break;

            case R.id.dialog_action_button_delete:
                dismiss();
                CustomDeleteDialog deleteDialog = new CustomDeleteDialog(context, eRefreshHandler);
                deleteDialog.show();
                break;

            case R.id.dialog_action_button_cancel:
                dismiss();
                break;
        }
    }
}
