
package com.goldmf.GMFund.manager.mine;

import com.goldmf.GMFund.MyApplication;
import com.goldmf.GMFund.NotificationCenter;
import com.goldmf.GMFund.base.MResults;
import com.goldmf.GMFund.model.User;
import com.goldmf.GMFund.protocol.AuthenticateProtocol;
import com.goldmf.GMFund.protocol.GetProProtocol;
import com.goldmf.GMFund.protocol.LoginProtocol;
import com.goldmf.GMFund.protocol.LogoutProtocol;
import com.goldmf.GMFund.protocol.ModifyPasswordProtocol;
import com.goldmf.GMFund.protocol.ModifyPhoneProtocol;
import com.goldmf.GMFund.protocol.ModifyProtocol;
import com.goldmf.GMFund.protocol.RegisterProtocol;
import com.goldmf.GMFund.protocol.TraderPSProtocol;
import com.goldmf.GMFund.protocol.WXProtocol;
import com.goldmf.GMFund.protocol.base.CHostName;
import com.goldmf.GMFund.protocol.base.CommonPostProtocol;
import com.goldmf.GMFund.protocol.base.ComonProtocol;
import com.goldmf.GMFund.protocol.base.ProtocolBase;
import com.goldmf.GMFund.protocol.base.ProtocolCallback;
import com.goldmf.GMFund.protocol.base.ProtocolManager;
import com.goldmf.GMFund.util.GsonUtil;
import com.goldmf.GMFund.util.ModelSerialization;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import static com.goldmf.GMFund.extension.UIControllerExtension.runOnMain;

/**
 * Created by cupide on 15/7/25.
 */
public class MineManager {

    public static int PhoneLogin = 0; //手机号码
    public static int WeChatLogin = 1; //微信登录

    private static String sMineManagerMineDataKey = "MineManagerMineDataKey";

    private Mine mMe = null;
    private boolean hasEnterTraderPS = false;

    /**
     * 静态方法
     */
    private static MineManager manager = new MineManager();

    public static MineManager getInstance() {
        return manager;
    }

    /**
     * 获取用户的Mine的String 方法
     */
    public final String getMineString() {
        return ModelSerialization.loadByKey(sMineManagerMineDataKey, String.class);
    }

    /**
     * 私有的默认构造
     */
    private MineManager() {

        mMe = Mine.loadData();

        if (mMe == null) {
            mMe = new Mine();

            //没有登录,强制清空下token
            ProtocolManager.getInstance().setAppToken("");
            ProtocolManager.getInstance().setTradeToken("");
            ProtocolManager.getInstance().setSNSToken("");
        } else {
            MyApplication.SHARE_INSTANCE.mHandler.post(() -> {
                //获取用户的新数据
                this.getPro(null);
            });
        }

        NotificationCenter.logoutSubject.subscribe(aVoid -> {
            //删除本地数据
            MineManager.this.mMe.remove();

            MineManager.this.mMe = new Mine();
            hasEnterTraderPS = false;

            ProtocolManager.getInstance().setAppToken("");
            ProtocolManager.getInstance().setSNSToken("");
            ProtocolManager.getInstance().setTradeToken("");
        });

        NotificationCenter.cancelLoginSubject.subscribe(nil -> {

            MineManager.this.mMe = new Mine();

            ProtocolManager.getInstance().setAppToken("");
            ProtocolManager.getInstance().setSNSToken("");
            ProtocolManager.getInstance().setTradeToken("");
        });
    }

    public Mine getmMe() {
        return mMe;
    }

    public String getPhone() {
        return mMe.phone;
    }

    public boolean getHasEnterTraderPS() {
        return hasEnterTraderPS;
    }


    public static boolean isMe(int index) {

        Mine mine = MineManager.getInstance().getmMe();
        if (mine != null
                && mine.isLoginOk()
                && mine.index == index) {
            return true;
        }
        return false;
    }

    /**
     * 返回是否登录成功
     *
     * @return
     */
    public boolean isLoginOK() {
        ProtocolManager pm = ProtocolManager.getInstance();
        return mMe != null && mMe.isLoginOk();
    }

    /**
     * 登录
     *
     * @param loginType PhoneLogin or WeChatLogin
     * @param value1    表示phone or accessToken
     * @param value2    表示 password
     * @param results   返回值
     */
    public final void login(int loginType,
                            String value1,
                            String value2,
                            final MResults<User> results) {

        //先登录
        ProtocolCallback callback = new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResults.MResultsInfo.SafeOnResult(results, protocol.<User>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {

                MineManager.this.getPro(new MResults<User>() {
                    @Override
                    public void onResult(MResultsInfo<User> result) {
                        if (result.isSuccess) {

                            //清除掉已经输入交易密码
                            hasEnterTraderPS = false;

                            runOnMain(() -> NotificationCenter.loginSubject.onNext(null));
                        }

                        MResultsInfo.SafeOnResult(results, result);
                    }
                });

            }
        };

        if (loginType == PhoneLogin) {
            LoginProtocol p = new LoginProtocol(callback);
            p.cellphone = value1;
            p.password = value2;
            p.startWork();
        } else {
            WXProtocol p = new WXProtocol(callback);
            p.accessToken = value1;
            p.action = WXProtocol.WX_ACTION_LOGIN;
            p.openID = value2;
            p.startWork();
        }
    }

    /**
     * 登出协议
     */
    public void logout() {
        runOnMain(() -> NotificationCenter.logoutSubject.onNext(null));
        new LogoutProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {
            }
        }).startWork();
    }

    /**
     * 获取用户 信息
     *
     * @param results
     */
    public void getPro(final MResults<User> results) {

        new GetProProtocol(mMe, new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResults.MResultsInfo.SafeOnResult(results, protocol.<User>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {

                GetProProtocol p1 = (GetProProtocol) protocol;

                MResults.MResultsInfo<User> info = protocol.buildRet();
                info.data = p1.mMe;
                MResults.MResultsInfo.SafeOnResult(results, info);

                //存一份server的数据，给web用。
                ModelSerialization<String> mSerialization = new ModelSerialization<>(p1.mData);
                mSerialization.saveByKey(sMineManagerMineDataKey);

                //存数据
                mMe.save();
            }
        }).startWork();
    }

    /**
     * 处理3种情况：1、没有邀请码， 2、有邀请码，未注册，3、需要登录,详细见PhoneState
     */
    public final void verifyPhone(String phone,
                                  final MResults<PhoneState> results) {

        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "user/reg-qualify/")
                .params(ComonProtocol.buildParams("cellphone", phone))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.<PhoneState>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResults.MResultsInfo<PhoneState> info = protocol.buildRet();

                        if (ret.isJsonObject()) {
                            info.data = PhoneState.translateFromJsonData(ret);
                        }

                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }


    /**
     * type = 1 表示 为注册做的验证码
     * type = 2 表示 为重置密码做的验证码s
     * type = 4 表示 修改手机号码验证原手机号码的验证码。VERIFYCODE_ModifyCellPhone_Old
     * type = 5 表示 修改手机号码验证新手机号码的验证码。VERIFYCODE_ModifyCellPhone_New
     */

    public enum VerifyCode {
        Regist(1),       //注册的验证码
        ResetPS(2),      //重置登录密码的验证码
        ModifyCellPhone_Old(4),  //修改手机号码验证原手机号码的验证码。
        ModifyCellPhone_New(5),  //修改手机号码验证新手机号码的验证码。
        ResetTraderPS(6);       //重置交易密码的验证码

        private int nType;

        VerifyCode(int _nType) {
            this.nType = _nType;
        }

        public int toInt() {
            return this.nType;
        }
    }

    public final void sendPhoneVerifyCode(String phone,
                                          VerifyCode type,
                                          final MResults<Boolean> results) {
        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "sms-verify-code")
                .params(ComonProtocol.buildParams("cellphone", phone, "type", Integer.toString(type.toInt())))
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.<Boolean>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResults.MResultsInfo<Boolean> info = protocol.buildRet();
                        info.data = (protocol.returnCode == 0);
                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }

    public final void register(String phone,
                               String password,
                               String name,
                               String verifyCode,
                               String invitedID,
                               final MResults<User> results) {

        RegisterProtocol p = new RegisterProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResults.MResultsInfo.SafeOnResult(results, protocol.<User>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {

                getPro(new MResults<User>() {
                    @Override
                    public void onResult(MResultsInfo<User> result) {
                        MResultsInfo.SafeOnResult(results, result);
                    }
                });
            }
        });
        p.nickName = name;
        p.cellphone = phone;
        p.password = password;
        p.verifyCode = verifyCode;
        p.invitedId = invitedID;

        p.startWork();
    }

    /**
     * 实名认证接口
     *
     * @param name    名字
     * @param idCard  身份证
     * @param results 返回
     */

    public final void authenticate(String name,
                                   String idCard,
                                   final MResults<Void> results) {

        //        if (!mMe.isLoginOk() || !mMe.needAuthenticate) {
        //            assert ("no login or no need Authenticate" != null);
        //            return false;
        //        }


        AuthenticateProtocol p = new AuthenticateProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {

                getPro(new MResults<User>() {
                    @Override
                    public void onResult(MResultsInfo<User> result) {

                        MResultsInfo.SafeOnResult(results, new MResults.MResultsInfo<Void>());
                    }
                });

            }
        });
        p.name = name;
        p.ID = idCard;
        p.startWork();
    }

    /**
     * 修改密码， 和重置密码
     * reset=NO:表示修改密码。certificate为old_passwd，reset=YES 表示重置密码，certificate为verify_code
     *
     * @param reset
     * @param certificate
     * @param newPassword
     * @param confirmPassword
     * @param results
     */
    public final void ModifyPassword(boolean reset,
                                     String certificate,
                                     String newPassword,
                                     String confirmPassword,
                                     final MResults<Void> results) {
        ModifyPasswordProtocol p = new ModifyPasswordProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {
                MResults.MResultsInfo.SafeOnResult(results, new MResults.MResultsInfo<Void>());

            }
        });
        p.reset = reset;
        p.certificate = certificate;
        p.newPassword = newPassword;
        p.confirmPassword = confirmPassword;
        p.startWork();
    }

    /**
     * 重置交易密码
     *
     * @param certificate
     * @param newPassword
     * @param results
     */
    public final void resetTradePassword(String certificate,
                                         String newPassword,
                                         final MResults<Void> results) {

        tradePassword(certificate, null, newPassword, true, null, results);
    }

    /**
     * 修改交易密码
     *
     * @param oldPassword
     * @param newPassword
     * @param results
     */
    public final void modifyTradePassword(String oldPassword,
                                          String newPassword,
                                          final MResults<Void> results) {

        tradePassword(null, oldPassword, newPassword, true, null, results);
    }

    /**
     * 设置交易密码
     *
     * @param password
     * @param results
     */
    public final void setTradePassword(String password,
                                       final MResults<Void> results) {
        tradePassword(null, null, null, true, password, results);
    }

    /**
     * 验证交易密码
     *
     * @param password
     * @param results
     */
    public final void enterTraderPassword(String password,
                                          final MResults<Void> results) {
        tradePassword(null, null, null, false, password, new MResults<Void>() {
            @Override
            public void onResult(MResultsInfo<Void> result) {
                if (result.isSuccess) {
                    hasEnterTraderPS = true;
                }

                MResultsInfo.SafeOnResult(results, result);
            }
        });
    }

    /**
     * 修改各种信息：用户昵称、头像
     *
     * @param type    1:name, 2:avatar， 3:location, 4:shippingAddress , 5:hideVtcProfile
     * @param value   修改的值
     * @param results
     */
    public final void modifyInfo(int type,
                                 Object value,
                                 final MResults<Void> results) {


        ModifyProtocol p = new ModifyProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());

                switch (type) {
                    case 1:
                        getmMe().modifyName.cancel();
                        break;
                    case 2:
                        getmMe().modifyPhotoUrl.cancel();
                        break;
                    case 3:
                        getmMe().modifyLocation.cancel();
                        break;
                    case 4:
                        getmMe().address.cancel();
                        break;
                    case 5:
                        getmMe().modifyHideVtcProfile.cancel();
                        break;
                    default:
                        assert ("type不对" == null);
                }
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {
                MResults.MResultsInfo<Void> info = protocol.buildRet();
                MResults.MResultsInfo.SafeOnResult(results, info);
            }
        });

        if (type == 1 && value instanceof String) {
            p.nickName = (String) value;
            getmMe().modifyName.modify(p.nickName);
        } else if (type == 2 && value instanceof String) {
            p.avatarUrl = (String) value;
            getmMe().modifyPhotoUrl.modify(p.avatarUrl);
        } else if (type == 3 && value instanceof Mine.CLocation) {
            p.location = (Mine.CLocation) value;
            getmMe().modifyLocation.modify(p.location);
        } else if (type == 4 && value instanceof Mine.ShippingAddress) {
            p.address = (Mine.ShippingAddress) value;
            getmMe().address.modify(p.address);
        } else if (type == 5 && value instanceof Boolean) {
            p.hideVtcProfile = (Boolean) value;
            getmMe().modifyHideVtcProfile.modify(p.hideVtcProfile);
        } else {
            assert ("type不对" == null);
        }
        p.startWork();
    }

    /**
     * 注释：注意看⚠, 注意看⚠, 注意看⚠, 重要的事情说三遍
     * 修改手机号码有4步
     * 第一步：发送手机验证码给旧手机：sendPhoneVerifyCode(oldPhone, ModifyCellPhone_Old(4))
     * 第二步：验证旧手机的验证码：modifyPhone(null, oldPhoneVerifyCode)
     * 第三步：发送手机验证码给新手机：sendPhoneVerifyCode(newPhone, ModifyCellPhone_New(5))
     * 第四步：验证新手机的验证码：modifyPhone(newPhone, oldPhoneVerifyCode)
     *
     * @param newPhone
     * @param verifyCode
     * @param results
     */
    public void modifyPhone(String newPhone,
                            String verifyCode,
                            final MResults<Void> results) {
        ModifyPhoneProtocol p = new ModifyPhoneProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {
                MResults.MResultsInfo<Void> info = protocol.buildRet();
                MResults.MResultsInfo.SafeOnResult(results, info);
            }
        });
        p.phoneNew = newPhone;
        p.verifyCode = verifyCode;
        p.startWork();
    }

    /**
     * 获取用户可选择的头像列表
     *
     * @param results
     */
    public void getAvatarLis(final MResults<List<String>> results) {

        new ComonProtocol.Builder()
                .url(CHostName.HOST1 + "user/avatar-list")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                        MResults.MResultsInfo.SafeOnResult(results, protocol.<List<String>>buildRet());
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {
                        MResults.MResultsInfo<List<String>> info = protocol.buildRet();

                        if (ret.isJsonArray()) {
                            info.data = new ArrayList<>();
                            JsonArray array = ret.getAsJsonArray();
                            for (JsonElement element : array) {
                                String url = GsonUtil.getAsString(element);
                                info.data.add(url);
                            }
                        }
                        MResults.MResultsInfo.SafeOnResult(results, info);
                    }
                })
                .build()
                .startWork();
    }


    /**
     * 微信绑定
     */
    public void binkWX(String accessToken,
                       String openID,
                       final MResults<Void> results) {

        this.actionWX(WXProtocol.WX_ACTION_BINK, accessToken, openID, results);
    }

    /**
     * 微信解绑
     */

    public void clearWX(final MResults<Void> results) {
        this.actionWX(WXProtocol.WX_ACTION_CLEAR, null, null, results);
    }

    public void freshSNSKey() {
        new CommonPostProtocol.Builder()
                .url(CHostName.HOST2 + "token/exchange")
                .callback(new ComonProtocol.ComonCallback() {
                    @Override
                    public void onFailure(ComonProtocol protocol, int errCode, String errMsg) {
                    }

                    @Override
                    public void onSuccess(ComonProtocol protocol, JsonElement ret) {

                        if (ret.isJsonArray()) {
                            String snsToken = GsonUtil.getAsString(ret, "sns_token");
                            ProtocolManager.getInstance().setSNSToken(snsToken);
                        }
                    }
                })
                .build()
                .startWork();
    }

    private void actionWX(int action,
                          String accessToken,
                          String openID,
                          final MResults<Void> results) {
        WXProtocol p = new WXProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {
                MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
            }
        });
        p.accessToken = accessToken;
        p.action = action;
        p.openID = openID;
        p.startWork();
    }


    private void tradePassword(String certificate,
                               String oldPassword,
                               String newPassword,
                               boolean set,
                               String enterPassword,
                               final MResults<Void> results) {

        TraderPSProtocol modify = new TraderPSProtocol(new ProtocolCallback() {
            @Override
            public void onFailure(ProtocolBase protocol, int errCode) {
                MResults.MResultsInfo.SafeOnResult(results, protocol.<Void>buildRet());
            }

            @Override
            public void onSuccess(ProtocolBase protocol) {
                MResults.MResultsInfo<Void> info = protocol.buildRet();
                MResults.MResultsInfo.SafeOnResult(results, info);
            }
        });
        modify.certificate = certificate;
        modify.PSOld = oldPassword;
        modify.PSNew = newPassword;
        modify.set = set;
        modify.PSEnter = enterPassword;

        modify.startWork();
    }


}
