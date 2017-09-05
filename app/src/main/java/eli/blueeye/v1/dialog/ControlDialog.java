package eli.blueeye.v1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import eli.blueeye.v1.R;
import eli.blueeye.v1.data.Velocity;
import eli.blueeye.v1.inter.OnControlStateChangeListener;
import eli.blueeye.v1.util.Util;
import eli.blueeye.v1.view.ItemSelectView;
import eli.blueeye.v1.view.LightSwitchView;
import eli.blueeye.v1.view.MoveControlView;

public class ControlDialog extends Dialog implements OnControlStateChangeListener {

    private static final String TAG = "ControlDialog";
    private Context context;

    //分辨率选择视图
    private ItemSelectView itemSelectView;
    //LED开关视图
    private LightSwitchView lightSwitchView;
    //移动控制视图
    private MoveControlView moveControlView;
    //分辨率的可选条目
    List<String> items;

    public ControlDialog(Context context) {
        super(context, R.style.style_dialog_control);
        this.context = context;
        initView();
    }

    /**
     * 初始化视图
     */
    private void initView() {
        setContentView(R.layout.dialog_control);
        getWindow().setWindowAnimations(R.style.animation_control_dialog);
        setCanceledOnTouchOutside(true);

        //初始化移动控制视图
        moveControlView = (MoveControlView) findViewById(R.id.control_view);
        moveControlView.setOnControlStateChangedListener(this);
        //初始化分辨率选择视图
        itemSelectView = (ItemSelectView) findViewById(R.id.control_resolution);
        itemSelectView.setOnControlStateChangeListener(this);
        //设置可选的分辨率
        items = new ArrayList<>();
        items.add("0");
        items.add("480");
        items.add("720");
        items.add("1080");
        itemSelectView.setSelectItems(items);
        //LED开关控制视图
        lightSwitchView = (LightSwitchView) findViewById(R.id.control_switch);
        lightSwitchView.setOnControlStateChangedListener(this);
        lightSwitchView.setSwitch(true);
    }

    @Override
    public void show() {
        super.show();
        Window window = getWindow();
        //设置全屏
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowManager.LayoutParams lp = window.getAttributes();

        lp.width = Util.dip2px(context, 300);
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        lp.gravity = Gravity.LEFT;
        window.setAttributes(lp);
    }

    @Override
    public void onItemSelectedChanged(int index) {

    }

    @Override
    public void onSwitchStateChanged(boolean isOpen) {

    }

    @Override
    public void onVelocityStateChanged(Velocity velocity) {

    }
}