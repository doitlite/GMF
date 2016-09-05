package com.goldmf.GMFund.util;

/**
 * Created by cupide on 15/9/7.
 */
public class VersionUtil {

    public static boolean isBigger(String version1, String version2){

        return  (compare(version1, version2) >0);
    }

    private static int compare(String version1, String version2){

        if(version1 != null) version1 = version1.replaceAll(" ", "");
        if(version2 != null) version2 = version2.replaceAll(" ", "");

        if(version1 == null || version1.length() == 0){
            if(version2 != null && version2.length() != 0) {
                return -1;
            }else {
                return 0;
            }
        }

        if(version2 == null || version2.length() == 0){
            return 1;
        }

        String [] sp1 = version1.split("\\.");
        String [] sp2 = version2.split("\\.");

        for (int i = 0; i < Math.min(sp1.length, sp2.length); i++){
            int value1 = Integer.valueOf(sp1[i]);
            int value2 = Integer.valueOf(sp2[i]);
            if(value1 > value2){
                return 1;
            }else if(value1 < value2){
                return -1;
            }
        }

        if(sp1.length > sp2.length){
            return 1;
        }else if(sp1.length < sp2.length){
            return -1;
        }
        else{
            return 0;
        }
    }
}
