package com.goldmf.GMFund.util;

import java.lang.reflect.Field;

/**
 * Created by Evan on 16/4/29 下午4:14.
 */
public class ReflectionUtils {


    public static boolean setField(Object owner, String className, String fieldName, Object value) {
        boolean result = false;
        try {
            Class classObj = Class.forName(className);
            Field field = classObj.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(owner, value);
            result = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Object getField(Object owner, String className, String fieldName) {
        Object fieldValue = null;
        try {
            Class classObj = Class.forName(className);
            Field field = classObj.getDeclaredField(fieldName);
            field.setAccessible(true);
            fieldValue = field.get(owner);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return fieldValue;
    }
}
