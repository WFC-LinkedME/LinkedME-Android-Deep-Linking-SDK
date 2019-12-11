package com.microquation.linkedme.android.log;


import android.os.Build;
import android.text.TextUtils;

import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.util.LMConstant;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import static com.microquation.linkedme.android.network.base.LMRemoteInterface.NO_CONNECTIVITY_STATUS;
import static com.microquation.linkedme.android.network.base.LMRemoteInterface.NO_LINKEDME_KEY_STATUS;

/**
 * 错误状态码类
 */
public class LMErrorCode {

    String errorMessage_ = "";
    private static int errorCode_;

    /**
     * Session没有正确初始化
     */
    public static final int ERR_NO_SESSION = -101;

    /**
     * 未检测到网络访问权限
     */
    public static final int ERR_NO_INTERNET_PERMISSION = -102;

    /**
     * referral code无效
     */
    public static final int ERR_INVALID_REFERRAL_CODE = -103;

    /**
     * {@link LinkedME}初始化失败
     */
    public static final int ERR_LINKEDME_INIT_FAILED = -104;

    /**
     * url已被占用
     */
    public static final int ERR_LINKEDME_DUPLICATE_URL = -105;
    /**
     * referral code已被占用
     */
    public static final int ERR_LINKEDME_DUPLICATE_REFERRAL_CODE = -106;

    /**
     * 此API需要最低{@link Build.VERSION_CODES#ICE_CREAM_SANDWICH}才可以正确工作
     */
    public static final int ERR_API_LVL_14_NEEDED = -108;
    /**
     * {@link LinkedME}未初始化
     */
    public static final int ERR_LINKEDME_NOT_INSTANTIATED = -109;
    /**
     * 没有{@link LinkedME}提供分享支持的App
     */
    public static final int ERR_LINKEDME_NO_SHARE_OPTION = -110;
    /**
     * 服务器请求超时
     */
    public static final int ERR_LINKEDME_REQ_TIMED_OUT = -111;
    /**
     * 无法访问{@link LinkedME}服务器
     */
    public static final int ERR_LINKEDME_UNABLE_TO_REACH_SERVERS = -112;
    /**
     * 设备无法访问网络
     */
    public static final int ERR_LINKEDME_NO_CONNECTIVITY_STATUS = -113;
    /**
     * LinkedME Key无效
     */
    public static final int ERR_LINKEDME_KEY_INVALID = -114;
    /**
     * 资源文件存在冲突
     */
    public static final int ERR_LINKEDME_RESOURCE_CONFLICT = -115;
    /**
     * {@link LinkedME}请求无效
     */
    public static final int ERR_LINKEDME_INVALID_REQUEST = -116;

    /**
     * {@link LinkedME}非深度链接唤起应用
     */
    public static final int ERR_LINKEDME_NOT_DEEPLINK = -118;

    /**
     * json格式化数据异常
     */
    public static final int ERR_JSON_FORMAT = -119;

    /**
     * 未知错误
     */
    public static final int ERR_UNDEFINED = -120;

    /**
     * <p>错误信息</p>
     *
     * @return 错误提示信息
     */
    public String getMessage() {
        return errorMessage_;
    }

    /**
     * <p>错误代码</p>
     *
     * @return 错误提示代码
     */
    public int getErrorCode() {
        return errorCode_;
    }

    /**
     * <p>重载{@link Object#toString()},以便于输入错误信息</p>
     *
     * @return 当前错误提示信息
     */
    @Override
    public String toString() {
        return getMessage();
    }

    public LMErrorCode(String failMsg, int statusCode) {
        errorMessage_ = failMsg + initErrorCodeAndGetLocalisedMessage(statusCode);
    }

    /**
     * 格式化error信息
     *
     * @param errorCode 错误码
     * @param result    附加信息
     * @return 格式化错误信息
     */
    public static String formatErrorInfo(int errorCode, String result) {

        String errorMsg = initErrorCodeAndGetLocalisedMessage(errorCode);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(LMConstant.RESULT_CODE, errorCode);
            jsonObject.put(LMConstant.RESULT_ERRORMSG, errorMsg);
            if (!TextUtils.isEmpty(result)) {
                jsonObject.put(LMConstant.RESULT_ORIGINMSG, result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    /**
     * <p> 初始化错误代码并转换为内置的提示信息 </p>
     *
     * @param statusCode HTTP状态码或LinkedMe错误代码
     * @return 给定的状态码对应的错误信息
     */
    private static String initErrorCodeAndGetLocalisedMessage(int statusCode) {
        switch (statusCode) {
            case NO_CONNECTIVITY_STATUS:
                errorCode_ = ERR_LINKEDME_NO_CONNECTIVITY_STATUS;
                return "LinkedMe服务接口访问失败:\t网络链接不稳定,请稍后重试";
            case NO_LINKEDME_KEY_STATUS:
                errorCode_ = ERR_LINKEDME_KEY_INVALID;
                return "LinkedMe服务接口访问失败:\t请确认您在AndroidManifest.xml已经正确配置了LinkedMe Key";
            case ERR_LINKEDME_INIT_FAILED:
                errorCode_ = ERR_LINKEDME_INIT_FAILED;
                return "LinkedMe服务接口访问失败:\t请您在首次调用initSession之前一定调用LinkedMe的初始化过程";
            case ERR_NO_SESSION:
                errorCode_ = ERR_NO_SESSION;
                return "LinkedMe服务接口访问失败:\t请您在调用其它LinkedMe服务前,确保已经初始化session";
            case ERR_NO_INTERNET_PERMISSION:
                errorCode_ = ERR_NO_INTERNET_PERMISSION;
                return "请您确保已经在AndroidManifest.xml已经声明了Internet权限";
            case ERR_LINKEDME_DUPLICATE_URL:
                errorCode_ = ERR_LINKEDME_DUPLICATE_URL;
                return "无法使用这个alias创建url.如果您想从用alias,请确保您提交的配置中包含相同参数和用户信息";
            case ERR_LINKEDME_DUPLICATE_REFERRAL_CODE:
                errorCode_ = ERR_LINKEDME_DUPLICATE_REFERRAL_CODE;
                return "这个LinkedMe的referral code已经没使用了";
            case ERR_API_LVL_14_NEEDED:
                errorCode_ = ERR_API_LVL_14_NEEDED;
                return "LinkedMeApp需要工作在最低API14的SDK版本,如果您需要支持API14一下的版本,请使用LinkedMe.getInstance(Context)代替";
            case ERR_LINKEDME_NOT_INSTANTIATED:
                errorCode_ = ERR_LINKEDME_NOT_INSTANTIATED;
                return "请您确保已经在Application中已经初始化了LinkedMe.getInstance(Context)";
            case ERR_LINKEDME_NO_SHARE_OPTION:
                errorCode_ = ERR_LINKEDME_NO_SHARE_OPTION;
                return "没有LinkedMe提供分享支持的App";
            case ERR_LINKEDME_REQ_TIMED_OUT:
                errorCode_ = ERR_LINKEDME_REQ_TIMED_OUT;
                return "请求LinkedMe服务器超时,请检查您的网络状况";
            case ERR_LINKEDME_NOT_DEEPLINK:
                errorCode_ = ERR_LINKEDME_NOT_DEEPLINK;
                return "非深度链接唤起应用";
            case ERR_JSON_FORMAT:
                return "JSON格式化数据异常";
            case ERR_UNDEFINED:
                return "未知错误";
            default:
                if (statusCode >= HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    errorCode_ = ERR_LINKEDME_UNABLE_TO_REACH_SERVERS;
                    return "无法访问LinkedMe服务器暂不可用,请稍后重试";
                } else if (statusCode == HttpURLConnection.HTTP_CONFLICT) {
                    errorCode_ = ERR_LINKEDME_RESOURCE_CONFLICT;
                    return "当前身份的资源存在冲突";
                } else if (statusCode > HttpURLConnection.HTTP_BAD_REQUEST) {
                    errorCode_ = ERR_LINKEDME_INVALID_REQUEST;
                    return "请求无效";
                } else {
                    errorCode_ = ERR_LINKEDME_NO_CONNECTIVITY_STATUS;
                    return "请检查网络状况并确保您进行了正确的初始化工作";
                }
        }
    }
}
