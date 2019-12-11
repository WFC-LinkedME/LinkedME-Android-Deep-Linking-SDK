package com.microquation.linkedme.android.log;

import android.text.TextUtils;
import android.util.Log;

import com.microquation.linkedme.BuildConfig;
import com.microquation.linkedme.android.util.StringUtils;

public class LMLogger {

    public static final String TAG = "LinkedME_LinkPage";
    public static final String TAG_INNER = "LinkedME_LinkPage_Inner";

    private static boolean isDebug = false;
    private static boolean isInnerDebug = BuildConfig.DEBUG;

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    /**
     * 内部调试日志
     *
     * @param msg 日志
     */
    public static void debug(String msg) {
        debug(TAG_INNER, msg, null);
    }

    private static void debug(String tag, String msg, Throwable throwable) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG_INNER;
        }
        if (isInnerDebug) {
            Log.i(tag, msg, throwable);
        }
    }

    /**
     * 用户可见日志
     *
     * @param msg 日志
     */
    public static void info(String msg) {
        info(TAG, msg, null);
    }

    private static void info(String tag, String msg, Throwable throwable) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        if (isDebug) {
            Log.i(tag, msg, throwable);
        }
    }

    /**
     * 既输出内部日志也输出用户日志
     *
     * @param msg 日志
     */
    public static void all(String msg) {
        all(StringUtils.EMPTY, msg, null);
    }

    public static void all(String tag, String msg, Throwable throwable) {
        debug(tag, msg, throwable);
        info(tag, msg, throwable);
    }

    public static void error(int errorCode, String result, Throwable throwable) {

        String errorMsg = LMErrorCode.formatErrorInfo(errorCode, result);
        info(TAG, errorMsg, throwable);
    }

    /**
     * jsonexception异常信息打印
     *
     * @param throwable throwable
     */
    public static void jsonError(Throwable throwable) {
        error(LMErrorCode.ERR_JSON_FORMAT, StringUtils.EMPTY, throwable);
    }

    /**
     * 未定义异常信息打印
     *
     * @param throwable throwable
     */
    public static void undefinedError(Throwable throwable) {
        error(LMErrorCode.ERR_UNDEFINED, StringUtils.EMPTY, throwable);
    }

    /**
     * 未定义异常信息内部日志打印
     *
     * @param throwable throwable
     */
    public static void debugExceptionError(Throwable throwable) {
        debug(TAG_INNER, StringUtils.EMPTY, throwable);
    }
}