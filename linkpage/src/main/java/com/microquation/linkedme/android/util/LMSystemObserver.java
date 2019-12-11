package com.microquation.linkedme.android.util;

import android.util.DisplayMetrics;

/**
 * 获取系统参数 Created by chenhao@linkedme.cc on 16-3-11.
 */
public interface LMSystemObserver {

    String getAppVersion();

    /**
     * 获得当前应用的编译版本
     *
     * @return AppVersionCode
     */
    int getAppVersionCode();

    String getCarrier();

    boolean getBluetoothPresent();

    boolean getNFCPresent();

    boolean getTelephonePresent();

    String getPhoneBrand();

    String getPhoneModel();

    String getOS();

    int getOSVersion();

    /**
     * 获取OSVersion String
     *
     * @return OSVersion
     */
    String getOSVersionString();

    int getUpdateState(boolean updatePrefs);

    DisplayMetrics getScreenDisplay();

    /**
     * wifi连接状态
     */
    boolean getWifiConnected();

    String getAdvertisingId();

    boolean getLATValue();

    /**
     * 获取IMEI号
     *
     * @return IMEI号
     */
    String getIMEI();

    /**
     * 获取IMSI号
     *
     * @return IMSI号
     */
    String getIMSI();

    /**
     * 获取AndroidID
     *
     * @return Android id
     */
    String getAndroidId();

    /**
     * 获取SerialNumber
     *
     * @return SerialNumber
     */
    String getSerialNumber();

    /**
     * 获取MAC地址
     *
     * @return mac地址
     */
    String getMAC();

    /**
     * 获取构建版本信息
     *
     * @return 构建版本信息
     */
    String getFingerPrint();

    /**
     * 获取设备名称
     *
     * @return 获取设备名称
     */
    String getDeviceName();

    /**
     * 获取ip地址
     *
     * @return 获取ip地址
     */
    String getIP();

    /**
     * <p>存储设备唯一标识</p>
     *
     * @param device_id 设备唯一标识
     */
    void setDeviceId(String device_id);

    /**
     * <p>存储设备唯一标识，同时存储设备mac地址等其他信息</p>
     * <p>格式为：device_id,imei,android_id,serial_number,mac;</p>
     *
     * @param deviceInfo 设备信息
     */
    void setDeviceInfo(String deviceInfo);

    /**
     * 设备信息是否更新，IMEI号及mac地址调用install接口时可能未能获取，需要在open接口补全信息
     *
     * @return 0:未更新 1:设备信息已更新
     */
    String isDeviceInfoUpdate();

    /**
     * 获取设备唯一标识
     *
     * @return 设备唯一标识
     */
    String getDeviceId();

    /**
     * 获取应用名称
     *
     * @return 应用名称
     */
    String getApplicationName();

    /**
     * 获取剪切版中的内容
     */
    String getBrowserIdentityId();

    void obtainOAIDAsync();
}