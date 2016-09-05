package com.goldmf.GMFund.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.view.View;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;

/**
 * Created by Evan on 16/1/29 下午3:35.
 */
public class ChatBaseView extends View {

    /**
     * 默认背景色
     */
    private static final int DEFAULT_BACKGROUND = WHITE_COLOR;
    /**
     * 默认xy轴字体大小
     */
    public static final int DEFAULT_AXIS_TITLE_SIZE = 24;
    /**
     * 默认xy坐标轴颜色
     */
    private static final int DEFAULT_AXIS_COLOR = RED_COLOR;
    /**
     * 默认经纬线颜色
     */
    private static final int DEFAULT_LONGI_LATITUDE_COLOR = GREY_COLOR;
    /**
     * 默认经纬线数量
     */
    private static final int DEFAULT_LONGI_LATITUDE_NUM = 3;
    /**
     * 默认上表纬线数
     */
    private static final int DEFAULT_UPPER_LATITUDE_NUM = 3;
    /**
     * 默认下表纬线数
     */
    private static final int DEFAULT_LOWER_LONGI_LATITUDE_NUM = 1;

    /**
     * 默认边框颜色
     */
    private static final int DEFAULT_BORDER_COLOR = RED_COLOR;
    /**
     * 默认虚线效果
     */
    private static final PathEffect DEFAULT_DASH_EFFECT = new DashPathEffect(new float[]{3, 3, 3, 3}, 1);
    /**
     * 下表的顶部
     */
    public static float LOWER_CHART_TOP;
    /**
     * 上表的底部
     */
    public static float UPPER_CHART_BOTTOM;

    /**
     * 背景色
     */
    private int mBackGround;
    /**
     * 坐标轴xy轴颜色
     */
    private int mAxisColor;
    /**
     * 经纬线颜色
     */
    private int mLongiLatitudeColor;
    /**
     * 虚线效果
     */
    private PathEffect mDashEffect;
    /**
     * 边框颜色
     */
    private int mBorderColor;
    /**
     * 上表高度
     */
    private float mUpperChartHeight;
    /**
     * 是否显示顶部titles
     */
    private boolean showTopTitles;
    /**
     * 顶部titles高度
     */
    private float topTitleHeight;
    /**
     * 下表TabIndex
     */
    private int mTabIndex;
    /**
     * 下表TabTitles
     */
    private String[] mLowerChartTabTitles;
    /**
     * 下表TAB宽度
     */
    private float mTabWidth;
    /**
     * 下表TAB高度
     */
    private float mTabHight;
    /**
     * 下表高度
     */
    private float mLowerChartHeight;
    private float longitudeSpacing;
    private float latitudeSpacing;

    private OnTabClickListener mListener;

    public ChatBaseView(Context context) {
        this(context, null);
    }

    public ChatBaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChatBaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBackGround = DEFAULT_BACKGROUND;
        mAxisColor = DEFAULT_AXIS_COLOR;
        mLongiLatitudeColor = DEFAULT_LONGI_LATITUDE_COLOR;
        mDashEffect = DEFAULT_DASH_EFFECT;
        mBorderColor = DEFAULT_BORDER_COLOR;
        showTopTitles = true;
        topTitleHeight = 0;
        mTabIndex = 0;
        mTabWidth = 0;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setBackgroundColor(DEFAULT_BACKGROUND);
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        if (showTopTitles) {
            topTitleHeight = DEFAULT_AXIS_TITLE_SIZE + 2;
        }

        longitudeSpacing = (viewWidth - 40) / (DEFAULT_LONGI_LATITUDE_NUM + 1);
        latitudeSpacing = (viewHeight - 4 - DEFAULT_AXIS_TITLE_SIZE - topTitleHeight - mTabHight) / (DEFAULT_UPPER_LATITUDE_NUM + DEFAULT_LOWER_LONGI_LATITUDE_NUM + 2);
        mUpperChartHeight = latitudeSpacing * (DEFAULT_UPPER_LATITUDE_NUM + 1);
        LOWER_CHART_TOP = viewHeight - latitudeSpacing * (DEFAULT_LOWER_LONGI_LATITUDE_NUM + 1);
        UPPER_CHART_BOTTOM = 1 + topTitleHeight + latitudeSpacing * (DEFAULT_UPPER_LATITUDE_NUM + 1);
        mLowerChartHeight = viewHeight - 2 - UPPER_CHART_BOTTOM;

        // 绘制边框
        //        drawBorders(canvas, viewHeight, viewWidth);
        // 绘制经线
        //        drawLongitudes(canvas, viewHeight, longitudeSpacing);
        //        // 绘制纬线
        //        drawLatitudes(canvas, viewHeight, viewWidth, latitudeSpacing);
        // 绘制x线
        drawRegions(canvas, viewHeight, viewWidth);

    }

    private void drawBorders(Canvas canvas, int viewHeight, int viewWidth) {
        Paint paint = new Paint();
        paint.setColor(mBorderColor);
        paint.setStrokeWidth(2);
        canvas.drawLine(1, 1, viewWidth - 1, 1, paint);
        canvas.drawLine(1, 1, 1, viewHeight - 1, paint);
        canvas.drawLine(viewWidth - 1, viewHeight - 1, viewWidth - 1, 1, paint);
        canvas.drawLine(viewWidth - 1, viewHeight - 1, 1, viewHeight - 1, paint);

    }

    private void drawLongitudes(Canvas canvas, int viewHeight, float longitudeSpacing) {
        Paint paint = new Paint();
        paint.setColor(mLongiLatitudeColor);
        paint.setPathEffect(mDashEffect);
        for (int i = 0; i <= DEFAULT_LONGI_LATITUDE_NUM; i++) {
            canvas.drawLine(1 + longitudeSpacing * i, topTitleHeight + 2, 1 + longitudeSpacing * i, UPPER_CHART_BOTTOM, paint);
            canvas.drawLine(1 + longitudeSpacing * i, LOWER_CHART_TOP, 1 + longitudeSpacing * i, viewHeight - 1, paint);
        }

    }

    private void drawLatitudes(Canvas canvas, int viewHeight, int viewWidth, float latitudeSpacing) {

        Paint paint = new Paint();
        paint.setColor(mLongiLatitudeColor);
        paint.setPathEffect(mDashEffect);
        for (int i = 0; i <= DEFAULT_UPPER_LATITUDE_NUM; i++) {
            canvas.drawLine(1, topTitleHeight + 1 + latitudeSpacing * i, viewWidth - 1, topTitleHeight + 1 + latitudeSpacing * i, paint);
        }
        for (int i = 0; i <= DEFAULT_LOWER_LONGI_LATITUDE_NUM; i++) {
            canvas.drawLine(1, viewHeight - 1 - latitudeSpacing, viewWidth - 1, viewHeight - 1 - latitudeSpacing, paint);
        }
    }

    private void drawRegions(Canvas canvas, int viewHeight, int viewWidth) {

        Paint paint = new Paint();
        paint.setColor(mAxisColor);
        paint.setAlpha(150);
        if (showTopTitles) {
            canvas.drawLine(1, 1 + DEFAULT_AXIS_TITLE_SIZE + 2, viewWidth - 1, 1 + DEFAULT_AXIS_TITLE_SIZE + 2, paint);
        }
        canvas.drawLine(20, UPPER_CHART_BOTTOM, viewWidth - 20, UPPER_CHART_BOTTOM, paint);
        canvas.drawLine(20, LOWER_CHART_TOP, viewWidth - 20, LOWER_CHART_TOP, paint);
        canvas.drawLine(20, UPPER_CHART_BOTTOM + DEFAULT_AXIS_TITLE_SIZE + 2, viewWidth - 20, UPPER_CHART_BOTTOM + DEFAULT_AXIS_TITLE_SIZE + 2, paint);
        if (mLowerChartTabTitles == null || mLowerChartTabTitles.length <= 0) {
            return;
        }
        mTabWidth = (viewWidth - 2) / 10.0f * 10.0f / mLowerChartTabTitles.length;
        if (mTabWidth < DEFAULT_AXIS_TITLE_SIZE * 2.5f + 2) {
            mTabWidth = DEFAULT_AXIS_TITLE_SIZE * 2.5f + 2;
        }

        Paint textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(DEFAULT_AXIS_TITLE_SIZE);
        for (int i = 0; i < mLowerChartTabTitles.length && mTabWidth * (i + 1) <= viewWidth - 2; i++) {
            if (i == mTabIndex) {
                Paint bgPaint = new Paint();
                bgPaint.setColor(Color.MAGENTA);
                canvas.drawRect(mTabWidth * i + 1, LOWER_CHART_TOP, mTabWidth * (i + 1) + 1, UPPER_CHART_BOTTOM + DEFAULT_AXIS_TITLE_SIZE + 2, bgPaint);
            }
            canvas.drawLine(mTabWidth * i + 1, LOWER_CHART_TOP, mTabWidth * (i + 1) + 1, UPPER_CHART_BOTTOM + DEFAULT_AXIS_TITLE_SIZE + 2, paint);
            canvas.drawText(mLowerChartTabTitles[i], mTabWidth * i + mTabWidth / 2.0f - mLowerChartTabTitles[i].length() / 3.0f + DEFAULT_AXIS_TITLE_SIZE, LOWER_CHART_TOP - mTabHight / 2.0f + DEFAULT_AXIS_TITLE_SIZE / 2.0f, textPaint);
        }
    }


    public void setOnTabClickListener(OnTabClickListener listener) {
        mListener = listener;
    }

    public interface OnTabClickListener {
        void onTabClick(int index);
    }

    public int getBackGround() {
        return mBackGround;
    }

    public void setBackGround(int BackGround) {
        this.mBackGround = BackGround;
    }

    public int getAxisColor() {
        return mAxisColor;
    }

    public void setAxisColor(int AxisColor) {
        this.mAxisColor = AxisColor;
    }

    public int getLongiLatitudeColor() {
        return mLongiLatitudeColor;
    }

    public void setLongiLatitudeColor(int LongiLatitudeColor) {
        this.mLongiLatitudeColor = LongiLatitudeColor;
    }

    public PathEffect getDashEffect() {
        return mDashEffect;
    }

    public void setDashEffect(PathEffect DashEffect) {
        this.mDashEffect = DashEffect;
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderColor(int BorderColor) {
        this.mBorderColor = BorderColor;
    }

    public float getUpperChartHeight() {
        return mUpperChartHeight;
    }

    public void setUpperChartHeight(float UperChartHeight) {
        this.mUpperChartHeight = UperChartHeight;
    }

    public float getLowerChartHeight() {
        return mLowerChartHeight;
    }

    public void setLowerChartHeight(float LowerChartHeight) {
        this.mLowerChartHeight = LowerChartHeight;
    }

    public String[] getLowerChartTabTitles() {
        return mLowerChartTabTitles;
    }

    public void setLowerChartTabTitles(String[] LowerChartTabTitles) {
        this.mLowerChartTabTitles = LowerChartTabTitles;
    }

    public float getLongitudeSpacing() {
        return longitudeSpacing;
    }

    public void setLongitudeSpacing(float longitudeSpacing) {
        this.longitudeSpacing = longitudeSpacing;
    }

    public float getLatitudeSpacing() {
        return latitudeSpacing;
    }

    public void setLatitudeSpacing(float latitudeSpacing) {
        this.latitudeSpacing = latitudeSpacing;
    }

    public void setShowTopTitles(boolean showTopTitles) {
        this.showTopTitles = showTopTitles;
    }

    public float getTopTitleHeight() {
        return topTitleHeight;
    }
}
