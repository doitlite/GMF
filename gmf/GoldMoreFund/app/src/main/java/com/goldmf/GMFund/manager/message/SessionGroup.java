package com.goldmf.GMFund.manager.message;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cupide on 16/7/15.
 */
public class SessionGroup extends  MessageSession {

    private List<MessageSession> sessionList = new ArrayList<>();

    public SessionGroup(String ID) {
        super(ID);
    }

    public List<MessageSession> getSessionList(){
        return sessionList;
    }

    public void readFromJsonData(JsonObject dic){
        super.readFromJsonData(dic);
    }


    /**
     * 清除number
     */
    public void clearNumber() {

        for (MessageSession  temp : this.sessionList) {
            temp.clearNumber();
        }

        super.clearNumber();
    }

    public void removeAll(){
        sessionList.clear();
    }

    public void add(MessageSession session){
        sessionList.add(session);
    }


}
