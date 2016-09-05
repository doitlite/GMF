package com.goldmf.GMFund.widget.keyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.goldmf.GMFund.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by yale on 15/8/1.
 */
public class AsciiPad extends BasePad {
    private CustomKeyboardView mKeyboardView;
    private RelativeLayout mExtraContainer;
    private TextView mHintLabel;

    public AsciiPad(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AsciiPad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // add extra view
        {
            mExtraContainer = new RelativeLayout(getContext());
            mExtraContainer.setBackgroundColor(0xFFF2F2F2);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(48));

            addView(mExtraContainer, params);
        }

        // add close image to extra view
        {
            ImageView closeImage = new ImageView(getContext());
            closeImage.setId(R.id.icon1);
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

        // add hintLabel to extra view
        {
            mHintLabel = new TextView(context);
            mHintLabel.setGravity(Gravity.CENTER);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(0, WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params.addRule(RelativeLayout.CENTER_VERTICAL);
            params.addRule(RelativeLayout.LEFT_OF, R.id.icon1);
            mExtraContainer.addView(mHintLabel, params);
        }

        // add keyboard view
        {
            mKeyboardView = (CustomKeyboardView) LayoutInflater.from(getContext()).inflate(R.layout.keyboard_ascii, this, false);
            mKeyboardView.setAnotherKeyLabels(new String[]{"ABC", "Del", "DEL", "123"});
            mKeyboardView.setKeyboard(new Keyboard(getContext(), R.xml.qwerty_ascii_num));
            mKeyboardView.setOnKeyboardActionListener(createKeyboardActionListener());
            addView(mKeyboardView);
        }
    }

    @Override
    protected void onAfterKeyboardKeyRelease(int primaryCode) {
        super.onAfterKeyboardKeyRelease(primaryCode);
        if (primaryCode == 50004) {
            mKeyboardView.setKeyboard(new Keyboard(getContext(), R.xml.qwerty_ascii_text));
        } else if (primaryCode == 50001) {
            mKeyboardView.setKeyboard(new Keyboard(getContext(), R.xml.qwerty_ascii_num));
        }
    }

    public TextView getHintLabel() {
        return mHintLabel;
    }
}
