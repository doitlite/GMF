package com.goldmf.GMFund.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.R;
import com.goldmf.GMFund.controller.business.UserController;
import com.goldmf.GMFund.controller.dialog.GMFDialog;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.util.StringFactoryUtil;
import com.goldmf.GMFund.util.UmengUtil;
import com.goldmf.GMFund.widget.GMFProgressDialog;

import rx.Observable;
import yale.extension.common.Optional;

import static com.goldmf.GMFund.controller.FragmentStackActivity.goBack;
import static com.goldmf.GMFund.controller.internal.SignalColorHolder.STATUS_BAR_BLACK;
import static com.goldmf.GMFund.extension.MResultExtension.getErrorMessage;
import static com.goldmf.GMFund.extension.SpannableStringExtension.concatNoBreak;
import static com.goldmf.GMFund.extension.UIControllerExtension.createAlertDialog;
import static com.goldmf.GMFund.extension.UIControllerExtension.findToolbar;
import static com.goldmf.GMFund.extension.UIControllerExtension.setStatusBarBackgroundColor;
import static com.goldmf.GMFund.extension.UIControllerExtension.setupBackButton;
import static com.goldmf.GMFund.extension.UIControllerExtension.showKeyboardFromWindow;
import static com.goldmf.GMFund.extension.ViewExtension.v_addTextChangedListener;
import static com.goldmf.GMFund.extension.ViewExtension.v_findView;
import static com.goldmf.GMFund.extension.ViewExtension.v_setClick;
import static com.goldmf.GMFund.extension.ViewExtension.v_setText;

/**
 * Created by yale on 15/10/14.
 */
public class AuthenticFragments {
    private AuthenticFragments() {
    }

    public static class ViewAuthenticStateFragment extends BaseFragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_view_authentic_state, container, false);
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

            Mine mine = MineManager.getInstance().getmMe();
            v_setText(view, R.id.label_name, mine.authenticate.name);
            v_setText(view, R.id.label_id, mine.authenticate.card);
        }
    }

    public static class AuthenticFragment extends BaseFragment {

        private EditText mNameField;
        private EditText mIdCardField;
        private Button mAuthenticButton;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.frag_authentic, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            setStatusBarBackgroundColor(this, STATUS_BAR_BLACK);
            setupBackButton(this, findToolbar(this), R.drawable.ic_close_light);

            // bind child views
            mNameField = v_findView(this, R.id.field_name);
            mIdCardField = v_findView(this, R.id.field_id_card);
            mAuthenticButton = v_findView(this, R.id.btn_authentic);
            v_setText(this, R.id.contact_customer, concatNoBreak("为保证您的资金同卡进出安全，认证后只能绑定与实名认证为同一人的银行卡。如遇困难，请",
                    StringFactoryUtil.contactCustomerService(getActivity(), v_findView(this, R.id.contact_customer))));

            Observable.just(mNameField, mIdCardField)
                    .subscribe(field -> v_addTextChangedListener(field, s -> mAuthenticButton.setEnabled(s.length() != 0)));
            v_setClick(mAuthenticButton, v -> performAuthentic());
            mAuthenticButton.setEnabled(false);

            mNameField.requestFocus();
            runOnUIThreadDelayed(() -> showKeyboardFromWindow(this), 200);

            UmengUtil.stat_authentic_name_page(getActivity(), Optional.of(this));
        }

        @Override
        protected boolean onInterceptGoBack() {
            NotificationCenter.closeAuthenticPageSubject.onNext(null);
            return super.onInterceptGoBack();
        }

        private void performAuthentic() {
            GMFProgressDialog progressDialog = new GMFProgressDialog(getActivity(), "正在实名认证");
            progressDialog.show();
            String name = mNameField.getText().toString();
            String idCard = mIdCardField.getText().toString();
            consumeEventMR(UserController.authentic(name, idCard))
                    .onNextStart(response -> progressDialog.dismiss())
                    .onNextSuccess(response -> {
                        MineManager.getInstance().getmMe().setAuthenticate = true;
                        NotificationCenter.userInfoChangedSubject.onNext(null);

                        UmengUtil.stat_authentic_name(getActivity(), true, Optional.of(response.errCode), Optional.of(response.msg));
                        GMFDialog.Builder builder = new GMFDialog.Builder(getActivity());
                        builder.setMessage("实名认证成功!");
                        builder.setPositiveButton("完成", (dialog, which) -> {
                            dialog.dismiss();
                            NotificationCenter.closeAuthenticPageSubject.onNext(null);
                            getActivity().finish();
                        });
                        builder.create().show();

                    })
                    .onNextFail(response -> {
                        createAlertDialog(this, getErrorMessage(response)).show();
                        UmengUtil.stat_authentic_name(getActivity(), false, Optional.of(response.errCode), Optional.of(response.msg));
                    })
                    .done();
        }
    }
}
