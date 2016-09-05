package com.goldmf.GMFund.controller.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.IntExtension;
import com.goldmf.GMFund.manager.score.DayActionManager;
import com.goldmf.GMFund.manager.score.DayActionManager.DayInfo;
import com.goldmf.GMFund.manager.score.ScoreManager;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import yale.extension.common.rx.ProxyCompositionSubscription;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_SEP_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.extension.ListExtension.getFromList;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageResource;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setTextColor;
import static java.lang.Math.max;

/**
 * Created by Evan on 16/2/26 下午2:58.
 */
public class SigningDialog extends BasicDialog {

    public View mContentWrapper;
    private ProxyCompositionSubscription mSubscription = ProxyCompositionSubscription.create();
    private boolean mCancelable = true;

    public SigningDialog(Context context) {
        super(context, R.style.GMFDialog_FullScreen);
        getWindow().setLayout(-1, -2);
        setContentView(R.layout.dialog_signing);
        v_setClick(this, R.id.btn_signing, v -> dismiss());
        resetContentSection(new VM(ScoreManager.getInstance().checkin));
        mContentWrapper = v_findView(this, R.id.wrapper_content);
    }

    @Override
    public void show() {
        super.show();
        mSubscription.reset();
    }

    @Override
    public void dismiss() {
        if (mCancelable) {
            super.dismiss();
            mSubscription.close();
        }
    }

    private void resetContentSection(VM vm) {
        DayInfo dayInfo = safeGet(() -> getFromList(vm.dayInfoList, vm.todayPos), null);
        String todayScore = String.format(Locale.getDefault(), "获得%s积分", safeGet(() -> String.valueOf((int) dayInfo.amount), "???"));
        v_setText(this, R.id.label_score_today, todayScore);

        resetTable(vm);
    }


    private static final int CELL_STYLE_SMALL = 0;
    private static final int CELL_STYLE_BIG = 1;

    private void resetTable(VM vm) {
        LinearLayout parent = v_findView(this, R.id.section_signing_date);
        parent.removeAllViewsInLayout();

        Integer[][] columnStyleArray = {{CELL_STYLE_SMALL, CELL_STYLE_SMALL, CELL_STYLE_SMALL, CELL_STYLE_SMALL}, {CELL_STYLE_SMALL, CELL_STYLE_SMALL, CELL_STYLE_BIG}};
        int[] columnCountArray = {columnStyleArray[0].length, columnStyleArray[1].length};
        int rowCount = columnStyleArray.length;
        int horizontalSpacing = dp2px(5);
        int verticalSpacing = dp2px(5);
        /**
         * parentWidth = fixWidth - marginLeft - marginRight
         */
        Rect parentSize = new Rect(0, 0, dp2px(335 - 40), parent.getMeasuredHeight());
        Rect columnSize = new Rect();
        int positionOffset = 0;
        for (int i = 0; i < rowCount; i++) {
            int columnCount = columnCountArray[i];
            Integer[] columnStyle = columnStyleArray[i];
            LinearLayout row = createRow(parent);
            for (int j = 0; j < columnCount; j++) {
                measureColumnSize(parentSize, columnSize, columnStyle[j], horizontalSpacing, columnStyle);
                View column = createColumn(row, columnSize);
                configureColumn(column, j + positionOffset, vm);
                row.addView(column);
                View space = createHorizontalSpace(row, horizontalSpacing);
                row.addView(space);
            }
            positionOffset += columnCount;
            if (columnCount > 0) {
                row.removeViewAt(row.getChildCount() - 1);
            }
            parent.addView(row);

            View space = createVerticalSpace(parent, verticalSpacing);
            parent.addView(space);
        }

        if (rowCount > 0) {
            parent.removeViewAt(parent.getChildCount() - 1);
        }
    }

    private static void measureColumnSize(Rect parentSize, Rect outSize, int style, int spacing, Integer[] columnStyles) {
        int offsetColumnCount = (int) Stream.of(columnStyles).filter(it -> it == CELL_STYLE_BIG).count();
        int columnCount = columnStyles.length + offsetColumnCount;
        int totalSpacing = max(spacing * (columnCount - 1), 0);
        int cellUnitWidth = max((parentSize.width() - totalSpacing) / (columnCount), 0);
        int cellUnitHeight = -2;

        if (IntExtension.anyMatch(style, CELL_STYLE_SMALL)) {
            outSize.set(0, 0, cellUnitWidth, cellUnitHeight);
        } else if (IntExtension.anyMatch(style, CELL_STYLE_BIG)) {
            outSize.set(0, 0, cellUnitWidth * 2 + spacing, cellUnitHeight);
        } else {
            outSize.setEmpty();
        }
    }

    private LinearLayout createRow(LinearLayout parent) {
        Context ctx = parent.getContext();
        LinearLayout row = new LinearLayout(ctx);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        row.setLayoutParams(params);
        return row;
    }

    private static View createHorizontalSpace(LinearLayout parent, int spacingInPx) {
        Context ctx = parent.getContext();
        Space space = new Space(ctx);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(spacingInPx, spacingInPx);
        params.width = spacingInPx;
        params.height = 1;
        params.gravity = Gravity.CENTER_VERTICAL;
        space.setLayoutParams(params);
        return space;
    }

    private static View createVerticalSpace(LinearLayout parent, int spacingInPx) {
        Context ctx = parent.getContext();
        Space space = new Space(ctx);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(spacingInPx, spacingInPx);
        params.width = 1;
        params.height = spacingInPx;
        params.gravity = Gravity.CENTER_VERTICAL;
        space.setLayoutParams(params);
        return space;
    }

    private static View createColumn(LinearLayout parent, Rect size) {
        Context ctx = parent.getContext();
        View column = LayoutInflater.from(ctx).inflate(R.layout.cell_dialog_signing, parent, false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size.width(), size.height());
        column.setLayoutParams(params);
        return column;
    }

    private static void configureColumn(View cell, int position, VM vm) {
        DayInfo dayInfo = safeGet(() -> getFromList(vm.dayInfoList, position), null);
        boolean beforeToday = position < vm.todayPos;
        boolean isToday = position == vm.todayPos;
        boolean afterToday = position > vm.todayPos;

        View contentSection = v_findView(cell, R.id.section_content);

        final int RADIUS = dp2px(2);
        contentSection.setBackgroundDrawable(new ShapeDrawable(new Shape() {
            RectF rect = new RectF();
            RectF cacheRect = new RectF();
            Path path = new Path();

            @Override
            public void draw(Canvas canvas, Paint paint) {
                path.reset();
                rect.set(0, 0, canvas.getWidth(), canvas.getHeight());

                paint.setStyle(Paint.Style.FILL);
                paint.setColor(WHITE_COLOR);
                canvas.drawRoundRect(rect, RADIUS, RADIUS, paint);

                saveRect();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(isToday ? RED_COLOR : 0xFFF2F2F2);
                rect.set(rect.left, rect.top, rect.right, dp2px(24));
                path.addRoundRect(rect, new float[]{RADIUS, RADIUS, RADIUS, RADIUS, 0, 0, 0, 0}, Path.Direction.CW);
                canvas.drawPath(path, paint);
                restoreRect();

                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(isToday ? RED_COLOR : LINE_SEP_COLOR);
                canvas.drawRoundRect(rect, RADIUS, RADIUS, paint);
            }

            private void saveRect() {
                cacheRect.set(rect);
            }

            private void restoreRect() {
                rect.set(cacheRect);
            }
        }));

        TextView titleLabel = v_findView(cell, R.id.label_title);
        titleLabel.setTextColor(vm.todayPos == position ? WHITE_COLOR : 0x33000000);
        if (position == 0) {
            v_setText(titleLabel, "第一天");
        } else if (position == 1) {
            v_setText(titleLabel, "第二天");
        } else if (position == 2) {
            v_setText(titleLabel, "第三天");
        } else if (position == 3) {
            v_setText(titleLabel, "第四天");
        } else if (position == 4) {
            v_setText(titleLabel, "第五天");
        } else if (position == 5) {
            v_setText(titleLabel, "第六天");
        } else if (position == 6) {
            v_setText(titleLabel, "第七天");
        }

        v_setImageResource(cell, R.id.img_icon, isToday ? R.mipmap.ic_day_sign : 0);
        v_setText(cell, R.id.label_score, !isToday ? safeGet(() -> String.valueOf((int) dayInfo.amount), "???") : "");
        v_setTextColor(cell, R.id.label_score, beforeToday ? 0x33000000 : TEXT_BLACK_COLOR);

    }

    private static class VM {
        private CharSequence desc;
        private int todayPos;
        private boolean isGained;
        private CharSequence buttonLabel;
        private List<DayInfo> dayInfoList;

        private CharSequence columnTitle;

        public VM(DayActionManager manager) {
            dayInfoList = safeGet(() -> manager.dayActionInfoList, Collections.<DayInfo>emptyList());
            todayPos = safeGet(() -> manager.todayPos, 0);
            isGained = safeGet(() -> manager.todayGained, false);
            desc = safeGet(() -> manager.desc, "签到领好礼");
            buttonLabel = safeGet(() -> manager.todayGained ? "今天已经签过了" : "立即签到", "立即签到");
        }
    }


}
