package com.goldmf.GMFund.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by cupide on 15/7/25.
 */
public class FileUtil {
    public static Context mContext;

    public static String getCacheDir() {
        return mContext.getCacheDir().getPath();
    }

    public static String getFileDir() {
        return mContext.getFilesDir().getPath();
    }

    public static boolean isFileExist(String file){
        File file1 = new File(file);
        return file1.exists();
    }

    public static boolean saveToFile(byte[] data, String file) {
        removeFile(file);

        FileOutputStream output = null;
        try {
            output = new FileOutputStream(file);
            output.write(data);
            output.close();
            output = null;

            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception e) {
                return false;
            }
        }
    }


    public static byte[] readFromFile(String file) {
        File f = new File(file);
        if (f.isFile()) {
            if (f.exists()) {
                FileInputStream input = null;
                byte[] buffer = null;
                try {
                    input = new FileInputStream(file);
                    int byteCount = input.available();
                    buffer = new byte[byteCount];
                    final int read = input.read(buffer, 0, byteCount);
                    input.close();
                    input = null;
                    return buffer;
                } catch (Exception e) {
                    return null;
                } finally {
                    try {
                        if (input != null) {
                            input.close();
                        }
                    } catch (Exception ignored) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    public static SharedPreferences getSharedPreferences(String name, int mode) {
        return mContext.getSharedPreferences(name, mode);
    }

    public static void saveValue(String name, String key, String value){
        if(name == null || key == null || value == null)return;

        SharedPreferences sp = FileUtil.getSharedPreferences(name, Context.MODE_PRIVATE); //私有数据
        SharedPreferences.Editor editor = sp.edit();//获取编辑器
        editor.putString(key, value);
        editor.apply();//提交修改
    }

    public static String getValue(String name, String key){
        SharedPreferences sp = FileUtil.getSharedPreferences(name, Context.MODE_PRIVATE); //私有数据
        return sp.getString(key, null);
    }

    public static void removeFile(String file){

        File f = new File(file);
        if (f.isFile() && f.exists()) {
            f.delete();
        }
    }
}
