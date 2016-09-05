package com.goldmf.GMFund.widget.keyboard;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.LinearLayout;

import com.goldmf.GMFund.util.DimensionConverter;

/**
 * Created by yale on 15/8/1.
 */
public class BasePad extends LinearLayout {
    public static final int ACTION_CODE_DEL = 80001;
    private static final int[] ACTION_CODES = new int[]{ACTION_CODE_DEL};

    private boolean mIsAnimating = false;

    protected OnPadKeyActionListener mPadActionListener = OnPadKeyActionListener.NULL;

    public BasePad(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BasePad(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    public void setOnPadKeyActionListener(OnPadKeyActionListener listener) {
        mPadActionListener = listener == null ? OnPadKeyActionListener.NULL : listener;
    }


    public boolean isAnimating() {
        return mIsAnimating;
    }

    public final void toggle() {
        if (mIsAnimating) return;

        if (getTranslationY() == 0) {
            hide();
        } else {
            show();
        }
    }

    public final void show() {
        if (mIsAnimating) return;
        mIsAnimating = true;
        setVisibility(View.VISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "translationY", 0);
        animator.setDuration(200L);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mIsAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animator.start();
    }

    public final void hide() {
        if (mIsAnimating) return;
        mIsAnimating = true;

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "translationY", getHeight());
        animator.setDuration(200L);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIsAnimating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mIsAnimating = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }

    protected void onAfterKeyboardKeyRelease(int primaryCode) {
    }

    protected final boolean isActionKeyCode(int primaryCode) {
        return primaryCode > 80000 && primaryCode < 90000;
    }

    protected final int dp2px(float dp) {
        return (int) DimensionConverter.dp2px(getContext(), dp);
    }

    protected final KeyboardView.OnKeyboardActionListener createKeyboardActionListener() {
        return new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int primaryCode) {
            }

            @Override
            public void onRelease(int primaryCode) {
                playSoundEffect(SoundEffectConstants.CLICK);
                if (primaryCode == 32) {
                    // space
                    mPadActionListener.onKeyPressed((char) primaryCode);
                }
                if (primaryCode >= 48 && primaryCode <= 57) {
                    // number
                    mPadActionListener.onKeyPressed((char) primaryCode);
                } else if (primaryCode >= 65 && primaryCode <= 90) {
                    // lower letter
                    mPadActionListener.onKeyPressed((char) primaryCode);
                } else if (primaryCode >= 97 && primaryCode <= 122) {
                    // upper letter
                    mPadActionListener.onKeyPressed((char) primaryCode);
                } else if (isActionKeyCode(primaryCode)) {
                    mPadActionListener.onActionTrigger(primaryCode);
                }
                onAfterKeyboardKeyRelease(primaryCode);
            }

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
            }

            @Override
            public void onText(CharSequence text) {
            }

            @Override
            public void swipeLeft() {
            }

            @Override
            public void swipeRight() {
            }

            @Override
            public void swipeDown() {
            }

            @Override
            public void swipeUp() {
            }
        };
    }

    public interface OnPadKeyActionListener {
        void onKeyPressed(char ch);

        void onActionTrigger(int action);

        OnPadKeyActionListener NULL = new OnPadKeyActionListener() {

            @Override
            public void onKeyPressed(char ch) {
            }

            @Override
            public void onActionTrigger(int action) {
            }
        };
    }
}
