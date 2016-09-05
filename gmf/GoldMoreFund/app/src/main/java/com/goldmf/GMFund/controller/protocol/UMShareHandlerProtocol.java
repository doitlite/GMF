package com.goldmf.GMFund.controller.protocol;

import com.goldmf.GMFund.controller.dialog.ShareDialog;
import com.goldmf.GMFund.manager.common.ShareInfo;

/**
 * Created by yale on 15/11/3.
 */
public interface UMShareHandlerProtocol {
    void onPerformShare(ShareInfo shareInfo, ShareDialog.SharePlatform[] platforms);

    void onPerformShare(ShareInfo shareInfo, ShareDialog.SharePlatform platform);
}
