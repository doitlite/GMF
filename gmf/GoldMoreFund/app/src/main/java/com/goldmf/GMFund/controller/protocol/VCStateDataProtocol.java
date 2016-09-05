package com.goldmf.GMFund.controller.protocol;

import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by yale on 16/6/14.
 */
public interface VCStateDataProtocol<R> extends Serializable, Parcelable {

    VCStateDataProtocol<R> init(R vc);

    void restore(R vc);
}
