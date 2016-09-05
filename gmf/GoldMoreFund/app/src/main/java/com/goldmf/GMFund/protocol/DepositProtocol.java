package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.manager.cashier.BankCard;
import com.goldmf.GMFund.model.FundBrief;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/10/5.
 */
public class DepositProtocol extends ProtocolBase {

    public int moneyType;
    public BankCard card;
    public String certificate;
    public double amount;
    public String remark;

    public DepositProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        return CHostName.HOST1 + "cashier/deposit/foreign";
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String, Object> params = new HashMap<>();

        params.put("bank_name", this.card.bank.bankName);
        params.put("bank_username", this.card.cardUserName);
        params.put("bank_card_id", this.card.cardNO);
        params.put("amount", String.valueOf(this.amount));

        params.put("certificate_file", this.certificate);
        params.put("remark", this.remark);

        params.put("market", this.moneyType);

        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
