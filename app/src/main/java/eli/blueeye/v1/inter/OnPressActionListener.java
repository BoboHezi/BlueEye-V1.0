package eli.blueeye.v1.inter;

/**
 * 按压动作接口，区分单击和长按
 *
 * @author eli chang
 */
public interface OnPressActionListener {
    void singleTap();

    void longPress();
}