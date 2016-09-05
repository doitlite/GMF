package com.goldmf.GMFund.protocol.base;


/**
 * Created by cupide on 15/7/23.
 */
public interface ProtocolCallback {

    void onFailure(ProtocolBase protocol, int errCode);

    void onSuccess(ProtocolBase protocol);

}
