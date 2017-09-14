package eli.blueeye.v1.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import eli.blueeye.v1.dialog.ControlDialog;
import eli.blueeye.v1.inter.OnControlStateChangeListener;

/**
 * 三元选择视图 本项目中用于线控的选择操作
 *
 * @author eli chang
 */
public class TernarySelectView extends View {

    private static final String TAG = "TernarySelectView";
    private static final int LINE_COLOR = 0xFF0C719F;
    private static final int POINT_COLOR = 0xFF023546;
    private static final int CENTER_COLOR = 0xffaaaaaa;

    public static final int STATE_CENTER = 0;
    public static final int STATE_UP = 1;
    public static final int STATE_DOWN = 2;

    private Context context;
    private Paint paint;

    private float eViewWidth;
    private float eViewHeight;
    private float eHorizontalBaseLine;
    private float eLineWidth;
    private float ePointRadius;
    private float pointY;

    private int state = STATE_CENTER;

    private OnControlStateChangeListener changeListener;

    public TernarySelectView(Context context) {
        this(context, null);
    }

    public TernarySelectView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TernarySelectView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.context = context;
        paint = new Paint();
        paint.setAntiAlias(true);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        eViewHeight = getMeasuredHeight();
        eViewWidth = getMeasuredWidth();

        eLineWidth = eViewWidth / 3;
        ePointRadius = eLineWidth;

        eHorizontalBaseLine = eViewWidth / 2;
        pointY = eViewHeight / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //绘制竖线
        paint.setColor(LINE_COLOR);
        canvas.drawRect(eHorizontalBaseLine - eLineWidth / 2, ePointRadius, eHorizontalBaseLine + eLineWidth / 2, eViewHeight - ePointRadius, paint);
        canvas.drawCircle(eHorizontalBaseLine, ePointRadius, eLineWidth / 2, paint);
        canvas.drawCircle(eHorizontalBaseLine, eViewHeight - ePointRadius, eLineWidth / 2, paint);

        //绘制大圆
        paint.setColor(POINT_COLOR);
        canvas.drawCircle(eHorizontalBaseLine, pointY, ePointRadius, paint);

        //绘制小圆
        paint.setColor(CENTER_COLOR);
        canvas.drawCircle(eHorizontalBaseLine, pointY, eLineWidth / 2, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //手指离开屏幕时，重新定位到中间
        if (event.getAction() == MotionEvent.ACTION_UP) {
            pointY = eViewHeight / 2;
        } else {
            //获取Y值
            pointY = event.getY();
            //当手指离开激活区域，控制其偏移位置
            if (pointY < ePointRadius) {
                pointY = ePointRadius;
            } else if (pointY > eViewHeight - ePointRadius) {
                pointY = eViewHeight - ePointRadius;
            }
        }
        postInvalidate();

        //更新线控状态
        int nowState = getState();
        if (nowState != state && changeListener != null) {
            changeListener.onLineControlChanged(nowState);
            state = nowState;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 设置开关变化监听
     *
     * @param changedListener
     */
    public void setOnControlStateChangedListener(ControlDialog changedListener) {
        this.changeListener = changedListener;
    }

    /**
     * 获取当前指针状态
     *
     * @return
     */
    public int getState() {
        if (pointY < eViewHeight / 2 - 10)
            return STATE_UP;
        else if (pointY > eViewHeight / 2 + 10)
            return STATE_DOWN;
        else
            return STATE_CENTER;
    }
}