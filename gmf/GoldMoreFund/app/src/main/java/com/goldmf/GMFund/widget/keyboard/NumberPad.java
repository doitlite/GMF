package com.goldmf.GMFund.widget.keyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.goldmf.GMFund.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by yale on 15/8/1.
 */
public class NumberPad extends BasePad {
    private CustomKeyboardView mKeyboardView;
    private RelativeLayout mExtraContainer;

    public NumberPad(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumberPad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // add extra view
        {
            mExtraContainer = new RelativeLayout(getContext());
            mExtraContainer.setBackgroundColor(0xFFF2F2F2);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(44));

            addView(mExtraContainer, params);
        }

        // add close image to extra view
        {
            ImageView closeImage = new ImageView(getContext());
            closeImage.setImageResource(R.mipmap.ic_close_keyboard);
            closeImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    hide();
                }
            });
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            params.rightMargin = dp2px(16);

            mExtraContainer.addView(closeImage, params);
        }

        // add keyboard view
        {
            mKeyboardView = (CustomKeyboardView) LayoutInflater.from(getContext()).inflate(R.layout.keyboard_num, this, false);
            mKeyboardView.setAnotherKeyLabels(new String[]{"", "Del"});
            mKeyboardView.setKeyboard(new Keyboard(getContext(), R.xml.qwerty_number));
            mKeyboardView.setOnKeyboardActionListener(createKeyboardActionListener());
            addView(mKeyboardView);
        }
    }
}
