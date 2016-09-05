package com.goldmf.GMFund.protocol;

import android.text.TextUtils;

import com.goldmf.GMFund.manager.message.SendMessage;
import com.goldmf.GMFund.manager.message.UpImageMessage;
import com.goldmf.GMFund.model.GMFMessage;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cupide on 15/12/1.
 */
public class PostMessageProtocol extends ProtocolBase {

    public SendMessage message;
    public String sessionID;
    public boolean bSnsSever = false;

    public PostMessageProtocol(ProtocolCallback callback) {
        super(callback);
    }

    @Override
    protected boolean parseData(JsonElement data) {
        if(super.returnCode == 0){

            message.readFromJsonData(GsonUtil.getAsJsonObject(data, "message_record"));
        }
        return (super.returnCode == 0);
    }

    @Override
    protected String getUrl() {
        if(bSnsSever)
            return CHostName.HOST2 + "message/add";
        else
            return CHostName.HOST1 + "message/post";
    }

    @Override
    protected Map<String, Object> getPostData() {
        HashMap<String, Object> params = new HashMap<>();

        params.put("session_id", this.sessionID);
        params.put("content", this.message.content);

        if(!TextUtils.isEmpty(this.message.title)) {
            params.put("title", this.message.title);
        }
        if(!TextUtils.isEmpty(this.message.imageUrl)){
            params.put("image_url", this.message.imageUrl);
            params.put("image_size", this.message.imageSize());
        }

        if(!TextUtils.isEmpty(this.message.tarLink)) {
            params.put("tar_link_text", this.message.tarLinkText);
            params.put("tar_link", this.message.tarLink);
        }

        params.put("template_type", String.valueOf(this.message.templateType));
        params.put("message_type", String.valueOf(this.message.messagType));

        params.put("local_id", this.message.localID);


        if (message instanceof UpImageMessage){
            UpImageMessage imageMessage = (UpImageMessage) message;

            if(imageMessage.contentList.size()>0) {
                params.put("topic_image_list[]", imageMessage.imageUrlList);
                params.put("topic_content_list[]", imageMessage.contentList);
            }else{
                if(imageMessage.imageUrlList.size()> 0) {
                    params.put("image_list[]", imageMessage.imageUrlList);
                }
            }
        }

        if( message.attachInfo != null){
            params.put("attache_tarlink", this.message.attachInfo.url);
            params.put("attache_title", this.message.attachInfo.title);
            params.put("attache_icon", this.message.attachInfo.imageUrl);
            params.put("attache_msg", this.message.attachInfo.msg);
        }

        if(message.intelligence != null){
            params.put("unlock_score", this.message.intelligence.unlockScore);
        }

        return params;
    }

    @Override
    protected Map<String, String> getParam() {
        return null;
    }
}
