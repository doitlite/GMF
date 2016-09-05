package com.goldmf.GMFund.controller.dialog;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.goldmf.GMFund.R;

/**
 * Created by yale on 16/3/22.
 */
public abstract class RequestPermissionDialog extends BasicDialog {
    public RequestPermissionDialog(Context context, String[] permissions) {
        super(context, R.style.GMFDialog);
        setCancelable(false);

        setContentView(R.layout.dialog_request_permission);
        findViewById(R.id.cell_storage).setVisibility(contain(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE) ? View.VISIBLE : View.GONE);
        findViewById(R.id.cell_phone_state).setVisibility(contain(permissions, Manifest.permission.READ_PHONE_STATE) ? View.VISIBLE : View.GONE);
        findViewById(R.id.cell_location).setVisibility(contain(permissions, Manifest.permission.ACCESS_FINE_LOCATION) ? View.VISIBLE : View.GONE);

        findViewById(R.id.btn_next).setOnClickListener(v -> onNextButtonClick(this, v));
    }

    protected abstract void onNextButtonClick(Dialog dialog, View button);

    private boolean contain(String[] array, String item) {
        if (array == null || item == null)
            return false;

        for (String it : array) {
            if (it.equals(item))
                return true;
        }

        return false;
    }

}
