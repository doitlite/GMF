package com.goldmf.GMFund.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.controller.business.AwardController;
import com.goldmf.GMFund.controller.business.UserController;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.manager.fortune.RegisterBounty;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.mine.PhoneState;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.AppUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.util.UmengUtil.WXLoginInfo;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.ProgressButton;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.FragmentStackActivity.pushFragment;
import static com.goldmf.GMFund.controller.LoginUserFragments.ResetLoginPasswordFragment;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_AccountProfilePage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WebViewPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.BLUE_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.extension.MResultExtension.createObservableCountDown;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessageList;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setClickEvent;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.StringExtension.encryptPhoneNumberTransformer;
import static com.goldmf.GMFund.extension.StringExtension.map;
import static com.goldmf.GMFund.extension.UIControllerExtension.OnContinueRegisterListener;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.hideKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_unsignedNumberFormatter;

/**
 * Created by yale on 15/10/17.
 */
public class LoginFragments {
    public static class VerifyPhoneFragment extends SimpleFragment {
        private EditText mPhoneField;
        private ProgressButton mNextButton;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_verify_phone, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
            findToolbar(this).setNavigationOnClickListener(v -> {
                NotificationCenter.cancelLoginSubject.onNext(null);
                goBack(this);
                hideKeyboardFromWindow(this);
            });
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);

            // bind child views
            mPhoneField = v_findView(mContentSection, R.id.field_phone);
            mNextButton = v_findView(mContentSection, R.id.btn_next);

            // init child views
            v_setClick(this, R.id.btn_wechat, v -> performFetchWXAccessToken());
            v_setClick(mNextButton, this::performVerifyPhone);
            v_addTextChangedListener(mPhoneField, this::onPhoneNumberChanged);
            v_setText(mPhoneField, MineManager.getInstance().getPhone());
            mPhoneField.requestFocus();

//            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200L);
            UmengUtil.stat_enter_register_or_login_page(getActivity(), opt(this));

            consumeEvent(NotificationCenter.onEnterMainActivitySubject)
                    .setTag("enter_main")
                    .onNextFinish(ignored -> {
                        goBack(this);
                    })
                    .done();
        }


        @Override
        protected boolean onInterceptKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                NotificationCenter.cancelLoginSubject.onNext(null);
            }
            return super.onInterceptKeyDown(keyCode, event);
        }

        private void performFetchWXAccessToken() {

            UmengUtil.stat_click_event(UmengUtil.eEVENTIDWXLoginClick);

            hideKeyboardFromWindow(this);
            GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity(), "正在获取微信信息…");
            progressDialog.show();
            consumeEventMR(UmengUtil.fetchWXAccessToken(getActivity()))
                    .setTag("wx_login")
                    .onNextSuccess(response -> {
                        if (!progressDialog.isShowing()) {
                            progressDialog.show();
                        }
                        performWXLogin(progressDialog, response.data);
                    })
                    .onNextFail(response -> {
                        progressDialog.dismiss();
                        CharSequence msg = safeGet(() -> response.msg, "");
                        if (!TextUtils.isEmpty(msg)) {
                            showToast(this, getErrorMessage(response));
                        }
                    })
                    .done();
        }

        private void performWXLogin(GMFProgressDialog dialog, WXLoginInfo loginInfo) {
            consumeEventMR(UserController.loginByWXAccount(loginInfo))
                    .onNextStart(response -> {
                        dialog.dismiss();
                    })
                    .onNextSuccess(response -> {
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDWxLoginSuc);
                        goBack(this);
                    })
                    .onNextFail(response -> {
                        pushFragment(this, new CheckPhoneIsBindToWechatFragment().init(loginInfo));
                    })
                    .done();
        }

        private static CharSequence updateLoginButtonTextWithPhoneState(int state) {
            if (state == PhoneState.LOGINRESULT_NEED_LOGIN)
                return "登录";
            else if (state == PhoneState.LOGINRESULT_NEED_REG)
                return "注册";
            else
                return "登录/注册";
        }

        private void onPhoneNumberChanged(Editable editable) {
            final String phone = editable.toString().trim();
            if (phone.length() == 11) {
                consumeEventMR(UserController.verifyPhone(phone))
                        .onNextSuccess(response -> {
                            CharSequence buttonText = updateLoginButtonTextWithPhoneState(response.data.phoneState);
                            mNextButton.setText(buttonText, ProgressButton.Mode.Normal);
                        })
                        .done();
            } else {
                mNextButton.setText("登录/注册", ProgressButton.Mode.Normal);
            }
        }

        private void performVerifyPhone() {
            final String phone = mPhoneField.getText().toString().trim();
            if (TextUtils.isEmpty(phone) || phone.length() != 11) {
                showToast(this, "请输入正确的手机号码");
            } else {
                hideKeyboardFromWindow(this);
                mNextButton.setMode(ProgressButton.Mode.Loading);
                consumeEventMR(UserController.verifyPhone(phone))
                        .onNextStart(response -> {
                            mNextButton.setMode(ProgressButton.Mode.Normal);
                        })
                        .onNextSuccess(response -> {
                            switch (response.data.phoneState) {
                                case PhoneState.LOGINRESULT_NEED_LOGIN:
                                    pushFragment(this, new LoginFragment().init(response.data.name, phone));
                                    break;
                                case PhoneState.LOGINRESULT_NEED_REG:
                                    pushFragment(this, new RegistrationFragment().init(phone, response.data.invitedUser, null));
                                    break;
                                default:
                                    pushFragment(this, new NotInvitedFragment().init(phone));
                            }
                        })
                        .onNextFail(response -> {
                            showToast(this, getErrorMessage(response));
                        })
                        .done();
            }
        }
    }

    public static class CheckPhoneIsBindToWechatFragment extends SimpleFragment {
        private WXLoginInfo mLoginInfo;

        private PublishSubject<String> mOnPhoneChangedSubject = PublishSubject.create();

        private ProgressButton mNextButton;

        public CheckPhoneIsBindToWechatFragment init(WXLoginInfo loginInfo) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(WXLoginInfo.class.getName(), loginInfo);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mLoginInfo = (WXLoginInfo) getArguments().getSerializable(WXLoginInfo.class.getName());
            return inflater.inflate(R.layout.frag_verify_phone, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this));
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);

            mNextButton = v_findView(mContentSection, R.id.btn_next);

            mNextButton.setText("下一步", ProgressButton.Mode.Normal);
            mNextButton.setText("操作中...", ProgressButton.Mode.Loading);
            v_setGone(mContentSection, R.id.section_left_bottom);
            v_setText(mContentSection, R.id.label_hint, mLoginInfo.nickName + "，请输入手机号来完成登录");
            v_addTextChangedListener(mContentSection, R.id.field_phone, editable -> mOnPhoneChangedSubject.onNext(editable.toString()));
            v_setClick(mNextButton, v -> performBindWXAccount());

            consumeEvent(mOnPhoneChangedSubject.debounce(300, TimeUnit.MILLISECONDS))
                    .setTag("on_phone_change")
                    .onNextFinish(it -> performCheckPhone(it))
                    .done();


            v_findView(mContentSection, R.id.field_phone).requestLayout();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200L);
        }

        private boolean mLastIsValid = false;
        private String mLastValidPhone;
        private int mLastPhoneState;
        private User mLastInviteUser;

        private void performCheckPhone(String phone) {
            mLastIsValid = false;
            consumeEventMR(UserController.verifyPhone(phone))
                    .setTag("checkPhone")
                    .onNextSuccess(response -> {
                        PhoneState data = response.data;
                        mLastValidPhone = phone;
                        mLastPhoneState = data.phoneState;
                        mLastInviteUser = data.invitedUser;
                        mLastIsValid = true;
                    })
                    .onNextFail(response -> {
                        mLastIsValid = false;
                    })
                    .done();

            mLastValidPhone = phone;
        }

        private void performBindWXAccount() {

            if (mLastIsValid) {
                if (mLastPhoneState == PhoneState.LOGINRESULT_NEED_LOGIN) {
                    pushFragment(this, new BindPhoneToWXAccountFragment().init(mLastValidPhone, mLoginInfo));
                } else if (mLastPhoneState == PhoneState.LOGINRESULT_NEED_REG) {
                    pushFragment(this, new RegistrationFragment().init(mLastValidPhone, mLastInviteUser, mLoginInfo));
                }
                mNextButton.setMode(ProgressButton.Mode.Normal);
            } else {
                mNextButton.setMode(ProgressButton.Mode.Loading);
                String phone = ((EditText) v_findView(mContentSection, R.id.field_phone)).getText().toString();

                consumeEventMR(UserController.verifyPhone(phone))
                        .setTag("check_phone")
                        .onNextSuccess(response -> {
                            PhoneState data = response.data;
                            mLastValidPhone = phone;
                            mLastPhoneState = data.phoneState;
                            mLastInviteUser = data.invitedUser;
                            mLastIsValid = true;
                            performBindWXAccount();
                        })
                        .onNextFail(response -> {
                            mLastIsValid = false;
                            showToast(this, getErrorMessage(response));
                            mNextButton.setMode(ProgressButton.Mode.Normal);
                        })
                        .done();
            }
        }
    }


    public static class BindPhoneToWXAccountFragment extends SimpleFragment {
        private String mPhone;
        private WXLoginInfo mLoginInfo;

        private EditText mPhoneField;
        private EditText mPwdField;
        private ProgressButton mBindButton;

        public BindPhoneToWXAccountFragment init(String phone, WXLoginInfo loginInfo) {
            Bundle arguments = new Bundle();
            arguments.putString("phone", phone);
            arguments.putSerializable(WXLoginInfo.class.getName(), loginInfo);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mPhone = getArguments().getString("phone");
            mLoginInfo = (WXLoginInfo) getArguments().getSerializable(WXLoginInfo.class.getName());
            return inflater.inflate(R.layout.frag_bind_phone, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            // bind children
            mPhoneField = v_findView(mContentSection, R.id.field_phone);
            mPwdField = v_findView(mContentSection, R.id.field_pwd);
            mBindButton = v_findView(mContentSection, R.id.btn_bind);

            v_setText(mContentSection, R.id.label_hint, mLoginInfo.nickName + "，登录即可绑定");
            mPhoneField.setFocusableInTouchMode(false);
            mPhoneField.setText(mPhone);

            mPwdField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200L);

            v_setClick(mContentSection, R.id.btn_bind, v -> performBindWXAccount());
        }

        private void performBindWXAccount() {
            mBindButton.setMode(ProgressButton.Mode.Loading);
            if (MineManager.getInstance().isLoginOK()) {
                UserController.bindWXAccount(mLoginInfo)
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(response -> {
                            if (isSuccess(response)) {
                                pushFragment(this, new BindWXAccountSuccessFragment().init(true));
                            } else {
                                showToast(this, getErrorMessage(response));
                            }
                            mBindButton.setMode(ProgressButton.Mode.Normal);
                        })
                        .subscribe();
            } else {
                String phone = mPhone;
                String pwd = mPwdField.getText().toString();
                consumeEventMR(UserController.login(phone, pwd))
                        .onNextSuccess(response -> {
                            performBindWXAccount();
                        })
                        .onNextFail(response -> {
                            showToast(this, getErrorMessage(response));
                            mBindButton.setMode(ProgressButton.Mode.Normal);
                        })
                        .done();
            }
        }
    }

    public static class BindWXAccountSuccessFragment extends SimpleFragment {

        public BindWXAccountSuccessFragment init(boolean forceClose) {
            Bundle arguments = new Bundle();
            arguments.putBoolean("force_close", forceClose);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mForceFinishOnGoBack = getArguments().getBoolean("force_close", true);
            return inflater.inflate(R.layout.frag_bind_wechat_success, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            v_setClick(this, R.id.btn_done, v -> goBack(this));
        }
    }

    public static class LoginFragment extends BaseFragment {

        private TextView mHiLabel;
        private EditText mPasswordField;
        private ProgressButton mLoginButton;

        private String mName;
        private String mPhone;

        public LoginFragment init(String name, String phone) {
            Bundle arguments = new Bundle();
            arguments.putString("name", name);
            arguments.putString("phone", phone);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mName = getArguments().getString("name");
            mPhone = getArguments().getString("phone");
            return inflater.inflate(R.layout.frag_login, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this));
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);

            // bind child views
            mHiLabel = v_findView(view, R.id.label_hi);
            mPasswordField = v_findView(view, R.id.field_pwd);
            mLoginButton = v_findView(this, R.id.btn_login);

            // init child views
            mPasswordField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200L);
            v_setClick(mLoginButton, this::performLogin);
            v_setClick(view, R.id.btn_reset, v -> pushFragment(this, new ResetLoginPasswordFragment().init(mName, mPhone)));
            mHiLabel.setText(concatNoBreak(setFontSize(mName + "(" + map(mPhone, encryptPhoneNumberTransformer()) + ")", sp2px(17)), ", 欢迎回来"));

            mPasswordField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);
        }

        private void performLogin() {
            final String pwd = mPasswordField.getText().toString().trim();
            if (TextUtils.isEmpty(pwd)) {
                Toast.makeText(getActivity(), "密码不能为空", Toast.LENGTH_SHORT).show();
            } else {
                hideKeyboardFromWindow(this);
                mLoginButton.setMode(ProgressButton.Mode.Loading);
                String phone = mPhone;
                consumeEventMR(UserController.login(phone, pwd))
                        .onNextStart(response -> {
                            mLoginButton.setMode(ProgressButton.Mode.Normal);
                        })
                        .onNextSuccess(response -> {
                            getActivity().finish();
                            UmengUtil.stat_login_success_event(getActivity(), true, opt(response.errCode), opt(response.msg));
                        })
                        .onNextFail(response -> {
                            if (response.errCode == CommonDefine.LOGINRESULT_PASSWORD_WRONG) {
                                mPasswordField.setText("");
                            }
                            showToast(this, getErrorMessage(response));
                            UmengUtil.stat_login_success_event(getActivity(), false, opt(response.errCode), opt(response.msg));
                        })
                        .done();
            }
        }
    }

    public static class RegistrationFragment extends BaseFragment {

        private EditText mCodeField;
        private EditText mNameField;
        private EditText mPwdField;
        private EditText mInviteUserIdField;
        private Button mSendCodeButton;
        private ProgressButton mRegisterButton;

        private String mPhone;
        private User mInviteUser;
        private WXLoginInfo mLoginInfo;

        public RegistrationFragment init(String phone, User inviteUserOrNil, WXLoginInfo loginInfo) {
            Bundle arguments = new Bundle();
            arguments.putString("phone", phone);
            if (inviteUserOrNil != null) {
                arguments.putSerializable(User.class.getName(), inviteUserOrNil);
            }
            if (loginInfo != null) {
                arguments.putSerializable(WXLoginInfo.class.getName(), loginInfo);
            }
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mPhone = getArguments().getString("phone");
            mInviteUser = (User) getArguments().getSerializable(User.class.getName());
            mLoginInfo = (WXLoginInfo) getArguments().getSerializable(WXLoginInfo.class.getName());
            return inflater.inflate(R.layout.frag_registration, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this));
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            mCodeField = v_findView(view, R.id.field_code);
            mNameField = v_findView(view, R.id.field_name);
            mPwdField = v_findView(view, R.id.field_pwd);
            mInviteUserIdField = v_findView(view, R.id.field_invite_user_id);
            mSendCodeButton = v_findView(view, R.id.btn_send_code);
            mRegisterButton = v_findView(view, R.id.btn_register);
            v_addTextChangedListener(mCodeField, v_unsignedNumberFormatter());
            v_setClick(mSendCodeButton, this::performSendRegisterCode);
            v_setClick(mRegisterButton, this::performRegister);
            v_setText(this, R.id.label_hint, concatNoBreak("已发送验证码至", setFontSize(map(mPhone, encryptPhoneNumberTransformer()), sp2px(17))));
            TextView protocolLabel = v_findView(this, R.id.label_protocol);
            protocolLabel.setMovementMethod(LinkMovementMethod.getInstance());
            v_setText(protocolLabel, concatNoBreak("注册账号意味着同意《", setColor(setClickEvent("操盘侠用户协议", v -> showActivity(this, an_WebViewPage(CommonDefine.H5URL_UserAgreement()))), BLUE_COLOR), "》"));
            if (mInviteUser != null && mInviteUser.index > 0) {
                v_setText(view, R.id.field_invite_user_id, "" + mInviteUser.index);
                v_setText(view, R.id.label_invited_user_name, mInviteUser.getName());
            } else {
                v_setText(mInviteUserIdField, "");
                v_setText(view, R.id.label_invited_user_name, "");
                v_setGone(view, R.id.label_hint_invited_user);
            }
            v_addTextChangedListener(mInviteUserIdField, editable -> v_setText(this, R.id.label_invited_user_name, ""));

            mCodeField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);
            performSendRegisterCode();
        }

        protected void performRegister() {
            String phone = mPhone;
            String code = mCodeField.getText().toString().trim();
            String name = mNameField.getText().toString().trim();
            String pwd = mPwdField.getText().toString().trim();
            String inviteUserId = mInviteUserIdField.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                showToast(this, "验证码不能为空");
            } else if (TextUtils.isEmpty(name)) {
                showToast(this, "昵称不能为空");
            } else if (TextUtils.isEmpty(pwd)) {
                showToast(this, "密码不能为空");
            } else {
                mRegisterButton.setMode(ProgressButton.Mode.Loading);


                Observable<MResultsInfo<User>> observable = UserController.register(phone, pwd, name, code, inviteUserId)
                        .flatMap(response -> {
                            if (isSuccess(response) && mLoginInfo != null) {
                                UmengUtil.stat_click_event(UmengUtil.eEVENTIDWxLoginReg);
                                return UserController.bindWXAccount(mLoginInfo).map(it -> MResultExtension.map(it, response.data));
                            } else {
                                return Observable.just(response);
                            }
                        });
                consumeEventMR(observable)
                        .onNextFinish(response -> {
                            mRegisterButton.setMode(ProgressButton.Mode.Normal);
                            if (response.isSuccess) {
                                NotificationCenter.registrationSuccessSubject.onNext(response.data);
//                                showToast(this, "注册成功");
                                NotificationCenter.loginSubject.onNext(null);
                                pushFragment(this, new RegistrationSuccessFragment());

                                UmengUtil.stat_register_event(getActivity(), true, opt(response.errCode), opt(response.msg));
                                if (mInviteUser != null && mInviteUser.index > 0) {
                                    UmengUtil.stat_auto_inviter_register_event(getActivity(), true, opt(response.errCode), opt(response.msg));
                                } else if (inviteUserId.length() > 0) {
                                    UmengUtil.stat_manual_inviter_register_event(getActivity(), true, opt(response.errCode), opt(response.msg));
                                } else {
                                    UmengUtil.stat_no_inviter_register_event(getActivity(), true, opt(response.errCode), opt(response.msg));
                                }
                            } else {
                                //                                createAlertDialog(this, getErrorMessage(callback)).show();
                                createAlertDialog(this, getErrorMessageList(response), new OnContinueRegisterListener() {
                                    @Override
                                    public void onContinueRegister() {
                                        consumeEventMR(UserController.register(phone, pwd, name, code, null))
                                                .onNextFinish(registResponse -> {
                                                    mRegisterButton.setMode(ProgressButton.Mode.Normal);
                                                    if (registResponse.isSuccess) {
                                                        NotificationCenter.registrationSuccessSubject.onNext(registResponse.data);
                                                        showToast(RegistrationFragment.this, "注册成功");
                                                        NotificationCenter.loginSubject.onNext(null);
                                                        getActivity().finish();

                                                        UmengUtil.stat_register_event(getActivity(), true, opt(response.errCode), opt(response.msg));
                                                        UmengUtil.stat_no_inviter_register_event(getActivity(), true, opt(registResponse.errCode), opt(registResponse.msg));
                                                    } else {
                                                        createAlertDialog(RegistrationFragment.this, getErrorMessage(registResponse)).show();
                                                    }
                                                })
                                                .done();
                                    }

                                    @Override
                                    public void onCancel() {

                                    }
                                }).show();
                            }
                        })
                        .done();
            }
        }

        private void performSendRegisterCode() {
            String phone = mPhone;
            mSendCodeButton.setEnabled(false);
            consumeEventMR(UserController.sendRegistrationCode(phone))
                    .setTag("send_regist_code")
                    .onNextSuccess(response -> {
                        enableSendCodeButtonLater();
                    })
                    .onNextFail(response -> {
                        createAlertDialog(this, getErrorMessage(response)).show();
                        mSendCodeButton.setEnabled(true);
                    })
                    .done();
        }

        private void enableSendCodeButtonLater() {
            mSendCodeButton.setEnabled(false);
            consumeEvent(createObservableCountDown(60, 1000))
                    .setTag("count_down")
                    .onNextFinish(integer -> {
                        mSendCodeButton.setText("重新发送" + integer);
                    })
                    .onComplete(() -> {
                        mSendCodeButton.setText("获取验证码");
                        mSendCodeButton.setEnabled(true);
                    })
                    .done();
        }
    }

    public static class RegistrationSuccessFragment extends SimpleFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_registration_success, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            v_setVisible(mLoadingSection);
            v_setGone(mContentSection);
            consumeEventMR(AwardController.fetchRegistBounity())
                    .onNextFinish(response -> {
                        updateContentView(response.data);
                        v_setVisible(mContentSection);
                        v_setGone(mLoadingSection);
                    })
                    .done();

            mForceFinishOnGoBack = true;
        }

        public void updateContentView(@Nullable RegisterBounty bounty) {
            if (bounty == null) {
                v_setGone(this, R.id.img_award);
                v_setGone(this, R.id.label_award_desc);
            } else {
                v_setVisible(this, R.id.img_award);
                v_setVisible(this, R.id.label_award_desc);
                v_setImageUri(this, R.id.img_award, bounty.imageURL);
                v_setText(this, R.id.label_award_desc, bounty.bountyDesc);
            }
            v_setClick(this, R.id.btn_edit, v -> {
                mForceFinishOnGoBack = true;
                goBack(this);
                showActivity(this, an_AccountProfilePage());
            });
        }
    }

    public static class NotInvitedFragment extends BaseFragment {

        private String mPhone;

        NotInvitedFragment init(String phone) {
            Bundle arguments = new Bundle();
            arguments.putString("phone", phone);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mPhone = getArguments().getString("phone");
            return inflater.inflate(R.layout.frag_not_invited, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this));
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            v_setText(view, R.id.label_hint, getString(R.string.act_login_format_not_invited, mPhone));
            v_setClick(this, R.id.btn_open, v -> AppUtil.openPackage(this, "com.tencent.mm"));
        }
    }
}
