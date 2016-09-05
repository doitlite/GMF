package com.goldmf.GMFund.manager.discover;

import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.base.MResults.MResultsInfo;
import com.goldmf.GMFund.controller.MainFragments;
import com.goldmf.GMFund.manager.BaseManager;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommandPageArray;
import com.goldmf.GMFund.protocol.base.CommonPostProtocol;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.util.GsonUtil;
import com.google.gson.JsonElement;

import static com.goldmf.GMFund.protocol.base.ComonProtocol.buildParams;

/**
 * Created by cupide on 16/6/20.
 */
public class UserManager extends BaseManager {

    private TraderUserPage talentPage;
    private TraderUserPage traderPage;
    private CommandPageArray<User> followPage;

    /**
     * 静态方法
     */
    private static UserManager manager = new UserManager();

    public static UserManager getInstance() {
        return manager;
    }

    private UserManager() {

        //读取本地数据
        loadLocalData();
    }
    private void loadLocalData() {
    }


    /**
     * 获取所有操盘手list
     */
    public void freshAllTraderList(final MResults<TraderUserPage> results) {

        TraderUserPage page = new TraderUserPage(CHostName.HOST1 + "mobile-boot?cmd=recommandTrader");
        this.traderPage = page;

        this.traderPage.getPrePage(result ->{
            MResultsInfo.SafeOnResult(results,
                    MResultsInfo.<TraderUserPage>COPY(result).setData(this.traderPage));
        });
    }

    /**
     * 获取所有牛人Page
     */
    public void freshAllTalentPage(final MResults<TraderUserPage> results) {

        TraderUserPage page = new TraderUserPage(CHostName.HOST1 + "mobile-boot?cmd=recommandTalent");
        this.talentPage = page;

        this.talentPage.getPrePage(result ->{
            MResultsInfo.SafeOnResult(results,
                    MResultsInfo.<TraderUserPage>COPY(result).setData(this.talentPage));
        });
    }

    /**
     * 获取操盘手\牛人 的管理详情和管理组合
     * @param user  操盘手、牛人
     * @param results
     */
    public void freshMoreInfo(final User user,
                              final MResults<User> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "operator/info")
                .params(buildParams("user_id", String.valueOf(user.index)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResultsInfo.SafeOnResult(results, protocol.<User>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        if (ret != null && ret.isJsonObject()) {
                            user.readFromJsonData(ret.getAsJsonObject());
                        }

                        MResultsInfo<User> info = protocol.buildRet();
                        info.data = user;
                        MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 刷新用户详情
     * @param user  所有用户
     * @param results
     */
    public final void freshUserDetail(final User user,
                                      final MResults<User> results){
        new ComonProtocol.Builder()
                .url(CHostName.HOST2 + "page/user/detail")
                .params(buildParams("user_id", String.valueOf(user.index)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResultsInfo.SafeOnResult(results, protocol.<User>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResultsInfo<User> info = protocol.buildRet();

                        if (ret != null && ret.isJsonObject()) {
                            if (MineManager.isMe(user.index)){
                                info.data = MineManager.getInstance().getmMe();
                                info.data.more.readFromJsonData(GsonUtil.getAsJsonObject(ret));
                            }else {
                                user.readFromJsonData(GsonUtil.getAsJsonObject(ret));
                                info.data = user;
                            }
                        }

                        MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    public final static int Follow_Type_Follow = 1;     //关注列表
    public final static int Follow_Type_Fans = 2;       //粉丝列表

    /**
     * 获取某个用户的粉丝列表、关注列表
     * @param user 用户
     * @param type Follow_Type_Follow、Follow_Type_Fans
     * @param results 异步返回
     */
    public final void freshFollowUser(final User user, int type, final MResults<CommandPageArray<User>> results){

        this.followPage = new CommandPageArray.Builder<User>()
                .cgiUrl(CHostName.HOST2 + "fans/list")
                .cgiParam(buildParams("user_id", String.valueOf(user.index), "type", String.valueOf(type)))
                .classOfT(User.class)
                .commandPage(20)
                .build();
        this.followPage.getPrePage(results);
    }

    /**
     * 添加 关注
     *
     * @param user
     * @param results
     */
    public final void addFollowUser(User user,
                                     final MResults<Void> results) {
        if (user == null)
            return;

        new CommonPostProtocol.Builder()
                .url(CHostName.HOST2 + "fans/attention")
                .postParams(ComonProtocol.buildParams("user_id", String.valueOf(user.index)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        user.hasFollow = true;
                        user.more.fansNum += 1;
                        MineManager.getInstance().getmMe().more.followNum += 1;

                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }

    /**
     * 删除 关注
     *
     * @param user
     * @param results
     */
    public final void delFollowUser(User user,
                                     final MResults<Void> results) {
        if (user == null)
            return;

        new CommonPostProtocol.Builder()
                .url(CHostName.HOST2 + "fans/cancel")
                .postParams(ComonProtocol.buildParams("user_id", String.valueOf(user.index)))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        //本地数据
                        user.hasFollow = false;
                        user.more.fansNum -= 1;
                        if(MineManager.getInstance().getmMe().more.followNum >= 1) {
                            //防止减成负数
                            MineManager.getInstance().getmMe().more.followNum -= 1;
                        }

                        MResults.MResultsInfo.SafeOnResult(results, protocol.buildRet());
                    }
                })
                .build()
                .startWork();
    }

    public static class TraderUserPage extends CommandPageArray<User>{
        public FocusInfo focusInfo;

        public TraderUserPage(String url) {
            super(new Builder<User>()
                    .classOfT(User.class)
                    .cgiUrl(url)
                    .commandPage(20));

            protocol.parseMoreData = data -> {
                TraderUserPage.this.focusInfo = FocusInfo.translateFromJsonData(GsonUtil.getAsJsonObject(data, "focus_info"));
            };
        }
    }
}
