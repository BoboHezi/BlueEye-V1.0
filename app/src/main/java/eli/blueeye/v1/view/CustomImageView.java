package eli.blueeye.v1.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.Scroller;

public class CustomImageView extends AppCompatImageView implements ScaleGestureDetector.OnScaleGestureListener, View.OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {

    private final String TAG = this.getClass().getName();

    private ScaleGestureDetector eScaleGestureDetector;
    private GestureDetector eGestureDetector;
    private Matrix eScaleMatrix;
    private VelocityTracker eVelocityTracker;
    private FlingRunnable eFlingRunnable;
    private PressAction ePressAction;

    private boolean isFirst = false;
    private boolean isAutoScale = false;
    private boolean isCanDrag = false;
    private boolean isCheckLeftAndRight;
    private boolean isCheckTopAndBottom;

    private float eInitScale;
    private float eMaxScale;
    private float eMidScale;
    private float eMinScale;
    private float eMaxOverScale;

    private int eLastPointCount;
    private float eLastX;
    private float eLastY;
    private int eTouchSlop;

    public CustomImageView(Context context) {
        this(context, null);
    }

    public CustomImageView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CustomImageView(final Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
        eScaleGestureDetector = new ScaleGestureDetector(context, this);
        eScaleMatrix = new Matrix();
        this.setOnTouchListener(this);
        eTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        eGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isAutoScale)
                    return true;
                float x = e.getX();
                float y = e.getY();

                if (getScale() < eMidScale) {
                    post(new AutoScaleRunnable(eMidScale, x, y));
                } else {
                    post(new AutoScaleRunnable(eInitScale, x, y));
                }
                return true;
            }

            /**
             * 点击事件，点击消失
             * @param e
             * @return
             */
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (ePressAction != null)
                    ePressAction.singleTap();
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (ePressAction != null)
                    ePressAction.longPress();
                super.onLongPress(e);
            }
        });
    }

    /**
     * 设置点击消失的接口
     * @param pressAction
     */
    public void setOnPressAction(PressAction pressAction) {
        this.ePressAction = pressAction;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }

    @Override
    public void onGlobalLayout() {

        if (!isFirst) {
            isFirst = true;
            Drawable drawable = getDrawable();
            if (drawable == null)
                return;

            int width = getWidth();
            int height = getHeight();
            int dw = drawable.getIntrinsicWidth();
            int dh = drawable.getIntrinsicHeight();
            float scale = 1.0f;

            if (dw > width && dh < height) {
                scale = width * 1.0f / dw;
            }
            if (dh > height && dw < width) {
                scale = height * 1.0f / dh;
            }
            if ((dw < width && dh < height) || (dw > width && dh > height)) {
                scale = Math.min(width * 1.0f / dw, height * 1.0f / dh);
            }

            int dx = width / 2 - dw / 2;
            int dy = height / 2 - dh / 2;

            eScaleMatrix.postTranslate(dx, dy);
            eScaleMatrix.postScale(scale, scale, width / 2, height / 2);
            setImageMatrix(eScaleMatrix);

            eInitScale = scale;
            eMaxScale = scale * 4;
            eMidScale = scale * 2;
            eMinScale = eInitScale / 4;
            eMaxOverScale = eMaxScale * 2;
        }
    }

    /**
     * 获取当前缩放比例
     * @return
     */
    private final float getScale() {
        float[] values = new float[9];
        eScaleMatrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    /**
     * 根据当前图片的Matrix获得图片的范围
     * @return
     */
    private RectF getMatrixRectF() {
        Matrix matrix = eScaleMatrix;
        RectF rect = new RectF();
        Drawable drawable = getDrawable();
        if (drawable != null) {
            rect.set(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            matrix.mapRect(rect);
        }
        return rect;
    }

    /**
     * 在缩放时，进行图片显示范围的控制
     */
    private void checkBorderAndCenterWhenScale() {
        RectF rect = getMatrixRectF();
        float deltaX = 0;
        float deltaY = 0;

        int width = getWidth();
        int height = getHeight();

        if (rect.width() >= width) {
            if (rect.left > 0) {
                deltaX = -rect.left;
            }
            if (rect.right < width) {
                deltaX = width - rect.right;
            }
        }
        if (rect.height() >= height) {
            if (rect.top > 0) {
                deltaY = -rect.top;
            }
            if (rect.bottom < height) {
                deltaY = height - rect.bottom;
            }
        }
        if (rect.width() < width) {
            deltaX = width * 0.5f - rect.right + rect.width() * 0.5f;
        }
        if (rect.height() < height) {
            deltaY = height * 0.5f - rect.bottom + rect.height() * 0.5f;
        }
        eScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 平移时，检测图片的左右边界
     */
    private void checkBorderWhenTranslate() {
        RectF rectF = getMatrixRectF();
        float deltaX = 0.0f;
        float deltaY = 0.0f;

        int width = getWidth();
        int height = getHeight();

        if (isCheckLeftAndRight) {
            if (rectF.left > 0) {
                deltaX = -rectF.left;
            }
            if (rectF.right < width) {
                deltaX = width - rectF.right;
            }
        }
        if (isCheckTopAndBottom) {
            if (rectF.top > 0) {
                deltaY = -rectF.top;
            }
            if (rectF.bottom < height) {
                deltaY = height - rectF.bottom;
            }
        }
        eScaleMatrix.postTranslate(deltaX, deltaY);
    }

    /**
     * 缩放
     * @param detector
     * @return
     */
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        float scale = getScale();

        if (getDrawable() == null)
            return true;

        if ((scaleFactor > 1.0f && scaleFactor * scale < eMaxOverScale) || (scaleFactor < 1.0f && scaleFactor * scale > eMinScale)) {
            if (scale * scaleFactor > eMaxOverScale + 0.01f) {
                scaleFactor = eMaxOverScale / scale;
            }
            if (scale * scaleFactor < eMinScale + 0.01f) {
                scaleFactor = eMinScale / scale;
            }
            eScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            checkBorderAndCenterWhenScale();
            setImageMatrix(eScaleMatrix);
        }
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (eGestureDetector.onTouchEvent(motionEvent))
            return true;
        eScaleGestureDetector.onTouchEvent(motionEvent);

        float x = 0.0f;
        float y = 0.0f;

        int pointCount = motionEvent.getPointerCount();
        for (int i = 0; i<pointCount; i++) {
            x += motionEvent.getX(i);
            y += motionEvent.getY(i);
        }

        x /= pointCount;
        y /= pointCount;

        if (eLastPointCount != pointCount) {
            isCanDrag = false;
            eLastX = x;
            eLastY = y;
        }
        eLastPointCount = pointCount;
        RectF rectF = getMatrixRectF();

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                eVelocityTracker = VelocityTracker.obtain();
                if (eVelocityTracker != null) {
                    eVelocityTracker.addMovement(motionEvent);
                }
                if (eFlingRunnable != null) {
                    eFlingRunnable.cancelFling();
                    eFlingRunnable = null;
                }

                isCanDrag = false;
                if (rectF.width() > getWidth() + 0.1f || rectF.height() > getHeight() + 0.1f) {
                    if (getParent() instanceof ViewPager) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (rectF.width() > getWidth() + 0.1f || rectF.height() > getHeight() + 0.1f) {
                    if (getParent() instanceof ViewPager) {
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }
                }

                float dx = x - eLastX;
                float dy = y - eLastY;

                if (!isCanDrag) {
                    isCanDrag = isMoveAction(dx, dy);
                }

                if (isCanDrag) {
                    if (getDrawable() != null) {

                        if (eVelocityTracker != null) {
                            eVelocityTracker.addMovement(motionEvent);
                        }

                        isCheckLeftAndRight = true;
                        isCheckTopAndBottom = true;

                        if (rectF.width() < getWidth()) {
                            dx = 0;
                            isCheckLeftAndRight = false;
                        }
                        if (rectF.height() < getHeight()) {
                            dy = 0;
                            isCheckTopAndBottom = false;
                        }
                    }
                    eScaleMatrix.postTranslate(dx, dy);
                    checkBorderWhenTranslate();
                    setImageMatrix(eScaleMatrix);
                }
                eLastX = x;
                eLastY = y;
                break;
            case MotionEvent.ACTION_UP:
                eLastPointCount = 0;
                if (getScale() < eInitScale) {
                    post(new AutoScaleRunnable(eInitScale, getWidth() / 2, getHeight() / 2));
                }
                if (getScale() > eMaxScale) {
                    post(new AutoScaleRunnable(eMaxScale, getWidth() / 2, getHeight() / 2));
                }
                if (isCanDrag) {
                    if (eVelocityTracker != null) {
                        eVelocityTracker.addMovement(motionEvent);
                        eVelocityTracker.computeCurrentVelocity(1000);
                        final float vX = eVelocityTracker.getXVelocity();
                        final float vY = eVelocityTracker.getYVelocity();

                        eFlingRunnable = new FlingRunnable(getContext());
                        eFlingRunnable.fling(getWidth(), getHeight(), (int)-vX, (int)-vY);
                        post(eFlingRunnable);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (eVelocityTracker != null) {
                    eVelocityTracker.recycle();
                    eVelocityTracker = null;
                }
                break;
        }
        return true;
    }

    /**
     * 判断是否是移动的操作
     */
    private boolean isMoveAction(float dx, float dy) {
        return Math.sqrt(dx * dx + dy * dy) > eTouchSlop;
    }

    private class AutoScaleRunnable implements Runnable {
        private float targetScale;
        private float tempScale;
        private float x;
        private float y;

        private final float BIGGER = 1.07f;
        private final float SMALLER = 0.93f;

        public AutoScaleRunnable(float targetScale, float x, float y) {
            this.targetScale = targetScale;
            this.x = x;
            this.y = y;

            if (getScale() < targetScale) {
                tempScale = BIGGER;
            }
            if (getScale() > targetScale) {
                tempScale = SMALLER;
            }
        }
        @Override
        public void run() {
            eScaleMatrix.postScale(tempScale, tempScale, x, y);
            checkBorderAndCenterWhenScale();
            setImageMatrix(eScaleMatrix);

            float currentScale = getScale();

            if ( (tempScale > 1.0f) && currentScale < targetScale || (tempScale < 1.0f) && currentScale > targetScale ) {
                postDelayed(this, 16);
            } else {
                float scale = targetScale / currentScale;
                eScaleMatrix.postScale(scale, scale, x, y);
                checkBorderAndCenterWhenScale();
                setImageMatrix(eScaleMatrix);
                isAutoScale = false;
            }
        }
    }

    private class FlingRunnable implements Runnable {
        private Scroller scroller;
        private int currentX;
        private int currentY;

        public FlingRunnable(Context context) {
            scroller = new Scroller(context);
        }
        public void cancelFling() {
            scroller.forceFinished(true);
        }
        public void fling(int viewWidth, int viewHeight, int velocityX, int velocityY) {
            RectF rectF = getMatrixRectF();
            if (rectF == null)
                return;
            final int startX = Math.round(-rectF.left);
            final int minX;
            final int maxX;
            final int minY;
            final int maxY;

            if (rectF.width() > viewWidth) {
                minX = 0;
                maxX = Math.round(rectF.width() - viewWidth);
            } else {
                minX = maxX = startX;
            }
            final int startY = Math.round(-rectF.top);
            if (rectF.height() > viewHeight) {
                minY = 0;
                maxY = Math.round(rectF.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }
            currentX = startX;
            currentY = startY;
            if (startX != maxX || startY != maxY) {
                scroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
            }
        }

        @Override
        public void run() {
            if (scroller.isFinished())
                return;
            if (scroller.computeScrollOffset()) {
                final int newX = scroller.getCurrX();
                final int newY = scroller.getCurrY();
                eScaleMatrix.postTranslate(currentX - newX, currentY - newY);
                checkBorderWhenTranslate();
                setImageMatrix(eScaleMatrix);

                currentX = newX;
                currentY = newY;

                postDelayed(this, 16);
            }
        }
    }

    public interface PressAction {
        void singleTap();
        void longPress();
    }
}