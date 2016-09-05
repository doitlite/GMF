package com.goldmf.GMFund.controller;

import android.app.Dialog;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.controller.business.CashController;
import com.goldmf.GMFund.controller.business.ScoreController;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.dialog.GainScoreDialog;
import com.goldmf.GMFund.controller.dialog.SigningDialog;
import com.goldmf.GMFund.controller.internal.ActivityNavigation;
import com.goldmf.GMFund.extension.StringExtension;
import com.goldmf.GMFund.manager.cashier.CashierManager;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo;
import com.goldmf.GMFund.manager.score.ScoreAccount;
import com.goldmf.GMFund.manager.score.ScoreAccount.ScoreAccountInfo;
import com.goldmf.GMFund.manager.score.ScoreAccount.ScoreAction;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.CommonDefine.PlaceHolder;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.util.FormatUtil;
import com.goldmf.GMFund.util.GlobalVariableDic;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.yale.ui.support.AdvanceSwipeRefreshLayout;

import java.lang.ref.WeakReference;
import java.util.List;

import rx.subjects.PublishSubject;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_TAB_IDX_INT;
import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.FragmentStackActivity.replaceTopFragment;
import static com.goldmf.GMFund.controller.business.ScoreController.refreshDayAction;
import static com.goldmf.GMFund.controller.business.ScoreController.refreshScoreAccount;
import static com.goldmf.GMFund.controller.business.ScoreController.refreshScoreActions;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_RechargePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WebViewPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.YELLOW_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.IntExtension.anyMatch;
import static com.goldmf.GMFund.extension.IntExtension.notMatch;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addCellVerticalSpacing;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addHorizontalSepLine;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.isScrollToTop;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisibility;
import static com.goldmf.GMFund.manager.score.ScoreAccount.ScoreAction.ScoreAction_status_close;
import static com.goldmf.GMFund.manager.score.ScoreAccount.ScoreAction.ScoreAction_status_noFinish;
import static com.goldmf.GMFund.manager.score.ScoreAccount.ScoreAction.ScoreAction_status_receive;
import static com.goldmf.GMFund.manager.score.ScoreAccount.ScoreAction.ScoreAction_type_SignIn;
import static com.goldmf.GMFund.manager.score.ScoreAccount.ScoreAction.ScoreAction_type_normal;
import static com.goldmf.GMFund.util.FormatUtil.formatBigNumber;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * Created by Evan on 16/2/24 下午8:30.
 */
public class ScoreFragments {

    public static PublishSubject<Pair<Integer, String>> sWebBuyScoreSuccessSubject = PublishSubject.create();

    private ScoreFragments() {
    }

    public static class ScoreHomeFragment extends SimpleFragment {

        private int mInitTabIdx;

        private TabLayout mTabLayout;
        private ViewPager mPager;
        private FragmentPagerAdapter mAdapter;

        public ScoreHomeFragment init(int initTabIdx) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_TAB_IDX_INT, initTabIdx);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            UmengUtil.stat_click_event(UmengUtil.eEVENTIDXiabiEnter);

            mInitTabIdx = getArguments().getInt(KEY_TAB_IDX_INT);
            return inflater.inflate(R.layout.frag_score_home, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, RED_COLOR);
            findToolbar(this).setBackgroundColor(RED_COLOR);
            setupBackButton(this, findToolbar(this));
            v_setClick(findToolbar(this), R.id.btn_score_introduce, v -> {
                UmengUtil.stat_click_event(UmengUtil.eEVENTIDXiabiIntro);
                showActivity(this, an_WebViewPage(CommonDefine.H5URL_GCOIN_EXPLAIN()));
            });

            v_setClick(mReloadSection, v -> {
                fetchData(true);
            });

            consumeEvent(NotificationCenter.scoreChangedSubject)
                    .setTag("score_changed")
                    .onNextFinish(response -> {
                        fetchData(false);
                    })
                    .done();

            fetchData(true);
        }

        private void fetchData(boolean reload) {
            consumeEventMRUpdateUI(refreshScoreAccount(), reload)
                    .setTag("reload_data")
                    .onNextSuccess(response -> updateContent(response.data))
                    .done();
        }

        private void updateContent(ScoreAccount data) {
            // init headerSection
            View header = v_findView(this, R.id.header_award_or_score);
            v_setText(header, R.id.label_left_text1, "当前可用积分");
            v_setText(header, R.id.label_left_text2, FormatUtil.formatMoney(data.cashBalance, false, 0));
            v_setText(header, R.id.label_right_text1, "累计获得积分");
            v_setText(header, R.id.label_right_text2, FormatUtil.formatMoney(data.totalAmount, false, 0));

            // init pager
            mTabLayout = v_findView(this, R.id.tabLayout);
            mPager = v_findView(this, R.id.pager);
            if (mPager.getAdapter() == null) {
                mAdapter = new FragmentPagerAdapter(getChildFragmentManager()) {

                    @Override
                    public int getCount() {
                        return 3;
                    }

                    @Override
                    public Fragment getItem(int position) {
                        if (position == 0)
                            return new ScoreObtainFragment();
                        if (position == 1)
                            //                        return new ScoreConvertibleFragment().init(CommonDefine.H5URL_XiaBiSpend());
                            return new WebViewFragments.WebViewFragment().init(CommonDefine.H5URL_XiaBiSpend(), false);
                        if (position == 2)
                            return new ScoreRecordFragment();
                        return null;
                    }

                    @Override
                    public CharSequence getPageTitle(int position) {
                        if (position == 0)
                            return "如何赚";
                        else if (position == 1)
                            return "怎么花";
                        else if (position == 2)
                            return "记录";
                        else
                            return "未知";
                    }
                };

                mPager.setAdapter(mAdapter);
                mTabLayout.setupWithViewPager(mPager);
                mPager.setCurrentItem(min(max(mInitTabIdx, 0), mPager.getAdapter().getCount() - 1));
            }
        }

    }

    public static class ScoreObtainFragment extends SimpleFragment {

        private RecyclerView mRecyclerView;
        private boolean mDataIsExpired = true;

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
            return inflater.inflate(R.layout.frag_score_obtain, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            setOnSwipeRefreshListener(() -> fetchData(false));
            mRecyclerView = v_findView(mContentSection, R.id.recyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

            fetchData(true);

            consumeEvent(NotificationCenter.scoreChangedSubject)
                    .setTag("gain_score")
                    .onNextFinish(ignored -> {
                        mDataIsExpired = true;
                        if (getUserVisibleHint() && getView() != null) {
                            fetchData(false);
                        }
                    })
                    .done();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mDataIsExpired = true;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mDataIsExpired) {
                    fetchData(false);
                }
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            if (getUserVisibleHint() && getView() != null) {
                if (mDataIsExpired) {
                    fetchData(false);
                }
            }
        }

        private void fetchData(boolean reload) {
            consumeEventMRUpdateUI(refreshScoreActions(), reload)
                    .setTag("reload_data")
                    .onNextSuccess(response -> {
                        List<ScoreActionInfo> data = Stream.of(response.data).map(ScoreActionInfo::new).collect(Collectors.toList());
                        updateContent(data);
                    })
                    .done();

            mDataIsExpired = false;
        }

        private void updateContent(List<ScoreActionInfo> items) {
            if (mRecyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<ScoreActionInfo> adapter = getSimpleAdapter(mRecyclerView);
                adapter.resetItems(items);
            } else {
                addCellVerticalSpacing(mRecyclerView, dp2px(10));
                SimpleRecyclerViewAdapter<ScoreActionInfo> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView(R.layout.cell_score_action)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("iconImage", R.id.img_icon)
                                    .bindChildWithTag("titleLabel", R.id.label_title)
                                    .bindChildWithTag("briefLabel", R.id.label_brief)
                                    .bindChildWithTag("scoreLabel", R.id.label_score)
                                    .bindChildWithTag("actionBtn", R.id.btn_action)
                                    .configureView((item, pos) -> {
                                        SimpleDraweeView draweeView = builder.getChildWithTag("iconImage");
                                        v_setImageUri(draweeView, item.actionImageUrl);
                                        if (item.actionStatus == ScoreAction_status_close) {
                                            ColorMatrix matrix = new ColorMatrix();
                                            matrix.setSaturation(0);
                                            draweeView.setColorFilter(new ColorMatrixColorFilter(matrix));
                                        } else {
                                            draweeView.setColorFilter(null);
                                        }
                                        v_setText(builder.getChildWithTag("titleLabel"), item.actionTitle);
                                        v_setText(builder.getChildWithTag("briefLabel"), item.actionTip);
                                        TextView scoreLabel = builder.getChildWithTag("scoreLabel");
                                        v_setText(scoreLabel, item.score);
                                        v_setVisibility(scoreLabel, item.hasScore ? View.VISIBLE : View.GONE);
                                        scoreLabel.setAlpha(notMatch(item.actionStatus, ScoreAction_status_close) ? 1.0f : 0.2f);
                                        TextView actionBtn = builder.getChildWithTag("actionBtn");
                                        v_setText(actionBtn, item.buttonName);
                                        actionBtn.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(item.buttonColor, dp2px(2))));
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            v_setClick(holder.itemView, () -> {
                                ScoreActionInfo item = ad.getItem(holder.getAdapterPosition());
                                if (anyMatch(item.actionStatus, ScoreAction_status_noFinish)) {
                                    UmengUtil.stat_click_event(UmengUtil.eEVENTIDXiaBiToFinish);

                                    CMDParser.parse(item.buttonTarLink).call(getActivity());
                                    //                                    showActivity(this, an_ScoreBuyPage());
                                    mDataIsExpired = true;
                                } else if (anyMatch(item.actionStatus, ScoreAction_status_receive)) {
                                    if (anyMatch(item.actionType, ScoreAction_type_SignIn)) {

                                        //先刷新一下
                                        consumeEventMR(refreshDayAction())
                                                .onNextSuccess(ignored -> {
                                                    consumeEventMR(ScoreController.gainTodayScore())
                                                            .onNextSuccess(response -> NotificationCenter.scoreChangedSubject.onNext(null))
                                                            .done();
                                                    consumeEventMR(ScoreController.gainTodayScore())
                                                            .onNextFinish(response -> fetchData(false))
                                                            .done();
                                                    new SigningDialog(getActivity()).show();
                                                })
                                                .done();
                                    } else {
                                        performGainScore(item.actionID);
                                    }
                                }
                            });
                        })
                        .create();
                mRecyclerView.setAdapter(adapter);
            }
        }

        private void performGainScore(String actionID) {
            GMFProgressDialog loadingDialog = new GMFProgressDialog(getActivity(), "正在领取,请稍候...");
            loadingDialog.show();
            consumeEventMR(ScoreController.gainActionScore(actionID))
                    .onNextStart(response -> loadingDialog.dismiss())
                    .onNextSuccess(response -> {
                        Dialog dialog = new GainScoreDialog(getActivity(), response.data);
                        dialog.setOnDismissListener(d -> NotificationCenter.scoreChangedSubject.onNext(actionID));
                        dialog.show();
                    })
                    .onNextFail(response -> showToast(this, getErrorMessage(response)))
                    .done();
        }
    }

    private static class ScoreActionInfo {

        private String actionID;
        private int actionType;
        private int actionStatus;
        private String actionImageUrl;
        private CharSequence actionTitle;
        private CharSequence actionTip;
        private boolean hasScore;
        private CharSequence score;

        private CharSequence buttonName;
        private int buttonColor;
        private String buttonTarLink;

        public ScoreActionInfo(ScoreAction data) {
            actionID = safeGet(() -> data.aID, "");
            actionType = safeGet(() -> data.actionType, ScoreAction_type_normal);
            actionStatus = safeGet(() -> data.status, ScoreAction_status_close);
            actionImageUrl = safeGet(() -> data.imgUrl, "");
            actionTitle = safeGet(() -> setColor(data.title, notMatch(data.status, ScoreAction_status_close) ? STATUS_BAR_BLACK : 0x33000000), "");
            actionTip = safeGet(() -> setColor(data.tip, 0x33000000), "");
            hasScore = safeGet(() -> data.amount > 0, false);
            score = safeGet(() -> formatBigNumber(data.amount, false, 0), "");

            if (anyMatch(actionStatus, ScoreAction_status_receive)) {
                buttonName = setColor(safeGet(() -> data.button.name, ""), TEXT_WHITE_COLOR);
                buttonColor = RED_COLOR;
            } else if (anyMatch(actionStatus, ScoreAction_status_noFinish)) {
                buttonName = setColor(safeGet(() -> data.button.name, ""), TEXT_BLACK_COLOR);
                buttonColor = YELLOW_COLOR;
            } else if (anyMatch(actionStatus, ScoreAction_status_close)) {
                buttonName = setColor(safeGet(() -> data.button.name, ""), 0xFFB4B4B4);
                buttonColor = 0xFFE5E5E5;
            } else {
                buttonName = setColor(safeGet(() -> data.button.name, ""), TEXT_WHITE_COLOR);
                buttonColor = RED_COLOR;
            }
            buttonTarLink = safeGet(() -> data.button.tarLink, "");
        }
    }

    public static class ScoreRecordFragment extends SimpleFragment {

        private RecyclerView mRecyclerView;
        private boolean mDataIsExpired = true;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_score_record, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setOnSwipeRefreshListener(() -> fetchData(false));
            v_setClick(mReloadSection, v -> fetchData(true));

            mRecyclerView = v_findView(this, R.id.recyclerView);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            addHorizontalSepLine(mRecyclerView);

            AdvanceSwipeRefreshLayout.class
                    .cast(mRefreshLayout)
                    .setOnPreInterceptTouchEventDelegate(ev -> !isScrollToTop(mRecyclerView));

            fetchData(true);

            consumeEvent(NotificationCenter.scoreChangedSubject)
                    .setTag("gain_score")
                    .onNextFinish(ignored -> {
                        mDataIsExpired = true;
                        if (getUserVisibleHint() && getView() != null) {
                            fetchData(false);
                        }
                    })
                    .done();
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mDataIsExpired = true;
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && getView() != null) {
                if (mDataIsExpired) {
                    fetchData(false);
                }
            }
        }

        private void fetchData(boolean reload) {
            consumeEventMRUpdateUI(ScoreController.refreshScoreRecord(), reload)
                    .setTag("reload_data")
                    .onNextSuccess(response -> {
                        if (response.data.isEmpty()) {
                            changeVisibleSection(TYPE_EMPTY);
                        } else {
                            List<ScoreRecordInfo> data = Stream.of(response.data).map(ScoreRecordInfo::new).collect(Collectors.toList());
                            updateListSection(data);
                            changeVisibleSection(TYPE_CONTENT);
                        }
                    })
                    .done();

            mDataIsExpired = false;

        }

        private void updateListSection(List<ScoreRecordInfo> items) {
            if (mRecyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<ScoreRecordInfo> adapter = getSimpleAdapter(mRecyclerView);
                adapter.resetItems(items);
            } else {
                SimpleRecyclerViewAdapter<ScoreRecordInfo> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView(R.layout.cell_score_record)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("dateLabel", R.id.label_date)
                                    .bindChildWithTag("operationLabel", R.id.label_operation)
                                    .bindChildWithTag("valueLabel", R.id.label_value)
                                    .configureView((item, pos) -> {
                                        v_setText(builder.getChildWithTag("dateLabel"), item.time);
                                        v_setText(builder.getChildWithTag("operationLabel"), item.operation);
                                        v_setText(builder.getChildWithTag("valueLabel"), item.value);
                                    });
                            return builder.create();
                        })
                        .create();
                mRecyclerView.setAdapter(adapter);
            }
        }
    }

    private static class ScoreRecordInfo {
        private CharSequence time;
        private CharSequence operation;
        private CharSequence value;

        public ScoreRecordInfo(ScoreAccountInfo info) {
            time = safeGet(() -> formatSecond(info.time, "yyyy.MM.dd"), PlaceHolder.NULL_VALUE);
            value = setColor(safeGet(() -> formatMoney(info.amount, true, 0, 2), PlaceHolder.NULL_VALUE), getIncomeTextColor(info.amount));
            operation = setColor(safeGet(() -> info.desc, PlaceHolder.NULL_VALUE), getIncomeTextColor(info.amount));
        }
    }

    public static class ScoreBuyFragment extends SimpleFragment {

        private int mScoreValue;
        private EditText mAmountField;
        private Button mBuyButton;
        private TextView mBriefLabel;


        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mScoreValue = getArguments().getInt(CommonProxyActivity.KEY_SCORE_BUY_VALUE);
            return inflater.inflate(R.layout.frag_score_buy, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, BLACK_COLOR);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
            findToolbar(this).setNavigationOnClickListener(v -> {
                showExitDialog(new WeakReference<>(this));
            });

            mAmountField = v_findView(this, R.id.field_amount);
            mBuyButton = v_findView(this, R.id.btn_buy);
            mBriefLabel = v_findView(this, R.id.label_brief);

            v_setClick(mBuyButton, v -> performBuyScore());
            v_addTextChangedListener(mAmountField, editable -> {
                String normalMoney = StringExtension.map(editable, StringExtension.normalMoneyTransformer());
                if (!TextUtils.isEmpty(normalMoney) && !TextUtils.isDigitsOnly(normalMoney) && editable.length() > 0) {
                    editable.delete(editable.length() - 1, editable.length());
                    return;
                }

                String formatMoney = StringExtension.map(normalMoney, StringExtension.formatMoneyTransformer(false, 0));
                if (!formatMoney.equalsIgnoreCase(editable.toString())) {
                    mAmountField.setText(formatMoney);
                    mAmountField.setSelection(formatMoney.length());
                    return;
                }

                if (editable.length() == 1 && editable.toString().equalsIgnoreCase("0")) {
                    editable.clear();
                }

                safeCall(() -> {
                    if (editable.length() > 0) {
                        mBuyButton.setText("购买" + formatBigNumber(Double.parseDouble(normalMoney) * 10000, false, 0) + "积分");
                        mBuyButton.setEnabled(true);
                    } else {
                        mBuyButton.setText("购买");
                        mBuyButton.setEnabled(false);
                    }
                });

            });
            String balance = formatMoney(CashierManager.getInstance().getCnCashBalanceWithoutDecimal(), false, 0);
            String balanceUnit = FundBrief.Money_Type.getUnit(FundBrief.Money_Type.CN);
            v_setText(mBriefLabel, concat(
                    String.format("一元 = %s积分", mScoreValue),
                    String.format("仅支持账户余额购买，当前余额%s%s", balance, balanceUnit)));
            mBuyButton.setEnabled(false);
            mAmountField.setFocusable(true);
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);
        }

        @Override
        protected boolean onInterceptKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == event.KEYCODE_BACK) {
                showExitDialog(new WeakReference<>(this));
                return true;
            }
            return super.onInterceptKeyDown(keyCode, event);
        }

        private static void showExitDialog(WeakReference<BaseFragment> fragmentRef) {
            GMFDialog.Builder builder = new GMFDialog.Builder(fragmentRef.get().getActivity());
            builder.setTitle("提示");
            builder.setMessage("购买未完成，确认离开当前购买流程?");
            builder.setPositiveButton("确认离开", (dialog, which) -> {
                dialog.dismiss();
                if (fragmentRef != null && fragmentRef.get().getView() != null) {
                    goBack(fragmentRef.get());
                }
            });
            builder.setNegativeButton("取消", (dialog, which) -> {
                dialog.dismiss();
            });
            builder.create().show();
        }

        @Override
        protected boolean onInterceptGoBack() {
            return super.onInterceptGoBack();
        }

        private void performBuyScore() {

            GMFProgressDialog loadingDialog = new GMFProgressDialog(getActivity(), "正在购买,请稍候...");
            loadingDialog.show();

            if (hasEnoughAccountBalance(getBuyAmountFromView())) {
                consumeEventMRUpdateUI(CashController.fetchBuyScore(Double.parseDouble(getBuyAmountFromView())), false)
                        .setTag("fetch_buy")
                        .onNextStart(response -> loadingDialog.dismiss())
                        .onNextSuccess(response -> {
                            RechargeDetailInfo.PayAction payAction = response.data;
                            GlobalVariableDic.shareInstance().update(CommonProxyActivity.KEY_PAY_ACTION, payAction);
                            if (payAction != null) {
                                replaceTopFragment(this, new PayFragments.SinaPayFragment().init(payAction.url, true, true));
                            }else {
                                createAlertDialog(this, getErrorMessage(response)).show();
                            }
                        })
                        .onNextFail(response -> {
                            createAlertDialog(this, getErrorMessage(response)).show();
                        })
                        .done();
            } else {
                loadingDialog.dismiss();
                GMFDialog.Builder builder = new GMFDialog.Builder(getActivity());
                builder.setCancelable(false);
                builder.setMessage("账户余额不足，需再充值");
                builder.setPositiveButton("去充值", (dialog, which) -> {
                    dialog.dismiss();
                    showActivity(getActivity(), an_RechargePage(FundBrief.Money_Type.CN, 0D));
                });
                builder.setNegativeButton("重新输入", (dialog1, which) -> {

                    dialog1.dismiss();
                });
                builder.create().show();
            }

        }

        private boolean hasEnoughAccountBalance(String money) {
            if (money.length() == 0)
                return true;
            double accountBalance = CashierManager.getInstance().getCnCashBalanceWithoutDecimal();
            double moneyInDouble = Double.valueOf(money);
            return accountBalance >= moneyInDouble;
        }

        private String getBuyAmountFromView() {
            return StringExtension.map(mAmountField, StringExtension.normalMoneyTransformer());
        }
    }

    public static class ScoreBuySuccessFragment extends SimpleFragment {

        private Button mBuyButton;
        private String mMessage;

        public ScoreBuySuccessFragment init(String message) {
            Bundle argument = new Bundle();
            argument.putString(ActivityNavigation.KEY_SCORE_COST_SUCCESS_MESSAGE, message);
            setArguments(argument);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mMessage = getArguments().getString(ActivityNavigation.KEY_SCORE_COST_SUCCESS_MESSAGE, "");
            return inflater.inflate(R.layout.frag_buy_score_success, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
            v_setText(this, R.id.label_amount, mMessage);
            mBuyButton = v_findView(this, R.id.btn_buy);
            v_setClick(mBuyButton, this::performBuyScore);
        }

        private void performBuyScore(View view) {
            goBack(this);
        }
    }
}
