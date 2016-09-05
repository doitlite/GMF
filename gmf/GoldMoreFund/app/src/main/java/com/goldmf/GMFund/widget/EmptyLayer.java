package com.goldmf.GMFund.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.goldmf.GMFund.R;

import yale.extension.common.Optional;

/**
 * Created by yale on 16/3/17.
 */
public class EmptyLayer extends LinearLayout {
    private ImageView mIconImage;
    private TextView mTitleLabel;
    private TextView mSubTitleLabel;

    public EmptyLayer(Context context) {
        this(context, null);
    }

    public EmptyLayer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyLayer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setId(R.id.section_empty);
        setOrientation(VERTICAL);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.EmptyLayer, defStyleAttr, 0);

        String titleText = Optional.of(arr.getString(R.styleable.EmptyLayer_title)).or("没有找到内容");
        String subTitleText = Optional.of(arr.getString(R.styleable.EmptyLayer_subtitle)).or("");

        arr.recycle();

        mIconImage = new ImageView(context);
        mIconImage.setImageResource(R.mipmap.ic_warning);
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            addView(mIconImage, params);
        }

        mTitleLabel = new TextView(context);
        mTitleLabel.setId(R.id.label_title);
        mTitleLabel.setTextColor(getResources().getColor(R.color.gmf_text_grey));
        mTitleLabel.setTextSize(16);
        mTitleLabel.setText(titleText);
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            addView(mTitleLabel, params);
        }

        mSubTitleLabel = new TextView(context);
        mSubTitleLabel.setId(R.id.label_subtitle);
        mSubTitleLabel.setTextColor(getResources().getColor(R.color.gmf_text_grey));
        mSubTitleLabel.setTextSize(12);
        mSubTitleLabel.setText(subTitleText);
        {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            addView(mSubTitleLabel, params);
        }
    }

    public void setTitle(CharSequence title) {
        mTitleLabel.setText(title);
    }

    public void setSubTitle(CharSequence subTitle) {
        mSubTitleLabel.setText(subTitle);
    }
}
