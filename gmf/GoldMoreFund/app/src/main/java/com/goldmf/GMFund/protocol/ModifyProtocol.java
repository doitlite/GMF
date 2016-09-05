package com.goldmf.GMFund.protocol;

import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/9/2.
 */
public class ModifyProtocol extends ProtocolBase {
    public String nickName;
    public String avatarUrl;
    public Mine.CLocation location;
    public Mine.ShippingAddress address;
    public Boolean hideVtcProfile;

    public ModifyProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {

        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        if (this.hideVtcProfile != null) {
            return CHostName.HOST2 + "user/setting";
        }
        return CHostName.HOST1 + "user/edit";
    }

    @Override
    protected Map<String, Object> getPostData() {

        HashMap<String, Object> params = new HashMap<>();
        if (this.nickName != null)
            params.put("nick_name", this.nickName);

        if (this.avatarUrl != null)
            params.put("avatar_url", this.avatarUrl);

        if (this.location != null) {
            params.put("country", this.location.getCountry());
            params.put("province", this.location.getProvince());
            params.put("city", this.location.getCity());
        }

        if (this.hideVtcProfile != null){
            params.put("hide_vtc_profile", this.hideVtcProfile?1:0);
        }

        if (this.address != null) {
            params.put("modify_type", String.valueOf(4));
            params.put("address_nick_name", this.address.name);
            params.put("address_cellphone", this.address.cellphone);
            params.put("address_province", this.address.city.getProvince());
            params.put("address_city", this.address.city.getCity());
            params.put("address_detail", this.address.address);
        }

        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
