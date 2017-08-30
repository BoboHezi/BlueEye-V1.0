package eli.blueeye.v1.server;

import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.widget.ImageButton;

public class GravitySensorListener implements SensorEventListener {

    private static final String TAG = "GravitySensorListener";
    private long eLastTime;

    //触发点击事件的组件
    private ImageButton eFullScreen;
    //当前是否横屏
    private boolean isLand;
    private Context context;
    private SensorManager eSensorManager;
    private Sensor eGravitySensor;
    private KeyguardManager eKeyguardManager;

    private boolean disablePORI = false;
    private boolean disableLAND = false;

    private boolean sensorLAND = false;

    public GravitySensorListener(Context context, ImageButton eFullScreen, KeyguardManager eKeyguardManager) {
        this.context = context;
        this.eFullScreen = eFullScreen;
        this.eKeyguardManager = eKeyguardManager;
        this.initSensor();
        eLastTime = SystemClock.elapsedRealtime();
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
            eSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            eGravitySensor = eSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            eSensorManager.registerListener(this, eGravitySensor, SensorManager.SENSOR_DELAY_UI);
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
        if ( (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) && !eKeyguardManager.inKeyguardRestrictedInputMode()) {

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

                if ((SystemClock.elapsedRealtime() - eLastTime) > 1000) {
                    eLastTime = SystemClock.elapsedRealtime();
                    eFullScreen.performClick();
                }
            }
            //横屏转竖屏
            if ( (isLand && y > 7) ) {
                if (disablePORI) {
                    return;
                }

                if ((SystemClock.elapsedRealtime() - eLastTime) > 1000) {
                    eLastTime = SystemClock.elapsedRealtime();
                    eFullScreen.performClick();
                }
            }
        }
    }
}