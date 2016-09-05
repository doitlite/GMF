package com.goldmf.GMFund.controller;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.transition.ChangeBounds;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.TransitionSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import com.goldmf.GMFund.R;
import com.goldmf.GMFund.extension.ViewExtension;
import com.orhanobut.logger.Logger;

import java.util.Stack;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

import static com.goldmf.GMFund.extension.ObjectExtension.*;
import static com.goldmf.GMFund.extension.UIControllerExtension.getStatusBarHeight;
import static com.goldmf.GMFund.extension.UIControllerExtension.hideKeyboardFromWindow;

/**
 * Created by yale on 15/7/30.
 */
public class FragmentStackActivity extends BaseActivity {
    public Stack<BaseFragment> mFragmentStack = new Stack<>();

    @Override
    protected boolean logLifeCycleEvent() {
        return false;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        addExtraWindowOffsetIfNeeded();
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        addExtraWindowOffsetIfNeeded();
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
        addExtraWindowOffsetIfNeeded();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!mFragmentStack.isEmpty()) {
            if (mFragmentStack.peek().onInterceptKeyDown(keyCode, event))
                return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!mFragmentStack.isEmpty()) {
            mFragmentStack.peek().onInterceptActivityResult(requestCode, resultCode, data);
        }
    }

    protected void addExtraWindowOffsetIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            View rootView = ((ViewGroup) getWindow().getDecorView()).getChildAt(0);
            if (rootView != null) {
                ViewGroup.LayoutParams params = rootView.getLayoutParams();
                if (params instanceof ViewGroup.MarginLayoutParams) {
                    int statusBarHeight = getStatusBarHeight(this);
                    ((ViewGroup.MarginLayoutParams) params).topMargin = -statusBarHeight;
                    rootView.setLayoutParams(params);
                }
            }
        }
    }

    public BaseFragment peekTopFragmentOrNil() {
        return mFragmentStack.isEmpty() ? null : mFragmentStack.peek();
    }

    public static void goBack(BaseFragment currFragment) {
        if (currFragment.getActivity() instanceof FragmentStackActivity) {
            FragmentStackActivity activity = (FragmentStackActivity) currFragment.getActivity();
            activity.goBack();
        } else {
            if (!currFragment.onInterceptGoBack()) {
                currFragment.getActivity().finish();
            }
        }
    }

    public static void pushFragment(BaseFragment currFragment, BaseFragment newFragment) {
        FragmentStackActivity activity = (FragmentStackActivity) currFragment.getActivity();
        activity.pushFragment(newFragment, TRANSACTION_DIRECTION.DEFAULT);
    }

    public static void pushFragment(BaseFragment currFragment, BaseFragment newFragment, TRANSACTION_DIRECTION direction) {
        FragmentStackActivity activity = (FragmentStackActivity) currFragment.getActivity();
        activity.pushFragment(newFragment, direction);
    }

    public static void replaceTopFragment(BaseFragment currFragment, BaseFragment newFragment) {
        FragmentStackActivity activity = (FragmentStackActivity) currFragment.getActivity();
        activity.replaceTopFragment(newFragment, TRANSACTION_DIRECTION.DEFAULT);
    }

    public static void replaceTopFragment(BaseFragment currFragment, BaseFragment newFragment, TRANSACTION_DIRECTION direction) {
        FragmentStackActivity activity = (FragmentStackActivity) currFragment.getActivity();
        activity.replaceTopFragment(newFragment, direction);
    }

    public static void resetFragment(BaseFragment currFragment, BaseFragment newFragment) {
        FragmentStackActivity activity = (FragmentStackActivity) currFragment.getActivity();
        activity.resetFragment(newFragment, TRANSACTION_DIRECTION.DEFAULT);
    }

    public static void resetFragment(BaseFragment currFragment, BaseFragment newFragment, TRANSACTION_DIRECTION direction) {
        FragmentStackActivity activity = (FragmentStackActivity) currFragment.getActivity();
        activity.resetFragment(newFragment, direction);
    }

    public int getFragmentContainerId() {
        return R.id.container_fragment;
    }

    public void goBack() {
        if (!mFragmentStack.isEmpty() && mFragmentStack.peek().onInterceptGoBack()) {
            return;
        }

        if (fragmentCount() > 1 && !mFragmentStack.peek().mForceFinishOnGoBack) {
            popFragmentOrNil();
        } else {
            if (!mFragmentStack.isEmpty()) {
                mFragmentStack.peek().setUserVisibleHint(false);
            }
            mFragmentStack.clear();
            finish();
        }
    }

    public void pushFragment(BaseFragment fragment) {
        TRANSACTION_DIRECTION direction = safeGet(() -> (TRANSACTION_DIRECTION) getIntent().getSerializableExtra(KEY_TRANSACTION_DIRECTION), null);
        pushFragment(fragment, direction);
    }

    public void pushFragment(BaseFragment fragment, @Nullable TRANSACTION_DIRECTION direction) {

        hideKeyboardFromWindow(this);
        fragment.setExtraData(direction);

        BaseFragment prevFragmentOrNil = mFragmentStack.isEmpty() ? null : mFragmentStack.peek();
        if (prevFragmentOrNil != null) {
            prevFragmentOrNil.setUserVisibleHint(false);
//            getSupportFragmentManager().beginTransaction().hide(mFragmentStack.peek()).commit();
        }

        mFragmentStack.push(fragment);
        // 至少已经有一个Fragment的时候才会添加转换动画
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mFragmentStack.size() > 1) {
            if (direction == TRANSACTION_DIRECTION.DEFAULT) {
                transaction.setCustomAnimations(android.support.design.R.anim.abc_grow_fade_in_from_bottom, R.anim.stay);
            } else if (direction == TRANSACTION_DIRECTION.VERTICAL) {
                transaction.setCustomAnimations(android.support.design.R.anim.abc_slide_in_bottom, R.anim.stay);
            } else if (direction == TRANSACTION_DIRECTION.NONE) {
                transaction.setCustomAnimations(0, 0);
            }

        } else {
            transaction.setCustomAnimations(0, 0, 0, 0);
        }
        transaction.add(getFragmentContainerId(), fragment);
        if (prevFragmentOrNil != null) {
            transaction.hide(prevFragmentOrNil);
        }
        transaction.commit();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void pushFragmentWithShareElement(BaseFragment fragment, View shareElement, String transitionName) {
        hideKeyboardFromWindow(this);

        BaseFragment prevFragmentOrNil = mFragmentStack.isEmpty() ? null : mFragmentStack.peek();
        if (prevFragmentOrNil != null) {
            prevFragmentOrNil.setUserVisibleHint(false);
        }

        mFragmentStack.push(fragment);
        // 至少已经有一个Fragment的时候才会添加转换动画


        TransitionSet shareElementTransaction = new TransitionSet();
        shareElementTransaction.addTransition(new ChangeBounds());
        shareElementTransaction.addTransition(new ChangeTransform());

        fragment.setSharedElementEnterTransition(shareElementTransaction);
        fragment.setSharedElementReturnTransition(shareElementTransaction);

        fragment.setEnterTransition(new Fade(Fade.IN));
        fragment.setReturnTransition(new Fade(Fade.OUT));

        shareElement.setTransitionName(transitionName);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addSharedElement(shareElement, transitionName);
        transaction.add(getFragmentContainerId(), fragment);
        if (prevFragmentOrNil != null) {
            transaction.hide(prevFragmentOrNil);
        }
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void replaceTopFragment(BaseFragment fragment) {
        replaceTopFragment(fragment, TRANSACTION_DIRECTION.DEFAULT);
    }

    public void replaceTopFragment(BaseFragment fragment, TRANSACTION_DIRECTION direction) {
        hideKeyboardFromWindow(this);
        fragment.setExtraData(direction);
        final int count = fragmentCount();
        if (count == 0) {
            pushFragment(fragment, direction);
        } else {
            Fragment previousFragment = mFragmentStack.pop();
            previousFragment.setUserVisibleHint(false);
            mFragmentStack.push(fragment);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (direction == TRANSACTION_DIRECTION.DEFAULT) {
                transaction.setCustomAnimations(android.support.design.R.anim.abc_grow_fade_in_from_bottom, R.anim.stay);
            } else if (direction == TRANSACTION_DIRECTION.VERTICAL) {
                transaction.setCustomAnimations(android.support.design.R.anim.abc_slide_in_bottom, R.anim.stay);
            } else if (direction == TRANSACTION_DIRECTION.NONE) {
                transaction.setCustomAnimations(0, 0);
            }
            transaction.add(getFragmentContainerId(), fragment);
            transaction.commit();

            consumeEvent(Observable.empty().delaySubscription(300, TimeUnit.MILLISECONDS))
                    .onComplete(() -> getSupportFragmentManager().beginTransaction().remove(previousFragment).commit())
                    .done();
        }
    }

    public void resetFragment(BaseFragment fragment) {
        resetFragment(fragment, TRANSACTION_DIRECTION.DEFAULT);
    }

    public void resetFragment(BaseFragment fragment, TRANSACTION_DIRECTION direction) {
        hideKeyboardFromWindow(this);
        fragment.setExtraData(direction);
        final int count = fragmentCount();
        if (count == 0) {
            pushFragment(fragment, direction);
        } else {
            mFragmentStack.peek().setUserVisibleHint(false);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (direction == TRANSACTION_DIRECTION.DEFAULT) {
                transaction.setCustomAnimations(android.support.design.R.anim.abc_grow_fade_in_from_bottom, R.anim.stay);
            } else if (direction == TRANSACTION_DIRECTION.VERTICAL) {
                transaction.setCustomAnimations(android.support.design.R.anim.abc_slide_in_bottom, R.anim.stay);
            } else if (direction == TRANSACTION_DIRECTION.NONE) {
                transaction.setCustomAnimations(0, 0);
            }
            transaction.add(getFragmentContainerId(), fragment);
            transaction.commit();
            mFragmentStack.push(fragment);

            consumeEvent(Observable.empty().delaySubscription(300, TimeUnit.MILLISECONDS))
                    .onComplete(() -> {
                        if (mFragmentStack.size() > 1) {
                            BaseFragment topFragment = mFragmentStack.pop();

                            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                            while (!mFragmentStack.isEmpty()) {
                                t.remove(mFragmentStack.pop());
                            }
                            t.commit();
                            mFragmentStack.push(topFragment);
                        }
                    })
                    .done();
        }
    }

    @Override
    protected boolean isTracePageLifeRecycle() {
        return false;
    }

    protected Fragment popFragmentOrNil() {
        if (fragmentCount() == 0)
            return null;

        BaseFragment removeFragment = mFragmentStack.pop();
        removeFragment.setUserVisibleHint(false);

        BaseFragment topFragmentOrNil = mFragmentStack.isEmpty() ? null : mFragmentStack.peek();

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            TRANSACTION_DIRECTION direction = (TRANSACTION_DIRECTION) removeFragment.getExtraData();
            if (direction == null || direction == TRANSACTION_DIRECTION.DEFAULT) {
                transaction.setCustomAnimations(R.anim.stay, android.support.design.R.anim.abc_shrink_fade_out_from_bottom);
            } else if (direction == TRANSACTION_DIRECTION.VERTICAL) {
                transaction.setCustomAnimations(R.anim.stay, android.support.design.R.anim.abc_slide_out_bottom);
            } else if (direction == TRANSACTION_DIRECTION.NONE) {
                transaction.setCustomAnimations(0, 0);
            }
            transaction.remove(removeFragment);
            if (topFragmentOrNil != null) {
                transaction.show(topFragmentOrNil);
            }
            transaction.commit();
        }

        if (topFragmentOrNil != null) {
            topFragmentOrNil.setUserVisibleHint(true);
        }
        return removeFragment;
    }

    protected int fragmentCount() {
        return mFragmentStack.size();
    }

}
