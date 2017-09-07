package eli.blueeye.v1.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import eli.blueeye.v1.R;
import eli.blueeye.v1.data.Direction;
import eli.blueeye.v1.data.Velocity;
import eli.blueeye.v1.dialog.ControlDialog;
import eli.blueeye.v1.inter.OnControlStateChangeListener;

public class MoveControlView extends View {

    private static final String TAG = "MoveControlView";
    private Context context;
    //圆点颜色
    private int pointColor;
    //字体颜色
    private int textColor;
    //画笔宽度
    private float strokeWidth;
    //圆点半径
    private float pointRadius;
    //偏移距离
    private static final float OFFSET_PIX = 5;

    //画笔
    private Paint paint;
    //绘制区域宽度
    private float width;
    //绘制区域高度
    private float height;
    //默认圆心坐标X值
    private float defaultRadiusX;
    //默认圆心坐标Y值
    private float defaultRadiusY;
    //圆心坐标X值
    private float radiusX;
    //圆心坐标Y值
    private float radiusY;
    //圆点和中心点的距离
    private float offset;
    //圆点角度
    private float angle;
    //方向
    private Velocity velocity;
    //速度值改变的接口
    private OnControlStateChangeListener changeListener;
    //退回中心点的线程
    private BackToPoint backThread;

    public MoveControlView(Context context) {
        this(context, null);
    }

    public MoveControlView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MoveControlView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.context = context;
        paint = new Paint();
        paint.setAntiAlias(true);
        velocity = new Velocity(0, Direction.front);

        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.styleable_move_control);
        pointColor = ta.getColor(R.styleable.styleable_move_control_control_pointColor, 0xff000000);
        textColor = ta.getColor(R.styleable.styleable_move_control_control_textColor, 0xff000000);
        strokeWidth = ta.getFloat(R.styleable.styleable_move_control_control_strokeWidth, 3);
        pointRadius = ta.getFloat(R.styleable.styleable_move_control_control_pointRadius, 30);
        ta.recycle();

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    /**
     * 设置速度变化监听
     *
     * @param changedListener
     */
    public void setOnControlStateChangedListener(ControlDialog changedListener) {
        this.changeListener = changedListener;
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
            widthSpecSize = (int) (pointRadius * 22);
        }
        if (heightSpecMode == MeasureSpec.AT_MOST) {
            heightSpecSize = widthSpecSize / 2;
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);

        //获取当前组件宽高
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        //重置宽高比为2:1
        if (width > height * 2) {
            width = height * 2;
        } else if (width < height * 2) {
            height = width / 2;
        }
        //计算中心点的坐标
        defaultRadiusX = width / 2;
        defaultRadiusY = height;
        //设置起始圆点的位置
        radiusX = defaultRadiusX;
        radiusY = defaultRadiusY;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //计算方向和速度
        calculate();

        //绘制文字
        String speedText = velocity.getSpeed() + ":速度";
        String directionText = "方向:" + velocity.getDirection();
        paint.setColor(textColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(30);
        //书写速度
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(directionText, 0, 30, paint);
        //书写方向
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(speedText, width, 30, paint);

        //绘制圆点
        paint.setColor(pointColor);
        canvas.drawCircle(radiusX, radiusY, pointRadius, paint);

        //绘制边线
        paint.setColor(0x5553AEBA);
        canvas.drawCircle(width / 2, height, height - strokeWidth, paint);
        paint.setColor(0x77085660);
        canvas.drawCircle(width / 2, height, height - pointRadius * 2 - strokeWidth, paint);

        //绘制圆点
        paint.setColor(pointColor);
        canvas.drawCircle(radiusX, radiusY, pointRadius, paint);

        //绘制箭头
        Bitmap arrowHead = BitmapFactory.decodeResource(context.getResources(), R.drawable.arrowhead);
        Rect rectF = new Rect(0, 0, 80, 40);
        canvas.translate(height - 40, 20);
        canvas.drawBitmap(arrowHead, null, rectF, paint);
        //复位
        canvas.translate(40 - height, -20);
        //绘制箭头
        canvas.translate(pointRadius * 4 / 3, (float) (height * 0.65));
        canvas.rotate(-60);
        canvas.drawBitmap(arrowHead, null, rectF, paint);
        //复位
        canvas.rotate(60);
        canvas.translate(-pointRadius * 4 / 3, (float) -(height * 0.65));
        //绘制箭头
        canvas.translate(height * 2 - pointRadius - 50, (float) (height * 0.44));
        canvas.rotate(61);
        canvas.drawBitmap(arrowHead, null, rectF, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //获得触点的坐标
        float touchX = event.getX();
        float touchY = event.getY();

        //及计算触点和中心点的距离
        float distance = (float) Math.sqrt((touchX - defaultRadiusX) * (touchX - defaultRadiusX) + (touchY - defaultRadiusY) * (touchY - defaultRadiusY));

        //当距离大于大圆的半径时，重新计算坐标
        if ((distance + pointRadius * 3) > height) {

            //X，Y偏移
            float offsetX = height - touchX;
            float offsetY = height - touchY;

            //角度的正余弦
            float cos = offsetX / distance;
            float sin = offsetY / distance;

            //触点与圆心的连线，和圆弧的交点的位置
            float pointX = height - (height - (pointRadius * 3 + strokeWidth)) * cos;
            float pointY = height - (height - (pointRadius * 3 + strokeWidth)) * sin;

            //定义圆点的位置
            radiusX = pointX;
            radiusY = pointY;
        } else {
            //定义圆点的位置
            radiusX = touchX;
            radiusY = touchY;
        }

        //当垂直方向的距离过大时，将Y轴设为高度，防止溢出
        if (radiusY > height) {
            radiusY = height;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //当手指点击时
                if (backThread != null) {
                    //取消正在运行的返回中心点的任务
                    backThread.interrupt();
                    backThread = null;
                }
                postInvalidate();
                break;

            case MotionEvent.ACTION_UP:
                //当手指抬起后，让小球回到中心点
                backThread = new BackToPoint();
                backThread.start();
                break;

            case MotionEvent.ACTION_MOVE:
                //更新视图
                postInvalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 获取速度信息
     * @return
     */
    public Velocity getVelocity() {
        return this.velocity;
    }

    /**
     * 计算位移和角度
     */
    private void calculate() {
        //及计算触点和中心点的距离
        offset = (float) Math.sqrt((radiusX - defaultRadiusX) * (radiusX - defaultRadiusX) + (radiusY - defaultRadiusY) * (radiusY - defaultRadiusY));
        //计算正弦值
        float cos = (height - radiusX) / offset;
        //计算角度
        if (offset == 0) {
            angle = 0;
        } else {
            angle = (float) ((Math.PI / 2 - Math.asin(cos)) / Math.PI * 180);
        }
        //计算对应的速度
        int speed = (int) (offset / (height - pointRadius * 3) * 4);
        Direction direction = Direction.front;
        //计算方向
        if (angle > 0 && angle < 60) {
            direction = Direction.left;
        } else if (angle >= 60 && angle < 120) {
            direction = Direction.front;
        } else if (angle >= 120 && angle <= 180) {
            direction = Direction.right;
        }

        //当速度或者方向发生变化时，调用接口
        if ((Math.abs(velocity.getSpeed() - speed)) >= 1 || velocity.getDirection() != direction) {
            velocity.setDirection(direction);
            velocity.setSpeed(speed);

            if (backThread == null && changeListener != null)
                changeListener.onVelocityStateChanged(velocity);
        }
    }

    /**
     * 返回中心点的线程
     */
    private class BackToPoint extends Thread {
        @Override
        public void run() {
            while (true) {
                //当圆点和中心点距离小于10.或者线程被中断时，退出循环
                if (offset <= OFFSET_PIX * 2 || this.isInterrupted()) {
                    //退出循环之前调用状态改变的接口
                    if (changeListener != null) {
                        changeListener.onVelocityStateChanged(new Velocity(0, Direction.front));
                    }
                    break;
                }
                //计算角度对应的弧度
                float radius = (float) Math.toRadians(angle);
                //计算正余弦值
                float cos = (float) Math.cos(radius);
                float sin = (float) Math.sin(radius);
                //计算对应角度下的X，Y轴偏移
                float offsetX = OFFSET_PIX * cos;
                float offsetY = OFFSET_PIX * sin;
                //设置对应的偏移
                radiusX += offsetX;
                radiusY += offsetY;

                //当偏移的X轴或者Y轴接近中心点时，将位置设置到中心点
                if (Math.abs(radiusX - defaultRadiusX) < OFFSET_PIX * 2)
                    radiusX = defaultRadiusX;
                if (Math.abs(radiusY - defaultRadiusY) < OFFSET_PIX * 2)
                    radiusY = defaultRadiusY;

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    break;
                }
                postInvalidate();
            }
        }
    }
}