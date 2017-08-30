package eli.blueeye.v1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import eli.blueeye.v1.entity.LoadListView;

public abstract class BaseDialog extends Dialog {

    public final String TAG = this.getClass().getName();

    private Context context;
    protected TextView eTimeTextView;
    protected File file;
    private LoadListView.RefreshHandler eRefreshHandler;

    public BaseDialog(Context context, File file, LoadListView.RefreshHandler eRefreshHandler, int style) {
        super(context, style);
        this.context = context;
        this.file = file;
        this.eRefreshHandler = eRefreshHandler;
    }

    /**
     * 初始化视图，必须在子类中重写
     */
    protected abstract void initView();

    protected abstract void setWindowAnimation();

    /**
     * 弹出对话框
     */
    protected void showActionDialog() {
        CustomActionDialog actionDialog = new CustomActionDialog(context,file, eRefreshHandler);
        actionDialog.show();
    }

    /**
     * 设置窗口和风格
     */
    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.CENTER;
        window.setAttributes(layoutParams);
    }

    /**
     * 设置时间
     */
    protected void showTime() {
        if (eTimeTextView != null) {
            Date date = new Date(file.lastModified());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String timeText = format.format(date);
            eTimeTextView.setText(timeText);
        }
    }
}
