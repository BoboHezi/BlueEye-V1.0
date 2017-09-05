package eli.blueeye.v1.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import eli.blueeye.v1.R;

public class VideoLoadingView extends View {

    private static final String TAG = "VideoLoadingView";
    private Context context;
    private Paint paint;

    //圆颜色
    private int circleColor;
    //圆半径
    private float circleRadius;

    //组件宽度
    private float windowWidth;
    //组件高度
    private float windowHeight;
    //显示区域左边界
    private float leftBorder;
    //显示区域右边界
    private float rightBorder;

    //第一个圆圆心X位置
    private float circle1RadiusX;
    //第二个圆圆心X位置
    private float circle2RadiusX;
    //第三个圆圆心X位置
    private float circle3RadiusX;
    //圆心Y位置
    private float circleRadiusY;
    //计算位置的线程
    CalculatePositionThread calculatePositionThread;

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
        circleColor = ta.getColor(R.styleable.styleable_progress_loading_loading_circleColor, 0xaa40a8cc);
        circleRadius = ta.getFloat(R.styleable.styleable_progress_loading_loading_circleRadius, 20);

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
            widthSpecSize = (int) circleRadius * 12;
        }
        if (heightSpecMode == MeasureSpec.AT_MOST) {
            heightSpecSize = (int) circleRadius * 2;
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);

        //获取绘制区域的宽高
        windowWidth = getMeasuredWidth();
        windowHeight = getMeasuredHeight();
        //将圆的Y轴固定在组建的中间
        circleRadiusY = windowHeight / 2;
        //设置显示区域的左右边界
        leftBorder = (windowWidth - circleRadius * 12) / 2;
        rightBorder = (windowWidth + circleRadius * 12) / 2;
        circle1RadiusX = -circleRadius;
        circle2RadiusX = -circleRadius;
        circle3RadiusX = -circleRadius;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        paint.setColor(circleColor);
        paint.setStyle(Paint.Style.FILL);

        //Draw Circle2
        paint.setAlpha(255);
        canvas.drawCircle(circle2RadiusX, circleRadiusY, circleRadius, paint);

        //Draw Circle3
        int alpha = (int) (((circle1RadiusX - (leftBorder + circleRadius)) / (circleRadius * 4)) * 255);
        paint.setAlpha(255 - alpha);
        canvas.drawCircle(circle3RadiusX, circleRadiusY, circleRadius, paint);

        //Draw Circle1
        alpha = (alpha + 20 >= 255) ? 255 : alpha;
        paint.setAlpha(alpha);
        canvas.drawCircle(circle1RadiusX, circleRadiusY, circleRadius, paint);
    }

    /**
     * 开始Loading动画
     */
    public void startLoading() {
        calculatePositionThread = new CalculatePositionThread();
        calculatePositionThread.start();
    }

    /**
     * 取消Loading动画
     */
    public void cancelLoading() {
        if (calculatePositionThread != null) {
            calculatePositionThread.interrupt();
            calculatePositionThread = null;
        }
        circle1RadiusX = -circleRadius;
        circle2RadiusX = -circleRadius;
        circle3RadiusX = -circleRadius;
        postInvalidate();
    }

    /**
     * 计算位置的线程
     */
    private class CalculatePositionThread extends Thread {
        @Override
        public void run() {
            //定义三个小球的起始位置
            circle1RadiusX = leftBorder + circleRadius;
            circle2RadiusX = leftBorder + circleRadius * 5;
            circle3RadiusX = leftBorder + circleRadius * 7;

            //定义三个小球每次的位移值
            final int offset1 = 2;
            final int offset2 = 1;

            //定义一次周期的时间
            final int CYCLE = 1000;
            //计算对应的延时时间
            int sleepTime = (int) (CYCLE / (circleRadius * 4 / offset1));

            while (!this.isInterrupted()) {
                //更新视图
                postInvalidate();
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    break;
                }
                //位移
                circle1RadiusX += offset1;
                circle2RadiusX += offset2;
                circle3RadiusX += offset1;

                //当小球到达指定位置时，使其复位，重新开始移动
                if (circle1RadiusX >= leftBorder + circleRadius * 5) {
                    circle1RadiusX = leftBorder + circleRadius;
                    circle2RadiusX = leftBorder + circleRadius * 5;
                    circle3RadiusX = leftBorder + circleRadius * 7;
                }
            }
        }
    }
}