package com.goldmf.GMFund.util;

import com.goldmf.GMFund.model.FundBrief;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import yale.extension.common.Optional;

/**
 * Created by cupide on 15/8/4.
 */
@SuppressWarnings("ConstantConditions")
public class GsonUtil {

    private interface Transformer<T, R> {
        R onTransform(T in) throws Exception;
    }

    private static <T> T getValue(JsonElement json, T defValue, Transformer<JsonElement, T> transformer, String... keys) {
        try {
            for (String key : keys) {
                json = getAsJsonElement(json, key);
            }
            return Optional.of(transformer.onTransform(json)).or(defValue);
        } catch (Exception ignored) {
        }
        return defValue;
    }

    public static JsonElement getAsJsonElement(JsonElement json, String key) {
        try {
            return json.getAsJsonObject().get(key);
        } catch (Exception ignored) {
        }
        return null;
    }

    public static JsonElement getAsJsonElement(JsonElement json, String... keys) {
        return getValue(json, JsonNull.INSTANCE, it -> it, keys);
    }

    public static JsonObject getAsJsonObject(JsonElement json, String... keys) {
        return getValue(json, null, it -> it.getAsJsonObject(), keys);
    }

    public static JsonArray getAsJsonArray(JsonElement json, String... keys) {
        return getValue(json, new JsonArray(), it -> it.getAsJsonArray(), keys);
    }

    public static String getAsNullableString(JsonElement json, String... keys) {
        return getValue(json, null, it -> it.getAsString(), keys);
    }

    public static String getAsString(JsonElement json, String... keys) {
        return getValue(json, "", it -> it.getAsString(), keys);
    }

    public static Double getAsNullableDouble(JsonElement json, String... keys) {
        return getValue(json, null, it -> it.getAsDouble(), keys);
    }

    public static double getAsDouble(JsonElement json, String... keys) {
        return getValue(json, 0.0, it -> it.getAsDouble(), keys);
    }

    public static long getAsLong(JsonElement json, String... keys) {
        return (long) getAsDouble(json, keys);
    }

    public static int getAsInt(JsonElement json, String... keys) {
        return (int) getAsDouble(json, keys);
    }

    public static boolean getAsBoolean(JsonElement json, String... keys) {
        int temp = GsonUtil.getAsInt(json, keys);
        return temp == 1;
    }

    public static boolean has(JsonElement json, String key){
        try {
            return json.getAsJsonObject().has(key);
        } catch (Exception ignored) {
        }
        return false;
    }


    //jsonArray 相关
    public static JsonObject getChildAsJsonObject(JsonElement element, int i) {

        try {
            if (element.getAsJsonArray().size() > 0) {
                JsonElement value = element.getAsJsonArray().get(i);
                if (value.isJsonObject()) {
                    return value.getAsJsonObject();
                }
            }
        } catch (Exception ignored) {

        }
        return null;
    }

    public static int getChildAsInt(JsonElement element, int i) {

        try {
            if (element.getAsJsonArray().size() > 0) {
                JsonElement value = element.getAsJsonArray().get(i);
                if (value.isJsonPrimitive()) {
                    return getAsInt(value);
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    public static String getChildAsString(JsonElement element, int i) {

        try {
            if (element.getAsJsonArray().size() > 0) {
                JsonElement value = element.getAsJsonArray().get(i);
                if (value.isJsonPrimitive()) {
                    return value.getAsString();
                }
            }
        } catch (Exception ignored) {

        }
        return "";
    }

    public static double getChildAsDouble(JsonElement element, int i) {
        try {
            if (element.getAsJsonArray().size() > 0) {
                JsonElement value = element.getAsJsonArray().get(i);
                if (value.isJsonPrimitive()) {
                    return value.getAsDouble();
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    public static long getChildAsLong(JsonElement element, int i) {
        try {
            if (element.getAsJsonArray().size() > 0) {
                JsonElement value = element.getAsJsonArray().get(i);
                if (value.isJsonPrimitive()) {
                    return value.getAsLong();
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    public static List<Long> getAsLongList(JsonElement element) {
        if (element == null)
            return null;

        List<Long> list = new ArrayList<>();

        JsonArray array = GsonUtil.getAsJsonArray(element);
        if (array != null) {
            for (JsonElement temp : array) {
                long number = GsonUtil.getAsLong(temp);
                list.add(number);
            }
        }

        return list;
    }

    public static ArrayList<Integer> getAsIntList(JsonElement element) {
        if (element == null)
            return null;

        ArrayList<Integer> list = new ArrayList<>();

        JsonArray array = GsonUtil.getAsJsonArray(element);
        if (array != null) {
            for (JsonElement temp : array) {
                int number = GsonUtil.getAsInt(temp);
                list.add(number);
            }
        }

        return list;
    }

    public static List<Double> getAsDoubleList(JsonElement element) {
        if (element == null)
            return null;

        List<Double> list = new ArrayList<>();

        JsonArray array = GsonUtil.getAsJsonArray(element);
        if (array != null) {
            for (JsonElement temp : array) {
                double number = GsonUtil.getAsDouble(temp);
                list.add(number);
            }
        }

        return list;
    }

    public static List<String> getAsStringList(JsonElement element) {
        if (element == null)
            return null;

        List<String> list = new ArrayList<>();

        JsonArray array = GsonUtil.getAsJsonArray(element);
        if (array != null) {
            for (JsonElement temp : array) {
                String text = GsonUtil.getAsString(temp);
                list.add(text);
            }
        }

        return list;
    }

    public static void getAsStringList(List<String> list, JsonElement element) {
        if (list == null || element == null)
            return;

        JsonArray array = GsonUtil.getAsJsonArray(element);
        if (array != null) {
            for (JsonElement temp : array) {
                String str = GsonUtil.getAsString(temp);
                if (str != null) {
                    list.add(str);
                }
            }
        }
    }

    public static class ComPair {
        public String name;
        public String value;

        public static ComPair translateFromJsonData(JsonObject dic, String nameKey, String valueKey) {
            ComPair comPair = new ComPair();
            comPair.name = GsonUtil.getAsString(dic, nameKey);
            comPair.value = GsonUtil.getAsString(dic, valueKey);
            return comPair;
        }
    }

    public static String getAsString(List<Object> list) {
        String str = "";
        for (Object temp : list) {
            str = str + temp + ",";
        }
        return str;
    }
}
