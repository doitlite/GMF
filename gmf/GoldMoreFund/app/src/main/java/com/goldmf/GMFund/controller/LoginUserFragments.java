package com.goldmf.GMFund.controller;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.CardFragments.ChooseProvinceOrCityFragment;
import com.goldmf.GMFund.controller.business.UserController;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.internal.PickAvatarHelper;
import com.goldmf.GMFund.controller.protocol.VCStateDataProtocol;
import com.goldmf.GMFund.extension.StringExtension;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.Mine.ShippingAddress;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.manager.mine.MineManager.VerifyCode;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.util.StringFactoryUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.BasicCell;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.ProgressButton;
import com.goldmf.GMFund.widget.ToggleCell;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import yale.extension.common.Optional;
import yale.extension.common.rx.ProxyCompositionSubscription;

import static com.goldmf.GMFund.controller.CardFragments.ChooseProvinceOrCityFragment.TYPE_SETUP_ADDRESS;
import static com.goldmf.GMFund.controller.CardFragments.ChooseProvinceOrCityFragment.TYPE_SETUP_CITY;
import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.FragmentStackActivity.pushFragment;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WebViewPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.extension.MResultExtension.cast;
import static com.goldmf.GMFund.extension.MResultExtension.createObservableCountDown;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.MResultExtension.map;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setFontSize;
import static com.goldmf.GMFund.extension.StringExtension.encryptPhoneNumberTransformer;
import static com.goldmf.GMFund.extension.StringExtension.notEmpty;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.createErrorDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.hideKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showAlertDialogOrToastWithFragment;
import static com.goldmf.GMFund.extension.UIControllerExtension.showKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.sp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setGone;
import static com.goldmf.GMFund.extension.ViewExtension.v_setImageUri;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_setVisible;
import static com.goldmf.GMFund.extension.ViewExtension.v_unsignedNumberFormatter;
import static com.goldmf.GMFund.model.User.User_Type.isTrader;

/**
 * Created by yale on 15/10/14.
 */
public class LoginUserFragments {
    private LoginUserFragments() {
    }

    public static class AccountProfileFragment extends SimpleFragment {

        private static class VCStateData implements VCStateDataProtocol<AccountProfileFragment> {

            File[] pictureFile;

            public VCStateData() {

            }

            @Override
            public VCStateData init(AccountProfileFragment fragment) {
                pictureFile = fragment.mPictureFile;
                return this;
            }

            @Override
            public void restore(AccountProfileFragment fragment) {
                safeCall(() -> fragment.mPictureFile = pictureFile);
            }

            @Override
            public int describeContents() {
                return 0;
            }


            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeSerializable(this.pictureFile);
            }

            protected VCStateData(Parcel in) {
                this.pictureFile = (File[]) in.readSerializable();
            }

            public static final Creator<VCStateData> CREATOR = new Creator<VCStateData>() {
                @Override
                public VCStateData createFromParcel(Parcel source) {
                    return new VCStateData(source);
                }

                @Override
                public VCStateData[] newArray(int size) {
                    return new VCStateData[size];
                }
            };
        }

        private BasicCell mEditNameCell;
        private BasicCell mVerifyNameCell;
        private BasicCell mPhoneCell;
        private BasicCell mRiskCell;
        private BasicCell mWXCell;
        private File[] mPictureFile;
        private ProxyCompositionSubscription mRiskTestEventSubscription = new ProxyCompositionSubscription();
        private boolean mIsDataSetFresh = false;
        private BasicCell mEditLocationCell;
        private BasicCell mEditAddressCell;
        private String title = "选择常居地";
        private boolean isTrader;
        private boolean isTalent;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_account_profile, container, false);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putSerializable("vc_data", new VCStateData().init(this));
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            isTrader = safeGet(() -> isTrader(MineManager.getInstance().getmMe().type), false);
            isTalent = safeGet(() -> MineManager.getInstance().getmMe().type == User.User_Type.Talent, false);

            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            updateTitle(getString(R.string.act_edit_profile_title_main));

            if (savedInstanceState != null) {
                VCStateData data= (VCStateData) savedInstanceState.getSerializable("vc_data");
                if (data != null)
                    data.restore(this);
            }

            //bind child views
            mEditNameCell = v_findView(mContentSection, R.id.cell_edit_name);
            mVerifyNameCell = v_findView(mContentSection, R.id.cell_verify_name);
            mEditLocationCell = v_findView(mContentSection, R.id.cell_edit_location);
            mEditAddressCell = v_findView(mContentSection, R.id.cell_edit_address);
            mPhoneCell = v_findView(mContentSection, R.id.cell_phone);
            mRiskCell = v_findView(mContentSection, R.id.cell_risk);
            mWXCell = v_findView(mContentSection, R.id.cell_wechat);

            if (!isTrader && !isTalent) {
                v_setGone(mContentSection, R.id.contact_customer);
            } else {
                v_setText(mContentSection, R.id.contact_customer,
                        concatNoBreak(isTrader ? "已认证的操盘手 如需修改昵称头像,请" : "已认证的牛人 如需修改昵称头像,请",
                                StringFactoryUtil.contactCustomerService(super.getActivity(), v_findView(this, R.id.contact_customer))));
            }

            if (!isTrader && !isTalent) {
                v_setClick(this, R.id.cell_edit_avatar, v -> {
                    mPictureFile = new File[1];
                    PickAvatarHelper.showPickPhotoBottomSheet(this, mPictureFile);
                });
                v_setClick(mEditNameCell, v -> pushFragment(this, new EditNameFragment()));
                mEditNameCell.setClickable(true);
            } else {
                mEditNameCell.setClickable(false);
            }
            v_setClick(this, R.id.cell_verify_name, this::performVerifyName);
            v_setClick(this, R.id.cell_edit_location, v -> pushFragment(this, new ChooseProvinceOrCityFragment().init(title, CommonManager.getInstance().getProvinces(), TYPE_SETUP_CITY)));
            v_setClick(this, R.id.cell_edit_address, v -> pushFragment(this, new EditAddressFragment()));
            v_setClick(this, R.id.cell_modify_login_pwd, v -> pushFragment(this, new ModifyPasswordFragment().init(ModifyPasswordFragment.TYPE_LOGIN)));
            v_setClick(this, R.id.cell_phone, v -> pushFragment(this, new ViewPhoneFragment()));
            v_setClick(this, R.id.cell_risk, v -> {
                observerRiskTestEvent();
                showActivity(this, an_WebViewPage(CommonDefine.H5URL_RiskAssessment()));
            });
            v_setClick(this, R.id.cell_wechat, v -> performBindOrUnbindWeChat());
            v_setClick(this, R.id.cell_hide_setting, v -> pushFragment(this, new PrivacySettingFragment()));

            consumeEvent(NotificationCenter.userInfoChangedSubject)
                    .onNextFinish(ignored -> updateContentView())
                    .done();

            consumeEvent(CardFragments.sChooseCitySubject)
                    .onNextFinish(pair -> {
                        String province = pair.first;
                        String city = pair.second;
                        if (TextUtils.isEmpty(city)) {
                            mEditLocationCell.getExtraTitleLabel().setText(province);
                        } else {
                            mEditLocationCell.getExtraTitleLabel().setText(province + " " + city);
                        }
                        MineManager.getInstance().modifyInfo(3, Mine.CLocation.BuildLocation(province, city), null);

                        UmengUtil.stat_modify_location_event(getActivity(), Optional.of(this));
                    })
                    .done();
            consumeEvent(CardFragments.sChooseAddressSubject)
                    .onNextFinish(pair -> {
                        String province = opt(pair.first).or("");
                        String city = opt(pair.second).or("");
                        if (TextUtils.isEmpty(city)) {
                            mEditAddressCell.getExtraTitleLabel().setText(province);
                        } else {
                            mEditAddressCell.getExtraTitleLabel().setText(String.format("%s %s", province, city));
                        }
                    })
                    .done();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            PickAvatarHelper.handlePickAvatarCallback(this, requestCode, resultCode, data, mPictureFile);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            mIsDataSetFresh = false;
            mRiskTestEventSubscription.reset();
        }

        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (getView() != null) {
                if (isVisibleToUser) {
                    if (!mIsDataSetFresh) {
                        mIsDataSetFresh = true;
                        updateContentView();
                    }
                }
            }
        }


        private void updateContentView() {
            Mine mine = MineManager.getInstance().getmMe();
            v_setImageUri(getView(), R.id.img_avatar, mine.getPhotoUrl());
            mEditNameCell.getExtraTitleLabel().setText(mine.getName());
            mPhoneCell.getExtraTitleLabel().setText("已认证");
            mPhoneCell.getExtraTitleLabel().setCompoundDrawablesWithIntrinsicBounds(R.mipmap.ic_has_authenticed, 0, 0, 0);
            mPhoneCell.getExtraTitleLabel().setCompoundDrawablePadding(dp2px(4));
            if (mine.setAuthenticate) {
                mVerifyNameCell.getExtraTitleLabel().setText("已认证");
                mVerifyNameCell.getExtraTitleLabel().setTextColor(this.getResources().getColor(R.color.gmf_text_grey));
                v_setVisible(mVerifyNameCell);
            } else {
                //                mVerifyNameCell.getExtraTitleLabel().setText("尚未认证");
                //                mVerifyNameCell.getExtraTitleLabel().setTextColor(this.getResources().getColor(R.color.gmf_button_red_normal));
                v_setGone(mVerifyNameCell);
            }
            mVerifyNameCell.getExtraTitleLabel().setCompoundDrawablesWithIntrinsicBounds(mine.setAuthenticate ? R.mipmap.ic_has_authenticed : 0, 0, 0, 0);
            mVerifyNameCell.getExtraTitleLabel().setCompoundDrawablePadding(dp2px(4));
            if (mine.riskAssessmentGrade == 0) {
                mRiskCell.getExtraTitleLabel().setText("未填写");
                mRiskCell.getExtraTitleLabel().setTextColor(this.getResources().getColor(R.color.gmf_button_red_normal));
            } else {
                mRiskCell.getExtraTitleLabel().setText(mine.riskAssessmentGradeMsg);
                mRiskCell.getExtraTitleLabel().setTextColor(this.getResources().getColor(R.color.gmf_text_grey));
            }
            if (TextUtils.isEmpty(mine.getCity())) {
                mEditLocationCell.getExtraTitleLabel().setText(mine.getProvince());
            } else if (TextUtils.isEmpty(mine.getCity()) && TextUtils.isEmpty(mine.getProvince())) {
                mEditLocationCell.getExtraTitleLabel().setText("未设置");
                mRiskCell.getExtraTitleLabel().setTextColor(this.getResources().getColor(R.color.gmf_button_red_normal));
            } else {

                mEditLocationCell.getExtraTitleLabel().setText(mine.getProvince() + " " + mine.getCity());
            }

            Optional<ShippingAddress> shippingAddress = opt(safeGet(() -> mine.getAddress(), null));
            if (shippingAddress.isAbsent()) {
                mEditAddressCell.getExtraTitleLabel().setText("未设置");
                mEditAddressCell.getExtraTitleLabel().setTextColor(this.getResources().getColor(R.color.gmf_button_red_normal));
            } else {
                String province = shippingAddress.let(it -> it.city).let(it -> it.getProvince()).or("");
                String city = shippingAddress.let(it -> it.city).let(it -> it.getCity()).or("");
                mEditAddressCell.getExtraTitleLabel().setText(province + " " + city);
            }

            if (mine.isBindWX) {
                mWXCell.getExtraTitleLabel().setText(String.format("已绑定：%s", mine.wxNickName));
            } else {
                mWXCell.getExtraTitleLabel().setText("未绑定");
            }
        }

        private void observerRiskTestEvent() {
            mRiskTestEventSubscription.reset();
            mRiskTestEventSubscription.add(NotificationCenter.receiveRiskTestResultSubject
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(pair -> onReceiveRiskTestResult(pair.first, pair.second)));

            mRiskTestEventSubscription.add(NotificationCenter.riskTestFinishSubject
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::onRiskTestFinish));
        }

        private void onReceiveRiskTestResult(int level, String text) {
            Mine mine = MineManager.getInstance().getmMe();
            mine.riskAssessmentGrade = level;
            mine.riskAssessmentGradeMsg = text;
            mIsDataSetFresh = false;
        }

        private void onRiskTestFinish(Activity relativeActivity) {
            relativeActivity.finish();
            UmengUtil.stat_risk_test_event(relativeActivity, Optional.of(null));
        }

        private void performVerifyName() {
            boolean hasAuthentic = MineManager.getInstance().getmMe().setAuthenticate;
            if (hasAuthentic) {
                pushFragment(this, new AuthenticFragments.ViewAuthenticStateFragment());
            } else {
                pushFragment(this, new AuthenticFragments.AuthenticFragment());
            }
        }

        private void performBindOrUnbindWeChat() {
            Mine user = MineManager.getInstance().getmMe();
            if (user.isBindWX) {
                new GMFDialog.Builder(getActivity())
                        .setMessage("确定要解除微信绑定吗？解除后只能通过手机号登录操盘侠。")
                        .setPositiveButton("解除绑定", (dialog, which) -> {
                            dialog.dismiss();

                            GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity(), "正在解除绑定...");
                            progressDialog.show();
                            Observable<MResults.MResultsInfo<Void>> observable = UmengUtil.fetchWXAccessToken(getActivity())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .flatMap(response -> isSuccess(response) ? UserController.unbindWXAccount() : Observable.just(cast(response, Void.class)));
                            consumeEventMR(observable)
                                    .onNextStart(response -> progressDialog.dismiss())
                                    .onNextSuccess(response -> {
                                        showToast(this, "解除绑定成功");
                                        user.isBindWX = false;
                                        user.wxNickName = "";
                                        NotificationCenter.userInfoChangedSubject.onNext(null);
                                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDMineUnBindWx);
                                    })
                                    .onNextFail(response -> {
                                        showToast(this, getErrorMessage(response));
                                    })
                                    .done();
                        })
                        .setNegativeButton("取消")
                        .create()
                        .show();
            } else {
                new GMFDialog.Builder(getActivity())
                        .setMessage("确定要绑定微信吗？绑定后可直接使用微信登录。")
                        .setPositiveButton("立即绑定", (dialog, which) -> {
                            dialog.dismiss();
                            GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity(), "正在获取微信信息...");
                            progressDialog.show();
                            Observable<MResults.MResultsInfo<UmengUtil.WXLoginInfo>> observable = UmengUtil.fetchWXAccessToken(getActivity())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .flatMap(response -> isSuccess(response) ?
                                            UserController.bindWXAccount(response.data).map(it -> map(it, response.data)) :
                                            Observable.just(response));
                            consumeEventMR(observable)
                                    .setTag("fetch_wx_info")
                                    .onNextStart(response -> progressDialog.dismiss())
                                    .onNextSuccess(response -> {
                                        UmengUtil.WXLoginInfo loginInfo = response.data;
                                        user.wxNickName = loginInfo.nickName;
                                        user.isBindWX = true;
                                        NotificationCenter.userInfoChangedSubject.onNext(null);
                                        if (user.getName().equals(loginInfo.nickName)) {
                                            pushFragment(this, new LoginFragments.BindWXAccountSuccessFragment().init(false));
                                        } else {
                                            pushFragment(this, new CommonUserFragments.ChooseNameAndAvatarFragment().init(loginInfo));
                                        }
                                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDMineBindWx);
                                    })
                                    .onNextFail(response -> {
                                        showToast(this, getErrorMessage(response));
                                    })
                                    .done();
                        })
                        .setNegativeButton("取消")
                        .create()
                        .show();

            }
        }
    }

    public static class EditNameFragment extends BaseFragment {

        private EditText mNameField;
        private ProgressButton mSaveButton;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_edit_name, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            if (!MineManager.getInstance().isLoginOK()) {
                goBack(this);
            }

            mNameField = v_findView(this, R.id.field_name);
            mNameField.setText(MineManager.getInstance().getmMe().getName());
            mSaveButton = v_findView(this, R.id.btn_save);
            v_setClick(mSaveButton, v -> performModifyName());
            v_addTextChangedListener(mNameField, editable -> mSaveButton.setEnabled(editable.toString().trim().length() > 0));
            mSaveButton.setEnabled(false);

            mNameField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);
        }

        private void performModifyName() {
            mSaveButton.setMode(ProgressButton.Mode.Loading);
            String name = mNameField.getText().toString().trim();
            consumeEventMR(UserController.modifyName(name))
                    .onNextStart(response -> {
                        mSaveButton.setMode(ProgressButton.Mode.Normal);
                    })
                    .onNextSuccess(response -> {
                        NotificationCenter.userInfoChangedSubject.onNext(null);
                        Dialog dialog = createAlertDialog(this, "昵称修改成功");
                        dialog.setOnDismissListener(dialog1 -> goBack(this));
                        dialog.show();

                        UmengUtil.stat_modify_name_event(getActivity(), true, Optional.of(response.errCode), Optional.of(response.msg));
                    })
                    .onNextFail(data -> {
                        createAlertDialog(this, getErrorMessage(data)).show();
                    })
                    .done();
        }
    }

    public static class AvatarListFragment extends BaseFragment {

        private GridView mGridView;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_avatar_list, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            if (!MineManager.getInstance().isLoginOK()) {
                goBack(this);
                return;
            }

            mGridView = v_findView(this, R.id.gridView);
            mGridView.setOnItemClickListener((adapterView, v, position, id) -> {
                AvatarListAdapter adapter = (AvatarListAdapter) mGridView.getAdapter();
                String newAvatar = adapter.mDataSet.get(position);
                performModifyAvatar(newAvatar);
            });
            performFetchAvatarList();
        }

        private void performFetchAvatarList() {
            consumeEventMR(UserController.fetchAvatarList())
                    .setTag("avatar_list")
                    .onNextSuccess(response -> {
                        updateRecyclerView(response.data);
                        UmengUtil.stat_modify_avatar_event(getActivity(), true, Optional.of(response.errCode), Optional.of(response.msg));
                    })
                    .onNextFail(response -> {
                        createErrorDialog(this, getErrorMessage(response)).show();
                    })
                    .onNextFinish(response -> {
                        v_setGone(this, R.id.section_loading);
                    })
                    .done();
        }

        private void updateRecyclerView(List<String> avatarList) {
            mGridView.setAdapter(new AvatarListAdapter(avatarList));
        }

        private void performModifyAvatar(String newAvatar) {
            if (newAvatar.equalsIgnoreCase(MineManager.getInstance().getmMe().getPhotoUrl())) {
                goBack(this);
                return;
            }

            GMFProgressDialog progressView = new GMFProgressDialog(getActivity());
            progressView.setMessage("正在提交，请稍候");
            progressView.show();
            consumeEventMR(UserController.modifyAvatar(newAvatar, true))
                    .onNextStart(response -> {
                        progressView.dismiss();
                    })
                    .onNextSuccess(response -> {
                        NotificationCenter.userInfoChangedSubject.onNext(null);
                        showToast(this, "修改成功");
                        goBack(this);
                    })
                    .onNextFail(response -> {
                        createAlertDialog(this, getErrorMessage(response)).show();
                    })
                    .done();
        }
    }

    private static class AvatarListAdapter extends BaseAdapter {
        private final List<String> mDataSet;
        private final int mCount;

        public AvatarListAdapter(List<String> dataSet) {
            mDataSet = dataSet;
            mCount = mDataSet.size();
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public String getItem(int position) {
            return mDataSet.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_avatar, parent, false);
                AvatarListCellHolder holder = new AvatarListCellHolder(convertView);
                convertView.setTag(holder);
            }
            AvatarListCellHolder holder = (AvatarListCellHolder) convertView.getTag();
            String avatarURL = mDataSet.get(position);
            holder.mAvatarImage.setImageURI(Uri.parse(avatarURL));
            holder.mAvatarLayerImage.setVisibility(avatarURL.equalsIgnoreCase(MineManager.getInstance().getmMe().getPhotoUrl()) ? View.VISIBLE : View.GONE);
            return convertView;
        }
    }

    private static class AvatarListCellHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView mAvatarImage;
        private ImageView mAvatarLayerImage;

        public AvatarListCellHolder(View itemView) {
            super(itemView);
            mAvatarImage = v_findView(itemView, R.id.img_avatar);
            mAvatarLayerImage = v_findView(itemView, R.id.img_avatar_layer);
        }
    }

    public static class ModifyPasswordFragment extends BaseFragment {

        public static int TYPE_LOGIN = 1;
        public static int TYPE_TRANSACTION = 2;

        private int mType;

        private EditText mOldPwdField;
        private EditText mNewPwdField;
        private EditText mConfirmPwdField;
        private ProgressButton mSaveButton;

        public ModifyPasswordFragment init(int passwordType) {
            Bundle arguments = new Bundle();
            arguments.putInt("type", passwordType);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mType = getArguments().getInt("type");
            return inflater.inflate(R.layout.frag_modify_pwd, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            // bind child views
            mOldPwdField = v_findView(this, R.id.field_pwd_old);
            mNewPwdField = v_findView(this, R.id.field_pwd_new);
            mConfirmPwdField = v_findView(this, R.id.field_pwd_confirm);
            mSaveButton = v_findView(this, R.id.btn_save);
            v_setClick(mSaveButton, this::performModifyPassword);
            v_setClick(this, R.id.btn_reset, this::goToResetPassword);

            updateTitle(getString(mType == TYPE_LOGIN ? R.string.act_edit_profile_title_login_password : R.string.act_edit_profile_title_transaction_password));
            mSaveButton.setEnabled(false);

            Observable.just(mOldPwdField, mNewPwdField, mConfirmPwdField)
                    .subscribe(editText -> {
                        if (mType == TYPE_TRANSACTION) {
                            v_addTextChangedListener(editText, v_unsignedNumberFormatter());
                        }
                    });

            Observable.just(mOldPwdField, mNewPwdField, mConfirmPwdField)
                    .flatMap(editText -> Observable.create(sub -> v_addTextChangedListener(editText, editable -> {
                        sub.onNext(null);
                    })))
                    .subscribe(nil -> {
                        mSaveButton.setEnabled(mOldPwdField.length() > 0 && mNewPwdField.length() > 0 && mConfirmPwdField.length() > 0 && mNewPwdField.length() == mConfirmPwdField.length());
                    });

            mOldPwdField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);
        }

        private void goToResetPassword() {
            if (mType == TYPE_LOGIN) {
                Mine mine = MineManager.getInstance().getmMe();
                FragmentStackActivity.replaceTopFragment(this, new ResetLoginPasswordFragment().init(mine.getName(), mine.phone));
            } else if (mType == TYPE_TRANSACTION) {
                FragmentStackActivity.replaceTopFragment(this, new CommonFragments.ResetTransactionPasswordFragment());
            }
        }

        private void performModifyPassword() {
            String oldPwd = mOldPwdField.getText().toString().trim();
            String newPwd = mNewPwdField.getText().toString().trim();
            String confirmPwd = mConfirmPwdField.getText().toString().trim();

            if (!newPwd.equalsIgnoreCase(confirmPwd)) {
                showToast(getActivity(), "新密码与确认密码不相同");
            } else {
                hideKeyboardFromWindow(this);
                Observable<MResults.MResultsInfo<Void>> observable = null;
                if (mType == TYPE_LOGIN)
                    observable = requestModifyLoginPassword(new WeakReference<>(this), oldPwd, newPwd, confirmPwd);
                else if (mType == TYPE_TRANSACTION)
                    observable = requestModifyTransactionPassword(new WeakReference<>(this), oldPwd, newPwd);

                if (observable != null) {
                    mIsOperation = true;
                    mSaveButton.setMode(ProgressButton.Mode.Loading);
                    observable.observeOn(AndroidSchedulers.mainThread())
                            .subscribe(data -> {
                                mIsOperation = false;
                                mSaveButton.setMode(ProgressButton.Mode.Normal);
                                if (isSuccess(data)) {
                                    FragmentStackActivity.replaceTopFragment(this, new ModifyPasswordSuccessFragment().init(mType));

                                    if (mType == TYPE_LOGIN) {
                                        UmengUtil.stat_modify_login_pwd_event(getActivity(), true, Optional.of(data.errCode), Optional.of(data.msg));
                                    } else if (mType == TYPE_TRANSACTION) {
                                        UmengUtil.stat_modify_transaction_pwd_event(getActivity(), true, Optional.of(data.errCode), Optional.of(data.msg));
                                    }
                                } else {
                                    // handle by global hook
                                }
                            });
                }
            }
        }

        public static class ModifyPasswordSuccessFragment extends BaseFragment {

            public static int TYPE_LOGIN = 1;
            public static int TYPE_TRANSACTION = 2;

            private int mType;

            public ModifyPasswordSuccessFragment init(int type) {
                Bundle arguments = new Bundle();
                arguments.putInt("type", type);
                setArguments(arguments);
                return this;
            }

            @Nullable
            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                mType = getArguments().getInt("type");
                return inflater.inflate(R.layout.frag_reset_password_success, container, false);
            }

            @Override
            public void onViewCreated(View view, Bundle savedInstanceState) {
                super.onViewCreated(view, savedInstanceState);
                setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);

                if (mType == TYPE_LOGIN) {
                    updateTitle("修改登录密码");
                } else if (mType == TYPE_TRANSACTION) {
                    updateTitle("修改交易密码");
                }

                v_setClick(view, R.id.btn_done, v -> goBack(this));
            }
        }

        private static Observable<MResults.MResultsInfo<Void>> requestModifyLoginPassword(WeakReference<BaseFragment> fragRef, String oldPassword, String newPassword, String confirmPassword) {
            return UserController.modifyLoginPassword(oldPassword, newPassword, confirmPassword, callback -> {
                if (isSuccess(callback)) {
                    showToast(MyApplication.SHARE_INSTANCE, "修改登录密码成功");
                } else {
                    showAlertDialogOrToastWithFragment(fragRef, getErrorMessage(callback));
                }
            });
        }

        private static Observable<MResults.MResultsInfo<Void>> requestModifyTransactionPassword(WeakReference<BaseFragment> fragRef, String oldPassword, String newPassword) {
            return UserController.modifyTransactionPassword(oldPassword, newPassword, callback -> {
                if (isSuccess(callback)) {
                    showToast(MyApplication.SHARE_INSTANCE, "修改交易密码成功");
                } else {
                    showAlertDialogOrToastWithFragment(fragRef, getErrorMessage(callback));
                }
            });
        }
    }

    public static class ResetPhoneFirstStepFragment extends BaseFragment {
        private EditText mCodeField;
        private Button mSendCodeButton;
        private ProgressButton mNextButton;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_reset_phone_first_step, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);

            // bind child views
            mCodeField = v_findView(view, R.id.field_code);
            mSendCodeButton = v_findView(view, R.id.btn_send_code);
            mNextButton = v_findView(view, R.id.btn_next);

            v_setText(this, R.id.contact_customer, concatNoBreak("如手机已丢失或停用，请",
                    StringFactoryUtil.contactCustomerService(getActivity(), v_findView(this, R.id.contact_customer))));

            // init child views
            String userName = MineManager.getInstance().getmMe().getName();
            String phone = MineManager.getInstance().getPhone();
            v_setText(view, R.id.label_info, concatNoBreak(setFontSize(userName + "(" + encryptPhoneNumberTransformer().call(phone) + ")", sp2px(17)), "，验证身份后更换手机号"));
            v_setClick(mSendCodeButton, v -> performSendCode());
            v_setClick(mNextButton, v -> performVerifyOldPhone());
            v_addTextChangedListener(mCodeField, v_unsignedNumberFormatter());
            v_addTextChangedListener(mCodeField, editable -> mNextButton.setEnabled(mCodeField.length() > 0));

            mCodeField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);

            performSendCode();
        }

        private void performSendCode() {
            mSendCodeButton.setEnabled(false);
            String phone = MineManager.getInstance().getPhone();
            consumeEventMR(UserController.sendCode(phone, VerifyCode.ModifyCellPhone_Old))
                    .onNextSuccess(response -> {
                        enableSendCodeButtonLater();
                    })
                    .onNextFail(response -> {
                        createAlertDialog(this, getErrorMessage(response)).show();
                        mSendCodeButton.setEnabled(true);
                    })
                    .done();
        }

        private void performVerifyOldPhone() {
            mNextButton.setMode(ProgressButton.Mode.Loading);
            String code = mCodeField.getText().toString();

            mIsOperation = false;
            consumeEventMR(UserController.verifyOldPhone(code))
                    .onNextStart(response -> {
                                mNextButton.setMode(ProgressButton.Mode.Normal);
                                mIsOperation = true;
                            }
                    )
                    .onNextSuccess(response -> {
                        FragmentStackActivity.replaceTopFragment(this, new ResetPhoneSecondStepFragment());

                        UmengUtil.stat_modify_phone_event(getActivity(), true, Optional.of(response.errCode), Optional.of(response.msg));
                    })
                    .onNextFail(data -> {
                        createAlertDialog(this, getErrorMessage(data)).show();
                    })
                    .done();
        }

        private void enableSendCodeButtonLater() {
            mSendCodeButton.setEnabled(false);
            consumeEvent(createObservableCountDown(60, 1000))
                    .onNextStart(integer -> mSendCodeButton.setText("重新发送" + integer))
                    .onComplete(() -> {
                        mSendCodeButton.setText("获取验证码");
                        mSendCodeButton.setEnabled(true);
                    })
                    .done();
        }
    }

    public static class ResetPhoneSecondStepFragment extends BaseFragment {

        private EditText mPhoneField;
        private EditText mCodeField;
        private Button mSendCodeButton;
        private ProgressButton mModifyPhoneButton;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_reset_phone_second_step, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            //bind child views
            mPhoneField = v_findView(view, R.id.field_phone);
            mCodeField = v_findView(view, R.id.field_code);
            mSendCodeButton = v_findView(view, R.id.btn_send_code);
            mModifyPhoneButton = v_findView(view, R.id.btn_modify_phone);

            //init child views
            mModifyPhoneButton.setEnabled(false);
            v_setClick(mSendCodeButton, v -> performSendCode());
            v_setClick(mModifyPhoneButton, v -> performBindNewPhone());

            v_addTextChangedListener(mPhoneField, v_unsignedNumberFormatter());
            v_addTextChangedListener(mCodeField, v_unsignedNumberFormatter());
            Observable.just(mPhoneField, mCodeField)
                    .subscribe(editText -> {
                        v_addTextChangedListener(editText, editable -> {
                            mModifyPhoneButton.setEnabled(mPhoneField.length() == 11 && mCodeField.length() > 0);
                        });
                    });

            mPhoneField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);
        }

        private void performBindNewPhone() {

            mIsOperation = true;
            mModifyPhoneButton.setMode(ProgressButton.Mode.Loading);
            String phone = mPhoneField.getText().toString();
            String code = mCodeField.getText().toString();
            consumeEventMR(requestBindNewPhone(new WeakReference<>(this), phone, code))
                    .onNextStart(response -> {
                        mIsOperation = false;
                    })
                    .onNextSuccess(response -> {
                        FragmentStackActivity.replaceTopFragment(this, new ModifyPhoneSuccessFragment());
                    })
                    .onComplete(() -> mModifyPhoneButton.setMode(ProgressButton.Mode.Normal))
                    .done();
        }

        private void performSendCode() {
            mSendCodeButton.setEnabled(false);
            String phone = mPhoneField.getText().toString();
            consumeEventMR(UserController.sendCode(phone, VerifyCode.ModifyCellPhone_New))
                    .onNextSuccess(response -> {
                        enableSendCodeButtonLater();
                    })
                    .onNextFail(response -> {
                        mSendCodeButton.setEnabled(true);
                        createAlertDialog(this, getErrorMessage(response)).show();
                    })
                    .done();
        }

        private void enableSendCodeButtonLater() {
            mSendCodeButton.setEnabled(false);
            consumeEvent(createObservableCountDown(60, 1000))
                    .onNextFinish(integer -> mSendCodeButton.setText("重新发送" + integer))
                    .onComplete(() -> {
                        mSendCodeButton.setText("获取验证码");
                        mSendCodeButton.setEnabled(true);
                    })
                    .done();
        }

        private Observable<MResults.MResultsInfo<Void>> requestBindNewPhone(WeakReference<BaseFragment> fragRef, String phone, String code) {
            return UserController.bindNewPhone(phone, code, callback -> {
                if (isSuccess(callback)) {
                    showToast(MyApplication.SHARE_INSTANCE, "修改手机号成功");
                } else {
                    showAlertDialogOrToastWithFragment(fragRef, getErrorMessage(callback));
                }
            });
        }
    }

    public static class ModifyPhoneSuccessFragment extends BaseFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_reset_phone_success, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);

            v_setClick(view, R.id.btn_done, v -> goBack(this));
        }
    }

    public static class ViewPhoneFragment extends BaseFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_view_phone, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            String phone = MineManager.getInstance().getPhone();
            String encryptedPhone = StringExtension.map(phone, encryptPhoneNumberTransformer());
            v_setText(this, R.id.label_phone, encryptedPhone);
            v_setClick(this, R.id.btn_modify_phone, v -> FragmentStackActivity.replaceTopFragment(this, new ResetPhoneFirstStepFragment()));

            v_setText(this, R.id.contact_customer, concatNoBreak("如手机已丢失或停用，请",
                    StringFactoryUtil.contactCustomerService(getActivity(), v_findView(this, R.id.contact_customer))));
        }
    }

    public static class PrivacySettingFragment extends BaseFragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_privacy_setting, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            updateContent();
        }

        private void updateContent() {
            ToggleCell simulationPerformanceCell = v_findView(this, R.id.cell_simulate_performance);
            boolean hideVtcProfile = MineManager.getInstance().getmMe().isHideVtcProfile();
            simulationPerformanceCell.setToggleControlState(!hideVtcProfile);
            simulationPerformanceCell.setListener(isOn -> {
                consumeEventMR(UserController.modifyHideVtcProfile(!isOn))
                        .onNextFinish(response -> {
                            if (isSuccess(response)) {
                                showToast(this, isOn ? "模拟业绩已公开" : "模拟业绩已隐藏");
                            }
                        })
                        .done();
            });
        }
    }

    public static class EditAddressFragment extends BaseFragment {
        private String title = "选择地区";
        private ProgressButton mSaveButton;
        private BasicCell mEditLocationCell;
        private EditText mNameField;
        private EditText mPhoneField;
        private EditText mAddressField;
        private String mSelectedProvince;
        private String mSelectedCity;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_edit_address_view, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));

            if (!MineManager.getInstance().isLoginOK()) {
                goBack(this);
            }

            mNameField = v_findView(this, R.id.field_name);
            mPhoneField = v_findView(this, R.id.field_phone);
            mAddressField = v_findView(this, R.id.field_detail_address);
            mEditLocationCell = v_findView(this, R.id.cell_edit_location);
            mSaveButton = v_findView(this, R.id.btn_save);

            v_setClick(this, R.id.cell_edit_location, v -> pushFragment(this, new ChooseProvinceOrCityFragment().init(title, CommonManager.getInstance().getProvinces(), TYPE_SETUP_ADDRESS)));
            v_setClick(mSaveButton, v -> performModifyAddress());
            mSaveButton.setEnabled(false);

            consumeEvent(CardFragments.sChooseAddressSubject)
                    .onNextFinish(pair -> {
                        String province = opt(pair.first).or("");
                        String city = opt(pair.second).or("");
                        if (TextUtils.isEmpty(city)) {
                            mEditLocationCell.getExtraTitleLabel().setText(province);
                        } else {
                            mEditLocationCell.getExtraTitleLabel().setText(province + " " + city);
                        }
                        mSelectedProvince = province;
                        mSelectedCity = city;
                    })
                    .done();

            v_addTextChangedListener(mPhoneField, v_unsignedNumberFormatter());
            Observable.just(mNameField, mPhoneField, mAddressField, mEditLocationCell.getExtraTitleLabel())
                    .flatMap(editText -> Observable.create(sub -> v_addTextChangedListener(editText, editable -> sub.onNext(null))))
                    .subscribe(nil -> {
                        boolean isFormFilled = mNameField.length() > 0 && mPhoneField.length() > 0 && mAddressField.length() > 0 && mEditLocationCell.getExtraTitleLabel().length() > 0;
                        mSaveButton.setEnabled(isFormFilled);
                        UmengUtil.stat_click_event(UmengUtil.eEVENTIDMineAddressSet);
                    });
            mNameField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);

            fillFormFromLoginUserInfo();

        }

        @Override
        protected boolean onInterceptGoBack() {
            NotificationCenter.closeEditShippingAddressPageSubject.onNext(null);
            return super.onInterceptGoBack();
        }

        private void fillFormFromLoginUserInfo() {
            Optional<ShippingAddress> shippingAddress = opt(safeGet(() -> MineManager.getInstance().getmMe().getAddress(), null));
            String name = shippingAddress.let(it -> it.name).or("");
            String phone = shippingAddress.let(it -> it.cellphone).or("");
            String province = shippingAddress.let(it -> it.city).let(it -> it.getProvince()).or("");
            String city = shippingAddress.let(it -> it.city).let(it -> it.getCity()).or("");
            String address = shippingAddress.let(it -> it.address).or("");
            String location = TextUtils.isEmpty(city) ? province : province + " " + city;

            mNameField.setText(name);
            mPhoneField.setText(phone);
            mAddressField.setText(address);
            mEditLocationCell.getExtraTitleLabel().setText(location);
            mSelectedCity = city;
            mSelectedProvince = province;

            if (TextUtils.isEmpty(city) && TextUtils.isEmpty(province)) {
                mEditLocationCell.getExtraTitleLabel().setText("未设置");
            } else if (TextUtils.isEmpty(city)) {
                mEditLocationCell.getExtraTitleLabel().setText(province);
            } else {
                mEditLocationCell.getExtraTitleLabel().setText(String.format("%s %s", province, city));
            }
        }

        public void performModifyAddress() {
            String name = safeGet(() -> mNameField.getText().toString(), "");
            String phone = safeGet(() -> mPhoneField.getText().toString(), "");
            String province = safeGet(() -> mSelectedProvince, "");
            String city = safeGet(() -> mSelectedCity, "");
            String address = safeGet(() -> mAddressField.getText().toString(), "");
            if (notEmpty(name, phone, city, address)) {
                mSaveButton.setMode(ProgressButton.Mode.Loading);

                ShippingAddress shippingAddress = new ShippingAddress();
                shippingAddress.name = name;
                shippingAddress.cellphone = phone;
                shippingAddress.address = address;
                shippingAddress.city = Mine.CLocation.BuildLocation(province, city);
                consumeEventMR(UserController.modifyShippingAddress(shippingAddress))
                        .onNextFinish(response -> {
                            GMFDialog dialog = createAlertDialog(this, isSuccess(response) ? "修改成功" : getErrorMessage(response));
                            dialog.setCancelable(false);
                            dialog.setPositiveButton("确定", (d, which) -> {
                                d.dismiss();
                                mSaveButton.setMode(ProgressButton.Mode.Normal);
                                if (isSuccess(response)) {
                                    goBack(this);
                                }
                            });
                            dialog.show();
                        })
                        .done();

            } else {
                createAlertDialog(this, "信息不能为空").show();
            }
        }
    }

    public static class ResetLoginPasswordFragment extends BaseFragment {

        private String mName;
        private String mPhone;
        private boolean mAutoSendCode;

        private EditText mCodeField;
        private EditText mNewPwdField;
        private Button mSendCodeButton;
        private ProgressButton mResetButton;

        public ResetLoginPasswordFragment init(String name, String phone) {
            Bundle arguments = new Bundle();
            arguments.putString("name", name);
            arguments.putString("phone", phone);
            arguments.putBoolean("auto_send_code", true);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mName = getArguments().getString("name");
            mPhone = getArguments().getString("phone");
            mAutoSendCode = getArguments().getBoolean("auto_send_code");
            return inflater.inflate(R.layout.frag_reset_login_password, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            mCodeField = v_findView(view, R.id.field_code);
            mNewPwdField = v_findView(view, R.id.field_new_pwd);
            mSendCodeButton = v_findView(view, R.id.btn_send_code);
            mResetButton = v_findView(view, R.id.btn_reset);
            v_addTextChangedListener(mCodeField, v_unsignedNumberFormatter());
            v_setClick(mSendCodeButton, this::performSendCode);
            v_setClick(mResetButton, this::performResetPassword);

            v_setText(view, R.id.label_hint, concatNoBreak("已发送重置密码短信验证码至 ", setFontSize(StringExtension.map(mPhone, encryptPhoneNumberTransformer()), sp2px(17))));

            v_setText(this, R.id.contact_customer, concatNoBreak("如手机已丢失或停用，请",
                    StringFactoryUtil.contactCustomerService(getActivity(), v_findView(this, R.id.contact_customer))));

            Observable.just(mCodeField, mNewPwdField)
                    .subscribe(editText -> {
                        v_addTextChangedListener(editText, editable -> {
                            mResetButton.setEnabled(mCodeField.length() > 4 && mNewPwdField.length() > 4);
                        });
                    });

            if (mAutoSendCode) {
                mResetButton.setEnabled(false);
                performSendCode();
            }

            mCodeField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);
        }

        private void performResetPassword() {
            String code = mCodeField.getText().toString().trim();
            String newPwd = mNewPwdField.getText().toString().trim();

            mIsOperation = true;
            mResetButton.setMode(ProgressButton.Mode.Loading);
            consumeEventMR(requestResetLoginPassword(new WeakReference<>(this), code, newPwd, newPwd))
                    .onNextStart(response -> {
                        mIsOperation = false;
                        mResetButton.setMode(ProgressButton.Mode.Normal);
                    })
                    .onNextSuccess(response -> {
                        goBack(this);
                    })
                    .done();
        }

        private void performSendCode() {
            String phone = mPhone;
            mSendCodeButton.setEnabled(false);
            consumeEventMR(UserController.sendCode(phone, VerifyCode.ResetPS))
                    .onNextSuccess(response -> {
                        enableSendCodeButtonLater();
                    })
                    .onNextFail(response -> {
                        mSendCodeButton.setEnabled(true);
                    })
                    .done();
        }

        private void enableSendCodeButtonLater() {
            mSendCodeButton.setEnabled(false);
            consumeEvent(createObservableCountDown(60, 1000))
                    .onNextFinish(integer -> {
                        mSendCodeButton.setText("重新发送" + integer);
                    })
                    .onComplete(() -> {
                        mSendCodeButton.setText("获取验证码");
                        mSendCodeButton.setEnabled(true);
                    })
                    .done();
        }

        private static Observable<MResults.MResultsInfo<Void>> requestResetLoginPassword(WeakReference<BaseFragment> fragRef, String code, String newPwd, String confirmPwd) {
            return UserController.resetLoginPassword(code, newPwd, confirmPwd, callback -> {
                if (isSuccess(callback)) {
                    showToast(MyApplication.SHARE_INSTANCE, "重置登录密码成功");
                } else {
                    showAlertDialogOrToastWithFragment(fragRef, getErrorMessage(callback));
                }
            });
        }
    }
}
