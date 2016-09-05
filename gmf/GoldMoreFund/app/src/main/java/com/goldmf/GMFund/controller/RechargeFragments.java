package com.goldmf.GMFund.controller;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.controller.business.CashController;
import com.goldmf.GMFund.controller.dialog.GMFBottomSheet;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.controller.internal.CashUIController;
import com.goldmf.GMFund.extension.BitmapExtension;
import com.goldmf.GMFund.extension.MResultExtension;
import com.goldmf.GMFund.extension.StringExtension;
import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.manager.cashier.CashierManager;
import com.goldmf.GMFund.manager.cashier.RechargeDetailInfo;
import com.goldmf.GMFund.manager.common.CommonManager;
import com.goldmf.GMFund.manager.common.QiniuToken;
import com.goldmf.GMFund.manager.fortune.FortuneManager;
import com.goldmf.GMFund.model.CommonDefine;
import com.goldmf.GMFund.model.Fund;
import com.goldmf.GMFund.util.GlobalVariableDic;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.BasicCell;
import com.goldmf.GMFund.widget.EditableCell;
import com.goldmf.GMFund.widget.GMFProgressDialog;
import com.goldmf.GMFund.widget.ProgressButton;
import com.goldmf.GMFund.widget.TopNotificationView;
import com.qiniu.android.storage.UploadManager;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Map;

import rx.Observable;
import rx.functions.Action0;
import rx.subjects.PublishSubject;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.FragmentStackActivity.replaceTopFragment;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.KEY_CN_RECHARGE_SUCCESS_MESSAGE;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.KEY_INVEST_TYPE_INT;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.KEY_OPERATION_TYPE_INT;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.KEY_RECHARGE_TYPE_INT;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.an_WebViewPage;
import static com.goldmf.GMFund.controller.internal.ActivityNavigation.showActivity;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.YELLOW_COLOR;
import static com.goldmf.GMFund.extension.BitmapExtension.newScreenScaleFactor;
import static com.goldmf.GMFund.extension.BitmapExtension.toByteArray;
import static com.goldmf.GMFund.extension.FileExtension.createFile;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.MResultExtension.isSuccess;
import static com.goldmf.GMFund.extension.ObjectExtension.safeCall;
import static com.goldmf.GMFund.extension.ObjectExtension.safeGet;
import static com.goldmf.GMFund.extension.StringExtension.map;
import static com.goldmf.GMFund.extension.StringExtension.normalMoneyTransformer;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.hideKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showAlertDialogOrToastWithFragment;
import static com.goldmf.GMFund.extension.UIControllerExtension.showKeyboardFromWindow;
import static com.goldmf.GMFund.extension.UIControllerExtension.showToast;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.extension.ViewExtension.v_bankCardNumFormatter;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setProgress;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;
import static com.goldmf.GMFund.util.FormatUtil.formatBigNumber;
import static com.goldmf.GMFund.util.FormatUtil.formatMoney;
import static java.lang.Math.max;
import static yale.extension.common.PreCondition.checkNotNull;

/**
 * Created by yale on 15/10/14.
 */
public class RechargeFragments {

    public static PublishSubject<Pair<Integer, String>> sWebRechargeSuccessSubject = PublishSubject.create();

    public static class CNAccountRechargeFragment extends BaseFragment {

        private EditText mAmountField;
        private Button mRechargeButton;
        private double mExpectRechargeAmount;

        public CNAccountRechargeFragment init(Double expectRechargeAmount) {
            Bundle arguments = new Bundle();
            if (expectRechargeAmount != null) {
                arguments.putDouble(CommonProxyActivity.KEY_AMOUNT_DOUBLE, expectRechargeAmount);
            }
            setArguments(arguments);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mExpectRechargeAmount = safeGet(() -> getArguments().getDouble(CommonProxyActivity.KEY_AMOUNT_DOUBLE), 0D);
            GlobalVariableDic.shareInstance().update(KEY_OPERATION_TYPE_INT, KEY_RECHARGE_TYPE_INT);
            return inflater.inflate(R.layout.frag_recharge_cn_account, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
            mAmountField = v_findView(this, R.id.field_amount);
            mRechargeButton = v_findView(this, R.id.btn_recharge);
            mRechargeButton.setEnabled(false);
            v_setClick(view, R.id.btn_recharge, this::performRecharge);
            v_addTextChangedListener(mAmountField, editable -> {
                String normalMoney = StringExtension.map(editable, StringExtension.normalMoneyTransformer());
                if (TextUtils.isEmpty(normalMoney) && !TextUtils.isDigitsOnly(normalMoney)) {
                    editable.delete(editable.length() - 1, editable.length());
                    return;
                }

                String formatMoney = map(normalMoney, StringExtension.formatMoneyTransformer(false, 0));
                if (!formatMoney.equalsIgnoreCase(editable.toString())) {
                    mAmountField.setText(formatMoney);
                    mAmountField.setSelection(formatMoney.length());
                    return;
                }

                if (editable.length() == 1 && editable.toString().equalsIgnoreCase("0")) {
                    editable.clear();
                }

                double money = normalMoney.length() > 0 && TextUtils.isDigitsOnly(normalMoney) ? Double.valueOf(normalMoney) : 0;
                mRechargeButton.setEnabled(money >= 100);
            });

            mAmountField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);
            UmengUtil.stat_enter_recharge_cn_account_page(getActivity());

            if (mExpectRechargeAmount > 0D) {
                mAmountField.setText(String.valueOf(mExpectRechargeAmount));
            }

            consumeEventMR(CashController.fetchBankTips())
                    .setTag("fetch_tips")
                    .onNextSuccess(response -> {
                        TopNotificationView notificationView = v_findView(view, R.id.section_notification);
                        notificationView.setupWithTarLinkText(response.data, true);
                    })
                    .done();
        }

        @SuppressWarnings("ConstantConditions")
        private void performRecharge(View view) {
            hideKeyboardFromWindow(this);
            GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity(), "请稍后");
            progressDialog.show();

            boolean unableToRechargeMore = computeAvailableRechargeAmount() <= 0;
            String expectRechargeAmountStr = StringExtension.map(mAmountField, normalMoneyTransformer());
            double expectRechargeAmount = Double.valueOf(expectRechargeAmountStr);
            if (unableToRechargeMore) {
                String message = String.format("总充值%s,超过银行卡当日限额", expectRechargeAmountStr);
                progressDialog.dismiss();
                createAlertDialog(this, message).show();
            } else {
                CashUIController.performRecharge(this, progressDialog, false, expectRechargeAmount);
            }
        }
    }

    private static double computeAvailableRechargeAmount() {
        BankCard card = CashierManager.getInstance().getCard();
        double dayLimit = card.bank.dayLimit;
        double todayDeposit = FortuneManager.getInstance().cnAccount.todayDeposit;
        return max(dayLimit - todayDeposit, 0);
    }

    public static class CNAccountMultipleRechargeFragment extends BaseFragment {

        private RechargeDetailInfo mRechargeInfo;
        private Button mRechargeButton;

        public CNAccountMultipleRechargeFragment init(RechargeDetailInfo info) {
            Bundle argument = new Bundle();
            argument.putSerializable(RechargeDetailInfo.class.getSimpleName(), info);
            setArguments(argument);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mRechargeInfo = (RechargeDetailInfo) getArguments().getSerializable(RechargeDetailInfo.class.getSimpleName());
            GlobalVariableDic.shareInstance().update(CommonProxyActivity.KEY_RECHARGE_DETAIL_INFO, mRechargeInfo);
            return inflater.inflate(R.layout.frag_recharge_multiple_cn_account, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
            findToolbar(this).setNavigationOnClickListener(v -> showExitDialog(new WeakReference<>(this)));

            mRechargeButton = v_findView(this, R.id.btn_recharge);
            v_setClick(mRechargeButton, this::performRecharge);
            updateContentView();
        }

        @Override
        protected boolean onInterceptGoBack() {
            return super.onInterceptGoBack();
        }

        @Override
        protected boolean onInterceptKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                showExitDialog(new WeakReference<>(this));
                return true;
            }
            return super.onInterceptKeyDown(keyCode, event);
        }

        private static void showExitDialog(WeakReference<BaseFragment> fragmentRef) {
            GMFDialog.Builder builder = new GMFDialog.Builder(fragmentRef.get().getActivity());
            builder.setTitle("提示");
            builder.setMessage("处理尚未完成，确认离开当前流程?");
            builder.setPositiveButton("确认离开", (dialog, which) -> {
                dialog.dismiss();
                if (fragmentRef.get() != null && fragmentRef.get().getView() != null) {
                    BaseFragment fragment = fragmentRef.get();
                    fragment.mIsOperation = false;
                    FragmentStackActivity.goBack(fragment);
                }
            });
            builder.setNegativeButton("取消");
            builder.create().show();
        }

        private void updateContentView() {
            v_setText(mRechargeButton, mRechargeInfo.rechargeFinishAmount > 0 ? "继续充值" : "开始充值");
            v_setText(this, R.id.label_recharge_amount, "本次充值 " + formatMoney(mRechargeInfo.currentRechargeAmount, false, 0, 2));
            v_setText(this, R.id.label_recharge_hint, "总充值" + formatBigNumber(mRechargeInfo.rechargeTotalAmount, false, 0, 2) + "，受银行额度限制，需分多次充值");
            v_setText(this, R.id.label_recharged_amount, "已充值 " + formatBigNumber(mRechargeInfo.rechargeFinishAmount, false, 0, 2));
            v_setText(this, R.id.label_need_to_recharge_amount, "需再充值 " + formatBigNumber(mRechargeInfo.rechargeTotalAmount - mRechargeInfo.rechargeFinishAmount, false, 0, 2));
            v_setProgress(getView(), R.id.progress, mRechargeInfo.rechargeFinishAmount > 0 ? (int) (mRechargeInfo.rechargeFinishAmount * 100 / mRechargeInfo.rechargeTotalAmount) : 0);
        }

        private void performRecharge() {
            replaceTopFragment(this, new PayFragments.SinaPayFragment().init(mRechargeInfo.rechargePayAction.url, true, true));
        }
    }

    public static class CNAccountRechargeSuccessFragment extends BaseFragment {

        private String mMessage;
        private int mOperationType;
        private Button mInvestButton;

        public CNAccountRechargeSuccessFragment init(String message) {
            Bundle argument = new Bundle();
            argument.putString(KEY_CN_RECHARGE_SUCCESS_MESSAGE, message);
            setArguments(argument);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mMessage = getArguments().getString(KEY_CN_RECHARGE_SUCCESS_MESSAGE, "");
            mOperationType = GlobalVariableDic.shareInstance().get(KEY_OPERATION_TYPE_INT);
            return inflater.inflate(R.layout.frag_recharge_cn_account_success, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
            v_setText(view, R.id.label_amount, mMessage);
            mInvestButton = v_findView(view, R.id.btn_invest);
            if (mOperationType == KEY_RECHARGE_TYPE_INT) {
                v_setText(mInvestButton, "完成");
            } else if (mOperationType == KEY_INVEST_TYPE_INT) {
                v_setText(mInvestButton, "立即投资");
                findToolbar(this).setNavigationOnClickListener(v -> showExitDialog(new WeakReference<>(this)));
            }
            v_setClick(mInvestButton, this::performGoToInvest);
        }

        private void performGoToInvest(View view) {
            if (mOperationType == KEY_RECHARGE_TYPE_INT) {
                goBack(this);
            } else if (mOperationType == KEY_INVEST_TYPE_INT) {
                GMFProgressDialog dialog = new GMFProgressDialog(getActivity());
                dialog.show();
                Fund fund = GlobalVariableDic.shareInstance().get(CommonProxyActivity.KEY_FUND);
                double investAmount = GlobalVariableDic.shareInstance().get(CommonProxyActivity.KEY_AMOUNT_DOUBLE);
                String couponID = GlobalVariableDic.shareInstance().get(CommonProxyActivity.KEY_COUPON_ID_STRING);
                CashUIController.performInvest(this, dialog, fund.index, investAmount, couponID);
            }
        }

        @Override
        protected boolean onInterceptGoBack() {
            return super.onInterceptGoBack();
        }

        private static void showExitDialog(WeakReference<BaseFragment> fragmentRef) {
            GMFDialog.Builder builder = new GMFDialog.Builder(fragmentRef.get().getActivity());
            builder.setTitle("提示");
            builder.setMessage("投资尚未完成，确认离开当前流程?");
            builder.setPositiveButton("确认离开", (dialog, which) -> {
                dialog.dismiss();
                if (fragmentRef.get() != null && fragmentRef.get().getView() != null) {
                    BaseFragment fragment = fragmentRef.get();
                    fragment.mIsOperation = false;
                    FragmentStackActivity.goBack(fragment);
                }
            });
            builder.setNegativeButton("取消");
            builder.create().show();
        }
    }

    public static class CNAccountFailurePageFragment extends BaseFragment {

        private String mPageTitle;

        public CNAccountFailurePageFragment init(String title) {
            Bundle argument = new Bundle();
            argument.putString("fail_page_title", title);
            setArguments(argument);
            return this;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            mPageTitle = getArguments().getString("fail_page_title", "");
            return inflater.inflate(R.layout.frag_operation_failure_page, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);
            v_setText(view, R.id.label_message, mPageTitle);
            v_setClick(view, R.id.btn_close, this::performClose);
        }

        private void performClose() {
            goBack(this);
        }
    }

    public static class HKAccountRechargeFragment extends BaseFragment {
        private static final int REQUEST_CODE_IMAGE_FROM_CAMERA = 283;
        private static final int REQUEST_CODE_IMAGE_FROM_GALLERY = 284;

        private TextView mCashierBankLabel;
        private TextView mCashierAccountNameLabel;
        private TextView mCashierAccountNumLabel;
        private EditText mDepositBankField;
        private EditText mDepositUserNameField;
        private EditText mDepositCardIDField;
        private EditText mWithdrawAmountField;
        private EditText mRemarkField;
        private ImageButton mUploadButton;
        private Bitmap mUploadBitmap;
        private ProgressButton mNotifyButton;
        String mCurrentPhotoPath;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_recharge_hk_account, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, YELLOW_COLOR);
            setupBackButton(this, (View) v_findView(this, R.id.btn_cancel));

            // bind child views
            mUploadButton = v_findView(this, R.id.btn_upload);
            mNotifyButton = v_findView(this, R.id.btn_notify);
            mWithdrawAmountField = v_findView(this, R.id.field_withdraw_amount);
            mRemarkField = v_findView(this, R.id.field_remark);
            mCashierBankLabel = v_findView(this, R.id.label_cashier_bank_name);
            mCashierAccountNameLabel = v_findView(this, R.id.label_cashier_account_name);
            mCashierAccountNumLabel = ((BasicCell) v_findView(this, R.id.cell_cashier_account_num)).getExtraTitleLabel();
            mDepositBankField = ((EditableCell) v_findView(this, R.id.cell_depositBank)).getInputField();
            mDepositUserNameField = ((EditableCell) v_findView(this, R.id.cell_depositUserName)).getInputField();
            mDepositCardIDField = ((EditableCell) v_findView(this, R.id.cell_depositCardID)).getInputField();
            v_setClick(mUploadButton, this::performTakePicture);
            v_setClick(mNotifyButton, v -> performUploadPicture());
            v_setClick(this, R.id.btn_guide, v -> showActivity(this, an_WebViewPage(CommonDefine.H5URL_Guidance())));

            Observable.just(mDepositBankField, mDepositUserNameField, mDepositCardIDField, mWithdrawAmountField)
                    .subscribe(editText -> v_addTextChangedListener(editText, editable -> onFormInfoChanged()));

            BankCard hkBankCard = CashierManager.getInstance().getHkCard();
            if (hkBankCard.status == BankCard.Card_Status_Normal) {
                Observable.just(mDepositBankField, mDepositUserNameField, mDepositCardIDField)
                        .subscribe(field -> {
                            field.setFocusable(false);
                            field.setFocusableInTouchMode(false);
                        });
                mDepositBankField.setText(hkBankCard.bank.bankName);
                mDepositUserNameField.setText(hkBankCard.cardUserName);
                mDepositCardIDField.setText(hkBankCard.cardNO);
            } else {
                mDepositCardIDField.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                v_addTextChangedListener(mDepositCardIDField, v_bankCardNumFormatter(mDepositCardIDField));
            }

            // init child views
            BankCard bankCard = (BankCard) CommonManager.getInstance().getConfig(CommonManager.Config_Key_HK_Bank);
            if (bankCard != null) {
                mCashierBankLabel.setText(bankCard.bank.bankName);
                mCashierAccountNameLabel.setText(bankCard.cardUserName);
                mCashierAccountNumLabel.setText(bankCard.cardNO);
            }


            onFormInfoChanged();
            view.requestFocus();

            UmengUtil.stat_enter_recharge_hk_account_page(getActivity());
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == REQUEST_CODE_IMAGE_FROM_CAMERA) {
                // on finish image capture
                if (resultCode == Activity.RESULT_OK) {
                    // on success capture image
                    mUploadBitmap = BitmapExtension.decodeBitmap(mCurrentPhotoPath, newScreenScaleFactor(getActivity()));
                    mUploadButton.setImageBitmap(mUploadBitmap);
                    onFormInfoChanged();
                }
            } else if (requestCode == REQUEST_CODE_IMAGE_FROM_GALLERY) {
                if (resultCode == Activity.RESULT_OK) {
                    Uri imageUri = data.getData();
                    String[] columns = new String[]{MediaStore.Images.Media.DATA};
                    Cursor cursor = getActivity().getContentResolver().query(imageUri, columns, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        int columnIdx = cursor.getColumnIndex(columns[0]);
                        if (columnIdx >= 0) {
                            String imagePath = cursor.getString(columnIdx);
                            mUploadBitmap = BitmapExtension.decodeBitmap(imagePath, newScreenScaleFactor(getActivity()));
                            mUploadButton.setImageBitmap(mUploadBitmap);
                            onFormInfoChanged();
                        }
                        cursor.close();
                    }
                }
            }
        }

        private void onFormInfoChanged() {
            Observable.just(mDepositBankField, mDepositUserNameField, mDepositCardIDField, mWithdrawAmountField)
                    .filter(editText -> editText.getText().toString().trim().length() == 0)
                    .count()
                    .subscribe(count -> {
                        mNotifyButton.setEnabled(count == 0 && mUploadBitmap != null);
                    });
        }

        private void performTakePicture() {

            GMFBottomSheet.Builder builder = new GMFBottomSheet.Builder(getActivity());
            builder.setTitle("上传凭证");
            builder.addContentItem(new GMFBottomSheet.BottomSheetItem("camera", "拍照", R.mipmap.ic_bottomsheet_camera));
            builder.addContentItem(new GMFBottomSheet.BottomSheetItem("gallery", "从手机相册选取图片", R.mipmap.ic_bottomsheet_gallery));
            GMFBottomSheet sheet = builder.create();

            sheet.setOnItemClickListener((bottomSheet, item) -> {
                bottomSheet.dismiss();
                if (item.tag != null) {
                    String id = item.tag.toString();
                    if (id.equals("camera")) {
                        safeCall(() -> {
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                File photoFile = createImageFile("tmp_file");
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_FROM_CAMERA);
                            }
                        });
                    } else if (id.equals("gallery")) {
                        safeCall(() -> {
                            Intent takePictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                                startActivityForResult(takePictureIntent, REQUEST_CODE_IMAGE_FROM_GALLERY);
                            }
                        });
                    }
                }
            });
            sheet.show();
        }

        private void performUploadPicture() {
            Action0 resetButton = () -> mNotifyButton.setMode(ProgressButton.Mode.Normal);

            mIsOperation = true;
            mNotifyButton.setMode(ProgressButton.Mode.Loading);
            consumeEventMR(requestFetchUploadToken())
                    .onNextSuccess(response -> {
                        QiniuToken token = response.data.get(CommonManager.Forcert);
                        checkNotNull(token);
                        byte[] data = toByteArray(mUploadBitmap, Bitmap.CompressFormat.JPEG, 60);
                        new UploadManager().put(data, null, token.token, (key, info, uploadResponse) -> {
                            if (info.isOK()) {

                                String responseKey = safeGet(() -> uploadResponse.getString("key"), "");
                                if (TextUtils.isEmpty(key)) {
                                    String certificate = token.domain + responseKey;
                                    performNotifyCashier(certificate);
                                } else {
                                    createAlertDialog(this, info.error).show();
                                    resetButton.call();
                                    mIsOperation = false;
                                }
                            } else {
                                createAlertDialog(this, info.error).show();
                                resetButton.call();
                                mIsOperation = false;
                            }
                        }, null);
                    })
                    .onNextFail(callback -> {
                        resetButton.call();
                        mIsOperation = false;
                    })
                    .done();
        }

        @SuppressWarnings("ConstantConditions")
        private void performNotifyCashier(String certificate) {

            Action0 resetButton = () -> {
                mNotifyButton.setMode(ProgressButton.Mode.Normal);
            };

            double amount = Double.parseDouble(mWithdrawAmountField.getText().toString());
            String remark = mRemarkField.getText().toString();
            BankCard bankCard = CashierManager.getInstance().getHkCard();
            if (bankCard.status != BankCard.Card_Status_Normal) {
                String depositBank = mDepositBankField.getText().toString().trim();
                String depositUserName = mDepositUserNameField.getText().toString().trim();
                String depositCardID = mDepositCardIDField.getText().toString().trim();
                bankCard = BankCard.buildBankCard(depositBank, depositUserName, depositCardID);
            }
            consumeEventMR(requestNotifyCashier(new WeakReference<>(this), bankCard, certificate, amount, remark))
                    .onNextStart(response -> {
                        mIsOperation = false;
                    })
                    .onNextSuccess(response -> {
                        String bankName = safeGet(() -> CashierManager.getInstance().getHkCard().bank.bankName, "");
                        FragmentStackActivity.replaceTopFragment(this, new HKAccountRechargeSuccessFragment());
                        UmengUtil.stat_recharge_to_hk_account(getActivity(), true, bankName, amount, Optional.of(response.errCode), Optional.of(response.msg));
                    })
                    .done();
        }

        private File createImageFile(String fileName) throws IOException {
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM);
            String path = storageDir.getPath() + File.separator + fileName + ".jpg";
            File image = createFile(path, true);
            mCurrentPhotoPath = path;
            return image;
        }

        private static Observable<MResults.MResultsInfo<Void>> requestNotifyCashier(WeakReference<BaseFragment> fragRef, BankCard bankCard, String certificate, double amount, String remark) {
            return CashController.submitRechargeRecord(bankCard, certificate, amount, remark, response -> {
                if (isSuccess(response)) {
                    showToast(MyApplication.SHARE_INSTANCE, "操盘侠已收到您的查款通知");
                } else {
                    showAlertDialogOrToastWithFragment(fragRef, getErrorMessage(response));
                }
            });
        }

        private static Observable<MResults.MResultsInfo<Map<String, QiniuToken>>> requestFetchUploadToken() {
            return Observable.create(sub -> CommonManager.getInstance().getQiniuAppToken(CommonManager.Forcert, MResultExtension.createObservableMResult(sub, callback -> {
                if (!isSuccess(callback)) {
                    showToast(MyApplication.SHARE_INSTANCE, "上传凭证失败");
                }
            })));
        }
    }

    public static class HKAccountRechargeSuccessFragment extends BaseFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_recharge_hk_account_success, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, YELLOW_COLOR);
            v_setClick(this, R.id.btn_done, v -> goBack(this));
        }
    }
}
