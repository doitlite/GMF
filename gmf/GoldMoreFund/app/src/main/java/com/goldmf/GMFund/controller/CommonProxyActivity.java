package com.goldmf.GMFund.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.dialog.ShareDialog;
import com.goldmf.GMFund.controller.protocol.UMShareHandlerProtocol;
import com.goldmf.GMFund.extension.UIControllerExtension;
import com.goldmf.GMFund.manager.common.ShareInfo;
import com.goldmf.GMFund.manager.score.ScoreManager;
import com.goldmf.GMFund.util.UmengUtil;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;

/**
 * Created by yale on 15/10/14.
 */
public class CommonProxyActivity extends FragmentStackActivity implements UMShareHandlerProtocol {
    public static final String KEY_FRAGMENT_CLASS_NAME = "gmf_fragment_class_name";
    public static final String KEY_DISABLE_SAVE_STATE_BOOLEAN = "gmf_disable_save_state";
    public static final String KEY_PREFER_CLOSE_BUTTON_AT_TOOLBAR_BOOLEAN = "gmf_prefer_close_button_at_toolbar";

    public static final String KEY_URL_STRING = "gmf_url";
    public static final String KEY_HAS_TOOLBAR_BOOLEAN = "gmf_has_toolbar";

    public static final String KEY_VC_TITLE = "gmf_vc_title";
    public static final String KEY_MONEY_TYPE_INT = "gmf_money_type";

    public static final String KEY_TRADER_ID_INT = "gmf_trader_id";

    public static final String KEY_LAUNCH_MAIN_ACTIVITY_BOOLEAN = "gmf_launch_main_activity";

    // 通用
    public static final String KEY_USER = "user";
    public static final String KEY_SOURCE_TYPE_INT = "source_type";
    public static final String KEY_USER_ID_INT = "user_id";
    public static final String KEY_TAB_IDX_INT = "tab_idx";
    public static final String KEY_SHOW_SHARE_DIALOG_BOOLEAN = "show_share_dialog";
    public static final String KEY_AMOUNT_DOUBLE = "gmf_amount";
    public static final String KEY_TEXT_STRING = "gmf_text";
    public static final String KEY_SHARE_INFO_OBJECT = "gmf_share_info";
    public static final String KEY_FLAGS_INT = "gmf_flags";
    public static final String KEY_GLOBAL_OBJ_ID_STRING = "gmf_global_obj_id";

    // 浏览器页面相关
    public static final String KEY_HIDE_SHARE_BTN_BOOLEAN = "gmf_hide_share_btn";

    // 优惠券专用
    public static final String KEY_COUPON_ID_STRING="coupon_id";
    public static final String KEY_COUPON_LIST_OIBJECT = "gmf_coupon_list";

    // 积分购买专用
    public static final String KEY_SCORE_BUY_VALUE="score_buy_value";

    // 图片预览相关
    public static final String KEY_IMAGE_LIST = "image_list";
    public static final String KEY_RECT_LIST = "rect_list";

    // 组合详情页专用
    public static final String KEY_FUND_ID_INT = "fund_id";
    public static final String KEY_FUND_LIST = "fund_list";
    public static final String KEY_FUND = "fund";

    // 绑定沪深账户银行卡专用
    public static final String KEY_NEXT_ACTION_TYPE_INT = "next_action_type";
    public static final int VALUE_NEXT_ACTION_TYPE_NONE = 1;
    public static final int VALUE_NEXT_ACTION_TYPE_INVEST = 1;
    public static final int VALUE_NEXT_ACTION_TYPE_WITHDRAW = 2;
    public static final int VALUE_NEXT_ACTION_TYPE_RECHARGE = 3;
    public static final int VALUE_NEXT_ACTION_TYPE_BUY = 4;

    // 充值 投资 提现页专用
    public static final String KEY_RECHARGE_DETAIL_INFO = "recharge_detail_info";
    public static final String KEY_PAY_ACTION="pay_action";

    // 会话详情专用
    public static final String KEY_MESSAGE_TYPE_INT = "message_type";
    public static final String KEY_LINK_ID_STRING = "link_id";
    public static final String KEY_SESSION_ID_STRING = "session_id";
    public static final String KEY_SESSION_HEAD_ID_INT = "head_id";
    public static final String KEY_MESSAGE_ID_STRING = "message_id";
    public static final String KEY_RATE_TYPE_INT = "rate_type"; //0为坑,1为值

    // 股票相关页专用
    public static final String KEY_STOCK_ID_STRING = "stock_id";
    public static final String KEY_RANGE_STRING = "range";
    public static final String KEY_STOCK_SIMPLE_OBJECT = "stock_simple";
    public static final String KEY_ORDER_DATA_SOURCE_INT = "order_data_source";
    public static final String KEY_FUND_NAME_STRING = "fund_name";

    // 搜索页面专用
    public static final String KEY_KEYWORD_STRING = "keyword";

    // 股票大赛页面专用
    public static final String KEY_STOCK_COMPETE_TYPE_PAGE = "compete_page";
    public static final String KEY_STOCK_COMPETE_ID_STRING = "compete_id";
    public static final String KEY_STOCK_COMPETE_STATE_INT = "compete_state";
    public static final String KEY_STOCK_COMPETITOR_TYPE_INT = "competitor_type";

    // 股市直播页面专用
    public static final String KEY_FIRST_VISIBLE_CHILD_IDX_INT = "first_visible_child_index";

    // 用户排行版专用
    public static final String KEY_RANK_ID_STRING = "rank_id";

    // 用户详情页面
    public static final String KEY_USER_NAME_STRING = "user_name";
    public static final String KEY_AVATAR_URL_STRING = "avatar_url";
    private UMShareAPI mShareAPI;


    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getIntent().getBooleanExtra(KEY_DISABLE_SAVE_STATE_BOOLEAN, true)) {
            savedInstanceState = null;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_stack_template_no_actionbar);

        if (savedInstanceState == null) {
            String fragmentClassName = getIntent().getStringExtra(KEY_FRAGMENT_CLASS_NAME);

            if (TextUtils.isEmpty(fragmentClassName)) {
                finish();
                return;
            }

            if (fragmentClassName.equals(LoginFragments.VerifyPhoneFragment.class.getName())) {
                MyApplication.SHARE_INSTANCE.mLoginPageShowing = true;
            }

            try {
                Class clazz = Class.forName(fragmentClassName);
                BaseFragment fragment = (BaseFragment) clazz.newInstance();
                fragment.init(getIntent().getExtras());
                pushFragment(fragment);
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }
    }

    @Override
    public void finish() {
        super.finish();
        doOnExit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doOnExit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (UmengUtil.handleOnActivityResult(mShareAPI, requestCode, resultCode, data)) {
            mShareAPI = null;
        }
    }

    private void doOnExit() {
        String fragmentClassName = getIntent().getStringExtra(KEY_FRAGMENT_CLASS_NAME);
        if (!TextUtils.isEmpty(fragmentClassName)) {
            if (fragmentClassName.equals(LoginFragments.VerifyPhoneFragment.class.getName())) {
                MyApplication.SHARE_INSTANCE.mLoginPageShowing = false;
            }
        }
    }

    @Override
    public void onPerformShare(ShareInfo shareInfo, ShareDialog.SharePlatform[] platforms) {
        ShareDialog shareDialog = new ShareDialog(this, shareInfo, platforms);
        shareDialog.setShareItemClickEventDelegate((dialog, platform) -> {
            dialog.dismiss();
            onPerformShare(shareInfo, platform);
        });
        shareDialog.show();
    }

    @Override
    public void onPerformShare(ShareInfo shareInfo, ShareDialog.SharePlatform platform) {
        //统计!上报 晒收益
        if (shareInfo.shareIncomeShowType != 0) {
            ScoreManager.getInstance().reportShareIncomeShow(shareInfo.shareIncomeShowType, null);
        }

        mShareAPI = UmengUtil.createShareService(this, shareInfo.title);
        UmengUtil.performShare(this, mShareAPI, platform.shareMedia, shareInfo, new UMShareListener() {
            @Override
            public void onResult(SHARE_MEDIA share_media) {
                UIControllerExtension.showToast(CommonProxyActivity.this, "分享成功");
            }

            @Override
            public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            }

            @Override
            public void onCancel(SHARE_MEDIA share_media) {
            }
        });
    }
}
