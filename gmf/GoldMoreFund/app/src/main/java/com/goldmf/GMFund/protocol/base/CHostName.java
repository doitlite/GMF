package com.goldmf.GMFund.protocol.base;

import com.goldmf.GMFund.MyConfig;

import java.net.URL;

/**
 * Created by cupide on 16/3/11.
 */


public class CHostName {

    public static String HOST1 ="+host1+";  //host1替代符
    public static String HOST2 ="+host2+";  //host2替代符

    public static String getHost1(){
        return MyConfig.CURRENT_HOST_NAME.first;
    }

    public static String getHost2(){
        return MyConfig.CURRENT_HOST_NAME.second;
    }


    public static String formatUrl(String cgiUrl){
        if(cgiUrl.contains(HOST2)){
            return replaceHost(cgiUrl, HOST2, CHostName.getHost2());
        }
        else if(cgiUrl.contains(HOST1)) {
            return replaceHost(cgiUrl,HOST1, CHostName.getHost1());
        }
        else {
            if(isUrl(cgiUrl)) {
                return cgiUrl;
            }
            else {
                return formatUrl(HOST1+cgiUrl);//默认情况下，防止忘记修改url地址
            }
        }
    }

    private static String replaceHost(String url, String hostKey, String hostValue)
    {
        int index = url.indexOf(hostKey) + hostKey.length();

        if(url.charAt(index) != '/')
        {
            return url.replace(hostKey, hostValue+"/");
        }
        else {
            return url.replace(hostKey, hostValue);
        }
    }

    private static boolean isUrl(String strurl){

        try {
            URL url = new URL(strurl);
            if(url.getProtocol().length() > 0)
                return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    public static String formatUrl(String host, String cgiUrl){
        return CHostName.formatUrl(host + cgiUrl);
    }
}
