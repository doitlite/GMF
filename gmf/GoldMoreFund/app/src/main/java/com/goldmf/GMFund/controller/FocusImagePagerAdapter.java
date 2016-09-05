package com.goldmf.GMFund.controller;

import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.manager.discover.DiscoverManager;
import com.goldmf.GMFund.manager.discover.FocusInfo;
import com.goldmf.GMFund.manager.discover.TotalInfo;

import java.io.Serializable;
import java.util.List;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.extension.SpannableStringExtension.*;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.util.FormatUtil.formatBiggerNumber;
import static com.goldmf.GMFund.util.FormatUtil.formatRatio;

/**
 * Created by yale on 15/9/24.
 */
public class FocusImagePagerAdapter extends FragmentStatePagerAdapter {


    private List<FocusImageCellVM> mDataSet;

    public FocusImagePagerAdapter(FragmentManager fm, List<FocusImageCellVM> items) {
        super(fm);
        mDataSet = items;
    }

    @Override
    public Fragment getItem(int position) {
        position = position % mDataSet.size();
        FocusImageCellVM item = mDataSet.get(position);
        if (!item.hasData) {
            return new FocusImageFragment().init(item);
        } else {
            return new PlatformInfoFocusImageFragment().init(item);
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mDataSet.size() * 1000;
    }

    public static class FocusImageFragment extends BaseFragment {

        private FocusImageCellVM mData;

        public FocusImageFragment init(FocusImageCellVM info) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(FocusImageCellVM.class.getSimpleName(), info);
            setArguments(arguments);
            return this;
        }

        @Override
        protected boolean logLifeCycleEvent() {
            return false;
        }

        @Override
        protected boolean isTraceLifeCycle() {
            return false;
        }

        @Override
        protected boolean isDelegateLifeCycleEventToSetUserVisible() {
            return false;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mData = (FocusImageCellVM) getArguments().getSerializable(FocusImageCellVM.class.getSimpleName());

            GenericDraweeHierarchyBuilder hierarchyBuilder = new GenericDraweeHierarchyBuilder(getResources());
            GenericDraweeHierarchy hierarchy = hierarchyBuilder
                    .setPlaceholderImage(getResources().getDrawable(R.mipmap.ic_focus_placeholder)).setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP).build();
            SimpleDraweeView view = new SimpleDraweeView(inflater.getContext());
            view.setHierarchy(hierarchy);
            view.setImageURI(Uri.parse(mData.imageURL));
            return view;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            SimpleDraweeView imageView = (SimpleDraweeView) view;
            imageView.setImageURI(Uri.parse(mData.imageURL));
            v_setClick(view, v -> CMDParser.parse(mData.link).call(getActivity()));
        }
    }

    public static class PlatformInfoFocusImageFragment extends BaseFragment {

        private FocusImageCellVM mData;

        public PlatformInfoFocusImageFragment init(FocusImageCellVM viewModel) {
            Bundle argument = new Bundle();
            argument.putSerializable(FocusImageCellVM.class.getSimpleName(), viewModel);
            setArguments(argument);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mData = (FocusImageCellVM) getArguments().getSerializable(FocusImageCellVM.class.getSimpleName());
            return inflater.inflate(R.layout.frag_platform_focus_image, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            v_setImageUri(view, R.id.img_focus, mData.imageURL);
            v_setText(view, R.id.label_invested_count, mData.investedCount);
            v_setText(view, R.id.label_invested_money, mData.investedMoney);
            v_setText(view, R.id.label_total_trader, mData.totalTrader);
            v_setText(view, R.id.label_total_fund, mData.totalFund);
            v_setText(view, R.id.label_max_income_ratio, mData.maxIncomeRatio);
            v_setText(view, R.id.label_runnging_fund, mData.runningFund);
        }

        @Override
        protected boolean logLifeCycleEvent() {
            return false;
        }

        @Override
        protected boolean isTraceLifeCycle() {
            return false;
        }

        @Override
        protected boolean isDelegateLifeCycleEventToSetUserVisible() {
            return false;
        }
    }

    public static class FocusImageCellVM implements Serializable {
        public final String imageURL;
        public final String link;
        public final boolean showTotal;
        public boolean hasData = true;
        public transient final CharSequence investedCount;
        public transient final CharSequence investedMoney;
        public transient final CharSequence totalTrader;
        public transient final CharSequence totalFund;
        public transient final CharSequence maxIncomeRatio;
        public transient final CharSequence runningFund;

        public FocusImageCellVM(FocusInfo info) {
            imageURL = info.imageUrl;
            link = info.tarLink;
            showTotal = info.bShowTotal;
            if (!info.bShowTotal) {
                investedCount = "已为 " + 0 + " 人次投资管理";
                investedMoney = setStyle(setColor(setFontSize("-元", sp2px(24)), TEXT_BLACK_COLOR), Typeface.BOLD);
                totalTrader = 0 + "位操盘手";
                totalFund = "累计创建" + 0 + "个组合";
                maxIncomeRatio = "最高收益 " + formatRatio(null, false, 2);
                runningFund = 0 + "个组合运行中";
                hasData = false;
            } else {
                TotalInfo totalInfo = DiscoverManager.getInstance().totalInfo;
                if (totalInfo == null) {
                    investedCount = "已为 " + 0 + " 人次投资管理";
                    investedMoney = setStyle(setColor(setFontSize(formatBiggerNumber(null, false, 2, 2, true) + "元", sp2px(24)), TEXT_BLACK_COLOR), Typeface.BOLD);
                    totalTrader = 0 + "位操盘手";
                    totalFund = "累计创建" + 0 + "个组合";
                    maxIncomeRatio = "最高收益 " + formatRatio(null, false, 2);
                    runningFund = 0 + "个组合运行中";
                    hasData = false;
                } else {
                    investedCount = "已为 " + totalInfo.investCount + " 人次投资管理";
                    investedMoney = setStyle(setColor(setFontSize(formatBiggerNumber(totalInfo.investMoney, false, 2, 2, true), sp2px(24)), TEXT_BLACK_COLOR), Typeface.BOLD);
                    totalTrader = totalInfo.totalTrader + "位操盘手";
                    totalFund = "累计创建" + totalInfo.totalFund + "个组合";
                    maxIncomeRatio = "最高收益 " + formatRatio(totalInfo.maxIncomeRatio, false, 2);
                    runningFund = totalInfo.runningFund + "个组合运行中";
                    hasData = true;
                }
            }
        }
    }
}
