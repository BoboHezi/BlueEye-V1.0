package eli.blueeye.v1.inter;

/**
 * 分享平台选择接口
 *
 * @author eli chang
 */
public interface OnPlatformSelected {
    /**
     * @param platformID 对应平台的ID
     */
    void getSelectedPlatform(int platformID);
}