package eli.blueeye.view;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import eli.blueeye.v1.R;

public class TakePhotoView extends View {

    private static final String TAG = "TakePhotoView";

    //触摸标记
    private boolean isTouch = false;
    //延时间隔
    private int longTouchTime;
    //长按的回调接口
    private LongTouchListener longTouchListener;

    //定义画笔
    private Paint paint;
    //背景色
    private int backColor;
    //透明度
    private int alpha = 200;

    //插值器
    private static final TimeInterpolator interpolator = new DecelerateInterpolator();
    //透明度
    private static final float scale = 0.9f;
    //延时
    private static final int duration = 150;
    //点击
    private boolean clickAble = false;

    public TakePhotoView(Context context) {
        this(context, null);
    }

    public TakePhotoView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TakePhotoView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);

        TypedArray ta = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.TakePhotoView, defStyleAttr, 0);
        backColor = ta.getColor(0, Color.WHITE);
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int width = Math.min(getMeasuredHeight(), getMeasuredWidth());
        int paintWidth = width / 25;
        paint.setColor(backColor);
        paint.setAlpha(alpha);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(paintWidth);
        //绘制外环
        canvas.drawCircle(width / 2, width / 2, width / 2 - paintWidth * 2, paint);

        paint.setStyle(Paint.Style.FILL);
        //绘制内圆
        canvas.drawCircle(width / 2, width / 2, width / 2 - paintWidth * 3, paint);
    }

    /**
     * 设置为可见状态
     */
    public void setVisible() {
        this.alpha = 255;
        this.setClickable(true);
        this.clickAble = true;
        postInvalidate();
    }

    /**
     * 设置为不可见状态
     */
    public void setInVisible() {
        this.alpha = 0;
        this.setClickable(false);
        this.clickAble = false;
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //当组件被设置为不可点击，则退出
        if (!clickAble)
            return true;

        //按下按钮
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            /**
             * 背景颜色设为红色
             * 不透明度设为255
             */
            backColor = Color.RED;
            alpha = 255;
            this.animate().scaleX(scale).scaleY(scale).setDuration(duration).setInterpolator(interpolator);
            postInvalidate();

            isTouch = true;
            new LongTouchTask().execute();
        }
        //抬起按钮
        else if (event.getAction() == MotionEvent.ACTION_UP) {

            /**
             * 背景颜色设为红色
             * 不透明度设为150
             */
            backColor = Color.WHITE;
            alpha = 150;
            this.animate().scaleX(1).scaleY(1).setDuration(duration).setInterpolator(interpolator);
            postInvalidate();

            isTouch = false;
        }
        return super.onTouchEvent(event);
    }

    /**
     *
     * @param listener  监听器
     * @param time      回调的时间间隔
     */
    public void setOnLongTouchListener(LongTouchListener listener, int time) {
        this.longTouchListener = listener;
        this.longTouchTime = time;
    }

    /**
     * 长按的异步处理器
     */
    class LongTouchTask extends AsyncTask<Void, Integer, Void> {

        /**
         * 异步任务
         *
         *      1.当处于触摸状态下，会每隔1000毫秒的时间回调onProgressUpdate
         *      onProgressUpdate中调用onLongTouch方法，触发长按事件
         *
         *      2.首次触发事件时，会延时一个自定义的时间间隔，
         *      用来区分长按和点击
         *
         * @param params
         * @return
         */
        @Override
        protected Void doInBackground(Void... params) {
            boolean isFirst = true;

            while (isTouch) {
                if (isFirst) {
                    sleep(longTouchTime);
                    isFirst = false;
                }
                else
                    sleep(1000);
                if (isTouch)
                    publishProgress(0);
            }
            return null;
        }

        /**
         * 触摸事件结束后，调用onTouchStop()方法，触发长按结束事件
         * @param aVoid
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            if (longTouchListener != null) {
                longTouchListener.onTouchStop();
            }
        }

        /**
         * 调用onLongTouch(),触发长按事件
         * @param values
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            if (longTouchListener != null)
                longTouchListener.onLongTouch();
        }

        /**
         * 延时
         * @param time 需要延时的时间
         */
        private void sleep(int time) {
            try {
                Thread.sleep(time);
            }catch(InterruptedException e) {

            }
        }
    }

    /**
     * 长按监听接口
     */
    public interface LongTouchListener {

        /**
         * 处理长按的方法
         */
        void onLongTouch();

        void onTouchStop();

    }
}
