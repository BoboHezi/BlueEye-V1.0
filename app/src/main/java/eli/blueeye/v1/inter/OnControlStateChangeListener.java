package eli.blueeye.v1.inter;

import eli.blueeye.v1.data.Velocity;

/**
 * 控制数据改变的接口
 */
public interface OnControlStateChangeListener {

    void onItemSelectedChanged(int index);

    void onVelocityStateChanged(Velocity velocity);

    void onSwitchStateChanged(boolean isOpen);

    void onLineControlChanged(int lineState);
}
