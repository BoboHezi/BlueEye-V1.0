package eli.blueeye.v1.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import eli.blueeye.v1.R;

/**
 * 缓冲视图
 *
 * @author eli chang
 */
public class VideoLoadingView extends View {

    private static final String TAG = "VideoLoadingView";
    private Context context;
    private Paint paint;

    //圆颜色
    private int eCircleColor;
    //圆半径
    private float eCircleRadius;

    //组件宽度
    private float eViewWidth;
    //组件高度
    private float windowHeight;
    //显示区域左边界
    private float eLeftBorder;

    //第一个圆圆心X位置
    private float eCircle1RadiusX;
    //第二个圆圆心X位置
    private float eCircle2RadiusX;
    //第三个圆圆心X位置
    private float eCircle3RadiusX;
    //圆心Y位置
    private float eCircleRadiusY;
    //计算位置的线程
    private CalculatePositionThread eCalculatePositionThread;

    public VideoLoadingView(Context context) {
        this(context, null);
    }

    public VideoLoadingView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public VideoLoadingView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.context = context;
        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.styleable_progress_loading);
        eCircleColor = ta.getColor(R.styleable.styleable_progress_loading_loading_circleColor, 0xaa40a8cc);
        eCircleRadius = ta.getFloat(R.styleable.styleable_progress_loading_loading_circleRadius, 20);

        paint = new Paint();
        paint.setAntiAlias(true);
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
            widthSpecSize = (int) eCircleRadius * 12;
        }
        if (heightSpecMode == MeasureSpec.AT_MOST) {
            heightSpecSize = (int) eCircleRadius * 2;
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);

        //获取绘制区域的宽高
        eViewWidth = getMeasuredWidth();
        windowHeight = getMeasuredHeight();
        //将圆的Y轴固定在组建的中间
        eCircleRadiusY = windowHeight / 2;
        //设置显示区域的左右边界
        eLeftBorder = (eViewWidth - eCircleRadius * 12) / 2;
        eCircle1RadiusX = -eCircleRadius;
        eCircle2RadiusX = -eCircleRadius;
        eCircle3RadiusX = -eCircleRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        paint.setColor(eCircleColor);
        paint.setStyle(Paint.Style.FILL);

        //Draw Circle2
        paint.setAlpha(255);
        canvas.drawCircle(eCircle2RadiusX, eCircleRadiusY, eCircleRadius, paint);

        //Draw Circle3
        int alpha = (int) (((eCircle1RadiusX - (eLeftBorder + eCircleRadius)) / (eCircleRadius * 4)) * 255);
        paint.setAlpha(255 - alpha);
        canvas.drawCircle(eCircle3RadiusX, eCircleRadiusY, eCircleRadius, paint);

        //Draw Circle1
        alpha = (alpha + 20 >= 255) ? 255 : alpha;
        paint.setAlpha(alpha);
        canvas.drawCircle(eCircle1RadiusX, eCircleRadiusY, eCircleRadius, paint);
    }

    /**
     * 开始Loading动画
     */
    public void startLoading() {
        eCalculatePositionThread = new CalculatePositionThread();
        eCalculatePositionThread.start();
    }

    /**
     * 取消Loading动画
     */
    public void cancelLoading() {
        if (eCalculatePositionThread != null) {
            eCalculatePositionThread.interrupt();
            eCalculatePositionThread = null;
        }
        eCircle1RadiusX = -eCircleRadius;
        eCircle2RadiusX = -eCircleRadius;
        eCircle3RadiusX = -eCircleRadius;
        postInvalidate();
    }

    /**
     * 计算位置的线程
     */
    private class CalculatePositionThread extends Thread {
        @Override
        public void run() {
            //定义三个小球的起始位置
            eCircle1RadiusX = eLeftBorder + eCircleRadius;
            eCircle2RadiusX = eLeftBorder + eCircleRadius * 5;
            eCircle3RadiusX = eLeftBorder + eCircleRadius * 7;

            //定义三个小球每次的位移值
            final int offset1 = 2;
            final int offset2 = 1;

            //定义一次周期的时间
            final int CYCLE = 1000;
            //计算对应的延时时间
            int sleepTime = (int) (CYCLE / (eCircleRadius * 4 / offset1));

            while (!this.isInterrupted()) {
                //更新视图
                postInvalidate();
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    break;
                }
                //位移
                eCircle1RadiusX += offset1;
                eCircle2RadiusX += offset2;
                eCircle3RadiusX += offset1;

                //当小球到达指定位置时，使其复位，重新开始移动
                if (eCircle1RadiusX >= eLeftBorder + eCircleRadius * 5) {
                    eCircle1RadiusX = eLeftBorder + eCircleRadius;
                    eCircle2RadiusX = eLeftBorder + eCircleRadius * 5;
                    eCircle3RadiusX = eLeftBorder + eCircleRadius * 7;
                }
            }
        }
    }
}