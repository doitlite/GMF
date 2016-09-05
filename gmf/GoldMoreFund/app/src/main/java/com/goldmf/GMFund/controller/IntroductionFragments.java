package com.goldmf.GMFund.controller;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.annimon.stream.Stream;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.internal.ActivityNavigation;

import rx.Observable;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_LAUNCH_MAIN_ACTIVITY_BOOLEAN;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setInvisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;

/**
 * Created by yale on 15/10/17.
 */
public class IntroductionFragments {
    private IntroductionFragments() {
    }

    /**
     * Created by yale on 15/9/25.
     */
    public static class PPTHostFragment extends BaseFragment {

        private boolean mLaunchMainActivity = false;
        private Button mLeftButton;
        private Button mRightButton;

        public PPTHostFragment init(boolean launchMainActivity) {
            Bundle arguments = new Bundle();
            arguments.putBoolean(KEY_LAUNCH_MAIN_ACTIVITY_BOOLEAN, launchMainActivity);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mLaunchMainActivity = getArguments().getBoolean(KEY_LAUNCH_MAIN_ACTIVITY_BOOLEAN);
            return inflater.inflate(R.layout.frag_ppt_host, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            makeStatusBarTransparent();

            // bind child views
            mLeftButton = v_findView(view, R.id.btn_left);
            mRightButton = v_findView(view, R.id.btn_right);

            // init child views
            mLeftButton.setText("跳过");
            mRightButton.setText("下一步");
            ViewPager viewPager = v_findView(this, R.id.pager);
            IntroductionHostPagerAdapter adapter = new IntroductionHostPagerAdapter(getChildFragmentManager());
            viewPager.setAdapter(adapter);
            resetIndicatorSection(viewPager.getAdapter().getCount());
            makeIndicatorSelected(0);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                }

                @Override
                public void onPageSelected(int position) {
                    makeIndicatorSelected(position);
                    if (position == adapter.getCount() - 1) {
                        mRightButton.setText("立即体验");
                        v_setInvisible(mLeftButton);
                        view.setBackgroundColor(Color.parseColor("#3498DB"));
                    } else if (position == 0) {
                        v_setVisible(mLeftButton);
                        mRightButton.setText("下一步");
                        view.setBackgroundColor(Color.parseColor("#D73415"));
                    } else if (position == 1) {
                        v_setVisible(mLeftButton);
                        mRightButton.setText("下一步");
                        view.setBackgroundColor(Color.parseColor("#FFDE00"));
                    } else {
                        v_setVisible(mLeftButton);
                        mRightButton.setText("下一步");
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                }
            });
            v_setClick(mLeftButton, v -> {
                if (mLaunchMainActivity) {
                    ActivityNavigation.showActivity(this, ActivityNavigation.an_MainPage(null));
                }
                getActivity().finish();
            });
            v_setClick(mRightButton, v -> {
                if (viewPager.getCurrentItem() >= adapter.getCount() - 1) {
                    if (mLaunchMainActivity) {
                        ActivityNavigation.showActivity(this, ActivityNavigation.an_MainPage(null));
                    }
                    getActivity().finish();
                } else {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                }
            });
        }

        @Override
        protected boolean onInterceptKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return true;
            } else {
                return super.onInterceptKeyDown(keyCode, event);
            }
        }

        private void makeStatusBarTransparent() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }

        private void resetIndicatorSection(int count) {
            LinearLayout container = v_findView(this, R.id.section_indicator);
            container.removeAllViewsInLayout();
            if (count <= 0)
                return;

            Stream.range(0, count)
                    .map(index -> {
                        ImageView childView = new ImageView(getActivity());
                        childView.setImageResource(R.drawable.sel_circle_indicator);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.leftMargin = index == 0 ? 0 : dp2px(8);
                        childView.setLayoutParams(params);
                        return childView;
                    })
                    .forEach(container::addView);
        }

        private void makeIndicatorSelected(int selectedIndex) {
            LinearLayout container = v_findView(this, R.id.section_indicator);
            if (container.getChildCount() <= 0)
                return;

            Observable.range(0, container.getChildCount())
                    .subscribe(index -> container.getChildAt(index).setSelected(index == selectedIndex));
        }

    }

    static class IntroductionHostPagerAdapter extends FragmentPagerAdapter {

        public IntroductionHostPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return new StaticIntroductionFragment().init(R.layout.frag_ppt_1);
            else if (position == 1)
                return new StaticIntroductionFragment().init(R.layout.frag_ppt_2);
            else if (position == 2)
                return new StaticIntroductionFragment().init(R.layout.frag_ppt_3);

            return new StaticIntroductionFragment().init(R.layout.frag_ppt_3);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    public static class StaticIntroductionFragment extends BaseFragment {
        private int mLayoutId;

        public StaticIntroductionFragment init(int layoutId) {
            Bundle arguments = new Bundle();
            arguments.putInt("layout_id", layoutId);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mLayoutId = getArguments().getInt("layout_id");
            return inflater.inflate(mLayoutId, container, false);
        }
    }

    public static class FullScreenImageFragment extends BaseFragment {
        private int mImageResId;

        public FullScreenImageFragment init(int imgResId) {
            Bundle arguments = new Bundle();
            arguments.putInt("img_res_id", imgResId);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mImageResId = getArguments().getInt("img_res_id");
            return inflater.inflate(R.layout.frag_fullscreen_image, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            ImageView fullscreenImage = v_findView(this, R.id.img_fullscreen);
            fullscreenImage.setImageResource(mImageResId);
        }
    }
}
