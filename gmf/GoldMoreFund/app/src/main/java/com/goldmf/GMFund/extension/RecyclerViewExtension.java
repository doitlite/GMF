package com.goldmf.GMFund.extension;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.internal.SignalColorHolder;

import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_SEP_COLOR;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_setInvisible;
import static com.goldmf.GMFund.extension.ViewGroupExtension.v_forEach;

/**
 * Created by yale on 16/3/22.
 */
public class RecyclerViewExtension {
    private RecyclerViewExtension() {
    }

    @SuppressWarnings("unchecked")
    public static <T> SimpleRecyclerViewAdapter<T> getSimpleAdapter(RecyclerView recyclerView) {

        if (recyclerView != null && recyclerView.getAdapter() != null && recyclerView.getAdapter() instanceof SimpleRecyclerViewAdapter) {
            return (SimpleRecyclerViewAdapter<T>) recyclerView.getAdapter();
        }

        return null;
    }

    public static void addHorizontalSepLine(RecyclerView recyclerView) {
        addHorizontalSepLine(recyclerView, LINE_SEP_COLOR, dp2px(0.5f), new Rect());
    }

    public static void addHorizontalSepLine(RecyclerView recyclerView, Rect inset) {
        addHorizontalSepLine(recyclerView, LINE_SEP_COLOR, dp2px(0.5f), inset);
    }

    private static void addHorizontalSepLine(RecyclerView recyclerView, int color, int lineWidthInPx, Rect inset) {
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(lineWidthInPx);
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
                super.onDrawOver(c, parent, state);
                v_forEach(parent, (pos, child) -> {
                    int adapterPos = parent.getChildAdapterPosition(child);
                    if (adapterPos > 0) {
                        c.drawLine(inset.left, child.getTop() + inset.top, child.getWidth() - inset.right, child.getTop() + inset.top - inset.bottom, paint);
                    }
                });
            }
        });
    }

    public static void addCellVerticalSpacing(RecyclerView recyclerView, int spacing) {
        addCellVerticalSpacing(recyclerView, spacing, 0, 0);
    }

    public static void addCellVerticalSpacing(RecyclerView recyclerView, int spacing, int headerCount, int footerCount) {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getAdapter() != null) {
                    int pos = recyclerView.getChildAdapterPosition(view);
                    int itemCount = recyclerView.getAdapter().getItemCount();
                    boolean hasHeader = headerCount > 0;
                    boolean hasFooter = footerCount > 0;
//                    boolean isHeader = pos < headerCount;
                    boolean isLastHeader = hasHeader && pos == headerCount - 1;
                    boolean notLastHeader = hasHeader && (pos < headerCount - 1);
                    boolean isFirstCell = pos == headerCount;
                    boolean isFooter = pos >= (itemCount - footerCount);
                    boolean hasCell = ((headerCount + footerCount) < itemCount);
                    boolean isLastCell = (pos == itemCount - footerCount);

                    outRect.top = (!hasHeader && isFirstCell) ? spacing : 0;
                    outRect.bottom = (notLastHeader) || (isLastHeader && hasCell) || (isFooter) || (hasFooter && isLastCell) ? 0 : spacing;
                }
            }
        });
    }

    public static void addContentInset(RecyclerView recyclerView, int inset) {
        addContentInset(recyclerView, new Rect(inset, inset, inset, inset));
    }

    public static void addContentInset(RecyclerView recyclerView, int hInset, int vInset) {
        addContentInset(recyclerView, new Rect(hInset, vInset, hInset, vInset));
    }

    public static void addContentInset(RecyclerView recyclerView, Rect inset) {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getAdapter() != null) {
                    int pos = recyclerView.getChildAdapterPosition(view);
                    int itemCount = parent.getAdapter().getItemCount();

                    boolean firstCell = pos == 0;
                    boolean lastCell = pos == itemCount - 1;

                    outRect.set(inset);
                    outRect.top = firstCell ? outRect.top : 0;
                    outRect.bottom = lastCell ? outRect.bottom : 0;
                }
            }
        });
    }

    public static boolean isScrollToTop(RecyclerView recyclerView) {
        if (recyclerView == null || recyclerView.getAdapter() == null)
            return false;

        int childCount = recyclerView.getChildCount();
        if (childCount > 0) {
            View firstChild = recyclerView.getChildAt(0);
            int firstChildAdapterPos = recyclerView.getChildAdapterPosition(firstChild);
            if (firstChildAdapterPos == 0 && firstChild.getTop() >= 0) {
                return true;
            }
        }

        return false;
    }

    public static boolean isScrollToBottom(RecyclerView recyclerView) {
        if (recyclerView == null || recyclerView.getAdapter() == null)
            return false;

        int childCount = recyclerView.getChildCount();
        if (childCount > 0) {
            View lastChild = recyclerView.getChildAt(childCount - 1);
            int lastChildAdapterPos = recyclerView.getChildAdapterPosition(lastChild);
            if (lastChildAdapterPos == recyclerView.getAdapter().getItemCount() - 1) {
                return true;
            }
        }

        return false;
    }
}
