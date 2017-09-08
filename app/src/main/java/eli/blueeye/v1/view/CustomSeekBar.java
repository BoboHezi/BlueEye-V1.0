package eli.blueeye.v1.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import eli.blueeye.v1.R;
import eli.blueeye.v1.util.Util;

/**
 * 自定义进度条
 *
 * @author eli chang
 */
public class CustomSeekBar extends View {

    private static final String TAG = "CustomSeekBar";

    private int eBackColor;
    private int eProgressColor;
    private float eBackHeight;
    private float eProgressHeight;
    private int eTextColor;
    private float eTextHeight;
    private int eTotalTime;

    private boolean isTouching = false;
    private boolean isSeek = false;
    private int eSeekTime;
    private float eTouchX;

    private Paint paint;
    private int eCurrentTime;

    public CustomSeekBar(Context context) {
        this(context, null);
    }

    public CustomSeekBar(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CustomSeekBar(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);

        paint = new Paint();
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.styleable_video_seek_bar);
        eBackColor = typedArray.getColor(R.styleable.styleable_video_seek_bar_seekBar_backColor, Color.GRAY);
        eBackHeight = typedArray.getDimension(R.styleable.styleable_video_seek_bar_seekBar_backHeight, 5);

        eProgressColor = typedArray.getColor(R.styleable.styleable_video_seek_bar_seekBar_progressColor, Color.RED);
        eProgressHeight = typedArray.getDimension(R.styleable.styleable_video_seek_bar_seekBar_progressHeight, 5);

        eTextColor = typedArray.getColor(R.styleable.styleable_video_seek_bar_seekBar_textColor, Color.WHITE);
        eTextHeight = typedArray.getDimension(R.styleable.styleable_video_seek_bar_seekBar_textHeight, 30);

        eTotalTime = typedArray.getInt(R.styleable.styleable_video_seek_bar_seekBar_totalTime, 100);
        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //计算控件实际宽度
        int realWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        //计算分割点位置
        float end_X = (getCurrentTime() * 1.0f / eTotalTime) * realWidth;
        //设置时间数字
        String text = Util.formatTime(eCurrentTime / 1000) + "/" + Util.formatTime(eTotalTime / 1000);

        if (isTouching) {
            //处于触摸状态下，通过触摸点来设置分割点和数字
            end_X = eTouchX;
            eSeekTime = (int) (end_X / realWidth * eTotalTime);
            text = Util.formatTime(eSeekTime / 1000) + "/" + Util.formatTime(eTotalTime / 1000);
        }

        //文字
        paint.setColor(eTextColor);
        paint.setTextSize(eTextHeight);
        int textWidth = (int) paint.measureText(text);
        int y = (int) (-(paint.descent() + paint.ascent()) / 2) + 30;
        canvas.drawText(text, realWidth - textWidth, y, paint);

        //绘制将来的时间线
        if (eCurrentTime < eTotalTime) {
            paint.setColor(eBackColor);
            paint.setStrokeWidth(eBackHeight);
            paint.setAntiAlias(true);
            canvas.drawLine(end_X, 10, realWidth, 10, paint);
        }

        //绘制过去的时间线
        if (eCurrentTime > 0) {
            paint.setAntiAlias(true);
            paint.setColor(eProgressColor);
            paint.setStrokeWidth(eProgressHeight);
            canvas.drawLine(0, 10, end_X, 10, paint);
            canvas.drawCircle(end_X, 10, 8, paint);
        }
    }

    public synchronized void setTime(int time) {
        this.eTotalTime = time;
    }

    public synchronized int getCurrentTime() {
        return eCurrentTime;
    }

    public synchronized int getSeekTime() {
        return this.eSeekTime;
    }

    public synchronized boolean isSeek() {
        return this.isSeek;
    }

    public synchronized void cancelSeek() {
        this.isSeek = false;
    }

    public synchronized void setCurrentTime(int time) {
        if (time < 0) {
            throw new IllegalStateException("time will not less than 0.");
        }

        if (time > eTotalTime) {
            time = eTotalTime;
        }

        if (time <= eTotalTime) {
            this.eCurrentTime = time;
            postInvalidate();
        }
    }

    /**
     * 处理触摸事件
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            isTouching = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            isTouching = false;
            isSeek = true;
        }

        //获得屏幕的横向定位
        if (isTouching) {
            eTouchX = event.getX();
        }
        return super.onTouchEvent(event);
    }
}