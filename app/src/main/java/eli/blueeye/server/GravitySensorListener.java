package eli.blueeye.server;

import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageButton;

public class GravitySensorListener implements SensorEventListener {

    private static final String TAG = "GravitySensorListener";
    private long lastTime ;

    //触发点击事件的组件
    private ImageButton fullScreen;
    //当前是否横屏
    private boolean isLand;
    private Context context;
    private SensorManager sensorManager;
    private Sensor gravitySensor;
    private KeyguardManager keyguardManager;

    private boolean disablePORI = false;
    private boolean disableLAND = false;

    private boolean sensorLAND = false;

    public GravitySensorListener(Context context, ImageButton fullScreen, KeyguardManager keyguardManager) {
        this.context = context;
        this.fullScreen = fullScreen;
        this.keyguardManager = keyguardManager;
        this.initSensor();
        lastTime = SystemClock.elapsedRealtime();
    }

    /**
     * 屏蔽横屏指令
     */
    public void disableLAND() {
        disableLAND = true;
    }

    /**
     * 屏蔽竖屏指令
     */
    public void disablePORI() {
        disablePORI = true;
    }

    /**
     * 传感器方向
     * @return
     */
    public boolean isSensorLAND() {
        return sensorLAND;
    }

    /**
     * 初始化重力传感器
     */
    private void initSensor() {
        if(context != null ) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    /**
     * 设置当前的屏幕方向
     * @param isLand 是否横屏
     */
    public void setOrientation(boolean isLand) {
        this.isLand = isLand;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * 传感器状态改变
     *  当处于竖屏，x方向的重力加速度绝对值大于7
     *  或者处于横屏，y方向的重力加速度绝对值大于7
     *  触发横竖屏切换操作
     * @param sensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //需要在未锁屏的状态下激活
        if ( (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) && !keyguardManager.inKeyguardRestrictedInputMode()) {

            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];

            if (Math.abs(x) > 7) {
                sensorLAND = true;
                disablePORI = false;
            }
            if (Math.abs(y) > 7) {
                sensorLAND = false;
                disableLAND = false;
            }

            //竖屏转横屏
            if ( (!isLand && Math.abs(x) > 7)) {
                if(disableLAND) {
                    return;
                }

                if ((SystemClock.elapsedRealtime() - lastTime) > 1000) {
                    lastTime = SystemClock.elapsedRealtime();
                    fullScreen.performClick();
                }
            }
            //横屏转竖屏
            if ( (isLand && y > 7) ) {
                if (disablePORI) {
                    return;
                }

                if ((SystemClock.elapsedRealtime() - lastTime) > 1000) {
                    lastTime = SystemClock.elapsedRealtime();
                    fullScreen.performClick();
                }
            }
        }
    }
}
