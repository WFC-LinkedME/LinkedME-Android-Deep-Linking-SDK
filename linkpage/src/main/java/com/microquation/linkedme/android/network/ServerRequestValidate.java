package com.microquation.linkedme.android.network;

import com.microquation.linkedme.android.network.base.ServerRequest;

/**
 * 请求实例校验类
 * Created by chenhao on 16-3-8.
 */
public final class ServerRequestValidate {

    /**
     * 是否是创建深度链接请求
     * @param request 请求
     * @return true:是 false:否
     */
    public static boolean isCreateUrl(ServerRequest request) {
        return request instanceof ServerRequestCreateUrl;
    }

    /**
     * 是否是close请求
     * @param request 请求
     * @return true:是 false:否
     */
    public static boolean isRegisterClose(ServerRequest request) {
        return request instanceof ServerRequestRegisterClose;
    }

    /**
     * 是否是install请求
     * @param request 请求
     * @return true:是 false:否
     */
    public static boolean isRegisterInstall(ServerRequest request) {
        return request instanceof ServerRequestRegisterInstall;
    }

    /**
     * 是否是open请求
     * @param request 请求
     * @return true:是 false:否
     */
    public static boolean isRegisterOpen(ServerRequest request) {
        return request instanceof ServerRequestRegisterOpen;
    }

    /**
     * 是否是 install 或者 open 请求
     * @param request 请求
     * @return true:是 false:否
     */
    public static boolean isInitSession(ServerRequest request) {
        return isRegisterOpen(request) || isRegisterInstall(request);
    }
}
