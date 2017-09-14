package eli.blueeye.v1.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import eli.blueeye.v1.R;
import eli.blueeye.v1.util.Util;

/**
 * 显示WIFI信号强度的组件
 *
 * @author eli chang
 */
public class RSSIView extends View {

    private static final String TAG = "RSSIView";

    private Context context;
    private Paint paint;
    //画笔颜色
    private int eLineColor;
    private int eLineNegativeColor;
    //字体颜色
    private int eTextColor;
    //最外层圆半径
    float radius3;
    //画笔宽度
    float eLineWidth;
    //信号强度
    private int rssi;
    //组件高度
    private float eViewHeight;

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
        eLineColor = typedArray.getColor(R.styleable.styleable_view_rssi_rssi_lineColor, 0xff000000);
        eTextColor = typedArray.getColor(R.styleable.styleable_view_rssi_rssi_textColor, 0xff000000);
        typedArray.recycle();
        eLineNegativeColor = (eLineColor << 8 >>> 8) + 0x66000000;

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
        eViewHeight = getMeasuredHeight();
        //最外层圆半径
        radius3 = eViewHeight / 2;
        eLineWidth = eViewHeight / 30;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //当前信号强度下的线条数
        int count = calculateCount(rssi);

        //绘制弧线
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(eLineWidth);
        paint.setColor(eLineNegativeColor);

        if (count == 4) {
            paint.setColor(eLineColor);
        }
        //radian out left
        canvas.drawArc(eLineWidth / 2, -eViewHeight * 3 / 20, eViewHeight * 59 / 60, eViewHeight * 49 / 60, 137, 86, false, paint);
        //radian out right
        canvas.drawArc(eLineWidth / 2, -eViewHeight * 3 / 20, eViewHeight * 59 / 60, eViewHeight * 49 / 60, 317, 90, false, paint);

        if (count >= 3) {
            paint.setColor(eLineColor);
        } else {
            paint.setColor(eLineNegativeColor);
        }
        //radian center left
        canvas.drawArc(eViewHeight * 7 / 60, -eViewHeight / 60, eViewHeight * 17 / 20, eViewHeight * 41 / 60, 135, 90, false, paint);
        //radian center right
        canvas.drawArc(eViewHeight * 7 / 60, -eViewHeight / 60, eViewHeight * 17 / 20, eViewHeight * 41 / 60, 315, 90, false, paint);

        if (count >= 2) {
            paint.setColor(eLineColor);
        } else {
            paint.setColor(eLineNegativeColor);
        }
        //radian inner left
        canvas.drawArc(eViewHeight / 4, eViewHeight / 10, eViewHeight * 11 / 15, eViewHeight * 17 / 30, 135, 90, false, paint);
        //radian inner right
        canvas.drawArc(eViewHeight / 4, eViewHeight / 10, eViewHeight * 11 / 15, eViewHeight * 17 / 30, 315, 90, false, paint);

        //绘制中心点
        if (count >= 1) {
            paint.setColor(eLineColor);
        } else {
            paint.setColor(eLineNegativeColor);
        }
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(radius3, (float) (eViewHeight * 0.35), 5, paint);

        //绘制竖线
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(eLineColor);
        canvas.drawLine(radius3, eViewHeight / 2, radius3, eViewHeight, paint);

        //绘制文字
        String text = rssi + " dBm";
        paint.setTextSize(eViewHeight / 2);
        paint.setColor(eTextColor);
        float textHeight = -(paint.descent() + paint.ascent());
        canvas.drawText(text, (float) (radius3 * 2.5), (eViewHeight + textHeight) / 2, paint);
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