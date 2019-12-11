package com.microquation.linkedme.android.network;

import android.content.Context;

import com.microquation.linkedme.android.network.base.ServerRequest;

import org.json.JSONObject;

/**
 * <p>
 * 初始化请求的Session抽象类。所有初始化session的请求都应该继承该类。
 * </p>
 */
abstract class ServerRequestInitSession extends ServerRequest {
    public ServerRequestInitSession(Context context, String requestPath) {
        super(context, requestPath);
    }

    protected ServerRequestInitSession(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }

    @Override
    public boolean isGAdsParamsRequired() {
        return true; //Session start requests need GAds params
    }
}
