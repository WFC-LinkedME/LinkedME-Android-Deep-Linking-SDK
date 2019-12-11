package com.microquation.linkedme.android.util;

import com.microquation.linkedme.android.LinkedME;

/**
 * <p> Defines all Json keys associated with linkedme request parameters. </p>
 */
public class Defines {

    public enum Jsonkey {

        IdentityID("identity_id"),
        DeviceFingerprintID("device_fingerprint_id"),
        SessionID("session_id"),
        LinkClickID("click_id"),
        LinkRandom("r"),
        /**
         * 深度链接标识
         */
        LinkLKME("lkme"),
        LinkLKMECC("lkme.cc"),
        LinkWWWLKMECC("www.lkme.cc"),

        AppLinkUsed("linkedme_used"),

        OS("os"),
        AppIdentifier("app_identifier"),
        GoogleAdvertisingID("google_advertising_id"),
        LATVal("lat_val"),
        Clicked_LINKEDME_Link("clicked_linkedme_link"),

        CanonicalIdentifier("$canonical_identifier"),
        ContentTitle(LinkedME.OG_TITLE),
        ContentDesc(LinkedME.OG_DESC),
        ContentImgUrl(LinkedME.OG_IMAGE_URL),
        CanonicalUrl("$canonical_url"),


        ContentType("$content_type"),
        ContentKeyWords("$keywords"),
        ContentExpiryTime("$exp_date"),
        Params("params"),

        External_Intent_URI("external_intent_uri"),
        Last_Round_Trip_Time("lrtt"),
        Queue_Wait_Time("qwt"),
        /**
         * 校验参数
         */
        LKME_SIGN("sign"),
        /**
         * 设备唯一标识
         */
        LKME_DEVICE_ID("device_id"),
        /**
         * 设备名称
         */
        LKME_DEVICE_NAME("device_name"),
        /**
         * 设备唯一标识数据类型
         */
        LKME_DEVICE_TYPE("device_type"),
        /**
         * 设备IMEI号
         */
        LKME_DEVICE_IMEI("device_imei"),
        /**
         * 设备IMEI号（简化参数）
         */
        LKME_IMEI("imei"),
        /**
         * 设备IMSI号（简化参数）
         */
        LKME_IMSI("imsi"),
        /**
         * 设备IMEI号md5值
         */
        LKME_IMEI_MD5("imei_md5"),
        /**
         * 设备Android ID
         */
        LKME_DEVICE_ANDROID_ID("device_android_id"),
        /**
         * 设备Android ID（简化参数）
         */
        LKME_ANDROID_ID("android_id"),
        /**
         * 设备Android ID md5值
         */
        LKME_ANDROID_ID_MD5("android_id_md5"),
        /**
         * 设备SerialNumber
         */
        LKME_DEVICE_SERIAL_NUMBER("device_serial_number"),
        /**
         * 设备mac地址
         */
        LKME_DEVICE_MAC("device_mac"),
        /**
         * 内网IP地址
         */
        LKME_LOCAL_IP("local_ip"),
        /**
         * 设备版本编译信息
         */
        LKME_DEVICE_FINGERPRINT("device_fingerprint"),
        /**
         * 设备生产商
         */
        LKME_DEVICE_BRAND("device_brand"),
        /**
         * 设备型号,比如华为5X的设备型号:KIW-AL10
         */
        LKME_DEVICE_MODEL("device_model"),
        /**
         * 设备是否存在蓝牙模块
         */
        LKME_HAS_BLUETOOTH("has_bluetooth"),
        /**
         * 设备是否存在NFC模块
         */
        LKME_HAS_NFC("has_nfc"),
        /**
         * 设备是否存在SIM卡
         */
        LKME_HAS_SIM("has_sim"),
        /**
         * 操作系统类型
         */
        LKME_OS("os"),
        /**
         * 操作系统版本号 23
         */
        LKME_OS_VERSION_INT("os_version_int"),
        /**
         * 操作系统版本 6.0.1
         */
        LKME_OS_VERSION("os_version"),
        /**
         * 设备屏幕像素密度
         */
        LKME_SCREEN_DPI("screen_dpi"),
        /**
         * 设备屏幕高度(像素)
         */
        LKME_SCREEN_HEIGHT("screen_height"),
        /**
         * 设备屏幕宽度(像素)
         */
        LKME_SCREEN_WIDTH("screen_width"),
        /**
         * 设备当前是否在Wi-Fi环境下
         */
        LKME_IS_WIFI("is_wifi"),
        /**
         * 当前请求是否为referrable
         */
        LKME_IS_REFERRABLE("is_referrable"),
        /**
         * 当前请求是否为debug模式
         */
        LKME_IS_DEBUG("is_debug"),

        LKME_GoogleAdvertisingID("google_advertising_id"),
        /**
         * 运营商
         */
        LKME_CARRIER("carrier"),
        /**
         * 应用版本
         */
        LKME_APP_VERSION("app_version"),
        /**
         * 应用编译版本号
         */
        LKME_APP_VERSION_CODE("app_version_code"),
        /**
         * SDK更新状态标识
         */
        LKME_SDK_UPDATE("sdk_update"),
        /**
         * 当前SDK版本
         */
        LKME_SDK_VERSION("sdk_version"),
        /**
         * 请求重试次数
         */
        LKME_RETRY_TIMES("retry_times"),
        /**
         * 设备指纹ID
         */
        LKME_DF_ID("device_fingerprint_id"),
        /**
         * 设备ID
         */
        LKME_IDENTITY_ID("identity_id"),
        /**
         * 跳转链接
         */
        LKME_LINK("link"),
        /**
         * 会话ID
         */
        LKME_SESSION_ID("session_id"),
        /**
         * 是否为首次会话
         */
        LKME_IS_FIRST_SESSION("is_first_session"),
        /**
         * 是否来自LinkedMe链接
         */
        LKME_CLICKED_LINKEDME_LINK("clicked_linkedme_link"),
        /**
         * 链接元数据
         */
        LKME_METADATA("$metadata"),
        /**
         * 控制参数
         */
        LKME_CONTROLL("$control"),
        /**
         * 用户身份标签
         */
        LKME_IDENTITY("identity"),
        /**
         * 深度链接生成的md5值
         */
        LKME_DEEPLINK_MD5("deeplink_md5_new"),
        /**
         * 是否为测试短链,true为是 false为否
         */
        LKME_IS_TEST_URL("is_test_url"),
        /**
         * timestamp 时间戳
         */
        LKME_TIMESTAMP("timestamp"),
        /**
         * 应用数据
         */
        LKME_DEVICE_UPDATE("device_update"),
        /**
         * 应用名称
         */
        LKME_APP_NAME("app_name"),
        /**
         * 剪切版中获取的identity_id
         */
        LKME_BROWSER_MISC("browser_misc"),

        LKME_MIIT_SUPPORT("miit_support"),

        LKME_MIIT_SDK_VERSION("miit_sdk_version"),

        LKME_AAID("aaid"),

        LKME_OAID("oaid"),

        LKME_UDID("udid"),

        LKME_VAID("vaid");

        private String key = "";

        Jsonkey(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return key;
        }
    }


    private static final String PATH_PREFIX = "/sdk";

    /**
     * <p> 定义所有服务请求的路径 </p>
     */
    public enum RequestPath {
        GetURL(PATH_PREFIX + "/url"),
        RegisterInstall(PATH_PREFIX + "/install"),
        RegisterClose(PATH_PREFIX + "/close"),
        RegisterOpen(PATH_PREFIX + "/open");

        private String key = "";

        RequestPath(String key) {
            this.key = key;
        }

        public String getPath() {
            return key;
        }

        @Override
        public String toString() {
            return key;
        }
    }

    /**
     * <p> Defines link parameter keys </p>
     */
    public enum LinkParam {
        Tags("tags"),
        Alias("alias"),
        Type("type"),
        Duration("duration"),
        Channel("channel"),
        Feature("feature"),
        Stage("stage"),
        /**
         * 跳转链接
         */
        LKME_Link("lkme_link"),
        /**
         * 是否为新用户
         */
        LKME_NewUser("lkme_new_user"),
        LKME_H5Url("h5_url"),
        Data("data"),
        Params("params");

        private String key = "";

        LinkParam(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return key;
        }

    }

    /**
     * <p> track </p>
     */
    public enum LMFilter {
        FILTER("filter"),
        US_PORT("us_port"),
        DEVICE_BRAND("device_brand"),
        DEVICE_MODEL("device_model"),
        START_TYPE("start_type");
        private String key = "";

        LMFilter(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return key;
        }

    }

}
