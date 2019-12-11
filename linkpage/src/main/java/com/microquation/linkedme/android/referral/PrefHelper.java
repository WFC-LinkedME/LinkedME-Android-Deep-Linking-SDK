package com.microquation.linkedme.android.referral;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import java.util.Calendar;

/**
 * <p> SharedPreference 帮助类</p>
 */
public class PrefHelper {

    /**
     * 参数无值时的默认值.
     */
    public static final String NO_STRING_VALUE = "";
    private static final int INTERVAL_RETRY = 0;
    /**
     * 一次请求尝试重新链接LinkedME服务的次数在放弃后者抛出一个异常之前
     */
    public static final int MAX_RETRIES = 2; // 默认重试次数 2 次，共3次请求
    public static final int TIMEOUT = 5500; // 默认超时时间 5.5 秒
    /**
     * 存储的sp文件名
     */
    private static final String SHARED_PREF_FILE = "linkedme_referral_shared_pref";
    /**
     * 获取app版本号
     */
    private static final String KEY_APP_VERSION = "lkme_app_version";
    /**
     * 设备指纹ID
     */
    private static final String KEY_DEVICE_FINGERPRINT_ID = "lkme_device_fingerprint_id";
    /**
     * 会话ID
     */
    private static final String KEY_SESSION_ID = "lkme_session_id";
    /**
     * 设备ID，用于deferred deep linking
     */
    private static final String KEY_IDENTITY_ID = "lkme_identity_id";
    /**
     * 标识是否是通过点击深度链接唤起APP
     */
    private static final String KEY_LINK_CLICK_IDENTIFIER = "lkme_link_click_identifier";
    /**
     * APP links
     */
    private static final String KEY_APP_LINK = "lkme_app_link";
    /**
     * 服务器返回的深度链接相关参数
     */
    private static final String KEY_SESSION_PARAMS = "lkme_session_params";
    /**
     * 服务器返回的安装时的深度链接相关参数
     */
    private static final String KEY_INSTALL_PARAMS = "lkme_install_params";
    /**
     * 用户跳转链接
     */
    private static final String KEY_USER_URL = "lkme_user_url";
    /**
     * 是否是deferred deep linking
     */
    private static final String KEY_IS_REFERRABLE = "lkme_is_referrable";
    /**
     * 请求重试次数
     */
    private static final String KEY_RETRY_COUNT = "lkme_retry_count";
    /**
     * 请求重试间隔时间
     */
    public static final String KEY_RETRY_INTERVAL = "lkme_retry_interval";
    /**
     * 请求超时时间
     */
    private static final String KEY_TIMEOUT = "lkme_timeout";
    /**
     * 重置读取系统信息时间
     */
    private static final String KEY_LAST_READ_SYSTEM = "lkme_system_read_date";
    /**
     * 唤起APP的uri scheme
     */
    private static final String KEY_EXTERNAL_INTENT_URI = "lkme_external_intent_uri";
    /**
     * 唤起APP的intent extra
     */
    private static final String KEY_EXTERNAL_INTENT_EXTRA = "lkme_external_intent_extra";
    /**
     * 设备唯一标识
     */
    private static final String KEY_DEVICE_ID = "lkme_device_id";
    /**
     * 结果数据是否处理
     */
    private static final String KEY_HANDLE_STATUS = "lkme_handle_status";
    /**
     * 深度链接
     */
    private static final String KEY_LKME_LINK = "lkme_link";
    /**
     * 用户ID
     */
    private static final String KEY_IDENTITY = "lkme_identity";
    /**
     * IMEI号
     */
    private static final String KEY_IMEI = "lkme_imei";
    /**
     * IMSI号
     */
    private static final String KEY_IMSI = "lkme_imsi";
    /**
     * mac地址
     */
    private static final String KEY_MAC = "lkme_mac";
    /**
     * 非自动session的情况下，是否自动调用close接口
     */
    private static boolean LKME_Smart_Session = true;
    private static final String KEY_DURATION = "lkme_duration";

    /**
     * 剪切板数据
     */
    private static final String KEY_BROWSER_IDENTITY_ID = "lkme_browser_identity_id";

    private static final String KEY_URI_SCHEME = "uri_scheme";
    // 处理由于应用被系统进程杀掉后，应用从最近任务列表恢复后uri scheme恢复的问题
    private static final String KEY_ORIGIN_URI_SCHEME = "origin_uri_scheme";
    private static final String KEY_HTTP_SERVER_URI_SCHEME = "http_server_uri_scheme";

    /**
     * PrefHelper 实例
     */
    private static PrefHelper prefHelper;

    /**
     * SharedPreferences 实例
     */
    private SharedPreferences appSharedPrefs;

    private Editor prefsEditor;

    /**
     * Application context
     */
    private Context context;

    public PrefHelper() {
    }

    private PrefHelper(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(SHARED_PREF_FILE,
                Context.MODE_PRIVATE);
        this.prefsEditor = this.appSharedPrefs.edit();
        this.context = context;
    }

    /**
     * <p>创建{@link PrefHelper}单例</p>
     *
     * @param context Context
     * @return PrefHelper单例
     */
    public static PrefHelper getInstance(Context context) {
        if (prefHelper == null) {
            prefHelper = new PrefHelper(context);
        }
        return prefHelper;
    }

    public int getTimeout() {
        return getInteger(KEY_TIMEOUT, TIMEOUT);
    }

    /**
     * 设置请求超时时间
     *
     * @param timeout 毫秒数
     */
    public void setTimeout(int timeout) {
        setInteger(KEY_TIMEOUT, timeout);
    }

    public int getRetryCount() {
        return getInteger(KEY_RETRY_COUNT, MAX_RETRIES);
    }

    /**
     * 设置重试次数
     *
     * @param retry {@link Integer} 重试次数
     */
    public void setRetryCount(int retry) {
        setInteger(KEY_RETRY_COUNT, retry);
    }

    public int getRetryInterval() {
        return getInteger(KEY_RETRY_INTERVAL, INTERVAL_RETRY);
    }

    /**
     * 设置重试时间间隔
     *
     * @param retryInt {@link Integer} 时间间隔
     */
    public void setRetryInterval(int retryInt) {
        setInteger(KEY_RETRY_INTERVAL, retryInt);
    }

    public String getAppVersion() {
        return getString(KEY_APP_VERSION);
    }

    /**
     * 设置 app 版本号
     *
     * @param version 版本号
     */
    public void setAppVersion(String version) {
        setString(KEY_APP_VERSION, version);
    }

    public String getDeviceFingerPrintID() {
        return getString(KEY_DEVICE_FINGERPRINT_ID);
    }

    /**
     * 设置设备指纹ID
     *
     * @param device_fingerprint_id 设备指纹ID
     */
    public void setDeviceFingerPrintID(String device_fingerprint_id) {
        setString(KEY_DEVICE_FINGERPRINT_ID, device_fingerprint_id);
    }

    public String getSessionID() {
        return getString(KEY_SESSION_ID);
    }

    /**
     * 设置会话ID
     *
     * @param session_id session id
     */
    public void setSessionID(String session_id) {
        setString(KEY_SESSION_ID, session_id);
    }

    public String getIdentityID() {
        return getString(KEY_IDENTITY_ID);
    }

    /**
     * 设置identity_id
     *
     * @param identity_id 浏览器标识
     */
    public void setIdentityID(String identity_id) {
        setString(KEY_IDENTITY_ID, identity_id);
    }

    public String getIdentity() {
        return getString(KEY_IDENTITY);
    }

    /**
     * 设置用户标识
     *
     * @param identity 用户标识
     */
    public void setIdentity(String identity) {
        setString(KEY_IDENTITY, identity);
    }

    public String getExternalIntentUri() {
        return getString(KEY_EXTERNAL_INTENT_URI);
    }

    /**
     * 设置uri scheme
     *
     * @param uri uri scheme字符串
     */
    public void setExternalIntentUri(String uri) {
        setString(KEY_EXTERNAL_INTENT_URI, uri);
    }

    public String getExternalIntentExtra() {
        return getString(KEY_EXTERNAL_INTENT_EXTRA);
    }

    /**
     * 设置额外的intent extra
     *
     * @param extras intent extra
     */
    public void setExternalIntentExtra(String extras) {
        setString(KEY_EXTERNAL_INTENT_EXTRA, extras);
    }

    public String getLinkClickIdentifier() {
        return getString(KEY_LINK_CLICK_IDENTIFIER);
    }

    /**
     * 设置深度链接标识
     *
     * @param identifier 深度链接标识
     */
    public void setLinkClickIdentifier(String identifier) {
        setString(KEY_LINK_CLICK_IDENTIFIER, identifier);
    }

    public String getAppLink() {
        return getString(KEY_APP_LINK);
    }

    /**
     * 设置APP links
     *
     * @param appLinkUrl app links
     */
    public void setAppLink(String appLinkUrl) {
        setString(KEY_APP_LINK, appLinkUrl);
    }

    public String getSessionParams() {
        return getString(KEY_SESSION_PARAMS);
    }

    /**
     * 设置深度链接相关参数
     *
     * @param params 参数字符串
     */
    public void setSessionParams(String params) {
        setString(KEY_SESSION_PARAMS, params);
    }

    public String getInstallParams() {
        return getString(KEY_INSTALL_PARAMS);
    }

    /**
     * 设置安装时深度链接相关参数
     *
     * @param params 参数字符串
     */
    public void setInstallParams(String params) {
        setString(KEY_INSTALL_PARAMS, params);
    }

    public String getUserURL() {
        return getString(KEY_USER_URL);
    }

    /**
     * 设置用户点击的深度链接
     *
     * @param user_url 用户点击的深度链接
     */
    public void setUserURL(String user_url) {
        setString(KEY_USER_URL, user_url);
    }

    public int getIsReferrable() {
        return getInteger(KEY_IS_REFERRABLE);
    }

    public void setIsReferrable() {
        setInteger(KEY_IS_REFERRABLE, 1);
    }

    /**
     * 清除referrable
     */
    public void clearIsReferrable() {
        setInteger(KEY_IS_REFERRABLE, 0);
    }

    public void clearSystemReadStatus() {
        Calendar c = Calendar.getInstance();
        setLong(KEY_LAST_READ_SYSTEM, c.getTimeInMillis() / 1000);
    }

    /**
     * 获取{@link Integer} 类型数据
     *
     * @param key key
     * @return 类型数据
     */
    public int getInteger(String key) {
        return getInteger(key, 0);
    }

    /**
     * 获取{@link Integer} 类型数据
     *
     * @param key          key
     * @param defaultValue 默认值
     * @return {@link Integer} 类型数据
     */
    public int getInteger(String key, int defaultValue) {
        return prefHelper.appSharedPrefs.getInt(key, defaultValue);
    }

    /**
     * 获取{@link Long} 类型数据
     *
     * @param key key
     */
    public long getLong(String key) {
        return prefHelper.appSharedPrefs.getLong(key, 0);
    }

    /**
     * 获取{@link Float} 类型数据
     *
     * @param key key
     * @return {@link Float} 类型数据
     */
    public float getFloat(String key) {
        return prefHelper.appSharedPrefs.getFloat(key, 0);
    }

    /**
     * 获取{@link String} 类型数据
     */
    public String getString(String key) {
        return prefHelper.appSharedPrefs.getString(key, NO_STRING_VALUE);
    }

    /**
     * 获取{@link Boolean} 类型数据
     */
    public boolean getBool(String key) {
        return prefHelper.appSharedPrefs.getBoolean(key, false);
    }

    /**
     * 设置{@link Integer} 类型数据
     */
    public void setInteger(String key, int value) {
        prefsEditor.putInt(key, value);
        prefsEditor.apply();
    }

    /**
     * 设置{@link Long} 类型数据
     */
    public void setLong(String key, long value) {
        prefsEditor.putLong(key, value);
        prefsEditor.apply();
    }

    /**
     * 设置{@link Float} 类型数据
     */
    public void setFloat(String key, float value) {
        prefsEditor.putFloat(key, value);
        prefsEditor.apply();
    }

    /**
     * 设置{@link String} 类型数据
     */
    public void setString(String key, String value) {
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    /**
     * 设置{@link Boolean} 类型数据
     */
    public void setBool(String key, Boolean value) {
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
    }

    public void disableSmartSession() {
        LKME_Smart_Session = false;
    }

    public boolean getSmartSession() {
        return LKME_Smart_Session;
    }

    /**
     * <p>获取{@link #KEY_DEVICE_ID} {@link String} 设备唯一标识的值</p>
     *
     * @return 设备唯一标识
     */
    public String getDeviceID() {
        return getString(KEY_DEVICE_ID);
    }

    /**
     * <p>设置 {@link #KEY_DEVICE_ID} {@link String} 的值</p> <p>这主要用于追踪一个唯一设备,用于后续的分析 </p>
     *
     * @param device_id 服务器返回的设备唯一标识
     */
    public void setDeviceID(String device_id) {
        setString(KEY_DEVICE_ID, device_id);
    }

    /**
     * <p>获取{@link #KEY_HANDLE_STATUS} {@link String} 结果处理状态的值</p>
     *
     * @return true 已处理 false 未处理
     */
    public boolean getHandleStatus() {
        return getBool(KEY_HANDLE_STATUS);
    }

    /**
     * <p>设置 {@link #KEY_HANDLE_STATUS} {@link String} 的值</p> <p>这主要用于标识服务器请求的结果是否被处理 </p>
     *
     * @param status 处理结果状态
     */
    public void setHandleStatus(boolean status) {
        setBool(KEY_HANDLE_STATUS, status);
    }

    /**
     * <p>获取{@link #KEY_LKME_LINK} {@link String} 点击唤起APP的深度链接</p>
     *
     * @return 深度链接
     */
    public String getLMLink() {
        return getString(KEY_LKME_LINK);
    }

    /**
     * <p>设置 {@link #KEY_LKME_LINK} {@link String} 的值</p>  <p>点击唤起APP的深度链接 </p>
     *
     * @param lkme_link 深度链接
     */
    public void setLMLink(String lkme_link) {
        setString(KEY_LKME_LINK, lkme_link);
    }

    /**
     * <p>获取IMEI号{@link #KEY_IMEI} {@link String}</p>
     *
     * @return IMEI号
     */
    public String getIMEI() {
        return getString(KEY_IMEI);
    }

    /**
     * <p>设置 {@link #KEY_IMEI} {@link String} 的值</p> <p> IMEI号 </p>
     *
     * @param imei imei号
     */
    public void setIMEI(String imei) {
        setString(KEY_IMEI, imei);
    }

    /**
     * <p>获取IMSI号{@link #KEY_IMSI} {@link String}</p>
     *
     * @return IMSI号
     */
    public String getIMSI() {
        return getString(KEY_IMSI);
    }

    /**
     * <p>设置 {@link #KEY_IMSI} {@link String} 的值</p> <p> IMSI号 </p>
     *
     * @param imsi imsi号
     */
    public void setIMSI(String imsi) {
        setString(KEY_IMSI, imsi);
    }

    /**
     * <p>获取mac地址{@link #KEY_MAC} {@link String}</p>
     *
     * @return mac地址
     */
    public String getMac() {
        return getString(KEY_MAC);
    }

    /**
     * <p>设置 {@link #KEY_MAC} {@link String} 的值</p> <p> mac地址 </p>
     *
     * @param mac mac地址
     */
    public void setMac(String mac) {
        setString(KEY_MAC, mac);
    }

    public int getDuration() {
        return getInteger(KEY_DURATION, 0);
    }

    public void setDuration(int duration) {
        setInteger(KEY_DURATION, duration);
    }

    public void setBrowserIdentityId(String browserIdentityId) {
        setString(KEY_BROWSER_IDENTITY_ID, browserIdentityId);
    }

    public String getBrowserIdentityId() {
        String browserIdentityId = TextUtils.equals(getString(KEY_BROWSER_IDENTITY_ID), NO_STRING_VALUE) ? "" : getString(KEY_BROWSER_IDENTITY_ID);
        setString(KEY_BROWSER_IDENTITY_ID, "");

        return browserIdentityId;
    }

    /***
     * @return String
     */
    public String getUriScheme() {
        return TextUtils.equals(getString(KEY_URI_SCHEME), NO_STRING_VALUE) ? "" : getString(KEY_URI_SCHEME);
    }

    /**
     * 记录格式化后的 HttpServer 打开的 Uri Scheme，用于重复跳转的校验
     */
    public void setUriScheme(String uriScheme) {
        setString(KEY_URI_SCHEME, uriScheme);
    }

    /***
     * @return String
     */
    public String getOriginUriScheme() {
        return TextUtils.equals(getString(KEY_ORIGIN_URI_SCHEME), NO_STRING_VALUE) ? "" : getString(KEY_ORIGIN_URI_SCHEME);
    }

    public void setOriginUriScheme(String uriScheme) {
        setString(KEY_ORIGIN_URI_SCHEME, uriScheme);
    }

    /***
     * @return String
     */
    public String getAndClearHttpServerUriScheme() {
        String uriScheme = TextUtils.equals(getString(KEY_HTTP_SERVER_URI_SCHEME), NO_STRING_VALUE) ? "" : getString(KEY_HTTP_SERVER_URI_SCHEME);
        setHttpServerUriScheme(NO_STRING_VALUE);
        return uriScheme;
    }

    /**
     * 存储 HttpServer 唤起拼接的 Uri Scheme
     */
    public void setHttpServerUriScheme(String uriScheme) {
        setString(KEY_HTTP_SERVER_URI_SCHEME, uriScheme);
    }

    private static final String IS_SUPPORT = "miit_is_support";

    public void setIsSupport(boolean isSupport) {
        setBool(IS_SUPPORT, isSupport);
    }

    public boolean getIsSupport() {
        return getBool(IS_SUPPORT);
    }

    private static final String AAID = "miit_aaid";

    public void setAAID(String aaid) {
        setString(AAID, aaid);
    }

    public String getAAID() {
        return getString(AAID);
    }

    private static final String OAID = "miit_oaid";

    public void setOAID(String oaid) {
        setString(OAID, oaid);
    }

    public String getOAID() {
        return getString(OAID);
    }

    private static final String UDID = "miit_udid";

    public void setUDID(String udid) {
        setString(UDID, udid);
    }

    public String getUDID() {
        return getString(UDID);
    }

    private static final String VAID = "miit_vaid";

    public void setVAID(String vaid) {
        setString(VAID, vaid);
    }

    public String getVAID() {
        return getString(VAID);
    }

    private static final String MIIT_SDK_VERSION = "miit_sdk_version";

    public void setMiitSdkVersion(String sdkVersion) {
        setString(MIIT_SDK_VERSION, sdkVersion);
    }

    public String getMiitSdkVersion() {
        return getString(MIIT_SDK_VERSION);
    }
}