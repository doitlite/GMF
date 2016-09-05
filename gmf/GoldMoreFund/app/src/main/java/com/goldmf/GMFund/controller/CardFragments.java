package com.goldmf.GMFund.controller;

import android.content.Context;
import android.graphics.drawable.ShapeDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.facebook.drawee.view.SimpleDraweeView;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.controller.business.CashController;
import com.goldmf.GMFund.controller.dialog.BindCardDialog;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.extension.StringExtension;
import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.manager.cashier.BankInfo;
import com.goldmf.GMFund.manager.cashier.CashierManager;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.FundBrief.Money_Type;
import com.goldmf.GMFund.util.StringFactoryUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.BindCNCardInfoCell;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.TopNotificationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import yale.extension.common.shape.RoundCornerShape;

import static com.goldmf.GMFund.controller.CardFragments.ChooseProvinceOrCityFragment.TYPE_SETUP_CITY;
import static com.goldmf.GMFund.controller.CommonProxyActivity.KEY_NEXT_ACTION_TYPE_INT;
import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.FragmentStackActivity.replaceTopFragment;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.TEXT_BLACK_COLOR;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.WHITE_COLOR;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.ObjectExtension.opt;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.SpannableStringExtension.setColor;
import static com.goldmf.GMFund.extension.StringExtension.encryptBankCardNoTransformer;
import static com.goldmf.GMFund.extension.StringExtension.normalBankCardNoTransformer;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.createErrorDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.hideKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.ViewExtension.dp2px;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.extension.ViewExtension.v_bankCardNumFormatter;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.extension.ViewExtension.v_unsignedNumberFormatter;
import static com.goldmf.GMFund.util.FormatUtil.formatBigNumber;
import static com.goldmf.GMFund.util.UmengUtil.stat_bind_cn_card_event;

/**
 * Created by yale on 15/10/14.
 */
public class CardFragments {
    public static PublishSubject<Pair<String, String>> sChooseCitySubject = PublishSubject.create();
    public static PublishSubject<Pair<String, String>> sChooseAddressSubject = PublishSubject.create();
    private static PublishSubject<BankInfo> sChooseBankSubject = PublishSubject.create();

    private CardFragments() {
    }

    public static class BindCNCardFragment extends BaseFragment {

        private View mChooseBankCell;
        private View mChooseCityCell;
        private TextView mChooseCityLabel;
        private TextView mChooseBankLabel;
        private EditText mBankCardField;
        private EditText mPhoneField;
        private EditText mCodeField;
        private Button mSendCodeButton;
        private Button mBindButton;

        private BankInfo mBankInfo;
        private String mProvince;
        private String mCity;
        private boolean mForceDisableSendButton = false;
        private boolean mHasPerformSendCodeRequest = false;
        private String title = "选择开户城市";
        private BindCardDialog mBindCardDialog;

        public BindCNCardFragment init(int nextActionType) {
            Bundle arguments = new Bundle();
            arguments.putInt(KEY_NEXT_ACTION_TYPE_INT, nextActionType);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_bind_cn_card, container, false);
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
            mChooseBankCell = v_findView(this, R.id.cell_choose_bank);
            mChooseBankLabel = v_findView(mChooseBankCell, R.id.label_choose_bank);
            mChooseCityCell = v_findView(this, R.id.cell_choose_city);
            mChooseCityLabel = v_findView(mChooseCityCell, R.id.label_choose_city);
            mBankCardField = v_findView(this, R.id.field_bank_card);
            mPhoneField = v_findView(this, R.id.field_phone);
            mCodeField = v_findView(this, R.id.field_code);
            mSendCodeButton = v_findView(this, R.id.btn_send_code);
            mBindButton = v_findView(this, R.id.btn_bind);

            // init child views
            Action1<View> setRoundCornerBGFunc = v -> v.setBackgroundDrawable(new ShapeDrawable(new RoundCornerShape(WHITE_COLOR, dp2px(4)).border(0x1A000000, dp2px(0.5f))));
            setRoundCornerBGFunc.call(v_findView(this, R.id.group_bank_and_city));
            setRoundCornerBGFunc.call(mBankCardField);
            setRoundCornerBGFunc.call(mPhoneField);
            setRoundCornerBGFunc.call(v_findView(this, R.id.group_code));
            v_setClick(mChooseCityCell, this::performChooseCity);
            v_setClick(mChooseBankCell, this::performChooseBank);

            String realName = MineManager.getInstance().getmMe().realName;
            v_setText(this, R.id.label_hint,
                    concatNoBreak("为保证您的资金同卡进出安全，只能绑定 ",
                            setColor(realName, TEXT_BLACK_COLOR), " 名下的银行卡。如果绑卡失败,请在消息中 ", StringFactoryUtil.contactCustomerService(getActivity(), v_findView(this, R.id.label_hint)),
                            "。"));
            mBindCardDialog = new BindCardDialog(getActivity());
            mBindCardDialog.show();

            consumeEvent(sChooseCitySubject)
                    .onNextFinish(pair -> {
                        mProvince = pair.first;
                        mCity = pair.second;
                        if (TextUtils.isEmpty(mCity) || mProvince.equals(mCity)) {
                            v_setText(this, R.id.label_choose_city, mProvince);
                        } else {
                            v_setText(this, R.id.label_choose_city, mProvince + " " + mCity);
                        }
                    })
                    .done();

            consumeEvent(sChooseBankSubject)
                    .onNextFinish(bankInfo -> {
                        mBankInfo = bankInfo;
                        v_setText(this, R.id.label_choose_bank, bankInfo.bankName);
                    })
                    .done();

            v_addTextChangedListener(mPhoneField, v_unsignedNumberFormatter());
            v_addTextChangedListener(mCodeField, v_unsignedNumberFormatter());
            v_addTextChangedListener(mBankCardField, v_bankCardNumFormatter(mBankCardField));

            v_setClick(mBindButton, this::performBindCard);

            mSendCodeButton.setEnabled(false);
            v_setClick(mSendCodeButton, this::performSendCode);
            consumeEvent(Observable.just(mChooseBankLabel, mChooseCityLabel, mPhoneField, mBankCardField))
                    .onNextFinish(textView -> {
                        v_addTextChangedListener(textView, editable -> {
                            if (mBankInfo == null || TextUtils.isEmpty(mProvince) || mPhoneField.getText().toString().length() != 11) {
                                mSendCodeButton.setEnabled(false);
                            } else {
                                if (!mForceDisableSendButton)
                                    mSendCodeButton.setEnabled(true);
                            }
                        });
                    })
                    .done();

            consumeEventMR(CashController.fetchBankTips())
                    .onNextSuccess(response -> {
                        TopNotificationView notificationView = v_findView(view, R.id.section_notification);
                        notificationView.setupWithTarLinkText(response.data, true);
                    })
                    .done();

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        @Override
        protected boolean onInterceptGoBack() {
            UmengUtil.stat_cancel_bind_cn_card(getActivity(), opt(this));
            NotificationCenter.closeBindCNCardPageSubject.onNext(null);
            return super.onInterceptGoBack();
        }


        private void performChooseBank(View view) {
            FragmentStackActivity.pushFragment(this, new ChooseBankFragment());
        }

        private void performChooseCity(View view) {
            List<String> cacheProvinces = CommonManager.getInstance().getProvinces();
            FragmentStackActivity.pushFragment(this, new ChooseProvinceOrCityFragment().init(title, cacheProvinces, TYPE_SETUP_CITY));
        }

        private void performBindCard(View view) {
            if (!mHasPerformSendCodeRequest) {
                createAlertDialog(this, "请先获取验证码").show();
                return;
            }

            GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity(), "正在绑卡");
            progressDialog.show();
            String code = mCodeField.getText().toString().trim();
            consumeEventMR(CashController.verifyBindBankCardCode(code))
                    .setTag("verify_card")
                    .onNextSuccess(response -> {
                        performRefreshCNBankCard(progressDialog);
                    })
                    .onNextFail(response -> {
                        progressDialog.dismiss();
                        createAlertDialog(this, getErrorMessage(response)).show();
                        stat_bind_cn_card_event(getActivity(), false, opt(response.errCode), opt(response.msg));
                    })
                    .done();
        }

        private void performRefreshCNBankCard(GMFProgressDialog progressDialog) {
            consumeEventMR(CashController.refreshBankCard(false, Money_Type.CN))
                    .setTag("refresh_card")
                    .onNextStart(response -> progressDialog.dismiss())
                    .onNextSuccess(response -> {
                        BankCard card = response.data;
                        String encryptedCardNo = StringExtension.map(mBankCardField, encryptBankCardNoTransformer());
                        if (card.status == BankCard.Card_Status_Normal) {
                            GMFDialog.Builder builder = new GMFDialog.Builder(getActivity());
                            builder.setMessage(mBankInfo.bankName + "(" + encryptedCardNo.substring(Math.max(encryptedCardNo.length() - 4, 0)) + ")" + "绑定成功!");
                            builder.setPositiveButton("完成", (dialog, which) -> {
                                dialog.dismiss();
                                goBack(this);
                                stat_bind_cn_card_event(getActivity(), true, opt(response.errCode), opt(response.msg));
                            });
                            builder.create().show();
                        } else {
                            replaceTopFragment(this, new BindCNCardWaitingFragment().init(mBankInfo, encryptedCardNo));
                        }
                    })
                    .onNextFail(response -> {
                        stat_bind_cn_card_event(getActivity(), false, opt(response.errCode), opt(response.msg));
                        createAlertDialog(this, getErrorMessage(response)).show();
                    })
                    .done();
        }


        private void performSendCode() {

            hideKeyboardFromWindow(this);
            mSendCodeButton.setEnabled(false);
            String bankCardNo = StringExtension.map(mBankCardField, normalBankCardNoTransformer());
            String phone = mPhoneField.getText().toString().trim();
            BankInfo bankInfo = mBankInfo;
            String province = mProvince;
            String city = mCity;

            consumeEventMR(CashController.sendBindBankCardCode(bankCardNo, phone, bankInfo, province, city))
                    .onNextSuccess(response -> {
                        mHasPerformSendCodeRequest = true;
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
            mForceDisableSendButton = true;
            Observable<Integer> countDownObservable = Observable.create(sub -> {
                int count = 60;
                while (count > 0) {
                    sub.onNext(count--);
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException ignored) {
                    }
                }
                sub.onCompleted();
            });

            countDownObservable
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .finallyDo(() -> {
                        mSendCodeButton.setText("发送验证码");
                        mSendCodeButton.setEnabled(true);
                        mForceDisableSendButton = false;
                    })
                    .subscribe(remainingTime -> mSendCodeButton.setText("重新发送(" + remainingTime + ")"));
        }
    }

    public static class BindCNCardWaitingFragment extends BaseFragment {
        private BankInfo mBankInfo;
        private String mEncryptedCardNo;

        public BindCNCardWaitingFragment init(BankInfo bankInfo, String encryptedCardNo) {
            Bundle arguments = new Bundle();
            arguments.putSerializable(BankCard.class.getSimpleName(), bankInfo);
            arguments.putString("encrypted_card_no", encryptedCardNo);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            if (getArguments() != null) {
                mBankInfo = (BankInfo) getArguments().getSerializable(BankCard.class.getSimpleName());
                mEncryptedCardNo = getArguments().getString("encrypted_card_no");
            }
            return inflater.inflate(R.layout.frag_bind_cn_card_waiting, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            mForceFinishOnGoBack = true;
            v_setClick(this, R.id.btn_cancel, v -> getActivity().finish());
            performRefreshCNBankCard(8);

            v_setText(this, R.id.contact_customer, concatNoBreak("如果长时间没响应，请",
                    StringFactoryUtil.contactCustomerService(getActivity(), v_findView(this, R.id.contact_customer))));

            BindCNCardInfoCell infoCell = v_findView(this, R.id.cell_card_info);
            infoCell.updateContent(mEncryptedCardNo, mBankInfo);
        }

        private void performRefreshCNBankCard(int retryCount) {
            if (retryCount > 0) {
                Observable<MResultsInfo<BankCard>> observable = CashController.refreshBankCard(false, Money_Type.CN)
                        .delaySubscription(500, TimeUnit.MILLISECONDS);
                consumeEventMR(observable)
                        .onNextSuccess(response -> {
                            BankCard card = response.data;
                            if (card.status == BankCard.Card_Status_Normal) {
                                GMFDialog.Builder builder = new GMFDialog.Builder(getActivity());
                                builder.setMessage(mBankInfo.bankName + "(" + mEncryptedCardNo.substring(Math.max(mEncryptedCardNo.length() - 4, 0)) + ")" + "绑定成功!");
                                builder.setPositiveButton("完成", (dialog, which) -> {
                                    dialog.dismiss();
                                    goBack(this);
                                    stat_bind_cn_card_event(getActivity(), true, opt(response.errCode), opt(response.msg));
                                });
                                builder.create().show();
                            } else {
                                int nextRetryCount = retryCount - 1;
                                if (nextRetryCount > 0) {
                                    performRefreshCNBankCard(nextRetryCount);
                                } else {
                                    stat_bind_cn_card_event(getActivity(), false, opt(response.errCode), opt(response.msg));
                                    GMFDialog errorDialog = createErrorDialog(this, "获取绑卡信息失败,请稍候重试");
                                    errorDialog.show();
                                }
                            }
                        })
                        .onNextFail(response -> {
                            createErrorDialog((BaseActivity) getActivity(), getErrorMessage(response)).show();
                        })
                        .done();
            } else {
                stat_bind_cn_card_event(getActivity(), false, opt((Integer) null), opt((String) null));
                createErrorDialog(this, "获取绑卡信息失败,请稍候重试").show();
            }
        }

        @Override
        protected boolean onInterceptGoBack() {
            NotificationCenter.closeBindCNCardPageSubject.onNext(null);
            return super.onInterceptGoBack();
        }
    }

    public static class ChooseBankFragment extends ListViewFragment {

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this));
            findToolbar(this).setBackgroundColor(STATUS_BAR_BLACK);

            //刷新一次bankInfo
            CashController.refreshBank().subscribe();

            updateTitle("选择开户行");

            setSwipeRefreshable(true);
            setOnSwipeRefreshListener(() -> {
                performFetchDataIfNeeded(false);
            });

            //set data
            List<BankInfo> bankInfoList = CashierManager.getInstance().banks;
            setupListView(bankInfoList);

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        private void performFetchDataIfNeeded(boolean reload) {
            consumeEventMRUpdateUI(CashController.refreshBank(), reload)
                    .setTag("fresh_bank")
                    .onNextSuccess(response -> {
                        List<BankInfo> bankInfoList = CashierManager.getInstance().banks;
                        setupListView(bankInfoList);
                    })
                    .done();
        }


        private void performPickBank(BankInfo bankInfo) {
            sChooseBankSubject.onNext(bankInfo);
            goBack(this);
        }

        private void setupListView(List<BankInfo> dataSet) {
            mListView.setOnItemClickListener((parent, view, position, id) -> {
                Adapter adapter = parent.getAdapter();
                if (adapter != null) {
                    BankInfo item = (BankInfo) adapter.getItem(position);
                    performPickBank(item);
                }
            });
            mListView.setAdapter(new BaseAdapter() {
                private final List<BankInfo> mDataSet = new ArrayList<>(dataSet);

                @Override
                public int getCount() {
                    return mDataSet.size();
                }

                @Override
                public BankInfo getItem(int position) {
                    return mDataSet.get(position);
                }

                @Override
                public long getItemId(int position) {
                    return position;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    if (convertView == null) {
                        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cell_bank_info, parent, false);
                        BankViewHolder holder = new BankViewHolder(convertView);
                        convertView.setTag(holder);
                    }
                    BankViewHolder holder = (BankViewHolder) convertView.getTag();
                    holder.configureCell(getItem(position));
                    return convertView;
                }
            });
        }
    }

    private static class BankViewHolder extends RecyclerView.ViewHolder {
        private SimpleDraweeView mIconImage;
        private TextView mNameLabel;
        private TextView mHintLabel;

        public BankViewHolder(View itemView) {
            super(itemView);
            mIconImage = (SimpleDraweeView) itemView.findViewById(R.id.img_icon);
            mNameLabel = (TextView) itemView.findViewById(R.id.label_name);
            mHintLabel = (TextView) itemView.findViewById(R.id.label_hint);
        }

        public void configureCell(BankInfo item) {
            mIconImage.setImageURI(Uri.parse(item.bankIcon));
            mNameLabel.setText(item.bankName);
            mHintLabel.setText("单笔限额 " + formatBigNumber(item.limit, false, 0, 2) + "元  |  单日限额 " + formatBigNumber(item.dayLimit, false, 0, 2) + "元");
        }
    }

    public static class ChooseProvinceOrCityFragment extends ListViewFragment {
        public static int TYPE_SETUP_CITY = 0;
        public static int TYPE_SETUP_ADDRESS = 1;
        private int mSetType;

        public static int TYPE_CHOOSE_PROVINCE = 0;
        public static int TYPE_CHOOSE_CITY = 1;
        private int mType;

        private String mSelectedProvince;
        private List<String> mDataSet;
        private String mTitle;

        public ChooseProvinceOrCityFragment init(String title, List<String> provinceList, int setType) {
            Bundle arguments = new Bundle();
            arguments.putInt("set_type", setType);
            arguments.putInt("type", TYPE_CHOOSE_PROVINCE);
            arguments.putStringArrayList("data_set", new ArrayList<>(provinceList));
            arguments.putString("title", title);
            setArguments(arguments);
            return this;
        }

        public ChooseProvinceOrCityFragment init(String title, String province, List<String> cityList, int setType) {
            Bundle arguments = new Bundle();
            arguments.putInt("set_type", setType);
            arguments.putInt("type", TYPE_CHOOSE_CITY);
            arguments.putString("selected_province", province);
            arguments.putStringArrayList("data_set", new ArrayList<>(cityList));
            arguments.putString("title", title);
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mSetType = getArguments().getInt("set_type");
            mType = getArguments().getInt("type");
            mSelectedProvince = getArguments().getString("selected_province");
            mDataSet = getArguments().getStringArrayList("data_set");
            mTitle = getArguments().getString("title");
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setupBackButton(this, findToolbar(this));
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            findToolbar(this).setBackgroundColor(STATUS_BAR_BLACK);
            updateTitle(mTitle);
            setSwipeRefreshable(false);
            changeVisibleSection(TYPE_CONTENT);
            setupListView(mType, mDataSet);

            if (getUserVisibleHint()) {
                setUserVisibleHint(true);
            }
        }

        private void performChooseCity(String province) {
            Observable.just(CommonManager.getInstance().getCity(province))
                    .filter(dataSet -> dataSet != null)
                    .subscribe(dataSet -> {
                        if (dataSet.isEmpty() || dataSet.size() == 1 && Stream.of(dataSet).findFirst().get().equals(province)) {
                            mSelectedProvince = province;
                            performPickUpCity(province);
                        } else {
                            replaceTopFragment(this, new ChooseProvinceOrCityFragment().init("选择开户城市", province, dataSet, mSetType));
                        }
                    });
        }

        private void performPickUpCity(String city) {
            if (mSetType == TYPE_SETUP_CITY) {
                sChooseCitySubject.onNext(Pair.create(mSelectedProvince, city));
            } else if (mSetType == TYPE_SETUP_ADDRESS) {
                sChooseAddressSubject.onNext(Pair.create(mSelectedProvince, city));
            }
            goBack(this);
        }

        private void setupListView(int type, List<String> dataSet) {
            mListView.setOnItemClickListener((parent, view, position, id) ->
            {
                ListAdapter adapter = mListView.getAdapter();
                if (adapter != null) {
                    String item = adapter.getItem(position).toString();
                    if (type == TYPE_CHOOSE_PROVINCE)
                        performChooseCity(item);
                    else if (type == TYPE_CHOOSE_CITY)
                        performPickUpCity(item);
                }
            });
            mListView.setAdapter(new BaseAdapter() {
                private final List<String> mDataSet = new ArrayList<>(dataSet);

                @Override
                public int getCount() {
                    return mDataSet.size();
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
                        convertView = newConvertView(parent);
                        InternalViewHolder holder = new InternalViewHolder(convertView);
                        convertView.setTag(holder);
                    }
                    InternalViewHolder holder = (InternalViewHolder) convertView.getTag();
                    holder.configureCell(getItem(position));
                    holder.showArrow(mType == TYPE_CHOOSE_PROVINCE);
                    return convertView;
                }

                private View newConvertView(ViewGroup parent) {
                    Context context = parent.getContext();
                    RelativeLayout cell = new RelativeLayout(context);
                    cell.setBackgroundResource(R.drawable.sel_cell_bg_default);
                    {
                        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(48));
                        cell.setLayoutParams(params);
                    }

                    {
                        TextView titleLabel = new TextView(context);
                        titleLabel.setTextSize(16);
                        titleLabel.setTextColor(TEXT_BLACK_COLOR);
                        titleLabel.setId(R.id.text1);

                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.addRule(RelativeLayout.CENTER_VERTICAL);
                        params.leftMargin = dp2px(16);
                        cell.addView(titleLabel, params);
                    }

                    {
                        View line = new View(context);
                        line.setId(R.id.line1);
                        line.setBackgroundColor(context.getResources().getColor(R.color.gmf_sep_Line));

                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(1));
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        params.leftMargin = dp2px(16);
                        cell.addView(line, params);
                    }


                    {
                        ImageView arrowImage = new ImageView(context);
                        arrowImage.setId(R.id.icon1);
                        arrowImage.setVisibility(View.VISIBLE);
                        arrowImage.setImageResource(R.mipmap.ic_arrow_right_dark);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        params.addRule(RelativeLayout.CENTER_VERTICAL);
                        params.rightMargin = dp2px(10);
                        cell.addView(arrowImage, params);
                    }
                    return cell;
                }
            });
        }
    }

    private static class InternalViewHolder {
        private TextView mTitleLabel;
        private View mBottomLine;
        private ImageView mArrowImage;

        public InternalViewHolder(View convertView) {
            mTitleLabel = (TextView) convertView.findViewById(R.id.text1);
            mBottomLine = convertView.findViewById(R.id.line1);
            mArrowImage = (ImageView) convertView.findViewById(R.id.icon1);
        }

        public void configureCell(String item) {
            mTitleLabel.setText(item);
        }

        public void showArrow(Boolean show) {
            mArrowImage.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
        }
    }
}
