package com.microquation.linkedme.android.referral;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;


/**
 * LinkedME 帮助类
 */
public class LMUtil {

    /**
     * 将 {@link JSONObject} 对象转化为{@link String} 类型，并去除无效字符
     *
     * @param params Link param JSONObject.
`     *
     * @return 字符串类型
     */
    public static String formatAndStringifyLinkParam(JSONObject params) {
        return stringifyAndAddSource(filterOutBadCharacters(params));
    }

    /**
     * 将JSONObject对象转化为string类型，并作为source键的值
     *
     * @param params JSONObject 对象
     *
     * @return 字符串类型
     */
    public static String stringifyAndAddSource(JSONObject params) {
        if (params == null) {
            params = new JSONObject();
        }
        try {
            params.put("source", "Android");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return params.toString();
    }

    /**
     * 去除无效字符
     *
     * @param inputObj {@link JSONObject} 对象
     * @return JSONObject对象
     */
    public static JSONObject filterOutBadCharacters(JSONObject inputObj) {
        JSONObject filteredObj = new JSONObject();
        if (inputObj != null) {
            Iterator<String> keys = inputObj.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    if (inputObj.has(key) && inputObj.get(key).getClass().equals(String.class)) {
                        filteredObj.put(key, inputObj.getString(key).replace("\n", "\\n").replace("\r", "\\r").replace("\"", "\\\""));
                    } else if (inputObj.has(key)) {
                        filteredObj.put(key, inputObj.get(key));
                    }
                } catch (JSONException ignore) {

                }
            }
        }
        return filteredObj;
    }

}
