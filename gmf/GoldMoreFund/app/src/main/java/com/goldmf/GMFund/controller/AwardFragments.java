package com.goldmf.GMFund.controller;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.business.AwardController;
import com.goldmf.GMFund.controller.dialog.ShareDialog;
import com.goldmf.GMFund.extension.UIControllerExtension;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.fortune.BountyAccount;
import com.goldmf.GMFund.manager.fortune.BountyAccount.BountyInfo;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.util.FormatUtil;
import com.goldmf.GMFund.util.PersistentObjectUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.umeng.socialize.UMShareAPI;
import com.yale.ui.support.AdvanceSwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import yale.extension.common.Optional;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_RED_COLOR;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addHorizontalSepLine;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.isScrollToTop;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;

/**
 * Created by yale on 15/10/23.
 */
public class AwardFragments {

    private AwardFragments() {
    }

    public static class AwardHomeFragmentV2 extends SimpleFragment {

        private UMShareAPI mShareAPI;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_award_home_v2, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, RED_COLOR);
            findToolbar(this).setBackgroundColor(RED_COLOR);
            setupBackButton(this, findToolbar(this));
            // bind child views
            v_setClick(mReloadSection, () -> performFetchData(true));

            performFetchData(true);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        protected boolean onInterceptActivityResult(int requestCode, int resultCode, Intent data) {
            if (UmengUtil.handleOnActivityResult(mShareAPI, requestCode, resultCode, data)) {
                mShareAPI = null;
                return true;
            }
            return super.onInterceptActivityResult(requestCode, resultCode, data);
        }

        private void performFetchData(boolean reload) {
            consumeEventMRUpdateUI(AwardController.refreshAccount(), reload)
                    .onNextSuccess(responses -> {
                        updateContent();
                    })
                    .done();
        }

        private void updateContent() {
            setupHeader();
            setupViewPager();
            setupBottomBar();
        }

        private void setupHeader() {
            BountyAccount cnAccount = FortuneManager.getInstance().cnBountyAccount;

            View header = v_findView(this, R.id.section_header);
            v_setText(header, R.id.label_left_text1, "累计获得奖励金(元)");
            v_setText(header, R.id.label_left_text2, formatMoney(cnAccount.totalAmount, false, 0, 2));
            v_setText(header, R.id.label_right_text1, "累计邀请好友(个)");
            v_setText(header, R.id.label_right_text2, "" + cnAccount.inviteNumber);
        }

        private void setupViewPager() {
            TabLayout tabLayout = v_findView(this, R.id.tabLayout);
            ViewPager pager = v_findView(this, R.id.pager);
            if (pager.getAdapter() == null) {
                FragmentPagerAdapter adapter = new FragmentPagerAdapter(getChildFragmentManager()) {

                    @Override
                    public int getCount() {
                        return 2;
                    }

                    @Override
                    public Fragment getItem(int position) {
                        if (position == 0)
                            return new WebViewFragments.WebViewFragment().init(CommonDefine.H5URL_BountyRule(), false);
                        if (position == 1)
                            return new AwardRecordFragment();
                        return null;
                    }

                    @Override
                    public CharSequence getPageTitle(int position) {
                        if (position == 0)
                            return "如何赚";
                        else if (position == 1)
                            return "记录";
                        else
                            return "未知";
                    }
                };

                pager.setAdapter(adapter);
                tabLayout.setupWithViewPager(pager);
            }
        }

        private void setupBottomBar() {

            View parent = v_findView(this, R.id.section_left_bottom);
            v_setClick(parent, R.id.btn_invite, v -> {
                ShareInfo shareInfo = FortuneManager.getInstance().shareButton.shareInfo;
                if (shareInfo != null) {
                    performInviteFriend(shareInfo);
                }
            });
        }

        private void performInviteFriend(ShareInfo shareInfoOrNil) {
            if (shareInfoOrNil == null)
                return;

            ShareDialog shareDialog = new ShareDialog(getActivity(), shareInfoOrNil);
            shareDialog.setShareItemClickEventDelegate((dialog, platform) -> {
                dialog.dismiss();

                mShareAPI = UmengUtil.createShareService(getActivity(), shareInfoOrNil.title);
                UmengUtil.performShare(getActivity(), mShareAPI, platform.shareMedia, shareInfoOrNil, null);
            });
            shareDialog.show();
            UmengUtil.stat_enter_share_from_award_page(getActivity(), Optional.of(this));
        }
    }

    public static class AwardHomeCellVM {
        public CharSequence nameAndDate = "";
        public CharSequence amountAndReason = "";
        public CharSequence state = "";

        AwardHomeCellVM(BountyInfo raw) {
            nameAndDate = concat(
                    raw.userName,
                    setColor(setFontSize(FormatUtil.formatSecond(raw.bountyTime, "yyyy.MM.dd"), sp2px(12)), TEXT_GREY_COLOR)
            );
            int reasonTextColor = parseTextColor(raw.color, TEXT_BLACK_COLOR);
            amountAndReason = concat(
                    setColor(formatMoney(raw.amount, false, 0, 2) + Money_Type.getUnit(raw.moneyType), reasonTextColor),
                    setColor(setFontSize(raw.msg, sp2px(12)), TEXT_GREY_COLOR)
            );
            state = setColor(raw.statusText, parseTextColor(raw.color, TEXT_GREY_COLOR));
        }

        private static int parseTextColor(String color, int defaultColor) {
            if (!TextUtils.isEmpty(color)) {
                if (color.equalsIgnoreCase("red"))
                    return TEXT_RED_COLOR;
            }

            return defaultColor;
        }
    }

    public static class AwardRecordFragment extends SimpleFragment {
        private RecyclerView mRecyclerView;
        private boolean mDataIsExpired = true;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_award_record, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setOnSwipeRefreshListener(() -> performFetchData(false));
            v_setClick(mReloadSection, () -> performFetchData(true));

            mRecyclerView = v_findView(this, R.id.recyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            addHorizontalSepLine(mRecyclerView);

            AdvanceSwipeRefreshLayout.class
                    .cast(mRefreshLayout)
                    .setOnPreInterceptTouchEventDelegate(ev -> !isScrollToTop(mRecyclerView));

            performFetchData(true);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (getView() != null && isVisibleToUser) {
                if (mDataIsExpired) {
                    performFetchData(false);
                    mDataIsExpired = false;
                }
            }
        }

        private void performFetchData(boolean reload) {
            consumeEventMRUpdateUI(AwardController.fetchAwardList(), reload)
                    .setTag("reload_data")
                    .onNextSuccess(response -> {
                        List<BountyInfo> data = safeGet(() -> FortuneManager.getInstance().bountyList, Collections.EMPTY_LIST);
                        if (data.isEmpty()) {
                            changeVisibleSection(TYPE_EMPTY);
                        } else {
                            List<AwardHomeCellVM> items = Stream.of(data).map(AwardHomeCellVM::new).collect(Collectors.toList());
                            setupRecyclerView(items);
                            changeVisibleSection(TYPE_CONTENT);
                        }
                    })
                    .done();
        }

        private void setupRecyclerView(List<AwardHomeCellVM> items) {
            if (mRecyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<AwardHomeCellVM> adapter = getSimpleAdapter(mRecyclerView);
                adapter.resetItems(items);
            } else {
                SimpleRecyclerViewAdapter<AwardHomeCellVM> adapter = new SimpleRecyclerViewAdapter.Builder<AwardHomeCellVM>(items)
                        .onCreateItemView(R.layout.cell_award_home_v2)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("nameAndDateLabel", R.id.label_name_and_date)
                                    .bindChildWithTag("amountAndReasonLabel", R.id.label_amount_and_reason)
                                    .bindChildWithTag("stateLabel", R.id.label_state)
                                    .configureView((item, pos) -> {
                                        v_setText(builder.getChildWithTag("nameAndDateLabel"), item.nameAndDate);
                                        v_setText(builder.getChildWithTag("amountAndReasonLabel"), item.amountAndReason);
                                        v_setText(builder.getChildWithTag("stateLabel"), item.state);
                                    });
                            return builder.create();
                        })
                        .create();
                mRecyclerView.setAdapter(adapter);
            }
        }
    }
}
