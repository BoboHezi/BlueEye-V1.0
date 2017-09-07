package eli.blueeye.v1.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import eli.blueeye.v1.R;
import eli.blueeye.v1.data.Direction;
import eli.blueeye.v1.data.Velocity;
import eli.blueeye.v1.inter.OnControlStateChangeListener;
import eli.blueeye.v1.server.SendCommandThread;
import eli.blueeye.v1.util.Util;
import eli.blueeye.v1.view.ItemSelectView;
import eli.blueeye.v1.view.LightSwitchView;
import eli.blueeye.v1.view.MoveControlView;
import eli.blueeye.v1.view.TernarySelectView;

/**
 * 控制面板
 *
 * @author eli chang
 */
public class ControlDialog extends Dialog implements OnControlStateChangeListener {

    private static final String TAG = "ControlDialog";
    private Context context;

    //分辨率选择视图
    private ItemSelectView itemSelectView;
    //LED开关视图
    private LightSwitchView lightSwitchView;
    //移动控制视图
    private MoveControlView moveControlView;
    //线控视图
    private TernarySelectView ternarySelectView;
    //分辨率的可选条目
    private List<String> items;
    //计算和发送命令的线程
    private SendControlCommand sendControlCommand;

    public ControlDialog(Context context) {
        super(context, R.style.style_dialog_control);
        this.context = context;
        items = new ArrayList<>();
        items.add("0");
        items.add("480");
        items.add("720");
        items.add("1080");
        initView();
        sendControlCommand = new SendControlCommand();
        sendControlCommand.start();
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
        itemSelectView.setSelectItems(items);
        //LED开关控制视图
        lightSwitchView = (LightSwitchView) findViewById(R.id.control_switch);
        lightSwitchView.setOnControlStateChangedListener(this);
        lightSwitchView.setSwitch(true);
        //线控视图
        ternarySelectView = (TernarySelectView) findViewById(R.id.control_line_control);
        ternarySelectView.setOnControlStateChangedListener(this);
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
    public void dismiss() {
        super.dismiss();
        if (sendControlCommand != null) {
            sendControlCommand.interrupt();
        }
    }

    @Override
    public void onItemSelectedChanged(int index) {
        refreshCommand();
    }

    @Override
    public void onSwitchStateChanged(boolean isOpen) {
        refreshCommand();
    }

    @Override
    public void onLineControlChanged(int lineState) {
        refreshCommand();
    }

    @Override
    public void onVelocityStateChanged(Velocity velocity) {
        refreshCommand();
    }

    /**
     * 更新控制命令
     */
    private void refreshCommand() {
        //停止发送线程
        if (sendControlCommand != null) {
            sendControlCommand.interrupt();
        }
        //发送命令
        sendCommand();
        //重新开启线程
        sendControlCommand = new SendControlCommand();
        sendControlCommand.start();
    }

    /**
     * 读取状态数据
     *
     * @return
     */
    private int getCommandData() {
        int command;
        //获取移动信息
        Velocity velocity = null;
        if (moveControlView != null) {
            velocity = moveControlView.getVelocity();
        }

        //获取LED开关状态
        boolean isOpen = lightSwitchView.isOpen();

        //获取分辨率
        String resolutionRatio = items.get(itemSelectView.getIndex());

        //获取线控状态
        int lineState = ternarySelectView.getState();

        command = convertInteger(isOpen, resolutionRatio, lineState, velocity);
        return command;
    }

    /**
     * 计算为Int数据
     *
     * @return
     */
    private int convertInteger(boolean isOpen, String resolutionRatio, int stepMotor, Velocity velocity) {
        int command = 0;
        //灯光
        byte led = (byte) (isOpen ? 1 : 0);
        //分辨率
        byte camera = (byte) items.indexOf(resolutionRatio);
        //移动方向
        byte direction = 0;
        if (velocity.getDirection() == Direction.left) {
            direction = 1;
        } else if (velocity.getDirection() == Direction.right) {
            direction = 2;
        }
        byte speed = (byte) velocity.getSpeed();

        command += speed;
        command += direction << 4;
        command += stepMotor << 8;
        command += camera << 16;
        command += led << 24;

        return command;
    }

    /**
     * 发送命令
     */
    private void sendCommand() {
        //获取命令
        int command = getCommandData();
        //发送命令
        new SendCommandThread(command).start();

        Log.i(TAG, "sendCommand: " + Integer.toBinaryString(command));

        byte led = (byte) (command >>> 24);
        byte camera = (byte) (command >>> 16);
        byte step = (byte) (command >>> 8);
        byte velocity = (byte) command;

        Log.i(TAG, "Led: " + Integer.toBinaryString(led));
        Log.i(TAG, "camera: " + Integer.toBinaryString(camera));
        Log.i(TAG, "step: " + Integer.toBinaryString(step));
        Log.i(TAG, "velocity: " + Integer.toBinaryString(velocity));
    }

    /**
     * 计算和发送控制命令
     */
    private class SendControlCommand extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    Thread.sleep(3000);
                    sendCommand();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}