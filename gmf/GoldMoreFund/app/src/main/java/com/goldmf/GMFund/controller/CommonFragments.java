package com.goldmf.GMFund.controller;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.Shape;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.transition.Visibility;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.imagepipeline.image.ImageInfo;
import com.goldmf.GMFund.MyConfig;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.business.CashController;
import com.goldmf.GMFund.controller.business.CommonController;
import com.goldmf.GMFund.controller.business.UserController;
import com.goldmf.GMFund.controller.dialog.DownloadDialog;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.common.UpdateInfo;
import com.goldmf.GMFund.manager.fortune.AccountTradeInfo;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.mine.MineManager.VerifyCode;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.PageArrayHelper;
import com.goldmf.GMFund.util.AppUtil;
import com.goldmf.GMFund.util.DownloadUtil;
import com.goldmf.GMFund.util.StringFactoryUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.BasicCell;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.ProgressButton;
import com.goldmf.GMFund.widget.fresco.DefaultZoomableController;
import com.goldmf.GMFund.widget.fresco.ZoomableDraweeView;

import java.io.File;
import java.util.List;

import rx.Observable;
import yale.extension.common.Optional;
import yale.extension.common.shape.RoundCornerShape;
import yale.extension.system.SimpleRecyclerViewAdapter;

import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WebViewPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.LINE_SEP_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.RED_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_GREY_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_WHITE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.getIncomeTextColor;
import static com.goldmf.GMFund.extension.MResultExtension.createObservableCountDown;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.addHorizontalSepLine;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.getSimpleAdapter;
import static com.goldmf.GMFund.extension.RecyclerViewExtension.isScrollToBottom;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concat;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.StringExtension.encryptPhoneNumberTransformer;
import static com.goldmf.GMFund.extension.StringExtension.map;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.createErrorDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.getScreenSize;
import static com.goldmf.GMFund.extension.UIControllerExtension.getStatusBarHeight;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_unsignedNumberFormatter;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static com.goldmf.GMFund.util.FormatUtil.formatSecond;

/**
 * Created by yale on 15/10/14.
 */
public class CommonFragments {
    private CommonFragments() {
    }

    public static class AboutFragment extends BaseFragment {
        private BasicCell mCheckUpdateCell;
        private ProgressButton mLogoutButton;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_about, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            mCheckUpdateCell = v_findView(this, R.id.cell_check_update);
            mLogoutButton = v_findView(this, R.id.btn_logout);
            v_setClick(mCheckUpdateCell, v -> performCheckUpdate());
            v_setClick(this, R.id.cell_user_protocol, v -> {
                showActivity(this, an_WebViewPage(CommonDefine.H5URL_UserAgreement()));

                UmengUtil.stat_enter_user_protocol_page(getActivity(), Optional.of(this));
            });
            v_setClick(this, R.id.cell_company, v -> {
                showActivity(this, an_WebViewPage(CommonDefine.H5URL_AboutUs()));
                UmengUtil.stat_enter_company_page(getActivity(), Optional.of(this));
            });
            v_setClick(this, R.id.cell_public_number, v -> {
                AppUtil.openPackage(this, "com.tencent.mm");
                UmengUtil.stat_enter_public_number_page(getActivity(), Optional.of(this));
            });
            v_setText(this, R.id.label_versionName, "v" + AppUtil.getVersionName(getActivity()));
            v_setClick(this, R.id.cell_question, v -> {
                showActivity(this, an_WebViewPage(CommonDefine.H5URL_Help()));

                UmengUtil.stat_enter_help_page(getActivity(), Optional.of(this));
            });
            //            v_setClick(this, R.id.cell_feedback, v -> {
            //                if (MineManager.getInstance().isLoginOK()) {
            //                    showActivity(this, an_FeedbackPage(Optional.of(this)));
            //                } else {
            //                    showActivity(this, an_LoginPage());
            //                }
            //            });
            v_setClick(mLogoutButton, v -> performLogout());


            consumeEvent(NotificationCenter.logoutSubject)
                    .setTag("logout")
                    .onNextFinish(ignored -> goBack(this))
                    .done();

        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (getView() != null) {
                UpdateInfo info = CommonManager.getInstance().getUpdateInfo();
                TextView updateInfoLabel = mCheckUpdateCell.getExtraTitleLabel();
                final int paddingVertical = dp2px(2);
                final int paddingHorizontal = dp2px(2);
                updateInfoLabel.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
                updateInfoLabel.setTextSize(12);
                if (info == null) {
                    updateInfoLabel.setText("已是最新");
                    updateInfoLabel.setTextColor(TEXT_GREY_COLOR);
                    updateInfoLabel.setBackgroundDrawable(null);
                } else {
                    updateInfoLabel.setText(info.updateVersion);
                    updateInfoLabel.setTextColor(TEXT_WHITE_COLOR);
                    updateInfoLabel.setBackgroundDrawable(new ShapeDrawable(new Shape() {
                        private final RectF mRect = new RectF();

                        @Override
                        public void draw(Canvas canvas, Paint paint) {
                            mRect.set(0, 0, canvas.getWidth(), canvas.getHeight());
                            final float radius = mRect.height() / 2;
                            paint.setColor(RED_COLOR);
                            canvas.drawRoundRect(mRect, radius, radius, paint);
                        }
                    }));
                }

                if (MineManager.getInstance().isLoginOK()) {
                    mLogoutButton.setMode(ProgressButton.Mode.Normal);
                    v_setVisible(mLogoutButton);
                } else {
                    v_setGone(mLogoutButton);
                }
            }
        }

        private void performLogout() {
            mLogoutButton.setMode(ProgressButton.Mode.Loading);
            MineManager.getInstance().logout();
            consumeEvent(NotificationCenter.logoutSubject)
                    .onNextFinish(ignored -> v_setGone(mLogoutButton))
                    .done();

            UmengUtil.stat_logout(getActivity(), Optional.of(this));
        }

        private void performCheckUpdate() {
            GMFProgressDialog GMFProgressDialog = new GMFProgressDialog(getActivity());
            GMFProgressDialog.setMessage("正在检查更新，请稍候");
            GMFProgressDialog.show();

            consumeEvent(CommonController.checkUpdate())
                    .onNextFinish(response -> {
                        GMFProgressDialog.dismiss();
                        if (isSuccess(response) && response.data != null) {
                            createUpdateDialog(response.data).show();
                        } else {
                            showToast(this, "暂时没有更新");
                        }
                    })
                    .done();
        }

        private GMFDialog createUpdateDialog(UpdateInfo updateInfo) {
            File savePath = new File(getActivity().getCacheDir().getAbsoluteFile() + File.separator + "update.apk");
            boolean isNeedToDownload = DownloadUtil.isNeedToDownload(savePath, updateInfo.md5);
            GMFDialog.Builder builder = new GMFDialog.Builder(getActivity());
            builder.setTitle(updateInfo.updateTitle);
            builder.setMessage(updateInfo.updateMsg);
            builder.setPositiveButton(isNeedToDownload ? "立即更新" : "免流量更新", (dialog, which) -> {
                dialog.dismiss();
                showDownloadDialog(updateInfo, savePath);
            });
            builder.setNegativeButton("下次", (dialog, which) -> {
                dialog.dismiss();
                CommonManager.getInstance().delayUpdateAlert();
                DownloadDialog.downloadOnBackground(updateInfo.url, savePath, updateInfo.md5, true);
            });
            return builder.create();
        }

        private void showDownloadDialog(UpdateInfo updateInfo, File savePath) {
            DownloadDialog dialog = new DownloadDialog(getActivity(), updateInfo.url, savePath, Optional.of(updateInfo.md5));
            dialog.setFinishDownloadListener((d, isSuccess, file) -> {
                d.dismiss();
                if (isSuccess) {
                    file.setReadable(true, false);
                    AppUtil.installApk(this, file);
                    if (updateInfo.needForceUpdate) {
                        Process.killProcess(Process.myPid());
                    }
                } else {
                    if (updateInfo.needForceUpdate) {
                        createErrorDialog(this, "下载失败").show();
                    } else {
                        showToast(this, "下载失败");
                    }
                }
            });
            dialog.show();
            dialog.startDownload();
        }

    }

    public static class FeedbackFragment extends BaseFragment {

        private EditText mContentField;
        private ProgressButton mSendButton;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_feedback, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            mContentField = v_findView(this, R.id.field_content);
            mSendButton = v_findView(this, R.id.btn_send);
            v_setClick(mSendButton, this::performSubmitFeedback);
            mSendButton.setEnabled(false);
            v_addTextChangedListener(mContentField, editable -> mSendButton.setEnabled(editable.length() > 10));

            mContentField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);
        }

        private void performSubmitFeedback() {
            String content = mContentField.getText().toString();

            if (content.equalsIgnoreCase("GoldMoreFund520")) {
                MyConfig.setDevModeEnable(true);
                showToast(this, "Hello World!");
            } else {
                CommonManager.getInstance().contactUs(MineManager.getInstance().getPhone(), content);
                showToast(this, "感谢您的反馈");
            }

            goBack(this);
        }
    }

    public static class ResetTransactionPasswordFragment extends BaseFragment {

        private EditText mCodeField;
        private EditText mNewPwdField;
        private EditText mConfirmPwdField;
        private Button mSendCodeButton;
        private ProgressButton mSaveButton;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_reset_transaction_pwd, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);

            if (!MineManager.getInstance().isLoginOK()) {
                goBack(this);
                return;
            }

            // bind child views
            mCodeField = v_findView(view, R.id.field_code);
            mNewPwdField = v_findView(view, R.id.field_pwd_new);
            mConfirmPwdField = v_findView(view, R.id.field_pwd_confirm);
            mSendCodeButton = v_findView(view, R.id.btn_send_code);
            mSaveButton = v_findView(view, R.id.btn_save);

            // init child views
            v_setClick(mSendCodeButton, this::performSendCode);
            v_setClick(mSaveButton, this::performResetTransactionPwd);

            v_setText(this, R.id.contact_customer, concatNoBreak("如手机已丢失或停用，请",
                    StringFactoryUtil.contactCustomerService(getActivity(), v_findView(this, R.id.contact_customer))));


            Mine mine = MineManager.getInstance().getmMe();
            v_setText(view, R.id.label_info, concatNoBreak("已发送重置密码短信验证码至 ", setFontSize(map(mine.phone, encryptPhoneNumberTransformer()), sp2px(17))));
            mSaveButton.setEnabled(false);
            Observable.just(mNewPwdField, mConfirmPwdField)
                    .subscribe(editText -> {
                        v_addTextChangedListener(editText, editable -> {
                            if (editable.length() > 6)
                                editable.delete(editText.length() - 1, editable.length());
                        });
                    });
            Observable.just(mCodeField, mNewPwdField, mConfirmPwdField)
                    .subscribe(editText -> {
                        v_addTextChangedListener(editText, v_unsignedNumberFormatter());
                        v_addTextChangedListener(editText, editable -> {
                            mSaveButton.setEnabled(mCodeField.length() >= 4 && mNewPwdField.length() == 6 && mConfirmPwdField.length() == 6);
                        });
                    });

            performSendCode();
        }

        private void performResetTransactionPwd() {
            String code = mCodeField.getText().toString();
            String newPwd = mNewPwdField.getText().toString();
            String confirmPwd = mConfirmPwdField.getText().toString();
            if (!newPwd.equals(confirmPwd)) {
                createAlertDialog(this, "两次输入的密码不一致").show();
            } else {
                mSaveButton.setMode(ProgressButton.Mode.Loading);
                consumeEventMR(UserController.resetTransactionPwd(code, newPwd))
                        .setTag("reset_pwd")
                        .onNextStart(response -> {
                            mSaveButton.setMode(ProgressButton.Mode.Normal);
                        })
                        .onNextSuccess(response -> {
                            goBack(this);
                            showToast(this, "修改成功");
                        })
                        .onNextFail(response -> {
                            createAlertDialog(this, getErrorMessage(response)).show();
                        })
                        .done();
            }
        }

        private void performSendCode() {
            mSendCodeButton.setEnabled(false);
            String phone = MineManager.getInstance().getPhone();
            consumeEventMR(UserController.sendCode(phone, VerifyCode.ResetTraderPS))
                    .setTag("send_code")
                    .onNextSuccess(response -> enableSendCodeButtonLater())
                    .onNextFail(response -> {
                        mSendCodeButton.setEnabled(true);
                        createAlertDialog(this, getErrorMessage(response)).show();
                    })
                    .done();
        }

        private void enableSendCodeButtonLater() {
            mSendCodeButton.setEnabled(false);
            consumeEvent(createObservableCountDown(60, 1000))
                    .setTag("count_down")
                    .onNextStart(value -> {
                        mSendCodeButton.setText("重新发送" + value);
                    })
                    .onComplete(() -> {
                        mSendCodeButton.setEnabled(true);
                        mSendCodeButton.setText("获取验证码");
                    })
                    .done();
        }
    }

    public static class CashJournalFragment extends SimpleFragment {
        private RecyclerView mRecyclerView;
        private boolean mHasData = false;
        private Button mFilterButton;
        private PopupWindow mVisiblePopWindow;
        private int currentIndex = 0;
        private List<String> mTypeItems;
        private List<Pair<Integer, String>> mTypeList;
        private CommandPageArray<AccountTradeInfo> mPageArray;
        private boolean mIsLoadingMore = false;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_cash_journal, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            changeVisibleSection(TYPE_LOADING);
            mFilterButton = v_findView(this, R.id.btn_filter);
            mRecyclerView = v_findView(this, android.R.id.list);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            setOnSwipeRefreshListener(() -> fetchData(currentIndex, false));
            v_setClick(mReloadSection, v -> fetchData(currentIndex, true));
            v_setClick(mFilterButton, () -> performFetchCashJournalTypeList());

            fetchData(currentIndex, false);
        }

        @Override
        protected boolean onInterceptGoBack() {
            boolean isConsumed = dismissCurrentVisiblePopWindow();
            return isConsumed || super.onInterceptGoBack();
        }

        private boolean dismissCurrentVisiblePopWindow() {
            if (mVisiblePopWindow != null) {
                mVisiblePopWindow.dismiss();
                mVisiblePopWindow = null;
                return true;
            }
            return false;
        }

        private void fetchData(int currentIndex, boolean reload) {

            consumeEventMRUpdateUI(CashController.fetchTradeJournal(currentIndex), reload)
                    .onNextSuccess(response -> {
                        if (!mHasData && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            TransitionSet set = new TransitionSet();
                            Slide slide = new Slide();
                            slide.setMode(Visibility.MODE_IN);
                            set.addTransition(slide);
                            set.setDuration(300L);
                            TransitionManager.beginDelayedTransition((ViewGroup) getView(), set);
                        }

                        mTypeList = FortuneManager.getInstance().traderListValue;
                        mTypeItems = Stream.of(mTypeList).map(item -> item.second).collect(Collectors.toList());
                        mPageArray = response.data;
                        resetRecyclerView(mPageArray);
                    })
                    .onNextFinish(response -> {
                        setSwipeRefreshing(false);
                    })
                    .done();
        }

        private void resetRecyclerView(CommandPageArray<AccountTradeInfo> pageArray) {
            List<CashJournalCellVM> items = Stream.of(pageArray.data()).map(it -> new CashJournalCellVM(it)).collect(Collectors.toList());
            if (items.isEmpty()) {
                changeVisibleSection(TYPE_EMPTY);
            } else {
                if (mRecyclerView.getAdapter() != null) {
                    SimpleRecyclerViewAdapter<CashJournalCellVM> adapter = getSimpleAdapter(mRecyclerView);
                    adapter.resetItems(items);
                } else {
                    SimpleRecyclerViewAdapter<CashJournalCellVM> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                            .onCreateItemView(R.layout.cell_cash_journal)
                            .onCreateViewHolder(builder -> {
                                builder.bindChildWithTag("timeLabel", R.id.label_time)
                                        .bindChildWithTag("actionAndDetailLabel", R.id.label_action_and_detail)
                                        .bindChildWithTag("amountAndStateLabel", R.id.label_amount_and_state)
                                        .configureView((item, pos) -> {
                                            v_setText(builder.getChildWithTag("timeLabel"), item.time);
                                            v_setText(builder.getChildWithTag("actionAndDetailLabel"), item.actionAndDetail);
                                            v_setText(builder.getChildWithTag("amountAndStateLabel"), item.amountAndState);
                                        });
                                return builder.create();
                            })
                            .create();

                    adapter.addFooter(adapter.createFooterView(getActivity(), R.layout.footer_loading_more));
                    adapter.addFooter(adapter.createFooterView(getActivity(), R.layout.footer_no_more_data));
                    mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (isScrollToBottom(recyclerView) && !mIsLoadingMore && PageArrayHelper.hasMoreData(mPageArray)) {
                                mIsLoadingMore = true;
                                consumeEventMR(PageArrayHelper.getNextPage(mPageArray))
                                        .onNextSuccess(response -> resetRecyclerView(mPageArray))
                                        .onNextFinish(response -> mIsLoadingMore = false)
                                        .done();
                            }
                        }
                    });
                    mRecyclerView.setAdapter(adapter);
                }

                SimpleRecyclerViewAdapter adapter = (SimpleRecyclerViewAdapter) mRecyclerView.getAdapter();
                View loadingMoreFooter = adapter.getFooter(0);
                View noMoreDataFooter = adapter.getFooter(1);
                loadingMoreFooter.setVisibility(PageArrayHelper.hasMoreData(mPageArray) ? View.VISIBLE : View.GONE);
                noMoreDataFooter.setVisibility(PageArrayHelper.hasMoreData(mPageArray) ? View.GONE : View.VISIBLE);

                changeVisibleSection(TYPE_CONTENT);
                mHasData = true;
            }
        }

        private void performFetchCashJournalTypeList() {
            dismissCurrentVisiblePopWindow();

            List<String> items = mTypeItems;
            RecyclerView typeRecyclerView = (RecyclerView) LayoutInflater.from(getActivity()).inflate(R.layout.section_recyclerview, null);
            typeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            addHorizontalSepLine(typeRecyclerView);
            PopupWindow window = new PopupWindow(getActivity());

            if (typeRecyclerView.getAdapter() != null) {
                SimpleRecyclerViewAdapter<String> adapter = getSimpleAdapter(typeRecyclerView);
                adapter.resetItems(items);
            } else {
                SimpleRecyclerViewAdapter<String> adapter = new SimpleRecyclerViewAdapter.Builder<>(items)
                        .onCreateItemView(R.layout.cell_cash_journal_type)
                        .onCreateViewHolder(builder -> {
                            builder.bindChildWithTag("titleLabel", R.id.label_title)
                                    .configureView((item, pos) -> {
                                        v_setText(builder.getChildWithTag("titleLabel"), item);
                                    });
                            return builder.create();
                        })
                        .onViewHolderCreated((ad, holder) -> {
                            v_setClick(holder.itemView, () -> {
                                window.dismiss();
                                String clickItemTitle = ad.getItem(holder.getAdapterPosition());
                                updateTitle(clickItemTitle);
                                Stream.of(mTypeList)
                                        .forEach(pair -> {
                                            if (clickItemTitle.equals(pair.second)) {
                                                int index = pair.first;
                                                fetchData(index, false);
                                                currentIndex = index;
                                            }
                                        });
                            });
                        })
                        .create();
                typeRecyclerView.setAdapter(adapter);
            }

            window.setContentView(typeRecyclerView);
            window.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            if (items.size() < 8) {
                window.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            } else {
                window.setHeight(dp2px(300));
            }
            window.setFocusable(true);
            window.setOutsideTouchable(true);
            window.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(0, 0).border(LINE_SEP_COLOR, dp2px(0.5f))));
            window.showAtLocation(findToolbar(this), Gravity.RIGHT | Gravity.TOP, 0, findToolbar(this).getHeight() + getStatusBarHeight(this));
            mVisiblePopWindow = window;
        }
    }

    private static class CashJournalCellVM {
        public final int moneyType;
        public final CharSequence time;
        public final CharSequence actionAndDetail;
        public final CharSequence amountAndState;

        public CashJournalCellVM(AccountTradeInfo journalInfo) {
            this.moneyType = journalInfo.moneyType;
            this.time = concat(formatSecond(journalInfo.transactTime, "MM/dd"), setFontSize(formatSecond(journalInfo.transactTime, "HH:mm"), sp2px(12)));
            this.actionAndDetail = concat(setColor(journalInfo.transactText, getIncomeTextColor(journalInfo.amount)), setFontSize(journalInfo.detail, sp2px(12)));
            this.amountAndState = concat(setColor(formatMoney(journalInfo.amount, true, 2), getIncomeTextColor(journalInfo.amount)), setFontSize(journalInfo.statusText, sp2px(12)));
        }
    }

    public static class ViewPictureFragment extends BaseFragment {
        private Uri mUri;
        private int mWidth;
        private int mHeight;
        private String mTransitionName;

        public ViewPictureFragment initWithRemoteImage(Uri uri, int width, int height, String transactionName) {
            Bundle arguments = new Bundle();
            arguments.putParcelable("url", uri);
            arguments.putInt("width", width);
            arguments.putInt("height", height);
            arguments.putString("transition_name", transactionName);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mUri = getArguments().getParcelable("url");
            mWidth = getArguments().getInt("width");
            mHeight = getArguments().getInt("height");
            mTransitionName = getArguments().getString("transition_name");
            View view = inflater.inflate(R.layout.frag_view_picture, container, false);
            configureViewSize(view.findViewById(R.id.img_actual), mWidth, mHeight, false);
            ViewCompat.setTransitionName(view.findViewById(R.id.img_actual), mTransitionName);
            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(getResources())
                    .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                    .setPlaceholderImage(getResources().getDrawable(R.mipmap.ic_fund_cover_placeholder), ScalingUtils.ScaleType.CENTER_CROP)
                    .build();
            AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(mUri)
                    .setControllerListener(new ControllerListener<ImageInfo>() {
                        @Override
                        public void onSubmit(String id, Object callerContext) {
                        }

                        @Override
                        public void onFinalImageSet(String id, @Nullable ImageInfo imageInfo, @Nullable Animatable animatable) {
                            View actualImage = view.findViewById(R.id.img_actual);
                            ViewGroup.LayoutParams params = actualImage.getLayoutParams();
                            params.width = -1;
                            params.height = -1;
                            actualImage.setLayoutParams(params);
                            //                            actualImage.invalidate();
                        }

                        @Override
                        public void onIntermediateImageSet(String id, @Nullable ImageInfo imageInfo) {

                        }

                        @Override
                        public void onIntermediateImageFailed(String id, Throwable throwable) {

                        }

                        @Override
                        public void onFailure(String id, Throwable throwable) {

                        }

                        @Override
                        public void onRelease(String id) {

                        }
                    })
                    .build();
            DefaultZoomableController zoomController = DefaultZoomableController.newInstance();
            zoomController.setMaxScaleFactor(computeMaxScale(mWidth, mHeight));
            zoomController.setMinScaleFactor(1.0f);
            ZoomableDraweeView draweeView = v_findView(this, R.id.img_actual);
            draweeView.setHierarchy(hierarchy);
            draweeView.setZoomableController(zoomController);
            draweeView.setController(controller);

            draweeView.setOnTouchListener(new View.OnTouchListener() {
                private GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        goBack(ViewPictureFragment.this);
                        return true;
                    }
                };
                private GestureDetector mGestureDetector = new GestureDetector(getActivity(), mGestureListener);

                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    return mGestureDetector.onTouchEvent(motionEvent);
                }
            });
        }

        @Override
        protected boolean onInterceptGoBack() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                View actualImage = v_findView(this, R.id.img_actual);
                configureViewSize(actualImage, mWidth, mHeight, true);
            }
            return super.onInterceptGoBack();
        }

        private float computeMaxScale(int imageWidth, int imageHeight) {
            if (imageWidth > 0 && imageHeight > 0) {
                Rect screenSize = getScreenSize(this);
                float ratio = (float) imageWidth / imageHeight;
                float screenRatio = (float) screenSize.width() / screenSize.height();
                if (ratio >= screenRatio) {
                    float actualHeight = (float) screenSize.width() / ratio;
                    return Math.max((float) screenSize.height() / actualHeight, 2);
                } else {
                    float actualWidth = (float) screenSize.height() * ratio;
                    return Math.max((float) screenSize.width() / actualWidth, 2);
                }
            }

            return 2;
        }

        private void configureViewSize(View view, int width, int height, boolean notify) {
            Rect screenSize = getScreenSize(this);
            if (width > 0 && height > 0) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                if (width <= screenSize.width() && height <= screenSize.height()) {
                    params.width = width;
                    params.height = height;
                } else {
                    float ratio = (float) width / height;
                    float screenRatio = (float) screenSize.width() / screenSize.height();
                    if (ratio > screenRatio) {
                        params.width = screenSize.width();
                        params.height = (int) (screenSize.width() / ratio);
                    } else if (ratio == 1) {
                        params.width = screenSize.width();
                        params.height = screenSize.width();
                    } else {
                        params.width = (int) (screenSize.height() * ratio);
                        params.height = screenSize.height();
                    }
                }
                if (notify) {
                    view.setLayoutParams(params);
                }
            }
        }
    }
}
