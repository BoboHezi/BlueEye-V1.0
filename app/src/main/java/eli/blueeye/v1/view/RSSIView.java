package eli.blueeye.v1.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import eli.blueeye.v1.R;
import eli.blueeye.v1.util.Util;

public class RSSIView extends View {

    private static final String TAG = "RSSIView";

    private Context context;
    private Paint paint;
    //画笔颜色
    private int lineColor;
    private int lineNegativeColor;
    //字体颜色
    private int textColor;
    //最外层圆半径
    float radius3;
    //画笔宽度
    float lineWidth;
    //信号强度
    private int rssi;
    //组件高度
    private float viewHeight;

    public RSSIView(Context context) {
        this(context, null);
    }

    public RSSIView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RSSIView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.context = context;

        //获取配置值
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.styleable_view_rssi);
        lineColor = typedArray.getColor(R.styleable.styleable_view_rssi_rssi_lineColor, 0xff000000);
        textColor = typedArray.getColor(R.styleable.styleable_view_rssi_rssi_textColor, 0xff000000);
        typedArray.recycle();
        lineNegativeColor = (lineColor << 8 >>> 8) + 0x66000000;

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
            widthSpecSize = Util.dip2px(context, 100);
        }
        if (heightSpecMode == MeasureSpec.AT_MOST) {
            heightSpecSize = Util.dip2px(context, 30);
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);

        //获取组件高度
        viewHeight = getMeasuredHeight();
        //最外层圆半径
        radius3 = viewHeight / 2;
        lineWidth = viewHeight / 30;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //当前信号强度下的线条数
        int count = calculateCount(rssi);

        //绘制弧线
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        paint.setColor(lineNegativeColor);

        if (count == 4) {
            paint.setColor(lineColor);
        }
        //radian out left
        canvas.drawArc(lineWidth / 2, -viewHeight * 3 / 20, viewHeight * 59 / 60, viewHeight * 49 / 60, 137, 86, false, paint);
        //radian out right
        canvas.drawArc(lineWidth / 2, -viewHeight * 3 / 20, viewHeight * 59 / 60, viewHeight * 49 / 60, 317, 90, false, paint);

        if (count >= 3) {
            paint.setColor(lineColor);
        } else {
            paint.setColor(lineNegativeColor);
        }
        //radian center left
        canvas.drawArc(viewHeight * 7 / 60, -viewHeight / 60, viewHeight * 17 / 20, viewHeight * 41 / 60, 135, 90, false, paint);
        //radian center right
        canvas.drawArc(viewHeight * 7 / 60, -viewHeight / 60, viewHeight * 17 / 20, viewHeight * 41 / 60, 315, 90, false, paint);

        if (count >= 2) {
            paint.setColor(lineColor);
        } else {
            paint.setColor(lineNegativeColor);
        }
        //radian inner left
        canvas.drawArc(viewHeight / 4, viewHeight / 10, viewHeight * 11 / 15, viewHeight * 17 / 30, 135, 90, false, paint);
        //radian inner right
        canvas.drawArc(viewHeight / 4, viewHeight / 10, viewHeight * 11 / 15, viewHeight * 17 / 30, 315, 90, false, paint);

        //绘制中心点
        if (count >= 1) {
            paint.setColor(lineColor);
        } else {
            paint.setColor(lineNegativeColor);
        }
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(radius3, (float) (viewHeight * 0.35), 5, paint);

        //绘制竖线
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(lineColor);
        canvas.drawLine(radius3, viewHeight / 2, radius3, viewHeight, paint);

        //绘制文字
        String text = rssi + " dBm";
        paint.setTextSize(viewHeight / 2);
        paint.setColor(textColor);
        float textHeight = -(paint.descent() + paint.ascent());
        canvas.drawText(text, (float) (radius3 * 2.5), (viewHeight + textHeight) / 2, paint);
    }

    /**
     * 设置RSSI
     *
     * @param rssi
     */
    public void setRssi(int rssi) {
        this.rssi = rssi;
        postInvalidate();
    }

    /**
     * 计算对应信号强度的线条数目
     *
     * @param number
     * @return
     */
    private int calculateCount(int number) {
        int count;

        number = (number < -95) ? -95 : number;
        number = (number > -35) ? -35 : number;

        float scale = (float) (number + 95) / 60;
        count = (int) (scale * 4);
        return count;
    }
}