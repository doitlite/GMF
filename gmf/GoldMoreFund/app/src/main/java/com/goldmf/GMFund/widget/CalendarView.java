package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.util.DimensionConverter;

import java.util.Calendar;
import java.util.LinkedList;

/**
 * Created by yale on 15/8/3.
 */
public class CalendarView extends View {

    public interface OnItemClickListener {
        void onPickDate(int year, int month, int dayInMonth);

        OnItemClickListener NULL = new OnItemClickListener() {
            @Override
            public void onPickDate(int year, int month, int dayInMonth) {
            }
        };
    }

    private static final String[] WEEK_LABELS = new String[]{"一", "二", "三", "四", "五", "六", "日"};
    private static final boolean IS_DEBUG = false;
    private static final float DEBUG_RECT_PADDING_IN_PX = 4;
    private GestureDetector mGestureDetector;

    private TextPaint mTextPaint;
    private Paint mDebugPaint;
    private int mTextSizeInPx;
    private float mElementRectSize;
    private float mHalfElementRectSize;
    private Calendar mCalendar;
    private MyViewModelHolder mHolder;
    private Drawable mDateBackgroundOrNil;
    private Delegate mDelegate = Delegate.DEFAULT;
    private boolean mIsDrawPreviousMonth = false;
    private boolean mIsDrawNextMonth = false;

    private OnItemClickListener mListener = OnItemClickListener.NULL;

    private GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mDownViewModelOrNil != null) {
                MyViewModel data = mDownViewModelOrNil;
                mListener.onPickDate(data.year, data.month, data.dayInMonth);
                return true;
            }
            return false;
        }
    };

    public CalendarView(Context context) {
        this(context, null);
    }

    public CalendarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFocusable(true);
        setFocusableInTouchMode(true);
        mCalendar = Calendar.getInstance();
        mGestureDetector = new GestureDetector(context, mOnGestureListener);

        // setup default attributes value
        {
            mTextSizeInPx = (int) sp2px(12);
        }

        // update attributes from xml
        {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CalendarView, defStyleAttr, 0);
            if (a != null) {
                mTextSizeInPx = a.getDimensionPixelSize(R.styleable.CalendarView_textSize, mTextSizeInPx);
                mDateBackgroundOrNil = a.getDrawable(R.styleable.CalendarView_dateBackground);
                a.recycle();
            }
        }

        // setup text paint
        {
            mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
            mTextPaint.setTextSize(mTextSizeInPx);
        }

        // setup debug paint
        {
            mDebugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mDebugPaint.setStyle(Paint.Style.STROKE);
            mDebugPaint.setColor(0xFFFF0000);
            mDebugPaint.setStrokeWidth(1);
        }

        mHolder = new MyViewModelHolder();
        mHolder.prepare();
    }

    public void setCalendar(Calendar calendar) {
        mCalendar = calendar;
        mHolder.prepare();
        invalidate();
    }

    public void setDelegate(Delegate delegate) {
        mDelegate = delegate == null ? Delegate.DEFAULT : delegate;
    }

    public void setTextSizeInSp(float sp) {
        mTextSizeInPx = (int) sp2px(sp);
        invalidate();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = (listener == null) ? OnItemClickListener.NULL : listener;
    }

    private MyViewModel mDownViewModelOrNil;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            final float x = event.getX() - getPaddingLeft();
            final float y = event.getY() - getPaddingTop();

            // compute relative rect
            if (x > 0 && y > 0) {
                int row;
                int column;

                column = (int) (x / mElementRectSize);
                row = (int) (y / mElementRectSize);
                if (row >= 1) {
                    mDownViewModelOrNil = mHolder.find(row - 1, column);
                    mDownViewModelOrNil.state = MyViewModel.STATE_PRESSED;
                }
            }
            invalidate();
        } else if (action == MotionEvent.ACTION_UP) {
            if (mDownViewModelOrNil != null) {
                playSoundEffect(SoundEffectConstants.CLICK);
                mDownViewModelOrNil.state = MyViewModel.STATE_NORMAL;
                invalidate();
            }
        }
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // compute rect size
        {
            float parentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            mElementRectSize = parentWidth / 7;
            mHalfElementRectSize = mElementRectSize / 2;
        }

        // update heightMeasureSpec if needed
        if (heightMode != MeasureSpec.EXACTLY) {
            final int firstDayInWeekOfCurrentMonth = getFirstDayInWeekOfCurrentMonth();
            final int dayCountInMonthOfCurrentMonth = mCalendar.getMaximum(Calendar.DAY_OF_MONTH);

            int rowCount = 2;
            int remainingDayCountOfFirstWeek = 7 - firstDayInWeekOfCurrentMonth + 1;
            rowCount += (dayCountInMonthOfCurrentMonth - remainingDayCountOfFirstWeek) / 7;
            if ((dayCountInMonthOfCurrentMonth - remainingDayCountOfFirstWeek) % 7 != 0) {
                rowCount++;
            }

            final int heightSize = (int) (rowCount * mElementRectSize) + getPaddingTop() + getPaddingBottom();
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
            setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
        }

        // update dateBackground bounds
        if (mDateBackgroundOrNil != null) {
            mDateBackgroundOrNil.setBounds(0, 0, (int) mElementRectSize, (int) mElementRectSize);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        float offsetX = getPaddingLeft();
        float offsetY = getPaddingTop();
        final float textHeight = measureTextHeight(mTextPaint);
        final float halfTextHeight = textHeight / 2;

        // write week label
        {
            mTextPaint.setColor(0xFF000000);
            for (String label : WEEK_LABELS) {
                final float textWidth = measureTextWidth(mTextPaint, label);
                final float halfTextWidth = textWidth / 2;
                final float x = offsetX + mHalfElementRectSize - halfTextWidth;
                final float y = offsetY + mHalfElementRectSize + halfTextHeight - mTextPaint.getFontMetrics().descent;
                canvas.drawText(label, 0, label.length(), x, y, mTextPaint);
                if (IS_DEBUG) {
                    canvas.drawRect(offsetX + DEBUG_RECT_PADDING_IN_PX, offsetY + DEBUG_RECT_PADDING_IN_PX, offsetX + mElementRectSize - DEBUG_RECT_PADDING_IN_PX, offsetY + mElementRectSize - DEBUG_RECT_PADDING_IN_PX, mDebugPaint);
                }
                offsetX += mElementRectSize;
            }
        }

        // write date
        {
            int previousColor = mTextPaint.getColor();

            offsetX = getPaddingLeft();
            offsetY = mElementRectSize + getPaddingTop();
            int writtenCount = 0;
            for (MyViewModel data : mHolder.getDataSet()) {

                if (data.type == MyViewModel.TYPE_CURRENT_MONTH || data.type == MyViewModel.TYPE_PREVIOUS_MONTH && mIsDrawPreviousMonth || data.type == MyViewModel.TYPE_NEXT_MONTH && mIsDrawNextMonth) {
                    // draw background
                    if (mDateBackgroundOrNil != null) {
                        canvas.translate(offsetX, offsetY);
                        mDateBackgroundOrNil.setState(data.state);
                        mDateBackgroundOrNil.draw(canvas);
                        canvas.translate(-offsetX, -offsetY);
                    }

                    // draw text
                    mTextPaint.setColor(data.backgroundColor.getColorForState(data.state, data.backgroundColor.getDefaultColor()));
                    canvas.drawCircle(offsetX + mHalfElementRectSize, offsetY + mHalfElementRectSize, mHalfElementRectSize, mTextPaint);

                    final String text = String.valueOf(data.dayInMonth);
                    final float textWidth = measureTextWidth(mTextPaint, text);
                    final float halfTextWidth = textWidth / 2;
                    final float x = offsetX + mHalfElementRectSize - halfTextWidth;
                    final float y = offsetY + mHalfElementRectSize + halfTextHeight - mTextPaint.getFontMetrics().descent;
                    mTextPaint.setColor(data.textColor.getColorForState(data.state, data.textColor.getDefaultColor()));
                    canvas.drawText(text, 0, text.length(), x, y, mTextPaint);
                }

                writtenCount++;
                if (writtenCount > 0 && writtenCount % 7 == 0) {
                    offsetX = getPaddingLeft();
                    offsetY += mElementRectSize;
                } else {
                    offsetX += mElementRectSize;
                }
            }

            mTextPaint.setColor(previousColor);
        }
    }

    private float measureTextWidth(Paint mPaint, String text) {
        return mPaint.measureText(text);
    }

    private float measureTextHeight(Paint mPaint) {
        Paint.FontMetrics metrics = mPaint.getFontMetrics();
        return metrics.descent - metrics.ascent;
    }

    /**
     * 默认一个星期的第一天为星期天，通过(day + 6) % 7 转换为周一是第一天
     */
    private int getFirstDayInWeekOfCurrentMonth() {
        int result;
        long timeInMills = mCalendar.getTimeInMillis();
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        result = mCalendar.get(Calendar.DAY_OF_WEEK);
        mCalendar.setTimeInMillis(timeInMills);

        return formatDayInWeek(result);
    }

    private int formatDayInWeek(int dayInWeek) {
        return (dayInWeek + 6) % 7;
    }

    private float sp2px(float sp) {
        return DimensionConverter.sp2px(getContext(), sp);
    }

    private class MyViewModelHolder {
        private LinkedList<MyViewModel> mDataSet = new LinkedList<>();

        public void prepare() {
            mDataSet.clear();
            final int firstDayInWeekOfCurrentMonth = getFirstDayInWeekOfCurrentMonth();

            // add previous month data
            if (firstDayInWeekOfCurrentMonth > 1) {

                // compute dayCountOfPreviousMonth
                final int year;
                final int month;
                final int dayCountOfPreviousMonth;
                {
                    long previousTimeInMills = mCalendar.getTimeInMillis();
                    mCalendar.add(Calendar.MONTH, -1);
                    dayCountOfPreviousMonth = mCalendar.getMaximum(Calendar.DAY_OF_MONTH);
                    year = mCalendar.get(Calendar.YEAR);
                    month = mCalendar.get(Calendar.MONTH);
                    mCalendar.setTimeInMillis(previousTimeInMills);
                }

                int dayInWeekOfPreviousMonth = firstDayInWeekOfCurrentMonth - 1;
                int dayInMonthOfPreviousMonth = dayCountOfPreviousMonth;
                for (int i = dayInWeekOfPreviousMonth; i > 0; i--) {
                    mDataSet.add(0, new MyViewModel(MyViewModel.TYPE_PREVIOUS_MONTH, year, month, dayInMonthOfPreviousMonth, mDelegate));
                    dayInMonthOfPreviousMonth--;
                }
            }

            final int firstDayInWeekOfNextMonth;
            // add current month data
            {
                final int year = mCalendar.get(Calendar.YEAR);
                final int month = mCalendar.get(Calendar.MONTH);
                int dayInWeekOfCurrentMonth = firstDayInWeekOfCurrentMonth;
                final int dayCountOfCurrentMonth = mCalendar.getMaximum(Calendar.DAY_OF_MONTH);
                for (int i = 1; i <= dayCountOfCurrentMonth; i++) {
                    mDataSet.add(new MyViewModel(MyViewModel.TYPE_CURRENT_MONTH, year, month, i, mDelegate));
                    if (dayInWeekOfCurrentMonth == 7) {
                        dayInWeekOfCurrentMonth = 1;
                    } else {
                        dayInWeekOfCurrentMonth++;
                    }
                }
                firstDayInWeekOfNextMonth = dayInWeekOfCurrentMonth;
            }

            // add next month data
            if (firstDayInWeekOfNextMonth > 1) {

                final int year;
                final int month;
                {
                    long previousTimeInMills = mCalendar.getTimeInMillis();

                    mCalendar.add(Calendar.MONTH, 1);
                    year = mCalendar.get(Calendar.YEAR);
                    month = mCalendar.get(Calendar.MONTH);

                    mCalendar.setTimeInMillis(previousTimeInMills);
                }

                int count = 7 - firstDayInWeekOfNextMonth + 1;
                for (int i = 0; i < count; i++) {
                    mDataSet.add(new MyViewModel(MyViewModel.TYPE_NEXT_MONTH, year, month, i + 1, mDelegate));
                }
            }
        }

        public MyViewModel find(int row, int column) {
            if (row >= 0 && column >= 0) {
                int position = column + row * 7;
                return position < mDataSet.size() ? mDataSet.get(position) : null;
            }

            return null;
        }

        public LinkedList<MyViewModel> getDataSet() {
            return mDataSet;
        }
    }

    private static class MyViewModel {
        public static final int[] STATE_NORMAL = new int[]{android.R.attr.state_enabled};
        public static final int[] STATE_PRESSED = new int[]{android.R.attr.state_pressed};

        public static final int TYPE_PREVIOUS_MONTH = 1;
        public static final int TYPE_CURRENT_MONTH = 2;
        public static final int TYPE_NEXT_MONTH = 3;
        public final int type;
        public final int year;
        public final int month;
        public final int dayInMonth;
        public int[] state;
        public final ColorStateList textColor;
        public final ColorStateList backgroundColor;


        public MyViewModel(int type, int year, int month, int dayInMonth, Delegate delegate) {
            this.type = type;
            this.year = year;
            this.month = month;
            this.dayInMonth = dayInMonth;
            this.state = STATE_NORMAL;
            this.textColor = delegate.getTextColor(year, month, dayInMonth);
            this.backgroundColor = delegate.getBackgroundColor(year, month, dayInMonth);
        }
    }

    public interface Delegate {
        ColorStateList getTextColor(int year, int month, int dayInMonth);

        ColorStateList getBackgroundColor(int year, int month, int dayInMonth);

        Delegate DEFAULT = new Delegate() {
            private ColorStateList mTextColor = new ColorStateList(new int[][]{{android.R.attr.state_enabled}, {android.R.attr.state_pressed}}, new int[]{0xFF333333, 0xFFFFFFFF});
            private ColorStateList mBackgroundColor = new ColorStateList(new int[][]{{android.R.attr.state_enabled}, {android.R.attr.state_pressed}}, new int[]{0, 0xFF000000});

            @Override
            public ColorStateList getTextColor(int year, int month, int dayInMonth) {
                return mTextColor;
            }

            @Override
            public ColorStateList getBackgroundColor(int year, int month, int dayInMonth) {
                return mBackgroundColor;
            }
        };
    }
}

