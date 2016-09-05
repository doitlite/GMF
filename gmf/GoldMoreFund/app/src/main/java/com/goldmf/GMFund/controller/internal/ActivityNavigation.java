package com.goldmf.GMFund.controller.internal;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.cmd.CMDParser;
import com.goldmf.GMFund.controller.BaseActivity;
import com.goldmf.GMFund.controller.BaseActivity.TRANSACTION_DIRECTION;
import com.goldmf.GMFund.controller.CardFragments;
import com.goldmf.GMFund.controller.CommonProxyActivity;
import com.goldmf.GMFund.controller.CouponFragments;
import com.goldmf.GMFund.controller.FundFragments.FundDetailFragmentV2;
import com.goldmf.GMFund.controller.FundFragments.FundListFragment;
import com.goldmf.GMFund.controller.InvestFragments;
import com.goldmf.GMFund.controller.LoginFragments;
import com.goldmf.GMFund.controller.MainActivityV2;
import com.goldmf.GMFund.controller.MainFragments;
import com.goldmf.GMFund.controller.PhotoViewerFragment;
import com.goldmf.GMFund.controller.RechargeFragments;
import com.goldmf.GMFund.controller.ScoreFragments;
import com.goldmf.GMFund.controller.SplashActivity;
import com.goldmf.GMFund.controller.StockAccountFragments.MyStockAccountDetailFragment;
import com.goldmf.GMFund.controller.StockChartActivity;
import com.goldmf.GMFund.controller.StockMatchFragments;
import com.goldmf.GMFund.controller.StockTradeFragments;
import com.goldmf.GMFund.controller.TraderFragments;
import com.goldmf.GMFund.controller.TransparentProxyActivity;
import com.goldmf.GMFund.controller.UserDetailFragments;
import com.goldmf.GMFund.controller.WithdrawFragments;
import com.goldmf.GMFund.controller.business.CashController;
import com.goldmf.GMFund.controller.business.StockController;
import com.goldmf.GMFund.controller.circle.CircleDetailFragment;
import com.goldmf.GMFund.controller.circle.CircleHintFragment;
import com.goldmf.GMFund.controller.circle.CircleListFragment;
import com.goldmf.GMFund.controller.circle.CircleRateUserListFragment;
import com.goldmf.GMFund.controller.circle.WriteFragments;
import com.goldmf.GMFund.controller.circle.WriteFragments.SelectFundFragment;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.extension.SimulationAccountExtension;
import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.manager.cashier.CashierManager;
import com.goldmf.GMFund.manager.cashier.ServerMsg;
import com.goldmf.GMFund.manager.fortune.Coupon;
import com.goldmf.GMFund.manager.message.MessageSession;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.model.GMFMessage.ShortcutImagePair;
import com.goldmf.GMFund.model.TarLinkButton;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.GlobalVariableDic;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.GMFProgressDialog;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.controller.AuthenticFragments.AuthenticFragment;
import static com.goldmf.GMFund.controller.AwardFragments.AwardHomeFragmentV2;
import static com.goldmf.GMFund.controller.CommonFragments.AboutFragment;
import static com.goldmf.GMFund.controller.CommonFragments.CashJournalFragment;
import static com.goldmf.GMFund.controller.CommonFragments.ResetTransactionPasswordFragment;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_AMOUNT_DOUBLE;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_COUPON_LIST_OIBJECT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_DISABLE_SAVE_STATE_BOOLEAN;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_FIRST_VISIBLE_CHILD_IDX_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_FLAGS_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_FRAGMENT_CLASS_NAME;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_FUND_ID_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_FUND_LIST;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_FUND_NAME_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_GLOBAL_OBJ_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_HIDE_SHARE_BTN_BOOLEAN;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_IMAGE_LIST;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_KEYWORD_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_LAUNCH_MAIN_ACTIVITY_BOOLEAN;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_LINK_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_MESSAGE_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_MESSAGE_TYPE_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_MONEY_TYPE_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_ORDER_DATA_SOURCE_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_PREFER_CLOSE_BUTTON_AT_TOOLBAR_BOOLEAN;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_RANGE_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_RANK_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_RATE_TYPE_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_RECT_LIST;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_SESSION_HEAD_ID_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_SESSION_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_SHOW_SHARE_DIALOG_BOOLEAN;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_SOURCE_TYPE_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_STOCK_COMPETE_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_STOCK_ID_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_TAB_IDX_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_URL_STRING;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_USER_ID_INT;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_VC_TITLE;
import static com.goldmf.GMFund.controller.CommonUserFragments.UserLeaderBoardFragment;
import static com.goldmf.GMFund.controller.DevModeFragments.DevModeHomeFragment;
import static com.goldmf.GMFund.controller.FundFragments.AllFundFragment;
import static com.goldmf.GMFund.controller.IntroductionFragments.PPTHostFragment;
import static com.goldmf.GMFund.controller.LoginUserFragments.AccountProfileFragment;
import static com.goldmf.GMFund.controller.LoginUserFragments.EditAddressFragment;
import static com.goldmf.GMFund.controller.MainFragments.ADHomeFragment;
import static com.goldmf.GMFund.controller.MainFragments.ConversationListFragment;
import static com.goldmf.GMFund.controller.MainFragments.InvestHomeFragment;
import static com.goldmf.GMFund.controller.MainFragments.MineFragment;
import static com.goldmf.GMFund.controller.MarketStatsFragments.MarketStatsFragment;
import static com.goldmf.GMFund.controller.MarketStatsFragments.StockMarketLiveFragment;
import static com.goldmf.GMFund.controller.QuotationFragments.StockDetailFragment;
import static com.goldmf.GMFund.controller.QuotationFragments.StockSearchFragment;
import static com.goldmf.GMFund.controller.ScoreFragments.ScoreHomeFragment;
import static com.goldmf.GMFund.controller.StockAccountFragments.OpenSimulationAccountFragment;
import static com.goldmf.GMFund.controller.StockChartFragments.StockChartDetailFragment;
import static com.goldmf.GMFund.controller.StockTradeFragments.SelectStockFragment;
import static com.goldmf.GMFund.controller.StockTradeFragments.StockAnalyseFragment;
import static com.goldmf.GMFund.controller.StockTradeFragments.StockTradeFragment;
import static com.goldmf.GMFund.controller.TraderFragments.MoreTraderFragment;
import static com.goldmf.GMFund.controller.TraderFragments.MyManagedFundsFragment;
import static com.goldmf.GMFund.controller.UserDetailFragments.UserDetailFragment;
import static com.goldmf.GMFund.controller.WebViewFragments.WebViewFragment;
import static com.goldmf.GMFund.controller.chat.ChatFragments.ConversationDetailFragment;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.SimulationAccountExtension.isNeedToCheckSimulationAccountState;
import static com.goldmf.GMFund.extension.SimulationAccountExtension.isOpenedSimulationAccount;
import static com.goldmf.GMFund.util.UmengUtil.eEVENTIDTopicDetailEnterFromWeb;
import static com.goldmf.GMFund.util.UmengUtil.stat_click_event;

/**
 * Created by yale on 15/7/20.
 * Activity的导航类，所以Activity的跳转都通过该类实现
 */
public class ActivityNavigation {
    private static final String KEY_IS_REQUIRED_LOGIN_BOOLEAN = "an_is_required_login";
    private static final String KEY_IS_REQUIRED_OPEN_SIMULATION_ACCOUNT_BOOLEAN = "an_is_required_open_simulation_account";
    private static final String KEY_IS_REQUIRED_AUTHENTIC_BOOLEAN = "an_is_required_authentic";
    private static final String KEY_IS_REQUIRED_CHANGE_CARD_BOOLEAN = "an_is_required_change_card";

    public static final String KEY_OPERATION_TYPE_INT = "operation_type";
    public static final int KEY_RECHARGE_TYPE_INT = 0;
    public static final int KEY_INVEST_TYPE_INT = 1;
    public static final String KEY_CN_RECHARGE_SUCCESS_MESSAGE = "an_recharge_success_message";
    public static final String KEY_CN_INVEST_SUCCESS_MESSAGE = "an_invest_success_message";
    public static final String KEY_CN_WITHDRAW_SUCCESS_MESSAGE = "an_withdraw_success_message";
    public static final String KEY_SCORE_COST_SUCCESS_MESSAGE = "an_score_success_message";
    public static final String KEY_CN_OPERATION_FAILURE_MESSAGE = "an_operation_failure_message";

    private ActivityNavigation() {
    }

    public static void showActivity(Fragment fragment, Func1<Context, Intent> pageIntentBuilder) {
        showActivity(fragment.getActivity(), pageIntentBuilder, TRANSACTION_DIRECTION.DEFAULT);
    }

    public static void showActivity(Fragment fragment, Func1<Context, Intent> pageIntentBuilder, TRANSACTION_DIRECTION direction) {
        showActivity(fragment.getActivity(), pageIntentBuilder, direction);
    }

    public static void showActivity(Context context, Func1<Context, Intent> pageIntentBuilder) {
        showActivity(context, pageIntentBuilder, TRANSACTION_DIRECTION.DEFAULT);
    }

    public static void showActivity(Context context, Func1<Context, Intent> pageIntentBuilder, TRANSACTION_DIRECTION direction) {
        Intent intent = pageIntentBuilder.call(context);
        showActivity(new WeakReference<>(context), intent, direction);
    }

    @SuppressWarnings("SimplifiableConditionalExpression")
    private static void showActivity(WeakReference<Context> contextRef, Intent intent, TRANSACTION_DIRECTION direction) {
        safeCall(() -> {
            boolean isRequiredLogin = intent.getBooleanExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, false);
            boolean isRequiredOpenSimulationAccount = intent.getBooleanExtra(KEY_IS_REQUIRED_OPEN_SIMULATION_ACCOUNT_BOOLEAN, false);
            boolean isRequiredAuthentic = intent.getBooleanExtra(KEY_IS_REQUIRED_AUTHENTIC_BOOLEAN, false);
            boolean isRequiredChangeCard = intent.getBooleanExtra(KEY_IS_REQUIRED_CHANGE_CARD_BOOLEAN, false);

            isRequiredLogin = (isRequiredOpenSimulationAccount || isRequiredAuthentic || isRequiredChangeCard) ? true : isRequiredLogin;
            isRequiredAuthentic = (isRequiredChangeCard) ? true : isRequiredAuthentic;

            boolean isLogin = MineManager.getInstance().isLoginOK();
            if (isRequiredLogin && !isLogin) {
                if (isContextAlive(contextRef)) {
                    showActivity(getContext(contextRef), an_LoginPage());
                }

                if (isBaseActivity(contextRef)) {
                    getBaseActivity(contextRef).consumeEvent(NotificationCenter.loginSubject.limit(1))
                            .setTag("action_after_login")
                            .onNextFinish(ignored -> {
                                if (isContextAlive(contextRef)) {
                                    showActivity(contextRef, intent, direction);
                                }
                            })
                            .done();

                    getBaseActivity(contextRef).consumeEvent(NotificationCenter.cancelLoginSubject.limit(1))
                            .setTag("action_after_cancel_login")
                            .onNextFinish(ignored -> {
                                if (isContextAlive(contextRef)) {
                                    getBaseActivity(contextRef).unsubscribeFromMain("action_after_login");
                                }
                            })
                            .done();
                }
                return;
            }

            if (isRequiredOpenSimulationAccount) {

                if (isNeedToCheckSimulationAccountState()) {
                    if (isBaseActivity(contextRef)) {
                        GMFProgressDialog dialog = new GMFProgressDialog(getContext(contextRef));
                        dialog.setMessage("正在查询模拟是否开通模拟帐户...");
                        dialog.show();

                        getBaseActivity(contextRef).consumeEvent(StockController.fetchSimulationAccount(false).limit(1))
                                .setTag("check_simulation_account_state")
                                .onNextFinish(ignored -> {
                                    dialog.dismiss();
                                    if (isContextAlive(contextRef) && !isNeedToCheckSimulationAccountState()) {
                                        showActivity(contextRef, intent, direction);
                                    }
                                })
                                .onComplete(() -> dialog.dismiss())
                                .done();
                    }
                    return;
                }

                if (!isOpenedSimulationAccount()) {
                    if (isContextAlive(contextRef)) {
                        showActivity(getContext(contextRef), an_OpenSimulationAccountPage());
                    }

                    if (isBaseActivity(contextRef)) {
                        getBaseActivity(contextRef).consumeEvent(NotificationCenter.closeOpenSimulationPageSubject.limit(1))
                                .setTag("action_after_open_simulation_account")
                                .onNextFinish(ignored -> {
                                    if (isContextAlive(contextRef)) {
                                        boolean isOpened = SimulationAccountExtension.isOpenedSimulationAccount();
                                        if (isOpened) {
                                            showActivity(contextRef, intent, direction);
                                        }
                                    }
                                })
                                .done();
                    }
                    return;
                }
            }

            if (isRequiredAuthentic) {
                Boolean isAuthentic = safeGet(() -> MineManager.getInstance().getmMe().setAuthenticate, false);
                if (!isAuthentic) {
                    if (isContextAlive(contextRef)) {
                        showActivity(getContext(contextRef), an_AuthenticPage());
                    }

                    if (isBaseActivity(contextRef)) {
                        getBaseActivity(contextRef).consumeEvent(NotificationCenter.closeAuthenticPageSubject.limit(1))
                                .setTag("action_after_authentic")
                                .onNextFinish(ignored -> {
                                    if (isContextAlive(contextRef)) {
                                        boolean isAuthenticNow = safeGet(() -> MineManager.getInstance().getmMe().setAuthenticate, false);
                                        if (isAuthenticNow) {
                                            showActivity(contextRef, intent, direction);
                                        }
                                    }
                                })
                                .done();
                    }
                    return;
                }
            }

            if (isRequiredChangeCard) {
                Observable<MResults.MResultsInfo<ServerMsg<Boolean>>> observable = CashController.queryChangeCard();
                observable.observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> {
                            if (isSuccess(response)) {
                                ServerMsg<Boolean> msg = response.data;
                                if (msg != null) {
                                    boolean needBindCNCard = msg.getData();
                                    if (needBindCNCard) {
                                        if (TextUtils.isEmpty(msg.tip)) {
                                            performRequiredBindCNCard(contextRef, intent, direction);
                                        } else {
                                            setupMessageTip(contextRef.get(), msg, () -> {
                                                performRequiredBindCNCard(contextRef, intent, direction);
                                            });
                                        }
                                        return;
                                    }
                                }
                                performToNextPage(contextRef, intent, direction);
                            }
                        });
            } else {
                performToNextPage(contextRef, intent, direction);
            }


        });
    }

    private static void performRequiredBindCNCard(WeakReference<Context> contextRef, Intent intent, TRANSACTION_DIRECTION direction) {
        if (isContextAlive(contextRef)) {
            showActivity(getContext(contextRef), an_BindCNCardPage());
        }

        if (isBaseActivity(contextRef)) {
            getBaseActivity(contextRef).consumeEvent(NotificationCenter.closeBindCNCardPageSubject)
                    .setTag("action_after_bind_cn_card")
                    .onNextFinish(ignored -> {
                        boolean isBindCNCardNow = safeGet(() -> CashierManager.getInstance().getCard().status == BankCard.Card_Status_Normal, false);
                        if (isBindCNCardNow) {
                            showActivity(contextRef, intent, direction);
                        }
                    })
                    .done();
        }
    }

    private static void performToNextPage(WeakReference<Context> contextRef, Intent intent, TRANSACTION_DIRECTION direction) {
        if (!(isActivity(contextRef))) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (direction != null) {
            intent.putExtra(BaseActivity.KEY_TRANSACTION_DIRECTION, direction);
        }

        if (isContextAlive(contextRef)) {
            getContext(contextRef).startActivity(intent);
        }
        if (direction != null && isBaseActivity(contextRef)) {
            if (direction == TRANSACTION_DIRECTION.DEFAULT) {
                getBaseActivity(contextRef).overridePendingTransition(android.support.design.R.anim.abc_grow_fade_in_from_bottom, R.anim.stay);
            } else if (direction == TRANSACTION_DIRECTION.VERTICAL) {
                getBaseActivity(contextRef).overridePendingTransition(android.support.design.R.anim.abc_slide_in_bottom, R.anim.stay);
            } else if (direction == TRANSACTION_DIRECTION.NONE) {
                getBaseActivity(contextRef).overridePendingTransition(0, 0);
            }
        }
    }

    public static void setupMessageTip(Context context, ServerMsg msg, Action0 action) {
        GMFDialog.Builder builder = new GMFDialog.Builder(context);
        builder.setTitle(msg.title);
        builder.setMessage(msg.tip);

        List<TarLinkButton> buttons = msg.buttons();
        if (buttons == null || buttons.isEmpty()) {
            if (buttons == null)
                buttons = new ArrayList<>();
            TarLinkButton button = new TarLinkButton();
            button.name = "我知道了";
            button.type = TarLinkButton.TarLinkButton_Type_Cancel;
            buttons.add(button);
        }

        for (int i = 0; i < Math.min(2, buttons.size()); i++) {
            TarLinkButton button = buttons.get(i);
            DialogInterface.OnClickListener listener = (dialog, which) -> {
                dialog.dismiss();
                onClickButton(context, button, () -> {
                    action.call();
                });
            };
            if (i == 0) {
                builder.setNegativeButton(button.name, listener);
            } else {
                builder.setPositiveButton(button.name, listener);
            }
        }
        builder.create().show();
    }

    public static void onClickButton(Context context, TarLinkButton button, Action0 action) {
        switch (button.type) {
            case TarLinkButton.TarLinkButton_Type_Continue:
                action.call();
                break;
            case TarLinkButton.TarLinkButton_Type_Cancel:
                break;
            case TarLinkButton.TarLinkButton_Type_Tarlink:
                CMDParser.parse(button.tarLink).call(context);
                break;
            case TarLinkButton.TarLinkButton_Type_Share:
                showActivity(context, an_WebViewPage(button.shareInfo.url));
                break;
            default:
                break;
        }
    }

    private static boolean isContextAlive(WeakReference<Context> contextRef) {
        return opt(contextRef).isPresent();
    }

    private static boolean isActivity(WeakReference<Context> contextRef) {
        return opt(contextRef).cast(Activity.class).isPresent();
    }

    private static boolean isBaseActivity(WeakReference<Context> contextRef) {
        return opt(contextRef).cast(BaseActivity.class).isPresent();
    }

    private static Context getContext(WeakReference<Context> contextRef) {
        return opt(contextRef).orNull();
    }

    private static BaseActivity getBaseActivity(WeakReference<Context> contextRef) {
        return opt(contextRef).cast(BaseActivity.class).orNull();
    }

    public static void showActivityWithShareElement(Fragment fragment, Func1<Context, Intent> pageIntentBuilder, View shareElement, String transactionName) {
        showActivityWithShareElement(fragment.getActivity(), pageIntentBuilder, shareElement, transactionName);
    }

    @TargetApi(21)
    public static void showActivityWithShareElement(Context context, Func1<Context, Intent> pageIntentBuilder, View shareElement, String transactionName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = pageIntentBuilder.call(context);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            if (context instanceof Activity) {
                shareElement.setTransitionName(transactionName);
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation((Activity) context, shareElement, transactionName).toBundle();
                context.startActivity(intent, bundle);
            } else {
                context.startActivity(intent);
            }
        }
    }

    /**
     * 跳转到欢迎页面
     */
    public static Func1<Context, Intent> an_SplashPage(Uri uri, String source) {
        return ctx -> {
            Intent intent = new Intent(ctx, SplashActivity.class);
            if (uri != null)
                intent.setData(uri);
            if (source != null)
                intent.putExtra("source", source);
            return intent;
        };
    }

    /**
     * 跳转到欢迎页面
     */
    public static Func1<Context, Intent> an_MainPage(Uri uri) {
        return ctx -> {
            Intent intent = new Intent(ctx, MainActivityV2.class);
            intent.setData(uri);
            return intent;
        };
    }

    public static Func1<Context, Intent> an_WebViewPage(String url) {
        return an_WebViewPage(url, false);
    }

    public static Func1<Context, Intent> an_WebViewPage(String url, boolean hideShareButton) {
        return ctx -> {
            Intent intent = new Intent(ctx, CommonProxyActivity.class);
            intent.putExtra(KEY_FRAGMENT_CLASS_NAME, WebViewFragment.class.getName());
            intent.putExtra(KEY_URL_STRING, url);
            intent.putExtra(KEY_HIDE_SHARE_BTN_BOOLEAN, hideShareButton);
            return intent;
        };
    }

    /**
     * 打开实名认证页面
     *
     * @return
     */
    public static Func1<Context, Intent> an_AuthenticPage() {
        return simpleFragmentPageFunc(AuthenticFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
        });
    }

    /**
     * 显示投资组合详情页面
     */
    public static Func1<Context, Intent> an_FundDetailPage(int fundId) {
        return ctx -> {
            UmengUtil.stat_enter_fund_detail_page(ctx, Optional.empty());
            Intent intent = new Intent(ctx, CommonProxyActivity.class);
            intent.putExtra(KEY_FRAGMENT_CLASS_NAME, FundDetailFragmentV2.class.getName());
            intent.putExtra(KEY_FUND_ID_INT, fundId);
            return intent;
        };
    }

    /**
     * 显示登录页面
     */
    public static Func1<Context, Intent> an_LoginPage() {
        return simpleFragmentPageFunc(LoginFragments.VerifyPhoneFragment.class);
    }

    /**
     * 显示用户主页面
     */
    public static Func1<Context, Intent> an_UserDetailPage(User user) {
        return simpleFragmentPageFunc(UserDetailFragment.class, intent -> {
            GlobalVariableDic.shareInstance().update(CommonProxyActivity.KEY_USER, user);
            intent.putExtra(KEY_DISABLE_SAVE_STATE_BOOLEAN, false);
        });
    }

    /**
     * 显示编辑资料页面
     */
    public static Func1<Context, Intent> an_AccountProfilePage() {
        UmengUtil.stat_click_event(UmengUtil.eEVENTIDHomePageInfoEdit);
        return simpleFragmentPageFunc(AccountProfileFragment.class, intent -> {
            intent.putExtra(KEY_DISABLE_SAVE_STATE_BOOLEAN, false);
        });
    }

    /**
     * 显示重置交易密码页面
     */
    public static Func1<Context, Intent> an_ResetTransactionPasswordPage() {
        return simpleFragmentPageFunc(ResetTransactionPasswordFragment.class);
    }

    /**
     * 显示充值页面
     */
    public static Func1<Context, Intent> an_RechargePage(int moneyType, @Nullable Double expectRechargeAmount) {
        if (moneyType == Money_Type.CN) {
            return simpleFragmentPageFunc(RechargeFragments.CNAccountRechargeFragment.class, intent -> {
                intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
                intent.putExtra(KEY_IS_REQUIRED_AUTHENTIC_BOOLEAN, true);
                intent.putExtra(KEY_IS_REQUIRED_CHANGE_CARD_BOOLEAN, true);
                if (expectRechargeAmount != null) {
                    intent.putExtra(KEY_AMOUNT_DOUBLE, expectRechargeAmount);
                }
            });
        } else if (moneyType == Money_Type.HK) {
            return simpleFragmentPageFunc(RechargeFragments.HKAccountRechargeFragment.class, intent -> {
                intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
                intent.putExtra(KEY_IS_REQUIRED_AUTHENTIC_BOOLEAN, true);
                if (expectRechargeAmount != null) {
                    intent.putExtra(KEY_AMOUNT_DOUBLE, expectRechargeAmount);
                }
            });
        } else {
            return none();
        }
    }

    /**
     * 显示投资页面
     */
    public static Func1<Context, Intent> an_InvestPage(int fundId) {
        return an_InvestPage(fundId, null);
    }

    /**
     * 显示投资页面
     */
    public static Func1<Context, Intent> an_InvestPage(int fundId, @Nullable Double expectInvestAmount) {
        return simpleFragmentPageFunc(InvestFragments.InvestFundFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_IS_REQUIRED_AUTHENTIC_BOOLEAN, true);
            intent.putExtra(KEY_IS_REQUIRED_CHANGE_CARD_BOOLEAN, true);
            intent.putExtra(KEY_FUND_ID_INT, fundId);
            if (expectInvestAmount != null) {
                intent.putExtra(KEY_AMOUNT_DOUBLE, expectInvestAmount);
            }
        });
    }

    /**
     * 显示提现页面
     */
    public static Func1<Context, Intent> an_WithdrawPage(int moneyType) {
        if (moneyType == Money_Type.CN) {
            return simpleFragmentPageFunc(WithdrawFragments.CNAccountWithdrawFragment.class, intent -> {
                intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
                intent.putExtra(KEY_IS_REQUIRED_AUTHENTIC_BOOLEAN, true);
                intent.putExtra(KEY_IS_REQUIRED_CHANGE_CARD_BOOLEAN, true);
            });
        } else if (moneyType == Money_Type.HK) {
            return simpleFragmentPageFunc(WithdrawFragments.HKAccountWithdrawFragment.class, intent -> {
                intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
                intent.putExtra(KEY_IS_REQUIRED_AUTHENTIC_BOOLEAN, true);
            });
        }

        return none();
    }

    /**
     * 显示积分购买页面
     */
    public static Func1<Context, Intent> an_ScoreBuyPage(int value) {
        return simpleFragmentPageFunc(ScoreFragments.ScoreBuyFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_IS_REQUIRED_CHANGE_CARD_BOOLEAN, true);
            intent.putExtra(CommonProxyActivity.KEY_SCORE_BUY_VALUE, value);
        });
    }

    /**
     * 显示绑定沪深银行卡页面
     */
    public static Func1<Context, Intent> an_BindCNCardPage() {
        return simpleFragmentPageFunc(CardFragments.BindCNCardFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_IS_REQUIRED_AUTHENTIC_BOOLEAN, true);
        });
    }

    /**
     * 显示我的股票账户详情页面
     */
    public static Func1<Context, Intent> an_MyStockAccountDetailPage(int moneyType) {
        UmengUtil.stat_click_event(UmengUtil.eEVENTIDHomePageAccMore);
        return ctx -> {
            Intent intent = new Intent(ctx, CommonProxyActivity.class);
            intent.putExtra(KEY_FRAGMENT_CLASS_NAME, MyStockAccountDetailFragment.class.getName());
            intent.putExtra(KEY_MONEY_TYPE_INT, moneyType);
            return intent;
        };
    }

    /**
     * 显示主页面
     */
    public static Func1<Context, Intent> an_ADHomePage() {
        return simpleFragmentPageFunc(ADHomeFragment.class);
    }

    /**
     * 显示股票Tab页面
     */
    public static Func1<Context, Intent> an_StockHomePage(int tabIndex) {
        return simpleFragmentPageFunc(MainFragments.StockHomeFragment.class, intent -> intent.putExtra(MainActivityV2.KEY_STOCK_TAB_INDEX, tabIndex));
    }

    /**
     * 显示推荐组合列表页
     */
    public static Func1<Context, Intent> an_RecommendFundListPage() {
        return simpleFragmentPageFunc(InvestHomeFragment.class);
    }

    /**
     * 显示推荐操盘手列表页
     */
    public static Func1<Context, Intent> an_RecommendTraderListPage() {
        return simpleFragmentPageFunc(MoreTraderFragment.class);
    }

    /**
     * 显示我的主页
     */
    public static Func1<Context, Intent> an_MineHomePage() {
        return simpleFragmentPageFunc(MineFragment.class);
    }


    /**
     * 显示意见反馈的页面
     */
    public static Func1<Context, Intent> an_FeedbackPage(Optional<Fragment> fragmentHolder) {
        return ctx -> {
            UmengUtil.stat_enter_feed_back_page(ctx, fragmentHolder);
            Intent intent = new Intent(ctx, CommonProxyActivity.class);
            intent.putExtra(KEY_FRAGMENT_CLASS_NAME, ConversationDetailFragment.class.getName());
            intent.putExtra(KEY_LINK_ID_STRING, "10000");
            intent.putExtra(KEY_MESSAGE_TYPE_INT, 2);
            return intent;
        };
    }

    /**
     * 预览图片页面
     *
     * @param imageURLs
     * @return
     */
    public static Func1<Context, Intent> an_PhotoViewerPage(List<ShortcutImagePair> imageURLs, Rect[] globalVisibleRects, int currentIdx) {
        return simpleFragmentPageFunc(PhotoViewerFragment.class, FLAG_ACTIVITY_TRANSPARENT, intent -> {
            boolean isSerializable = imageURLs != null && imageURLs instanceof Serializable;
            intent.putExtra(KEY_IMAGE_LIST, isSerializable ? (Serializable) imageURLs : null);
            intent.putExtra(KEY_TAB_IDX_INT, currentIdx);
            intent.putExtra(KEY_RECT_LIST, globalVisibleRects);
        });
    }

    /**
     * 显示资金明细页面面
     */
    public static Func1<Context, Intent> an_CashJournalPage(Optional<Fragment> framgentHolder) {
        return ctx -> {
            UmengUtil.stat_enter_cash_journal_page(ctx, framgentHolder);
            Intent intent = new Intent(ctx, CommonProxyActivity.class);
            intent.putExtra(KEY_FRAGMENT_CLASS_NAME, CashJournalFragment.class.getName());
            return intent;
        };
    }

    /**
     * 显示功能介绍页面
     */
    public static Func1<Context, Intent> an_FunctionIntroductionPage(boolean launchMainActivity) {
        return ctx -> {
            Intent intent = new Intent(ctx, CommonProxyActivity.class);
            intent.putExtra(KEY_FRAGMENT_CLASS_NAME, PPTHostFragment.class.getName());
            intent.putExtra(KEY_LAUNCH_MAIN_ACTIVITY_BOOLEAN, launchMainActivity);
            return intent;
        };
    }


    /**
     * 显示用户管理组合页面
     *
     * @return
     */
    public static Func1<Context, Intent> an_ManagedGroupsPage() {
        return simpleFragmentPageFunc(MyManagedFundsFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
        });
    }

    /**
     * 显示佣金页面
     */
    public static Func1<Context, Intent> an_AwardHomePage(Optional<Fragment> fragmentHolder) {
        return simpleFragmentPageFunc(AwardHomeFragmentV2.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
        });
    }

    /**
     * 显示侠劵页面
     */
    public static Func1<Context, Intent> an_ScoreHomePage() {
        return an_ScoreHomePage(0);
    }

    /**
     * 显示侠劵页面
     */
    public static Func1<Context, Intent> an_ScoreHomePage(int tabIdx) {

        return simpleFragmentPageFunc(ScoreHomeFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_TAB_IDX_INT, tabIdx);
        });
    }

    /**
     * 编辑收货地址
     */
    public static Func1<Context, Intent> an_EditAddressPage() {
        UmengUtil.stat_click_event(UmengUtil.eEVENTIDMineAddressEnter);
        return simpleFragmentPageFunc(EditAddressFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
        });
    }

    /**
     * 显示开发者模式的页面
     */
    public static Func1<Context, Intent> an_DevModePage() {
        return simpleFragmentPageFunc(DevModeHomeFragment.class);
    }

    /**
     * 显示关于页面
     */
    public static Func1<Context, Intent> an_AboutPage() {
        return simpleFragmentPageFunc(AboutFragment.class);
    }

    /**
     * 显示会话列表页
     */
    public static Func1<Context, Intent> an_ConversationListPage() {
        return simpleFragmentPageFunc(ConversationListFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
        });
    }

    /**
     * 显示会话群组页
     */
    public static Func1<Context, Intent> an_ConversationGroupPage(int messageType, String linkID, String sessionID) {
        return simpleFragmentPageFunc(MainFragments.ConversationGroupFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_MESSAGE_TYPE_INT, messageType);
            intent.putExtra(KEY_LINK_ID_STRING, linkID);
            intent.putExtra(KEY_SESSION_ID_STRING, sessionID);
        });
    }

    /**
     * 显示会话详情页
     */
    public static Func1<Context, Intent> an_ConversationDetailPage(int messageType, String linkId, String sessionId) {
        return simpleFragmentPageFunc(ConversationDetailFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_MESSAGE_TYPE_INT, messageType);
            intent.putExtra(KEY_LINK_ID_STRING, linkId);
            intent.putExtra(KEY_SESSION_ID_STRING, sessionId);
            intent.putExtra(KEY_DISABLE_SAVE_STATE_BOOLEAN, false);
        });
    }

    /**
     * 打开朋友圈列表
     */
    public static Func1<Context, Intent> an_CircleListPage(int messageType, String linkId, String sessionId) {
        return simpleFragmentPageFunc(CircleListFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_MESSAGE_TYPE_INT, messageType);
            intent.putExtra(KEY_LINK_ID_STRING, linkId);
            intent.putExtra(KEY_SESSION_ID_STRING, sessionId);
        });
    }

    /**
     * 打开朋友圈详情页
     */
    public static Func1<Context, Intent> an_CircleDetailPage(MessageSession session, int headID, GMFMessage message) {
        return an_CircleDetailPage(session.messageType, session.linkID, session.linkID, headID, message.messageID, false, message);
    }

    /**
     * 打开朋友圈详情页
     */
    public static Func1<Context, Intent> an_CircleDetailPage(MessageSession session, int headID, String messageID) {
        return an_CircleDetailPage(session.messageType, session.linkID, session.linkID, headID, messageID);
    }

    /**
     * 打开朋友圈详情页
     */
    public static Func1<Context, Intent> an_CircleDetailPage(int messageType, String linkID, String sessionID, int headID, String messageID) {
        return an_CircleDetailPage(messageType, linkID, sessionID, headID, messageID, false);
    }

    /**
     * 打开朋友圈详情页
     */
    public static Func1<Context, Intent> an_CircleDetailPage(int messageType, String linkID, String sessionID, int headID, String messageID, boolean showShareDialogNow) {
        return an_CircleDetailPage(messageType, linkID, sessionID, headID, messageID, showShareDialogNow, null);
    }

    /**
     * 打开朋友圈详情页
     */
    public static Func1<Context, Intent> an_CircleDetailPage(int messageType, String linkID, String sessionID, int headID, String messageID, boolean showShareDialogNow, GMFMessage message) {
        return simpleFragmentPageFunc(CircleDetailFragment.class, intent -> {

            if (CMDParser.LAST_PUSH_SOURCE == CMDParser.SOURCE_PUSH || CMDParser.LAST_PUSH_SOURCE == CMDParser.SOURCE_URL) {
                stat_click_event(eEVENTIDTopicDetailEnterFromWeb);
            } else {
                Class clazz = safeGet(() -> MyApplication.getTopFragmentOrNil().get().getClass(), null);
                if (clazz != null) {
                    if (clazz == CircleHintFragment.class) {
                        stat_click_event(UmengUtil.eEVENTIDTopicDetailEnterFromHint);
                    } else if (clazz == CircleListFragment.class) {
                        stat_click_event(UmengUtil.eEVENTIDTopicDetailEnterFromTopicList);
                    } else if (clazz == UserDetailFragments.UserDetailFragment.class) {
                        stat_click_event(UmengUtil.eEVENTIDTopicDetailEnterFromUserHome);
                    }
                }
            }

            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_MESSAGE_TYPE_INT, messageType);
            intent.putExtra(KEY_LINK_ID_STRING, linkID);
            intent.putExtra(KEY_SESSION_ID_STRING, sessionID);
            intent.putExtra(KEY_SESSION_HEAD_ID_INT, headID);
            intent.putExtra(KEY_MESSAGE_ID_STRING, messageID);
            intent.putExtra(KEY_SHOW_SHARE_DIALOG_BOOLEAN, showShareDialogNow);
            if (message != null) {
                GlobalVariableDic dic = GlobalVariableDic.shareInstance();
                String key = dic.generateUniqueKey();
                dic.update(key, message);
                intent.putExtra(KEY_GLOBAL_OBJ_ID_STRING, key);
            }
        });
    }

    /**
     * 打开写心情页面
     */
    public static Func1<Context, Intent> an_WriteMoodPage(int messageType, String linkID, String sessionID) {
        return simpleFragmentPageFunc(WriteFragments.WriteMoodFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_MESSAGE_TYPE_INT, messageType);
            intent.putExtra(KEY_LINK_ID_STRING, linkID);
            intent.putExtra(KEY_SESSION_ID_STRING, sessionID);
            intent.putExtra(KEY_DISABLE_SAVE_STATE_BOOLEAN, false);
        });
    }

    /**
     * 打开写情报页面
     */
    public static Func1<Context, Intent> an_WriteIntelligencePage(int messageType, String linkID, String sessionID) {
        return simpleFragmentPageFunc(WriteFragments.WriteIntelligenceFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_MESSAGE_TYPE_INT, messageType);
            intent.putExtra(KEY_LINK_ID_STRING, linkID);
            intent.putExtra(KEY_SESSION_ID_STRING, sessionID);
            intent.putExtra(KEY_DISABLE_SAVE_STATE_BOOLEAN, false);
        });
    }

    /**
     * 打开朋友圈提醒页面
     */
    public static Func1<Context, Intent> an_CircleHintPage(MessageSession session) {
        int messageType = safeGet(() -> session.messageType, 0);
        String linkID = safeGet(() -> session.linkID, "");
        String sessionID = safeGet(() -> session.sessionID, "");
        return an_CircleHintPage(messageType, linkID, sessionID);
    }

    /**
     * 打开朋友圈提醒页面
     */
    public static Func1<Context, Intent> an_CircleHintPage(int messageType, String linkID, String sessionID) {
        return simpleFragmentPageFunc(CircleHintFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_MESSAGE_TYPE_INT, messageType);
            intent.putExtra(KEY_LINK_ID_STRING, linkID);
            intent.putExtra(KEY_SESSION_ID_STRING, sessionID);
        });
    }

    /**
     * 打开投资红包列表
     */
    public static Func1<Context, Intent> an_CouponListPage() {
        return an_CouponListPage(0, null);
    }

    /**
     * 打开投资红包列表
     */
    public static Func1<Context, Intent> an_CouponListPage(int flags, LinkedList<Coupon> items) {
        return simpleFragmentPageFunc(CouponFragments.CouponListFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_FLAGS_INT, flags);
            intent.putExtra(KEY_COUPON_LIST_OIBJECT, items);
        });
    }

    public static Func1<Context, Intent> an_CircleRateUserListPage(MessageSession session, GMFMessage message, int rateType) {
        int messageType = safeGet(() -> session.messageType, 0);
        String linkID = safeGet(() -> session.linkID, "");
        String sessionID = safeGet(() -> session.sessionID, "");
        String messageID = safeGet(() -> message.messageID, "");

        return simpleFragmentPageFunc(CircleRateUserListFragment.class, intent -> {
            intent.putExtra(KEY_MESSAGE_TYPE_INT, messageType);
            intent.putExtra(KEY_LINK_ID_STRING, linkID);
            intent.putExtra(KEY_SESSION_ID_STRING, sessionID);
            intent.putExtra(KEY_MESSAGE_ID_STRING, messageID);
            intent.putExtra(KEY_RATE_TYPE_INT, rateType);
        });
    }

    /**
     * 显示股票详情页
     */
    public static Func1<Context, Intent> an_QuotationDetailPage(String stockId) {
        return simpleFragmentPageFunc(StockDetailFragment.class, intent -> intent.putExtra(KEY_STOCK_ID_STRING, stockId));
    }

    /**
     * 显示所有股票大赛页面
     */
    public static Func1<Context, Intent> an_AllStockCompetePage() {
        return simpleFragmentPageFunc(StockMatchFragments.StockMatchListPageAllFragment.class);
    }

    /**
     * 显示我的股票大赛
     */
    public static Func1<Context, Intent> an_MineStockCompetePage() {
        return simpleFragmentPageFunc(StockMatchFragments.StockMatchListPageMineFragment.class);
    }

    /**
     * 显示股票大赛详情页面
     */
    public static Func1<Context, Intent> an_StockMatchDetailPage(String matchId) {
        //        return simpleFragmentPageFunc(StockMatchDetailFragment.class, intent -> intent.putExtra(CommonProxyActivity.KEY_STOCK_COMPETE_ID_STRING, matchId));
        return simpleFragmentPageFunc(StockMatchFragments.StockMatchDetailFragment2.class, intent -> intent.putExtra(KEY_STOCK_COMPETE_ID_STRING, matchId));
    }

    /**
     * 显示股票交易页
     */
    public static Func1<Context, Intent> an_StockTradePage(int userID, String stockId, int tabIndex) {
        return simpleFragmentPageFunc(StockTradeFragment.class, intent -> {
            intent.putExtra(KEY_USER_ID_INT, userID);
            intent.putExtra(KEY_STOCK_ID_STRING, stockId);
            intent.putExtra(KEY_TAB_IDX_INT, tabIndex);
            int loginUserID = MineManager.getInstance().getmMe().index;
            if (userID == -1 || (userID == loginUserID)) {
                intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
                intent.putExtra(KEY_IS_REQUIRED_OPEN_SIMULATION_ACCOUNT_BOOLEAN, true);
            }
        });
    }

    /**
     * 显示组合交易页
     */
    public static Func1<Context, Intent> an_FundTradePage(int fundID, String fundName, String stockID, int tabIndex) {
        return simpleFragmentPageFunc(StockTradeFragment.class, intent -> {
            intent.putExtra(KEY_FUND_ID_INT, fundID);
            intent.putExtra(KEY_FUND_NAME_STRING, fundName);
            intent.putExtra(KEY_STOCK_ID_STRING, stockID);
            intent.putExtra(KEY_TAB_IDX_INT, tabIndex);
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
        });
    }

    /**
     * 显示股票搜索页,不需要登录
     */
    public static Func1<Context, Intent> an_StockSearchPage() {
        return simpleFragmentPageFunc(StockSearchFragment.class);
    }

    /**
     * 显示股票K线图详情页
     */
    public static Func1<Context, Intent> an_StockChartDetailPage(String stockId, int chartType, int authorityType, int specType) {
        return ctx -> {
            Intent intent = new Intent(ctx, StockChartActivity.class);
            intent.putExtra(StockChartActivity.KEY_FRAGMENT_CLASS_NAME, StockChartDetailFragment.class.getName());
            intent.putExtra(KEY_STOCK_ID_STRING, stockId);
            intent.putExtra(StockChartActivity.KEY_STOCK_CHART_TYPE, chartType);
            intent.putExtra(StockChartActivity.KEY_STOCK_AUTHORITY_TYPE, authorityType);
            intent.putExtra(StockChartActivity.KEY_STOCK_SPEC_TYPE, specType);
            return intent;
        };
    }

    /**
     * 显示选择股票页
     */
    public static Func1<Context, Intent> an_SelectStockPage(String keyword) {
        UmengUtil.stat_click_event(UmengUtil.eEVENTIDStockBuyPostionClick);
        return simpleFragmentPageFunc(SelectStockFragment.class, intent -> {
            intent.putExtra(KEY_KEYWORD_STRING, keyword);
        });
    }

    /**
     * 显示选择组合页
     */
    public static Func1<Context, Intent> an_SelectFundPage() {
        return simpleFragmentPageFunc(SelectFundFragment.class);
    }

    /**
     * 显示行情中心页
     */
    public static Func1<Context, Intent> an_MarketStatsPage() {
        return simpleFragmentPageFunc(MarketStatsFragment.class);
    }

    /**
     * 显示个股分析页
     */
    public static Func1<Context, Intent> an_StockAnalysePage(String userID, String stockID, String range) {
        int userIDInt = Integer.valueOf(userID);
        return an_StockAnalysePage(userIDInt, stockID, range);
    }

    /**
     * 显示个股分析页
     */
    public static Func1<Context, Intent> an_StockAnalysePage(int userID, String stockID, String range) {
        return simpleFragmentPageFunc(StockAnalyseFragment.class, intent -> {
            intent.putExtra(KEY_USER_ID_INT, userID);
            intent.putExtra(KEY_STOCK_ID_STRING, stockID);
            intent.putExtra(KEY_RANGE_STRING, range);
        });
    }

    /**
     * 显示股市直播页
     */
    public static Func1<Context, Intent> an_StockMarketLivePage(int firstVisibleChildIdx) {
        UmengUtil.stat_click_event(UmengUtil.eEVENTIDSelectStockLive);
        return simpleFragmentPageFunc(StockMarketLiveFragment.class, intent -> intent.putExtra(KEY_FIRST_VISIBLE_CHILD_IDX_INT, firstVisibleChildIdx));
    }

    /**
     * 显示更多组合页面
     */
    public static Func1<Context, Intent> an_MoreFundPage() {
        int flags = AllFundFragment.FLAG_ALLOW_SWIPE_REFRESH;
        return an_MoreFundPage(null, flags);
    }

    public static Func1<Context, Intent> an_MoreFundPage(List<FundBrief> funds) {
        return an_MoreFundPage(funds, 0);
    }

    /**
     * 显示全部组合页面
     */
    public static Func1<Context, Intent> an_MoreFundPage(List<FundBrief> funds, int flags) {
        UmengUtil.stat_click_event(UmengUtil.eEVENTIDTouziAllFundEnter);
        return simpleFragmentPageFunc(AllFundFragment.class, intent -> {
            if (funds instanceof Serializable) {
                intent.putExtra(KEY_FUND_LIST, (Serializable) funds);
            }
            intent.putExtra(KEY_FLAGS_INT, flags);
        });
    }

    /**
     * 显示组合列表页面
     */
    public static Func1<Context, Intent> an_FundListFragment(String title, List<FundBrief> funds) {
        return simpleFragmentPageFunc(FundListFragment.class, intent -> {
            if (funds instanceof Serializable) {
                intent.putExtra(KEY_VC_TITLE, title);
                intent.putExtra(KEY_FUND_LIST, (Serializable) funds);
            }
        });
    }

    /**
     * 显示全部操盘手页面
     */
    public static Func1<Context, Intent> an_AllTraderPage() {
        UmengUtil.stat_click_event(UmengUtil.eEVENTIDTouziAllTraderEnter);
        return simpleFragmentPageFunc(MoreTraderFragment.class, intent -> {
            intent.putExtra(KEY_SOURCE_TYPE_INT, MoreTraderFragment.SOURCE_TYPE_TRADER);
        });
    }

    /**
     * 显示全部牛人页面
     */
    public static Func1<Context, Intent> an_AllTalentPage() {
        return simpleFragmentPageFunc(MoreTraderFragment.class, intent -> {
            intent.putExtra(KEY_SOURCE_TYPE_INT, MoreTraderFragment.SOURCE_TYPE_TALENT);
        });
    }

    /**
     * 显示申请成为操盘手页面
     */
    public static Func1<Context, Intent> an_ApplyTraderPage() {
        return simpleFragmentPageFunc(TraderFragments.ApplyTraderFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_IS_REQUIRED_AUTHENTIC_BOOLEAN, true);
            intent.putExtra(KEY_PREFER_CLOSE_BUTTON_AT_TOOLBAR_BOOLEAN, true);
            intent.putExtra(KEY_URL_STRING, CommonDefine.H5URL_TRADER_APPLY());
            intent.putExtra(KEY_HIDE_SHARE_BTN_BOOLEAN, true);
        });
    }

    /**
     * 显示申请成为牛人页面
     */
    public static Func1<Context, Intent> an_ApplyTalentPage() {
        return simpleFragmentPageFunc(TraderFragments.ApplyTalentFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_IS_REQUIRED_AUTHENTIC_BOOLEAN, true);
            intent.putExtra(KEY_PREFER_CLOSE_BUTTON_AT_TOOLBAR_BOOLEAN, true);
            intent.putExtra(KEY_URL_STRING, CommonDefine.H5URL_TALENT_APPLY());
            intent.putExtra(KEY_HIDE_SHARE_BTN_BOOLEAN, true);
        });
    }

    /**
     * 显示创建组合的页面
     */
    public static Func1<Context, Intent> an_CreateFundPage() {
        return simpleFragmentPageFunc(TraderFragments.CreateFundFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_IS_REQUIRED_AUTHENTIC_BOOLEAN, true);
            intent.putExtra(KEY_PREFER_CLOSE_BUTTON_AT_TOOLBAR_BOOLEAN, true);
            intent.putExtra(KEY_URL_STRING, CommonDefine.H5URL_CREATE_FUND());
            intent.putExtra(KEY_HIDE_SHARE_BTN_BOOLEAN, true);
        });
    }


    /**
     * 显示用户排行榜页面
     */
    public static Func1<Context, Intent> an_UserLeaderBoardPage(String rankID) {
        return simpleFragmentPageFunc(UserLeaderBoardFragment.class, intent -> {
            intent.putExtra(KEY_RANK_ID_STRING, rankID);
        });
    }

    /**
     * 显示开通模拟账号页面
     */
    public static Func1<Context, Intent> an_OpenSimulationAccountPage() {
        return simpleFragmentPageFunc(OpenSimulationAccountFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
        });
    }

    /**
     * 显示自选股编辑页面
     */
    public static Func1<Context, Intent> an_FocusStockEditPage() {
        return simpleFragmentPageFunc(MainFragments.FocusStockEditFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_IS_REQUIRED_OPEN_SIMULATION_ACCOUNT_BOOLEAN, true);
        });
    }

    /**
     * 显示设置止盈止损页
     */
    public static Func1<Context, Intent> an_StockSettingPage(int userID, String stockID, String range) {
        return simpleFragmentPageFunc(StockTradeFragments.StockSettingFragment.class, intent -> {
            intent.putExtra(KEY_USER_ID_INT, userID);
            intent.putExtra(KEY_STOCK_ID_STRING, stockID);
            intent.putExtra(KEY_RANGE_STRING, range);
        });
    }


    /**
     * 显示订单列表页
     */
    public static Func1<Context, Intent> an_StockOrderListRealPage(int dataSource, int fundID) {
        return simpleFragmentPageFunc(StockTradeFragments.StockOrderListFragment.class, intent -> {
            intent.putExtra(KEY_IS_REQUIRED_LOGIN_BOOLEAN, true);
            intent.putExtra(KEY_ORDER_DATA_SOURCE_INT, dataSource);
            intent.putExtra(KEY_FUND_ID_INT, fundID);
        });
    }

    private static Func1<Context, Intent> none() {
        return ctx -> {
            return new Intent();
        };
    }

    private static Func1<Context, Intent> simpleFragmentPageFunc(Class<? extends Fragment> clazz) {
        return simpleFragmentPageFunc(clazz, null);
    }

    private static Func1<Context, Intent> simpleFragmentPageFunc(Class<? extends Fragment> clazz, Action1<Intent> configureCallback) {
        return simpleFragmentPageFunc(clazz, 0, configureCallback);
    }

    private static final int FLAG_ACTIVITY_TRANSPARENT = 1;

    private static Func1<Context, Intent> simpleFragmentPageFunc(Class<? extends Fragment> clazz, int flags, Action1<Intent> configureCallback) {
        return ctx -> {
            boolean activityTransparent = (flags & FLAG_ACTIVITY_TRANSPARENT) != 0;
            Intent intent = new Intent(ctx, activityTransparent ? TransparentProxyActivity.class : CommonProxyActivity.class);
            intent.putExtra(KEY_FRAGMENT_CLASS_NAME, clazz.getName());
            if (configureCallback != null) {
                configureCallback.call(intent);
            }
            return intent;
        };
    }
}
