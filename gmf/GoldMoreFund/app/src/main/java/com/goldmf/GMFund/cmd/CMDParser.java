package com.goldmf.GMFund.cmd;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.KeepClassProtocol;
import com.goldmf.GMFund.controller.BaseActivity;
import com.goldmf.GMFund.controller.BaseFragment;
import com.goldmf.GMFund.controller.FragmentStackActivity;
import com.goldmf.GMFund.controller.InvestFragments;
import com.goldmf.GMFund.controller.MainActivityV2;
import com.goldmf.GMFund.controller.MainFragments;
import com.goldmf.GMFund.controller.RechargeFragments;
import com.goldmf.GMFund.controller.ScoreFragments;
import com.goldmf.GMFund.controller.WebViewFragments;
import com.goldmf.GMFund.controller.WithdrawFragments;
import com.goldmf.GMFund.controller.dialog.ShareDialog.SharePlatform;
import com.goldmf.GMFund.controller.internal.RegexPatternHolder;
import com.goldmf.GMFund.controller.protocol.UMShareHandlerProtocol;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.model.User;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rx.functions.Action1;
import rx.functions.Func1;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.controller.MainFragments.ConversationListFragment;
import static com.goldmf.GMFund.controller.chat.ChatFragments.ConversationDetailFragment;
import static com.goldmf.GMFund.controller.chat.ChatFragments.sReceivedNewMessageSubject;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ADHomePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_AllTalentPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_AllTraderPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ApplyTalentPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ApplyTraderPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_AuthenticPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_AwardHomePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_BindCNCardPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CashJournalPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CircleDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CircleListPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ConversationDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ConversationListPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_CouponListPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_EditAddressPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_FundDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_InvestPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ManagedGroupsPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_MineHomePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_MyStockAccountDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_QuotationDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_RechargePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_RecommendFundListPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_RecommendTraderListPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ScoreBuyPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_ScoreHomePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockAnalysePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockHomePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockMarketLivePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockMatchDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_StockTradePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_UserDetailPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_UserLeaderBoardPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WebViewPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.util.UmengUtil.stat_enter_share_from_web_page;
import static com.goldmf.GMFund.util.UmengUtil.stat_launch_app_from_push;
import static com.goldmf.GMFund.util.UmengUtil.stat_launch_app_from_url;

/**
 * Created by yale on 15/9/12.
 */
public class CMDParser implements KeepClassProtocol {

    public static final int SOURCE_UNKNOWN = -1;
    public static final int SOURCE_IN_APP = 0;
    public static final int SOURCE_URL = 1;
    public static final int SOURCE_PUSH = 2;

    /**
     * 打开会话列表
     **/
    public static final String CMD_SESSION = "session";

    public static final String CMD_MESSAGE_HOME = "messagehome";

    public static int LAST_PUSH_SOURCE = SOURCE_UNKNOWN;

    private CMDParser() {
    }

    public static Action1<Context> parse(String link) {
        return parse(link, SOURCE_IN_APP);
    }

    @SuppressWarnings("unchecked")
    public static Action1<Context> parse(String link, int source) {
        LAST_PUSH_SOURCE = source;
        HashMap<String, String> params = toMap(link);
        if (params != null) {
            String methodName = getMethodNameByCmd(params.get("cmd"));
            try {
                Method method = CMDParser.class.getDeclaredMethod(methodName, HashMap.class);
                Object ret = method.invoke(null, params);
                if (ret != null && ret instanceof Action1) {
                    return (Action1<Context>) ret;
                }
            } catch (Exception e) {
                return emptyAction();
            }
            return emptyAction();
        } else {
            return emptyAction();
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static String parseCMDString(String link) {
        return safeGet(() -> toMap(link).get("cmd"), "");
    }

    public static HashMap<String, String> toMap(String link) {
        if (TextUtils.isEmpty(link)) {
            return null;
        }

        {
            if (link.startsWith("http") || RegexPatternHolder.MATCH_URL_ENTIRE.matcher(link.toLowerCase()).find()) {
                HashMap<String, String> params = new HashMap<>();
                params.put("cmd", "web");
                params.put("url", link.trim());
                return params;
            }
        }

        {
            HashMap<String, String> params = new HashMap<>();
            if (RegexPatternHolder.MATCH_VA_PAIR_ENTIRE.matcher(link).matches()) {
                //合法的link
                Matcher matcher = RegexPatternHolder.MATCH_SINGLE_PAIR.matcher(link);
                while (matcher.find()) {
                    String key = matcher.group(1);
                    String value = safeGet(() -> URLDecoder.decode(matcher.group(2), "UTF-8"), matcher.group(2));
                    params.put(key, value);
                }
            }

            if (params.containsKey("cmd")) {
                return params;
            }
        }

        return null;
    }

    private static String getMethodNameByCmd(String cmd) {
        return "parse_" + cmd + "Command";
    }

    private static Action1<Context> emptyAction() {
        return (ctx) -> {
        };
    }

    @Nullable
    public static String parseURLWithWebCommandLink(String link) {
        HashMap<String, String> params = CMDParser.toMap(link);
        if (params != null && params.get("url") != null) {
            return params.get("url");
        }
        return null;
    }

    private static Action1<Context> parse_webCommand(HashMap<String, String> params) {
        Log.e("AAA", "parseWebCommand");
        if (params.containsKey("url")) {
            String url = params.get("url");
            return ctx -> showActivity(ctx, an_WebViewPage(url));
        } else {
            return emptyAction();
        }
    }

    private static Action1<Context> parse_browserCommand(HashMap<String, String> params) {
        if (params.containsKey("url")) {
            String url = params.get("url");
            return ctx -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(url));
                if (!(ctx instanceof Activity)) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                ctx.startActivity(intent);
                if (LAST_PUSH_SOURCE == SOURCE_PUSH) {
                    stat_launch_app_from_push(ctx, Optional.of(null));
                } else if (LAST_PUSH_SOURCE == SOURCE_URL) {
                    stat_launch_app_from_url(ctx, Optional.of(null));
                }
            };
        } else {
            return emptyAction();
        }
    }

    public static String create_portfolioCommand(int fundID) {
        return "cmd=portfolio&id=" + fundID;
    }

    private static Action1<Context> parse_portfolioCommand(HashMap<String, String> params) {
        if (params.containsKey("id") && TextUtils.isDigitsOnly(params.get("id"))) {
            int fundId = Integer.valueOf(params.get("id"));
            return ctx -> showActivity(ctx, an_FundDetailPage(fundId));
        } else {
            return emptyAction();
        }
    }

    private static Action1<Context> parse_traderCommand(HashMap<String, String> params) {
        if (params.containsKey("id") && TextUtils.isDigitsOnly(params.get("id"))) {
            String traderId = params.get("id");
            User user = new User();
            user.index = Integer.valueOf(traderId);
            return ctx -> showActivity(ctx, an_UserDetailPage(user));
        } else {
            return emptyAction();
        }
    }

    private static Action1<Context> parse_promotionhomeCommand(HashMap<String, String> params) {
        return ctx -> {
            if (isAtMainActivity()) {
                MainActivityV2.SELECT_HOME_TAB_SUBJECT.onNext(Pair.create(0, 0));
            } else {
                showActivity(ctx, an_ADHomePage());
            }
        };
    }

    private static Action1<Context> parse_investCommand(HashMap<String, String> params) {
        return ctx -> {
            if (isAtMainActivity()) {
                MainActivityV2.SELECT_HOME_TAB_SUBJECT.onNext(Pair.create(2, 0));
            } else {
                showActivity(ctx, an_RecommendFundListPage());
            }
        };
    }

    private static Action1<Context> parse_traderhomeCommand(HashMap<String, String> params) {
        return ctx -> {
            if (isAtMainActivity()) {
                MainActivityV2.SELECT_HOME_TAB_SUBJECT.onNext(Pair.create(2, 0));
            } else {
                showActivity(ctx, an_RecommendTraderListPage());
            }
        };
    }

    private static Action1<Context> parse_messagehomeCommand(HashMap<String, String> params) {
        return parse_messagehomeCommandImpl(params, false);
    }

    public static Action1<Context> parse_messagehomeCommandImpl(HashMap<String, String> params, boolean disallowEnterActivity) {
        if (isAtConversationRelatedActivity()) {
            return ctx -> sReceivedNewMessageSubject.onNext(null);
        } else {
            if (!disallowEnterActivity) {
                if (isAtMainActivity()) {
                    MainActivityV2.SELECT_HOME_TAB_SUBJECT.onNext(Pair.create(3, 0));
                } else {
                    return ctx -> showActivity(ctx, an_ConversationListPage());
                }
            }
        }

        return emptyAction();
    }

    private static Action1<Context> parse_minehomeCommand(HashMap<String, String> params) {
        return ctx -> {
            if (isAtMainActivity()) {
                MainActivityV2.SELECT_HOME_TAB_SUBJECT.onNext(Pair.create(4, 0));
            } else {
                showActivity(ctx, an_MineHomePage());
            }
        };
    }


    private static Action1<Context> parse_userhomeCommand(HashMap<String, String> params) {
        if (params.containsKey("id")) {
            String userID = params.get("id");
            return ctx -> {
                User user = new User();
                user.index = Integer.valueOf(userID);
                showActivity(ctx, an_UserDetailPage(user));
            };
        }

        return emptyAction();
    }

    public static String create_stockCommand(String stockID) {
        return String.format("cmd=stock&id=%s", stockID);
    }

    private static Action1<Context> parse_stockCommand(HashMap<String, String> params) {
        String stockID = params.get("id");
        if (!TextUtils.isEmpty(stockID)) {
            return ctx -> {
                showActivity(ctx, an_QuotationDetailPage(stockID));
            };
        }

        return emptyAction();
    }

    private static Action1<Context> parse_allTraderCommand(HashMap<String, String> params) {
        return ctx -> {
            showActivity(ctx, an_AllTraderPage());
        };
    }

    private static Action1<Context> parse_allTalentCommand(HashMap<String, String> params) {
        return ctx -> {
            showActivity(ctx, an_AllTalentPage());
        };
    }

    private static Action1<Context> parse_stockPostionCommand(HashMap<String, String> params) {
        int userID = safeGet(() -> Integer.valueOf(params.get("id")), -1);
        return ctx -> {
            showActivity(ctx, an_StockTradePage(userID, "", 0));
        };
    }

    private static Action1<Context> parse_incomeAnalysisCommand(HashMap<String, String> params) {
        if (params.containsKey("id") && params.containsKey("stock") && params.containsKey("range")) {
            String userID = params.get("id");
            String stockID = params.get("stock");
            String range = params.get("range");
            return ctx -> {
                showActivity(ctx, an_StockAnalysePage(userID, stockID, range));
            };
        }

        return emptyAction();
    }

    private static Action1<Context> parse_sessionCommand(HashMap<String, String> params) {
        return parse_sessionCommandImpl(params, false);
    }

    private static Action1<Context> parse_xiabiCommand(HashMap<String, String> params) {
        int tabIdx = safeGet(() -> Integer.valueOf(params.get("id")), 0);
        return ctx -> {
            showActivity(ctx, an_ScoreHomePage(tabIdx));
        };
    }

    public static Action1<Context> parse_sessionCommandImpl(HashMap<String, String> params, boolean disallowEnterActivity) {
        if (params.containsKey("id") && params.containsKey("type") && TextUtils.isDigitsOnly(params.get("type"))) {
            int messageType = Integer.valueOf(params.get("type"));
            String linkId = params.get("id");
            if (isAtConversationRelatedActivity()) {
                return ctx -> sReceivedNewMessageSubject.onNext(null);
            } else {
                if (!disallowEnterActivity) {
                    return ctx -> showActivity(ctx, an_ConversationDetailPage( messageType, linkId, ""));
                }
            }

        }
        return emptyAction();
    }

    private static Action1<Context> parse_awardCommand(HashMap<String, String> params) {
        return ctx -> showActivity(ctx, an_AwardHomePage(Optional.of(null)));
    }

    private static Action1<Context> parse_joinCommand(HashMap<String, String> params) {
        if (params.containsKey("id") && TextUtils.isDigitsOnly(params.get("id"))) {
            int fundId = Integer.valueOf(params.get("id"));
            return ctx -> {
                showActivity(ctx, an_InvestPage(fundId));
            };
        } else {
            return emptyAction();
        }
    }

    private static Action1<Context> parse_cnaccountCommand(HashMap<String, String> params) {
        return ctx -> showActivity(ctx, an_MyStockAccountDetailPage(FundBrief.Money_Type.CN));
    }

    private static Action1<Context> parse_hkaccountCommand(HashMap<String, String> params) {
        return ctx -> showActivity(ctx, an_MyStockAccountDetailPage(FundBrief.Money_Type.HK));
    }

    private static Action1<Context> parse_cashdetailCommand(HashMap<String, String> params) {
        return ctx -> showActivity(ctx, an_CashJournalPage(Optional.of(null)));
    }

    private static Action1<Context> parse_shareCommand(HashMap<String, String> params) {
        if (params.containsKey("title") && params.containsKey("subtitle") && params.containsKey("url") && params.containsKey("image")) {
            String title = params.get("title");
            String content = params.get("subtitle");
            String url = params.get("url");
            String image = params.get("image");
            final ShareInfo shareInfo = new ShareInfo();
            shareInfo.url = url;
            shareInfo.imageUrl = image;
            shareInfo.title = title;
            shareInfo.msg = content;
            return ctx -> {
                if (ctx instanceof UMShareHandlerProtocol) {
                    UMShareHandlerProtocol shareHandler = (UMShareHandlerProtocol) ctx;
                    shareHandler.onPerformShare(shareInfo, parseSharePlatforms(params.get("platforms")));
                }

                boolean atFragmentStackActivity = safeGet(() -> MyApplication.getTopActivityOrNil().get() instanceof FragmentStackActivity, false);
                if (atFragmentStackActivity) {
                    FragmentStackActivity activity = (FragmentStackActivity) MyApplication.getTopActivityOrNil().get();
                    if (!activity.mFragmentStack.isEmpty()) {
                        BaseFragment fragment = activity.mFragmentStack.peek();
                        if (fragment instanceof WebViewFragments.WebViewFragment) {
                            stat_enter_share_from_web_page(ctx, Optional.of(fragment));
                        }
                    }
                }
            };
        } else {
            return emptyAction();
        }
    }

    private static Action1<Context> parse_setShareContentCommand(HashMap<String, String> params) {
        if (params.containsKey("title") && params.containsKey("subtitle") && params.containsKey("url") && params.containsKey("image")) {
            String title = params.get("title");
            String content = params.get("subtitle");
            String url = params.get("url");
            String image = params.get("image");
            final ShareInfo shareInfo = new ShareInfo();
            shareInfo.url = url;
            shareInfo.imageUrl = image;
            shareInfo.title = title;
            shareInfo.msg = content;
            SharePlatform[] platforms = parseSharePlatforms(params.get("platforms"));
            return ctx -> {
                WebViewFragments.sEditDefaultShareInfoSubject.onNext(Pair.create(shareInfo, platforms));
            };
        } else {
            return emptyAction();
        }
    }

    private static Action1<Context> parse_realNameCommand(HashMap<String, String> params) {
        return ctx -> {
            showActivity(ctx, an_AuthenticPage());
        };
    }

    private static Action1<Context> parse_screenShotShareCommand(HashMap<String, String> params) {
        if (params.containsKey("title") && params.containsKey("subtitle")) {
            String title = params.get("title");
            String content = params.get("subtitle");
            int incomeType = safeGet(() -> Integer.valueOf(params.get("income_type")), 0);
            final ShareInfo shareInfo = new ShareInfo();
            shareInfo.title = title;
            shareInfo.msg = content;
            shareInfo.shareIncomeShowType = incomeType;
            SharePlatform[] platforms = parseSharePlatforms(params.get("platforms"));
            return ctx -> {
                WebViewFragments.sPerformScreenShotShareSubject.onNext(Pair.create(shareInfo, platforms));
            };
        } else {
            return emptyAction();
        }
    }

    private static Action1<Context> parse_showShareBtnCommand(HashMap<String, String> params) {
        return ctx -> {
            WebViewFragments.sShowShareButtonSubject.onNext(null);
        };
    }

    private static Action1<Context> parse_hideShareBtnCommand(HashMap<String, String> params) {
        return ctx -> {
            WebViewFragments.sHideShareButtonSubject.onNext(null);
        };
    }

    private static Action1<Context> parse_stockGameDetailCommand(HashMap<String, String> params) {
        if (params.containsKey("id") && TextUtils.isDigitsOnly(params.get("id"))) {
            String matchId = params.get("id");
            return ctx -> showActivity(ctx, an_StockMatchDetailPage(matchId));
        }
        return emptyAction();
    }

    private static Action1<Context> parse_topTradersCommand(HashMap<String, String> params) {
        if (params.containsKey("id")) {
            String id = params.get("id");
            return ctx -> showActivity(ctx, an_UserLeaderBoardPage(id));
        }

        return emptyAction();
    }

    private static Action1<Context> parse_stockhomeCommand(HashMap<String, String> params) {
        return ctx -> {
            if (isAtMainActivity()) {
                MainActivityV2.SELECT_HOME_TAB_SUBJECT.onNext(Pair.create(1, 1));
            } else {
                showActivity(ctx, an_StockHomePage(1));
            }
            MainFragments.sStockSelectTabSubject.onNext(1);

        };
    }

    private static Action1<Context> parse_stockGameCommand(HashMap<String, String> params) {
        return ctx -> {
            if (isAtMainActivity()) {
                MainActivityV2.SELECT_HOME_TAB_SUBJECT.onNext(Pair.create(1, 2));
            } else {
                showActivity(ctx, an_StockHomePage(2));
            }
            MainFragments.sStockSelectTabSubject.onNext(2);

        };
    }

    private static Action1<Context> parse_marketLiveCommand(HashMap<String, String> params) {
        return ctx -> showActivity(ctx, an_StockMarketLivePage(0));
    }


    private static Action1<Context> parse_bindCardCommand(HashMap<String, String> params) {
        return ctx -> showActivity(ctx, an_BindCNCardPage());
    }

    private static Action1<Context> parse_modifyAddressCommand(HashMap<String, String> params) {
        return ctx -> showActivity(ctx, an_EditAddressPage());
    }

    private static Action1<Context> parse_xiabiChangedCommand(HashMap<String, String> params) {
        return ctx -> {
            NotificationCenter.scoreChangedSubject.onNext(null);
        };
    }

    private static Action1<Context> parse_stockBarSessionCommand(HashMap<String, String> params) {
        if (params.containsKey("id") && params.containsKey("type")) {
            return ctx -> {
                String linkID = params.get("id");
                int messageType = safeGet(() -> Integer.valueOf(params.get("type")), 0);
                showActivity(ctx, an_CircleListPage(messageType, linkID, ""));
            };
        }

        return emptyAction();
    }

    private static Action1<Context> parse_stockBarMsgDetailCommand(HashMap<String, String> params) {
        if (params.containsKey("id") && params.containsKey("type") && params.containsKey("msgId")) {
            return ctx -> {
                String linkID = params.get("id");
                int messageType = safeGet(() -> Integer.valueOf(params.get("type")), 0);
                String messageID = params.get("msgId");
                showActivity(ctx, an_CircleDetailPage(messageType, linkID, "", -1, messageID));
            };
        }

        return emptyAction();
    }

    private static Action1<Context> parse_checkMyFundCommand(HashMap<String, String> params) {
        return ctx -> {
            showActivity(ctx, an_ManagedGroupsPage());
        };
    }

    private static Action1<Context> parse_investfundCommand(HashMap<String, String> params) {
        if (containKeys(params, "id", "secondId", "amount", "market")) {
            Integer fundID = safeGet(() -> Integer.valueOf(params.get("id")), null);
            String userID = safeGet(() -> params.get("secondId"), "");
            Double amount = safeGet(() -> Double.valueOf(params.get("amount")), null);
            Integer moneyType = safeGet(() -> Integer.valueOf(params.get("market")), null);
            if (fundID != null && moneyType != null) {
                boolean isLoginOK = safeGet(() -> MineManager.getInstance().getmMe().isLoginOk(), false);
                String loginUserID = safeGet(() -> String.valueOf(MineManager.getInstance().getmMe().index), "");
                if (isLoginOK && userID.equals(loginUserID)) {
                    return ctx -> {
                        if (moneyType == FundBrief.Money_Type.CN) {
                            showActivity(ctx, an_InvestPage(fundID, amount), BaseActivity.TRANSACTION_DIRECTION.NONE);
                        }
                    };
                }
                return emptyAction();
            }
        }

        return emptyAction();
    }

    private static Action1<Context> parse_rechargeCommand(HashMap<String, String> params) {
        if (containKeys(params, "amount", "market", "secondId")) {
            Integer moneyType = safeGet(() -> Integer.valueOf(params.get("market")), null);
            String userID = safeGet(() -> params.get("secondId"), "");
            Double amount = safeGet(() -> Double.valueOf(params.get("amount")), null);
            if (moneyType != null) {
                boolean isLoginOK = safeGet(() -> MineManager.getInstance().isLoginOK(), false);
                String loginUserID = safeGet(() -> String.valueOf(MineManager.getInstance().getmMe().index), "");
                if (isLoginOK && userID.equals(loginUserID)) {
                    return ctx -> {
                        showActivity(ctx, an_RechargePage(moneyType, amount));
                    };
                }
                return emptyAction();
            }
        }

        return emptyAction();
    }

    private static Action1<Context> parse_webRechargeCommand(HashMap<String, String> params) {
        return ctx -> {
            int value = safeGet(() -> Integer.valueOf(params.get("value")), 0);
            String title = safeGet(() -> params.get("title"), "");
            RechargeFragments.sWebRechargeSuccessSubject.onNext(Pair.create(value, title));
        };
    }

    private static Action1<Context> parse_webInvestCommand(HashMap<String, String> params) {
        return ctx -> {
            int value = safeGet(() -> Integer.valueOf(params.get("value")), 0);
            String title = safeGet(() -> params.get("title"), "");
            InvestFragments.InvestFundFragment.sWebInvestSuccessSubject.onNext(Pair.create(value, title));
        };
    }

    private static Action1<Context> parse_webWithdrawCommand(HashMap<String, String> params) {
        return ctx -> {
            int value = safeGet(() -> Integer.valueOf(params.get("value")), 0);
            String title = safeGet(() -> params.get("title"), "");
            WithdrawFragments.sWebWithdrawSuccessSubject.onNext(Pair.create(value, title));
        };
    }

    private static Action1<Context> parse_webCostCommand(HashMap<String, String> params) {
        return ctx -> {
            int value = safeGet(() -> Integer.valueOf(params.get("value")), 0);
            String title = safeGet(() -> params.get("title"), "");
            ScoreFragments.sWebBuyScoreSuccessSubject.onNext(Pair.create(value, title));
        };
    }

    private static Action1<Context> parse_BuyXiabiCommand(HashMap<String, String> params) {
        if (params.containsKey("value")) {
            return ctx -> {
                int value = safeGet(() -> Integer.valueOf(params.get("value")), 0);
                showActivity(ctx, an_ScoreBuyPage(value));
            };
        }
        return emptyAction();
    }

    private static Action1<Context> parse_cancelToFinishButtonCommand(HashMap<String, String> params) {
        return ctx -> {
            NotificationCenter.disallowInterceptGoBackSubject.onNext(null);
        };
    }

    private static Action1<Context> parse_niurenApplyCommand(HashMap<String, String> params) {
        return ctx -> {
            showActivity(ctx, an_ApplyTalentPage());
        };
    }

    private static Action1<Context> parse_traderApplyCommand(HashMap<String, String> params) {
        return ctx -> {
            showActivity(ctx, an_ApplyTraderPage());
        };
    }

    private static Action1<Context> parse_openCouponCommand(HashMap<String, String> params) {
        return ctx -> {
            showActivity(ctx, an_CouponListPage());
        };
    }

    private static Action1<Context> parse_orderStatusChangedCommand(HashMap<String, String> params) {
        return ctx -> {
            NotificationCenter.currentTradingPageOrderStatusChange.onNext(null);
        };
    }

    private static boolean isAtMainActivity() {
        return safeGet(() -> MyApplication.getTopActivityOrNil().get() instanceof MainActivityV2, false);
    }

    private static boolean isAtConversationRelatedActivity() {
        boolean atFragmentStackActivity = safeGet(() -> MyApplication.getTopActivityOrNil().get() instanceof FragmentStackActivity, false);
        if (atFragmentStackActivity) {
            FragmentStackActivity activity = (FragmentStackActivity) MyApplication.getTopActivityOrNil().get();
            if (!activity.mFragmentStack.isEmpty()) {
                BaseFragment fragment = activity.mFragmentStack.peek();
                if (fragment instanceof ConversationListFragment || fragment instanceof ConversationDetailFragment) {
                    return true;
                }
            }
        }
        return false;
    }

    private static SharePlatform[] parseSharePlatforms(String data) {
        if (!TextUtils.isEmpty(data)) {
            Func1<String, SharePlatform> toSharePlatform = text -> {
                if (text.equalsIgnoreCase("wx_friend"))
                    return SharePlatform.WX;
                else if (text.equalsIgnoreCase("wx_circle"))
                    return SharePlatform.WX_CIRCLE;
                else if (text.equalsIgnoreCase("qq"))
                    return SharePlatform.QQ;
                else if (text.equalsIgnoreCase("qzone"))
                    return SharePlatform.QZONE;
                else if (text.equalsIgnoreCase("sms"))
                    return SharePlatform.SMS;
                else if (text.equalsIgnoreCase("copy"))
                    return SharePlatform.COPY;
                else if (text.equalsIgnoreCase("sina"))
                    return SharePlatform.SINA;
                return null;
            };


            LinkedList<SharePlatform> platforms = new LinkedList<>();
            Pattern pattern = Pattern.compile("[\\w_)]*");
            Matcher matcher = pattern.matcher(data);
            while (matcher.find()) {
                SharePlatform platformOrNil = toSharePlatform.call(matcher.group());
                if (platformOrNil != null) {
                    platforms.add(platformOrNil);
                }
            }
            if (!platforms.isEmpty()) {
                SharePlatform[] ret = new SharePlatform[platforms.size()];
                platforms.toArray(ret);
                return ret;
            }
        }
        return new SharePlatform[0];
    }

    private static boolean containKeys(HashMap<String, String> params, String... keys) {
        if (params != null && keys != null && keys.length > 0) {
            for (String key : keys) {
                if (!params.containsKey(key)) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }
}

