package com.goldmf.GMFund.extension;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.R;

import rx.functions.Action1;
import yale.extension.system.ImageTextSpan;
import yale.extension.system.RoundBackgroundColorSpan;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.YELLOW_COLOR;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;

/**
 * Created by yale on 15/8/5.
 */
public class SpannableStringExtension {
    private SpannableStringExtension() {
    }

    public static SpannableStringBuilder setColor(CharSequence source, int color) {
        source = opt(source).or("");
        if (source instanceof SpannableStringBuilder)
            return setColor((SpannableStringBuilder) source, color);
        else
            return setColor(new SpannableStringBuilder(source), color);
    }

    public static SpannableStringBuilder setColor(SpannableStringBuilder ss, int color) {
        ss = opt(ss).or(new SpannableStringBuilder());
        final int start = 0;
        final int end = ss.length();
        if (end > start)
            ss.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public static SpannableStringBuilder setBackgroundColor(CharSequence source, int color) {
        source = opt(source).or("");
        if (source instanceof SpannableStringBuilder)
            return setBackgroundColor((SpannableStringBuilder) source, color);
        else
            return setBackgroundColor(new SpannableStringBuilder(source), color);
    }

    public static SpannableStringBuilder setBackgroundColor(SpannableStringBuilder ss, int color) {
        ss = opt(ss).or(new SpannableStringBuilder());
        final int start = 0;
        final int end = ss.length();
        if (end > start)
            ss.setSpan(new BackgroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public static SpannableStringBuilder setRoundBackgroundColor(CharSequence source, int fgColor, int bgColor, int radius) {
        source = opt(source).or("");
        if (source instanceof SpannableStringBuilder)
            return setRoundBackgroundColor((SpannableStringBuilder) source, fgColor, bgColor, radius);
        else
            return setRoundBackgroundColor(new SpannableStringBuilder(source), fgColor, bgColor, radius);
    }

    public static SpannableStringBuilder setRoundBackgroundColor(SpannableStringBuilder ss, int fgColor, int bgColor, int radius) {
        ss = opt(ss).or(new SpannableStringBuilder());
        final int start = 0;
        final int end = ss.length();
        if (end > start)
            ss.setSpan(new RoundBackgroundColorSpan(fgColor, bgColor, radius), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public static SpannableStringBuilder setFontSize(CharSequence ss, int fontSizeInPx) {
        ss = opt(ss).or("");
        if (ss instanceof SpannableStringBuilder)
            return setFontSize((SpannableStringBuilder) ss, fontSizeInPx);
        else
            return setFontSize(new SpannableStringBuilder(ss), fontSizeInPx);
    }

    public static SpannableStringBuilder setFontSize(SpannableStringBuilder ss, int fontSizeInPx) {
        ss = opt(ss).or(new SpannableStringBuilder());
        final int start = 0;
        final int end = ss.length();
        if (end > start)
            ss.setSpan(new AbsoluteSizeSpan(fontSizeInPx), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public static SpannableStringBuilder setRelativeFontSize(CharSequence ss, float proportion) {
        ss = opt(ss).or("");
        if (ss instanceof SpannableStringBuilder) {
            return setRelativeFontSize((SpannableStringBuilder) ss, proportion);
        }

        return setRelativeFontSize(new SpannableStringBuilder(ss), proportion);
    }

    public static SpannableStringBuilder setRelativeFontSize(SpannableStringBuilder ss, float proportion) {
        ss = opt(ss).or(new SpannableStringBuilder());
        final int start = 0;
        final int end = ss.length();
        if (end > start)
            ss.setSpan(new RelativeSizeSpan(proportion), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public static SpannableStringBuilder setStyle(CharSequence ss, int style) {
        ss = opt(ss).or("");
        if (ss instanceof SpannableStringBuilder) {
            return setStyle(ss, style);
        }

        return setStyle(new SpannableStringBuilder(ss), style);
    }

    public static SpannableStringBuilder setStyle(SpannableStringBuilder ss, int style) {
        ss = opt(ss).or(new SpannableStringBuilder());
        final int start = 0;
        final int end = ss.length();
        if (end > start)
            ss.setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public static SpannableStringBuilder setClickEvent(CharSequence ss, Action1<View> clickEvent) {
        ss = opt(ss).or("");
        if (ss instanceof SpannableStringBuilder)
            return setClickEvent((SpannableStringBuilder) ss, clickEvent);

        return setClickEvent(new SpannableStringBuilder(ss), clickEvent);
    }

    public static SpannableStringBuilder setClickEvent(SpannableStringBuilder ss, Action1<View> clickEvent) {
        ss = opt(ss).or(new SpannableStringBuilder());
        final int start = 0;
        final int end = ss.length();
        if (end > start)
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    clickEvent.call(widget);
                }

                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }

    public static class ImageTextParams {
        public int fontSize = sp2px(14);
        public int maxWidth = UIControllerExtension.getScreenSize(MyApplication.SHARE_INSTANCE).width();
        public int iconResID = 0;
        public int tileResID = R.mipmap.tile_circle;
        public int bgColor = YELLOW_COLOR;
        public int textColor = TEXT_BLACK_COLOR;
        public int radius = dp2px(4);
        public RectF contentPadding = new RectF(dp2px(4), dp2px(0), dp2px(4), dp2px(0));
        public int drawablePadding = dp2px(4);
    }

    public static CharSequence appendImageText(CharSequence ss, String text, ImageTextParams params) {
        if (ss instanceof SpannableStringBuilder) {
            return appendImageText((SpannableStringBuilder) ss, text, params);
        }

        return appendImageText(new SpannableStringBuilder(ss), text, params);
    }

    public static CharSequence appendImageText(SpannableStringBuilder ss, String text, ImageTextParams params) {
        params = opt(params).or(new ImageTextParams());
        ss = opt(ss).or(new SpannableStringBuilder());
        int start = 0;
        int end = ss.length();
        if (end > start) {
            float[] measuredWidth = new float[1];
            Paint paint = new Paint();
            paint.setTextSize(params.fontSize);
            while (start < end) {
                start += paint.breakText(ss, start, end, true, params.maxWidth, measuredWidth);
            }

            float maxWidthOffset = measuredWidth[0] + params.contentPadding.left + params.contentPadding.right;
            Bitmap iconImage = params.iconResID == 0 ? null : BitmapFactory.decodeResource(MyApplication.getResource(), params.iconResID);
            if (iconImage != null) {
                maxWidthOffset += iconImage.getWidth() + params.drawablePadding;
            }

            text = text.replaceAll("\\n", "");
            start = 0;
            end = text.length();
            int measuredCharNum;
            while (start < end) {
                measuredCharNum = paint.breakText(text, start, end, true, params.maxWidth - maxWidthOffset, measuredWidth);
                CharSequence subText = text.subSequence(start, start + measuredCharNum);
                ss.append(subText);
                int iconResID = start == 0 ? params.iconResID : 0;
                int flags = start != 0 ? ImageTextSpan.FLAG_NO_LEFT_CORNER : 0;
                if (start + measuredCharNum < end) {
                    flags |= ImageTextSpan.FLAG_NO_RIGHT_CORNER;
                    flags |= ImageTextSpan.FLAG_DRAW_TO_END;
                }
                ss.setSpan(new ImageTextSpan(iconResID, params.tileResID, params.textColor, params.bgColor, params.radius, params.contentPadding, params.drawablePadding, flags), ss.length() - subText.length(), ss.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                start += measuredCharNum;
                maxWidthOffset = params.contentPadding.right + params.drawablePadding;
            }
        }
        return ss;
    }

    public static SpannableStringBuilder concat(CharSequence first, CharSequence... others) {
        first = opt(first).or("");
        if (first instanceof SpannableStringBuilder)
            return concat((SpannableStringBuilder) first, others);

        return concat(new SpannableStringBuilder(first), others);
    }

    public static SpannableStringBuilder concat(SpannableStringBuilder first, CharSequence... others) {
        for (CharSequence other : others)
            if (other != null) first.append("\n").append(other);
        return first;

    }

    public static SpannableStringBuilder concatNoBreak(CharSequence first, CharSequence... others) {
        if (first instanceof SpannableStringBuilder)
            return concatNoBreak((SpannableStringBuilder) first, others);

        return concatNoBreak(new SpannableStringBuilder(first), others);
    }

    public static SpannableStringBuilder concatNoBreak(SpannableStringBuilder first, CharSequence... others) {
        for (CharSequence other : others)
            if (other != null) first.append(other);
        return first;
    }
}
