package eli.blueeye.v1.data;

/**
 * 配置信息，首次标记、分辨率
 *
 * @author eli chang
 */
public class HistoryControlData {

    //灯光开关
    private boolean isLightOpen = false;
    //分辨率
    private int resolutionRatio = 0x02;

    /**
     * 获取灯光开关状态
     * @return
     */
    public boolean isLightOpen() {
        return isLightOpen;
    }

    /**
     * 设置开关状态
     * @param isLightOpen
     */
    public void setLightSwitch(boolean isLightOpen) {
        this.isLightOpen = isLightOpen;
    }

    /**
     * 获取分辨率
     * @return
     */
    public int getResolutionRatio() {
        return resolutionRatio;
    }

    /**
     * 设置分辨率
     * @param resolutionRatio
     */
    public void setResolutionRatio(int resolutionRatio) {
        this.resolutionRatio = resolutionRatio;
    }
}
