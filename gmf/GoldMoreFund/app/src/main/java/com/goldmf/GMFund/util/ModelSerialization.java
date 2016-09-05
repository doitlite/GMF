package com.goldmf.GMFund.util;

import com.goldmf.GMFund.extension.ObjectExtension;
import com.goldmf.GMFund.manager.mine.Mine;
import com.goldmf.GMFund.manager.mine.MineManager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.File;
import java.lang.reflect.Array;
import java.util.List;

/**
 * Created by cupide on 15/7/25.
 */
public class ModelSerialization<T> {

    private T mData;
    private static String sModelSerializationKey = "ModelSerialization";
    public String fileName;

    public ModelSerialization(T data) {
        mData = data;
    }

    /* 新目录 */
    private static String getNewFileName(String key, String className, boolean userOnly) {
        String userPhone = "";
        if(userOnly){
            userPhone = getString(Mine.sMinePhoneKey);
        }
        return getFileName(FileUtil.getFileDir(), key, className, userPhone);
    }

    /* 旧数据的目录 */
    private static String getOldFileName(String key, String className, boolean userOnly) {

        String userPhone = "";
        if(userOnly){
            userPhone = getString(Mine.sMinePhoneKey);
        }
        return getFileName(FileUtil.getCacheDir(), key, className, userPhone);
    }

    /* 删除的目录 */
    private static String getRemoveFileName(String key, String className, boolean userOnly) {

        String userPhone = "";
        if(userOnly){
            userPhone = getString(Mine.sBackMinePhone);
        }
        return getFileName(FileUtil.getFileDir(), key, className, userPhone);
    }

    private static String getFileName(String dir, String key, String className, String userPhone) {
        return dir + File.separator + key + "." + userPhone + "." + className;
    }

    //某个class的save、load、remove
    public void saveByKey(String key) {
        saveByKey(key, false);
    }
    public void saveByKey(String key, boolean userOnly) {
        if (key == null) return;

        ObjectExtension.safeCall(() -> {
            Gson gson = new Gson();
            String data = gson.toJson(mData);

            fileName = getNewFileName(key, mData.getClass().getSimpleName(), userOnly);

            FileUtil.saveToFile(data.getBytes(), fileName);
        });
    }
    public static <T> T loadByKey(String key, Class<T> classOfT){
        return loadByKey(key, classOfT, false);
    }
    public static <T> T loadByKey(String key, Class<T> classOfT, boolean userOnly) {

        if (key == null) {
            return null;
        }

        String loadFile = getNewFileName(key, classOfT.getSimpleName(), userOnly);
        if(!FileUtil.isFileExist(loadFile)){
            //处理新旧数据位置的问题，如果 有 新数据，则读新数据，如果没有 新数据，就读旧数据。
            loadFile = getOldFileName(key, classOfT.getSimpleName(), userOnly);
        }

        try {
            byte[] data = FileUtil.readFromFile(loadFile);
            if (data != null) {
                String fileData = new String(data);
                if (fileData.length() > 0) {
                    Gson gson = new Gson();
                    return gson.fromJson(fileData, classOfT);
                }
            }
        } catch (JsonSyntaxException e) {
            return null;
        }
        return null;
    }
    public void removeByKey(String key){
        removeByKey(key, false);
    }
    public void removeByKey(String key, boolean userOnly) {

        removeByKey(key, mData.getClass(), userOnly);

//        String fileName = getRemoveFileName(key, mData.getClass().getSimpleName(), userOnly);
//        FileUtil.removeFile(fileName);
    }

    //string的快捷存储
    public static void saveString(String key, String value) {
        FileUtil.saveValue(sModelSerializationKey, key, value);
    }
    public static String getString(String key) {
        return FileUtil.getValue(sModelSerializationKey, key);
    }

    //json格式的快捷存储
    public static void saveJsonByKey(JsonElement element, String key){
        saveJsonByKey(element, key, false);
    }
    public static void saveJsonByKey(JsonElement element, String key, boolean userOnly) {
        if(element == null)return;

        ModelSerialization<String> serialization = new ModelSerialization<>(element.toString());
        serialization.saveByKey(key, userOnly);
    }
    public static JsonElement loadJsonByKey(String key){
        return loadJsonByKey(key, false);
    }
    public static JsonElement loadJsonByKey(String key, boolean userOnly) {

        try {
            String data = loadByKey(key, String.class, userOnly);
            JsonParser parser = new JsonParser();
            JsonElement obj = parser.parse(data);

            return obj;

        } catch (Exception e) {
            return null;
        }
    }
    public static void removeJsonByKey(String key){
        removeJsonByKey(key, false);
    }
    public static void removeJsonByKey(String key, boolean userOnly) {

        removeByKey(key, String.class, userOnly);

//        String fileName = getRemoveFileName(key, String.class.getSimpleName(), userOnly);
//        FileUtil.removeFile(fileName);
    }

    //数组的存储
    public static <T> void saveByKey(List<T> list, Class<T> classOfT, String key) {
        saveByKey(list, classOfT, key, false);
    }
    public static <T> void saveByKey(List<T> list, Class<T> classOfT, String key, boolean userOnly) {
        if(list == null) return;

        ObjectExtension.safeCall(()->{
            if (list.size() > 0) {

                T[] dataSet = (T[]) Array.newInstance(classOfT, list.size());
                list.toArray(dataSet);

                ModelSerialization<T[]> serialization = new ModelSerialization<>(dataSet);
                serialization.saveByKey(key, userOnly);
            }
        });
    }
    public static <T> void removeByKey(String key, Class<T> classOfT){
        removeByKey(key, classOfT, false);
    }
    public static <T> void removeByKey(String key, Class<T> classOfT, boolean userOnly) {

        String fileName = getRemoveFileName(key, classOfT.getSimpleName(), userOnly);
        FileUtil.removeFile(fileName);
    }
}
