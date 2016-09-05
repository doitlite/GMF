package com.goldmf.GMFund.util;

import android.text.TextUtils;

import com.goldmf.GMFund.controller.AuthenticFragments;
import com.goldmf.GMFund.controller.AwardFragments.AwardHomeFragmentV2;
import com.goldmf.GMFund.controller.CommonFragments.AboutFragment;
import com.goldmf.GMFund.controller.CommonFragments.ViewPictureFragment;
import com.goldmf.GMFund.controller.FundFragments.FundDetailFragmentV2;
import com.goldmf.GMFund.controller.FundFragments.FundDetailPageInvestedMemberFragment;
import com.goldmf.GMFund.controller.LoginUserFragments.PrivacySettingFragment;
import com.goldmf.GMFund.controller.MainFragments.ConversationListFragment;
import com.goldmf.GMFund.controller.RechargeFragments.CNAccountMultipleRechargeFragment;
import com.goldmf.GMFund.controller.TraderFragments.MyManagedFundsFragment;
import com.goldmf.GMFund.controller.WebViewFragments.WebViewFragment;
import com.goldmf.GMFund.controller.chat.ChatFragments.ConversationDetailFragment;
import com.goldmf.GMFund.controller.chat.ChatFragments.SendGroupMessageFragment;
import com.goldmf.GMFund.controller.circle.CircleDetailFragment;
import com.goldmf.GMFund.controller.circle.CircleHintFragment;
import com.goldmf.GMFund.controller.circle.CircleListFragment;
import com.goldmf.GMFund.controller.circle.CircleListFragment.CircleListPageFragment;
import com.goldmf.GMFund.controller.circle.CircleRateUserListFragment;
import com.goldmf.GMFund.controller.dialog.DownloadDialog;
import com.goldmf.GMFund.controller.dialog.EnterTransactionPasswordDialog;
import com.goldmf.GMFund.controller.dialog.GMFBottomSheet;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.dialog.SetTransactionPasswordDialog;
import com.goldmf.GMFund.controller.dialog.ShareDialog;
import com.goldmf.GMFund.controller.dialog.TransactionDatePickerDialog;

import java.util.HashMap;

import static com.goldmf.GMFund.controller.AuthenticFragments.AuthenticFragment;
import static com.goldmf.GMFund.controller.CardFragments.BindCNCardFragment;
import static com.goldmf.GMFund.controller.CardFragments.BindCNCardWaitingFragment;
import static com.goldmf.GMFund.controller.CardFragments.ChooseBankFragment;
import static com.goldmf.GMFund.controller.CardFragments.ChooseProvinceOrCityFragment;
import static com.goldmf.GMFund.controller.CommonFragments.CashJournalFragment;
import static com.goldmf.GMFund.controller.CommonFragments.FeedbackFragment;
import static com.goldmf.GMFund.controller.CommonFragments.ResetTransactionPasswordFragment;
import static com.goldmf.GMFund.controller.CommonUserFragments.ChooseNameAndAvatarFragment;
import static com.goldmf.GMFund.controller.CommonUserFragments.UserLeaderBoardFragment;
import static com.goldmf.GMFund.controller.FocusImagePagerAdapter.FocusImageFragment;
import static com.goldmf.GMFund.controller.FocusImagePagerAdapter.PlatformInfoFocusImageFragment;
import static com.goldmf.GMFund.controller.FundFragments.AllFundFragment;
import static com.goldmf.GMFund.controller.FundFragments.AllFundPageListFragment;
import static com.goldmf.GMFund.controller.FundFragments.FundDetailPageWebFragment;
import static com.goldmf.GMFund.controller.FundFragments.FundProtocolFragment;
import static com.goldmf.GMFund.controller.IntroductionFragments.FullScreenImageFragment;
import static com.goldmf.GMFund.controller.IntroductionFragments.PPTHostFragment;
import static com.goldmf.GMFund.controller.IntroductionFragments.StaticIntroductionFragment;
import static com.goldmf.GMFund.controller.InvestFragments.InvestFundFragment;
import static com.goldmf.GMFund.controller.InvestFragments.InvestFundSuccessFragment;
import static com.goldmf.GMFund.controller.LoginFragments.LoginFragment;
import static com.goldmf.GMFund.controller.LoginFragments.NotInvitedFragment;
import static com.goldmf.GMFund.controller.LoginFragments.RegistrationFragment;
import static com.goldmf.GMFund.controller.LoginFragments.VerifyPhoneFragment;
import static com.goldmf.GMFund.controller.LoginUserFragments.AccountProfileFragment;
import static com.goldmf.GMFund.controller.LoginUserFragments.AvatarListFragment;
import static com.goldmf.GMFund.controller.LoginUserFragments.EditNameFragment;
import static com.goldmf.GMFund.controller.LoginUserFragments.ModifyPasswordFragment;
import static com.goldmf.GMFund.controller.LoginUserFragments.ModifyPhoneSuccessFragment;
import static com.goldmf.GMFund.controller.LoginUserFragments.ResetLoginPasswordFragment;
import static com.goldmf.GMFund.controller.LoginUserFragments.ResetPhoneFirstStepFragment;
import static com.goldmf.GMFund.controller.LoginUserFragments.ResetPhoneSecondStepFragment;
import static com.goldmf.GMFund.controller.LoginUserFragments.ViewPhoneFragment;
import static com.goldmf.GMFund.controller.RechargeFragments.CNAccountRechargeFragment;
import static com.goldmf.GMFund.controller.RechargeFragments.CNAccountRechargeSuccessFragment;
import static com.goldmf.GMFund.controller.RechargeFragments.HKAccountRechargeFragment;
import static com.goldmf.GMFund.controller.RechargeFragments.HKAccountRechargeSuccessFragment;
import static com.goldmf.GMFund.controller.StockAccountFragments.MyStockAccountDetailFragment;
import static com.goldmf.GMFund.controller.TraderFragments.MoreTraderFragment;
import static com.goldmf.GMFund.controller.WithdrawFragments.CNAccountWithdrawFragment;
import static com.goldmf.GMFund.controller.WithdrawFragments.CNAccountWithdrawSuccessFragment;
import static com.goldmf.GMFund.controller.WithdrawFragments.HKAccountWithdrawFragment;
import static com.goldmf.GMFund.controller.WithdrawFragments.HKAccountWithdrawSuccessFragment;
import static com.goldmf.GMFund.controller.circle.CircleDetailFragment.CircleDetailPageIntelligenceFragment;
import static com.goldmf.GMFund.controller.circle.CircleDetailFragment.CircleDetailPageMoodFragment;
import static com.goldmf.GMFund.controller.circle.WriteFragments.WriteIntelligenceFragment;
import static com.goldmf.GMFund.controller.circle.WriteFragments.WriteMoodFragment;

/**
 * Created by yale on 15/10/29.
 */
public class UIControllerNameMapper {
    private static HashMap<Class, String> sMap = new HashMap<>();

    static {
        // AuthenticFragments
        sMap.put(AuthenticFragments.ViewAuthenticStateFragment.class, "ViewAuthenticStateFragment");
        sMap.put(AuthenticFragment.class, "AuthenticFragment");

        // AwardFragments
        sMap.put(AwardHomeFragmentV2.class, "AwardHomeFragment");

        // CardFragments
        sMap.put(BindCNCardFragment.class, "BindCNCardFragment");
        sMap.put(BindCNCardWaitingFragment.class, "BindCNCardWaitingFragment");
        sMap.put(ChooseBankFragment.class, "ChooseBankFragment");
        sMap.put(ChooseProvinceOrCityFragment.class, "ChooseProvinceOrCityFragment");

        // CommonFragments
        sMap.put(AboutFragment.class, "AboutFragment");
        sMap.put(FeedbackFragment.class, "FeedbackFragment");
        sMap.put(ResetTransactionPasswordFragment.class, "ResetTransactionPasswordFragment");
        sMap.put(CashJournalFragment.class, "CashJournalFragment");
        sMap.put(ViewPictureFragment.class, "ViewPictureFragment");

        //CommonUserFragments
        sMap.put(ChooseNameAndAvatarFragment.class, "ChooseNameAndAvatarFragment");
        sMap.put(UserLeaderBoardFragment.class, "UserLeaderBoardFragment");


        //DevModeFragments ignore

        // FocusImagePagerAdapter
        sMap.put(FocusImageFragment.class, "FocusImageFragment");
        sMap.put(PlatformInfoFocusImageFragment.class, "PlatformInfoFocusImageFragment");

        // FundFragments
        sMap.put(FundDetailFragmentV2.class, "FundDetailFragment");
        sMap.put(FundDetailPageWebFragment.class, "FundDetailPageWebFragment");
        sMap.put(FundProtocolFragment.class, "FundProtocolFragment");
        sMap.put(FundDetailPageInvestedMemberFragment.class, "InvestedMemberListFragment");
        sMap.put(AllFundFragment.class, "AllFundFragment");
        sMap.put(AllFundPageListFragment.class, "AllFundPageListFragment");

        // IntroductionFragments
        sMap.put(FullScreenImageFragment.class, "FullScreenImageFragment");
        sMap.put(PPTHostFragment.class, "PPTHostFragment");
        sMap.put(StaticIntroductionFragment.class, "PPTHostFragment");

        // InvestFragments
        sMap.put(InvestFundFragment.class, "InvestFundFragment");
        sMap.put(InvestFundSuccessFragment.class, "InvestFundSuccessFragment");
        //        sMap.put(RechargeAndInvestFundFragment.class, "RechargeAndInvestFundFragment");
        //        sMap.put(RechargeAndInvestFundSuccessFragment.class, "RechargeAndInvestFundSuccessFragment");

        // LoginFragments
        sMap.put(VerifyPhoneFragment.class, "VerifyPhoneFragment");
        sMap.put(LoginFragment.class, "LoginFragment");
        sMap.put(RegistrationFragment.class, "RegistrationFragment");
        sMap.put(NotInvitedFragment.class, "NotInvitedFragment");

        // RechargeFragments
        sMap.put(CNAccountMultipleRechargeFragment.class, "CNAccountMultipleRechargeFragment");
        sMap.put(CNAccountRechargeFragment.class, "CNAccountRechargeFragment");
        sMap.put(CNAccountRechargeSuccessFragment.class, "CNAccountRechargeSuccessFragment");
        sMap.put(HKAccountRechargeFragment.class, "HKAccountRechargeFragment");
        sMap.put(HKAccountRechargeSuccessFragment.class, "HKAccountRechargeSuccessFragment");

        // TraderFragments
        sMap.put(MyManagedFundsFragment.class, "MyManagedFundsFragment");
        sMap.put(MoreTraderFragment.class, "MoreTraderFragment");

        // LoginUserFragments
        sMap.put(AccountProfileFragment.class, "AccountProfileFragment");
        sMap.put(EditNameFragment.class, "EditNameFragment");
        sMap.put(AvatarListFragment.class, "AvatarListFragment");
        sMap.put(ModifyPasswordFragment.class, "ModifyPasswordFragment");
        sMap.put(ResetPhoneFirstStepFragment.class, "ResetPhoneFirstStepFragment");
        sMap.put(ResetPhoneSecondStepFragment.class, "ResetPhoneSecondStepFragment");
        sMap.put(ModifyPhoneSuccessFragment.class, "ModifyPhoneSuccessFragment");
        sMap.put(PrivacySettingFragment.class, "PrivacySettingFragment");
        sMap.put(ViewPhoneFragment.class, "ViewPhoneFragment");
        sMap.put(MyStockAccountDetailFragment.class, "MyStockAccountDetailFragment");
        sMap.put(ResetLoginPasswordFragment.class, "ResetLoginPasswordFragment");

        // WebViewFragments
        sMap.put(WebViewFragment.class, "WebViewFragment");

        // WithdrawFragments
        sMap.put(CNAccountWithdrawFragment.class, "CNAccountWithdrawFragment");
        sMap.put(CNAccountWithdrawSuccessFragment.class, "CNAccountWithdrawSuccessFragment");
        sMap.put(HKAccountWithdrawFragment.class, "HKAccountWithdrawFragment");
        sMap.put(HKAccountWithdrawSuccessFragment.class, "HKAccountWithdrawSuccessFragment");

        // ChatFragments
        sMap.put(ConversationListFragment.class, "ConversationListFragment");
        sMap.put(ConversationDetailFragment.class, "ConversationDetailFragment");
        sMap.put(SendGroupMessageFragment.class, "SendGroupMessageFragment");

        //CircleFragments
        sMap.put(CircleDetailFragment.class, "CircleDetailFragment");
        sMap.put(CircleDetailPageIntelligenceFragment.class, "CircleDetailPageIntelligenceFragment");
        sMap.put(CircleDetailPageMoodFragment.class, "CircleDetailPageMoodFragment");
        sMap.put(CircleListFragment.class, "CircleListFragment");
        sMap.put(CircleListPageFragment.class, "CircleListPageFragment");
        sMap.put(CircleHintFragment.class, "CircleHintFragment");
        sMap.put(CircleRateUserListFragment.class, "CircleRateUserListFragment");
        sMap.put(WriteIntelligenceFragment.class, "WriteIntelligenceFragment");
        sMap.put(WriteMoodFragment.class, "WriteMoodFragment");

        // Dialogs
        sMap.put(DownloadDialog.class, "DownloadDialog");
        sMap.put(EnterTransactionPasswordDialog.class, "EnterTransactionPasswordDialog");
        sMap.put(GMFBottomSheet.class, "GMFBottomSheet");
        sMap.put(GMFDialog.class, "GMFDialog");
        sMap.put(SetTransactionPasswordDialog.class, "SetTransactionPasswordDialog");
        sMap.put(ShareDialog.class, "ShareDialog");
        sMap.put(TransactionDatePickerDialog.class, "ShareDialog");
    }

    public static String getName(Class clazz, String defaultVal) {
        String name = sMap.get(clazz);
        return !TextUtils.isEmpty(name) ? name : defaultVal;
    }
}
