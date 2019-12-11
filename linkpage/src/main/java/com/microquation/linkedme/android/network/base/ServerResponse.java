package com.microquation.linkedme.android.network.base;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 服务器响应类
 * Created by lipeng on 15/10/27.
 */
public class ServerResponse {

    /**
     * HTTP 响应状态码
     */
    private int statusCode;

    /**
     * 与该响应关联的标签
     */
    private String tag;

    /**
     * 响应结果，通过get方法获取该值
     */
    private Object post;

    public final byte[] data;

    /**
     * @param tag        标签
     * @param statusCode HTTP 状态吗
     */
    public ServerResponse(String tag, int statusCode) {
        this.tag = tag;
        this.statusCode = statusCode;
        data = new byte[0];
    }

    /**
     * @param tag        标签
     * @param statusCode HTTP 状态吗
     */
    public ServerResponse(String tag, int statusCode, byte[] data) {
        this.tag = tag;
        this.statusCode = statusCode;
        this.data = data;
    }

    public String getTag() {
        return tag;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setPost(Object post) {
        this.post = post;
    }

    public JSONObject getObject() {
        if (post instanceof JSONObject) {
            return (JSONObject) post;
        }

        return null;
    }

    public JSONArray getArray() {
        if (post instanceof JSONArray) {
            return (JSONArray) post;
        }

        return null;
    }

    public byte[] getData() {
        return data;
    }



    /**
     * 获取失败原因
     */
    public String getFailReason() {
        String causeMsg = "";
        try {
            JSONObject postObj = getObject();
            if (postObj != null
                    && postObj.has("error")
                    && postObj.getJSONObject("error").has("message")) {
                causeMsg = postObj.getJSONObject("error").getString("message");
                if (causeMsg != null && causeMsg.trim().length() > 0) {
                    causeMsg = causeMsg + ".";
                }
            }
        } catch (Exception ignore) {
        }
        return causeMsg;
    }

}
