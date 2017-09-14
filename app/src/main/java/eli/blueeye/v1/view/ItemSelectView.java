package eli.blueeye.v1.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import eli.blueeye.v1.R;
import eli.blueeye.v1.dialog.ControlDialog;
import eli.blueeye.v1.inter.OnControlStateChangeListener;

/**
 * 条目选择视图
 *
 * @author eli chang
 */
public class ItemSelectView extends View {

    private static final String TAG = "ItemSelect";

    //线条颜色
    private int eLineColor;
    //圆点颜色
    private int ePointColor;
    //字体颜色
    private int eTextColor;
    //线条高度
    private float eLineHeight;
    //圆点半径
    private float ePointRadius;
    //选择条目
    private List<String> eSelectItems;
    //组件宽度
    private float eWindowWidth;
    //组件高度
    private float eWindowHeight;
    //内部向下偏移值
    private float offset;
    //圆点位置X值
    private float ePointRadiusX;
    //圆点位置Y值
    private float ePointRadiusY;
    //被选择的index
    private int eSelectedIndex;
    //选择条目改变接口
    private OnControlStateChangeListener eChangeListener;

    private Context context;
    private Paint paint;

    public ItemSelectView(Context context) {
        this(context, null);
    }

    public ItemSelectView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ItemSelectView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        this.context = context;
        //初始化画笔
        paint = new Paint();
        paint.setAntiAlias(true);
        //初始化条目
        eSelectItems = new ArrayList<>();
        //获取配置值
        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.styleable_item_select);
        eLineColor = ta.getColor(R.styleable.styleable_item_select_select_lineColor, 0xff777777);
        ePointColor = ta.getColor(R.styleable.styleable_item_select_select_circleColor, 0xff000000);
        eTextColor = ta.getColor(R.styleable.styleable_item_select_select_textColor, 0xff000000);
        eLineHeight = ta.getInt(R.styleable.styleable_item_select_select_lineHeight, 5);
        ePointRadius = ta.getInt(R.styleable.styleable_item_select_select_circleRadius, 10);
        ta.recycle();

        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取组件宽高
        eWindowWidth = getMeasuredWidth();
        eWindowHeight = getMeasuredHeight();
        //设置内部向下偏移值
        offset = eWindowHeight / 6;
        //设置圆点坐标的Y值，固定
        ePointRadiusY = eWindowHeight / 2 + offset;
        //重置坐标
        float pieceWidth = eWindowWidth / eSelectItems.size();
        ePointRadiusX = eSelectedIndex * pieceWidth + pieceWidth / 2;
    }

    /**
     * 设置选择条目变化监听
     *
     * @param changeListener
     */
    public void setOnControlStateChangeListener(ControlDialog changeListener) {
        this.eChangeListener = changeListener;
    }

    /**
     * 设置可以选择的项目
     *
     * @param items
     */
    public void setSelectItems(List<String> items) {
        this.eSelectItems = items;
    }

    /**
     * 计算索引
     *
     * @return
     */
    private int calculateIndex() {
        float pieceWidth = eWindowWidth / eSelectItems.size();
        int index = (int) (ePointRadiusX / pieceWidth);
        return index;
    }

    /**
     * 获取当前索引
     *
     * @return
     */
    public int getIndex() {
        return calculateIndex();
    }

    /**
     * 设置当前索引
     *
     * @param index
     */
    public void setIndex(int index) {
        if (index < eSelectItems.size()) {
            eSelectedIndex = index;
            float pieceWidth = eWindowWidth / eSelectItems.size();
            ePointRadiusX = eSelectedIndex * pieceWidth + pieceWidth / 2;
            postInvalidate();
        } else {
            throw new IllegalStateException("Index is lager than the size!");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //绘制线条
        paint.setColor(eLineColor);
        paint.setStrokeWidth(eLineHeight);
        canvas.drawLine(0, eWindowHeight / 2 + offset, eWindowWidth, eWindowHeight / 2 + offset, paint);

        float itemWidth = eWindowWidth / eSelectItems.size();

        //绘制条目
        paint.setTextSize(26);
        for (int i = 0; i < eSelectItems.size(); i++) {
            paint.setColor(eTextColor);
            String itemText = eSelectItems.get(i);
            float itemTextWidth = paint.measureText(itemText);
            float itemTextHeight = -(paint.descent() + paint.ascent());
            float start = i * itemWidth + itemWidth / 2;
            //绘制条目文本
            canvas.drawText(itemText, start - itemTextWidth / 2, itemTextHeight + offset, paint);
            paint.setColor(ePointColor);
            //绘制对应小点
            canvas.drawCircle(start, eWindowHeight / 2 + offset, (float) (eLineHeight * 1.5), paint);
        }

        //绘制圆点
        paint.setColor(ePointColor);
        if (ePointRadiusX == 0) {
            ePointRadiusX = itemWidth / 2;
        }
        canvas.drawCircle(ePointRadiusX, ePointRadiusY, ePointRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //获得触点的X值
        ePointRadiusX = event.getX();

        //控制圆点坐标不会溢出
        if (ePointRadiusX < ePointRadius / 2)
            ePointRadiusX = ePointRadius / 2;
        if (ePointRadiusX > eWindowWidth - ePointRadius / 2)
            ePointRadiusX = eWindowWidth - ePointRadius / 2;

        //当手指抬起时，计算并定位到最近的条目
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float pieceWidth = eWindowWidth / eSelectItems.size();
            int index = calculateIndex();
            ePointRadiusX = index * pieceWidth + pieceWidth / 2;
            if (eChangeListener != null) {
                int nowIndex = calculateIndex();
                if (nowIndex != eSelectedIndex) {
                    eSelectedIndex = nowIndex;
                    eChangeListener.onItemSelectedChanged(eSelectedIndex);
                }
            }
        }

        postInvalidate();
        return super.onTouchEvent(event);
    }
}