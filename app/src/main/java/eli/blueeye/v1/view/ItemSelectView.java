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

public class ItemSelectView extends View {

    private static final String TAG = "ItemSelect";

    //线条颜色
    private int lineColor;
    //圆点颜色
    private int pointColor;
    //字体颜色
    private int textColor;
    //线条高度
    private float lineHeight;
    //圆点半径
    private float pointRadius;
    //选择条目
    private List<String> selectItems;
    //组件宽度
    private float windowWidth;
    //组件高度
    private float windowHeight;
    //内部向下偏移值
    private float offset;
    //圆点位置X值
    private float pointRadiusX;
    //圆点位置Y值
    private float pointRadiusY;
    //被选择的index
    private int selectedIndex;
    //选择条目改变接口
    private OnControlStateChangeListener changeListener;

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
        selectItems = new ArrayList<>();
        //获取配置值
        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.styleable_item_select);
        lineColor = ta.getColor(R.styleable.styleable_item_select_select_lineColor, 0xff777777);
        pointColor = ta.getColor(R.styleable.styleable_item_select_select_circleColor, 0xff000000);
        textColor = ta.getColor(R.styleable.styleable_item_select_select_textColor, 0xff000000);
        lineHeight = ta.getInt(R.styleable.styleable_item_select_select_lineHeight, 5);
        pointRadius = ta.getInt(R.styleable.styleable_item_select_select_circleRadius, 10);
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
        windowWidth = getMeasuredWidth();
        windowHeight = getMeasuredHeight();
        //设置内部向下偏移值
        offset = windowHeight / 6;
        //设置圆点坐标的Y值，固定
        pointRadiusY = windowHeight / 2 + offset;
        //重置坐标
        float pieceWidth = windowWidth / selectItems.size();
        pointRadiusX = selectedIndex * pieceWidth + pieceWidth / 2;
    }

    /**
     * 设置选择条目变化监听
     *
     * @param changeListener
     */
    public void setOnControlStateChangeListener(ControlDialog changeListener) {
        this.changeListener = changeListener;
    }

    /**
     * 设置可以选择的项目
     *
     * @param items
     */
    public void setSelectItems(List<String> items) {
        this.selectItems = items;
    }

    /**
     * 计算索引
     *
     * @return
     */
    private int calculateIndex() {
        float pieceWidth = windowWidth / selectItems.size();
        int index = (int) (pointRadiusX / pieceWidth);
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
        if (index < selectItems.size()) {
            selectedIndex = index;
            float pieceWidth = windowWidth / selectItems.size();
            pointRadiusX = selectedIndex * pieceWidth + pieceWidth / 2;
            postInvalidate();
        } else {
            throw new IllegalStateException("Index is lager than the size!");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        //绘制线条
        paint.setColor(lineColor);
        paint.setStrokeWidth(lineHeight);
        canvas.drawLine(0, windowHeight / 2 + offset, windowWidth, windowHeight / 2 + offset, paint);

        float itemWidth = windowWidth / selectItems.size();

        //绘制条目
        paint.setTextSize(26);
        for (int i = 0; i < selectItems.size(); i++) {
            paint.setColor(textColor);
            String itemText = selectItems.get(i);
            float itemTextWidth = paint.measureText(itemText);
            float itemTextHeight = -(paint.descent() + paint.ascent());
            float start = i * itemWidth + itemWidth / 2;
            //绘制条目文本
            canvas.drawText(itemText, start - itemTextWidth / 2, itemTextHeight + offset, paint);
            paint.setColor(pointColor);
            //绘制对应小点
            canvas.drawCircle(start, windowHeight / 2 + offset, (float) (lineHeight * 1.5), paint);
        }

        //绘制圆点
        paint.setColor(pointColor);
        if (pointRadiusX == 0) {
            pointRadiusX = itemWidth / 2;
        }
        canvas.drawCircle(pointRadiusX, pointRadiusY, pointRadius, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //获得触点的X值
        pointRadiusX = event.getX();

        //控制圆点坐标不会溢出
        if (pointRadiusX < pointRadius / 2)
            pointRadiusX = pointRadius / 2;
        if (pointRadiusX > windowWidth - pointRadius / 2)
            pointRadiusX = windowWidth - pointRadius / 2;

        //当手指抬起时，计算并定位到最近的条目
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float pieceWidth = windowWidth / selectItems.size();
            int index = calculateIndex();
            pointRadiusX = index * pieceWidth + pieceWidth / 2;
            if (changeListener != null) {
                int nowIndex = calculateIndex();
                if (nowIndex != selectedIndex) {
                    selectedIndex = nowIndex;
                    changeListener.onItemSelectedChanged(selectedIndex);
                }
            }
        }

        postInvalidate();
        return super.onTouchEvent(event);
    }
}