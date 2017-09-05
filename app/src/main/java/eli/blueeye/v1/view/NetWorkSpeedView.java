package eli.blueeye.v1.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import eli.blueeye.v1.R;
import eli.blueeye.v1.util.Util;

/**
 * 显示网速的自定义组件
 *
 * @author eli chang
 */
public class NetWorkSpeedView extends View {

    private static final String TAG = "NetWorkSpeedView";
    private Context context;

    //组件高度
    private float viewHeight;
    //画笔颜色
    private int paintColor;
    //画笔
    private Paint paint;
    //网速
    private float speed;
    //更新动画线程
    private TransitionThread transitionThread;

    //圆半径
    private float radius;
    //画笔宽度
    private float lineWidth;
    //圆点半径
    private float pointRadius;

    public NetWorkSpeedView(Context context) {
        this(context, null);
    }

    public NetWorkSpeedView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NetWorkSpeedView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.context = context;

        //获取配置值
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.styleable_view_rate);
        paintColor = typedArray.getColor(R.styleable.styleable_view_rate_rate_paintColor, 0xff1fa58a);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(paintColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取组件窗口模式和大小
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        //当宽高设置为wrap_content，重新定义其大小
        if (widthSpecMode == MeasureSpec.AT_MOST) {
            widthSpecSize = Util.dip2px(context, 130);
        }
        if (heightSpecMode == MeasureSpec.AT_MOST) {
            heightSpecSize = Util.dip2px(context, 30);
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);

        //获取组件高度
        viewHeight = getMeasuredHeight();
        //计算圆半径
        radius = (float) (viewHeight / (1 + Math.sin(45 * Math.PI / 180)));
        //计算画笔宽度
        lineWidth = radius / 15;
        //计算圆点半径
        pointRadius = radius / 10;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //角度
        int angle;

        //绘制圆环
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        paint.setColor(paintColor);
        canvas.drawArc(lineWidth / 2, lineWidth / 2, radius * 2 - lineWidth / 2, radius * 2 - lineWidth / 2, 135, 270, false, paint);

        //绘制圆点
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(radius, radius, pointRadius, paint);

        //绘制三角形
        float scale = (speed / 1000) > 1 ? 1 : (speed / 1000);
        angle = (int) (scale * 270);
        float X1 = (float) (radius * (1 - Math.cos((angle - 45) * Math.PI / 180) * 5 / 6));
        float Y1 = (float) (radius * (1 - Math.sin((angle - 45) * Math.PI / 180) * 5 / 6));
        float X2 = (float) (radius - pointRadius * Math.cos((135 - angle) * Math.PI / 180));
        float Y2 = (float) (radius + pointRadius * Math.sin((135 - angle) * Math.PI / 180));
        float X3 = (float) (radius + pointRadius * Math.cos((135 - angle) * Math.PI / 180));
        float Y3 = (float) (radius - pointRadius * Math.sin((135 - angle) * Math.PI / 180));
        Path path = new Path();
        path.moveTo(X1, Y1);
        path.lineTo(X2, Y2);
        path.lineTo(X3, Y3);
        path.close();
        canvas.drawPath(path, paint);

        //绘制文本
        String text = speed + " kb/s";
        paint.setTextSize(viewHeight / 2);
        float textHeight = -(paint.descent() + paint.ascent());
        canvas.drawText(text, (float) (radius * 2.5), (viewHeight + textHeight) / 2, paint);
    }

    /**
     * 设置当前网速
     *
     * @param speed
     */
    public void setSpeed(float speed) {
        if (transitionThread != null && !transitionThread.isInterrupted()) {
            transitionThread.interrupt();
            transitionThread = null;
        }
        //启动更新线程
        transitionThread = new TransitionThread(speed);
        transitionThread.start();
    }

    private class TransitionThread extends Thread {

        //更新的网速
        float nowSpeed;

        public TransitionThread(float speed) {
            this.nowSpeed = speed;
        }

        @Override
        public void run() {

            //当更新的网速与之前的网速相近时，直接更新数据
            if (Math.abs(nowSpeed - speed) <= 20) {
                speed = nowSpeed;
                postInvalidate();
                return;
            }
            //计算每次应该延时的时间，确保每次更新用事1.7秒
            int delay = (int) (700 / Math.abs(nowSpeed - speed) * 10);
            //判断是增大还是减小
            int offset = (nowSpeed > speed) ? 1 : -1;

            //循环更新视图
            while (nowSpeed != speed && !this.isInterrupted()) {
                if (Math.abs(nowSpeed - speed) <= 10) {
                    speed = nowSpeed;
                }
                //偏移一个数值
                speed += (offset * 10);
                if (speed < 0) {
                    speed = 0;
                }
                //更新视图
                postInvalidate();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}