package com.microquation.linkedme.android.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.bluetooth.BluetoothAdapter;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.log.LMLogger;
import com.microquation.linkedme.android.referral.IIdentifierHandler;
import com.microquation.linkedme.android.referral.PrefHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * <p>系统相关信息工具类</p>
 */
public class SystemObserver implements LMSystemObserver {

    /**
     * 当无法正确获取到系统信息时,将以此作为默认值
     */
    public static final String BLANK = "lkme_no_value";
    public static final int DEVICE_ID_TYPE_NONE = 10;
    public static final int DEVICE_ID_TYPE_UUID = 11;
    public static final int DEVICE_ID_TYPE_ANDROIDID = 12;

    public static final int STATE_FRESH_INSTALL = 0;
    public static final int STATE_UPDATE = 2;
    public static final int STATE_NO_CHANGE = 1;
    private Context mContext;
    public static final int NETWORK_TYPE_GSM = 16;
    public static final int NETWORK_TYPE_WCDMA = 17;

    //mac地址
    private String macSerial = "";
    //微信文件夹
    private String microWCDir = "";
    //qq号
    private String qqNum = "";

    private volatile static SystemObserver INSTANCE = null;


    /**
     * <p>{@link SystemObserver}的构造方法,将初始化 <i>isRealHardware</i> {@link Boolean} 默认值为
     * <i>true</i>.</p>
     *
     * @param context Application Context
     */
    public SystemObserver(Context context) {
        this.mContext = context;
    }

    public static SystemObserver getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new SystemObserver(context);
        }
        return INSTANCE;
    }

    /**
     * <p>Checks the current device's {@link ActivityManager} system service and returns the value
     * of the lowMemory flag.</p>
     *
     * @return <p>A {@link Boolean} value representing the low memory flag of the current
     * device.</p> <p> <ul> <li><i>true</i> - the free memory on the current device is below the
     * system-defined threshold that triggers the low memory flag.</li> <li><i>false</i> - the
     * device has plenty of free memory.</li> </ul>
     */
    private boolean isLowOnMemory() {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo mi = new MemoryInfo();
        activityManager.getMemoryInfo(mi);
        return mi.lowMemory;
    }

    @Override
    public int getAppVersionCode() {
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            if (packageInfo != null)
                return packageInfo.versionCode;
            else
                return 0;
        } catch (Exception ignored) {
        }
        return 0;
    }

    @Override
    public String getAppVersion() {
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            if (packageInfo.versionName != null)
                return packageInfo.versionName;
            else
                return BLANK;
        } catch (Exception ignored) {
        }
        return BLANK;
    }

    @Override
    public String getCarrier() {
        try {
            if (!PermissionUtils.selfPermissionGranted(
                    LinkedME.getInstance().getApplicationContext(), Manifest.permission.READ_PHONE_STATE)) {
                return BLANK;
            }
            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                String ret = telephonyManager.getNetworkOperatorName();
                if (ret != null)
                    return ret;
            }
        } catch (Exception ignore) {

        }
        return BLANK;
    }


    @Override
    public boolean getBluetoothPresent() {
        try {
            if (PermissionUtils.selfPermissionGranted(mContext, Manifest.permission.BLUETOOTH)) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    return bluetoothAdapter.isEnabled();
                }
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public boolean getNFCPresent() {
        try {
            return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC);
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public boolean getTelephonePresent() {
        try {
            return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY);
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public String getPhoneBrand() {
        return Build.MANUFACTURER;
    }

    @Override
    public String getPhoneModel() {
        return Build.MODEL;
    }

    @Override
    public String getOS() {
        return "Android";
    }

    @Override
    public int getOSVersion() {
        return Build.VERSION.SDK_INT;
    }

    @Override
    public String getOSVersionString() {
        return Build.VERSION.RELEASE;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public int getUpdateState(boolean updatePrefs) {
        PrefHelper pHelper = PrefHelper.getInstance(mContext);
        String currAppVersion = getAppVersion();
        if (PrefHelper.NO_STRING_VALUE.equals(pHelper.getAppVersion())) {
            // if no app version is in storage, this must be the first time LinkedME is here
            if (updatePrefs) {
                pHelper.setAppVersion(currAppVersion);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                // if we can access update/install time, use that to check if it's a fresh install or update
                try {
                    PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                    if (packageInfo.lastUpdateTime != packageInfo.firstInstallTime) {
                        return STATE_UPDATE;
                    }
                    return STATE_FRESH_INSTALL;
                } catch (Exception ignored) {
                }
            }
            // otherwise, just register an install
            return STATE_FRESH_INSTALL;
        } else if (!TextUtils.equals(pHelper.getAppVersion(), currAppVersion)) {
            // if the current app version doesn't match the stored, it's an update
            if (updatePrefs) {
                pHelper.setAppVersion(currAppVersion);
            }
            return STATE_UPDATE;
        }
        // otherwise it's an open
        return STATE_NO_CHANGE;
    }

    @Override
    public DisplayMetrics getScreenDisplay() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(displayMetrics);
        return displayMetrics;
    }

    @Override
    public boolean getWifiConnected() {
        try {
            if (PackageManager.PERMISSION_GRANTED == mContext.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
                ConnectivityManager connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Network[] networkInfos = connManager.getAllNetworks();
                    for (Network network : networkInfos) {
                        NetworkInfo networkInfo = connManager.getNetworkInfo(network);
                        if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                            return true;
                        }
                    }
                } else {
                    NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    return ((wifiInfo != null) && wifiInfo.isConnected());
                }
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    @Override
    public String getAdvertisingId() {
        return StringUtils.EMPTY;
    }

    @Override
    public boolean getLATValue() {
        return false;
    }

    @Override
    public String getIMEI() {
        try {
            if (!PermissionUtils.selfPermissionGranted(
                    LinkedME.getInstance().getApplicationContext(), Manifest.permission.READ_PHONE_STATE)) {
                return "";
            }
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            return imei != null ? imei : "";
        } catch (Exception ignore) {
        }
        return "";
    }

    @Override
    public String getIMSI() {
        try {
            if (!PermissionUtils.selfPermissionGranted(
                    LinkedME.getInstance().getApplicationContext(), Manifest.permission.READ_PHONE_STATE)) {
                return "";
            }
            TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            String imsi = tm.getSubscriberId();
            return imsi != null ? imsi : "";
        } catch (Exception ignore) {
        }
        return "";
    }

    @Override
    public String getAndroidId() {
        try {
            return Settings.System.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception ignore) {
        }
        return "";
    }

    @Override
    public String getSerialNumber() {
        try {
            return Build.SERIAL;
        } catch (Exception ignore) {
        }
        return "";
    }

    @Override
    public String getMAC() {
        try {
            if (!TextUtils.isEmpty(macSerial)) {
                return macSerial;
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                WifiManager wifi = (WifiManager) LinkedME.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifi != null) {
                    WifiInfo wifiInfo = wifi.getConnectionInfo();
                    if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getMacAddress()) &&
                            !TextUtils.equals("02:00:00:00:00:00", wifiInfo.getMacAddress().trim())) {
                        macSerial = wifiInfo.getMacAddress().trim();
                        return macSerial;
                    }
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                macSerial = getMacCompat23();
            } else {
                macSerial = getMacCompat24();
            }

            if (!TextUtils.isEmpty(macSerial)) {
                return macSerial;
            }
        } catch (Exception ignore) {
        }
        return "";
    }

    /**
     * Android 23及以下版本使用该方法获取mac地址
     */
    private String getMacCompat23() throws Exception {
        String mac23 = null;
        String str = "";
        String streth = "";
        //无线mac地址
        Process pp = Runtime.getRuntime().exec(
                "cat /sys/class/net/wlan0/address");
        InputStreamReader ir = new InputStreamReader(pp.getInputStream());
        LineNumberReader input = new LineNumberReader(ir);

        for (; null != str; ) {
            str = input.readLine();
            if (str != null) {
                mac23 = str.trim();// 去空格
                break;
            }
        }
        if (!TextUtils.isEmpty(mac23)) {
            return mac23;
        }
        //有线mac地址
        Process ppeth = Runtime.getRuntime().exec(
                "cat /sys/class/net/eth0/address");
        InputStreamReader ireth = new InputStreamReader(ppeth.getInputStream());
        LineNumberReader inputeth = new LineNumberReader(ireth);

        for (; null != streth; ) {
            streth = inputeth.readLine();
            if (streth != null) {
                mac23 = streth.trim();// 去空格
                break;
            }
        }
        return mac23;
    }

    /**
     * Android7.0及以上通过网络接口取mac地址
     */
    private static String getMacCompat24() throws Exception {
        List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
        for (NetworkInterface nif : all) {
            if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

            byte[] macBytes = nif.getHardwareAddress();
            if (macBytes == null) {
                return null;
            }

            StringBuilder res1 = new StringBuilder();
            for (byte b : macBytes) {
                res1.append(String.format("%02X:", b));
            }

            if (res1.length() > 0) {
                res1.deleteCharAt(res1.length() - 1);
            }
            return res1.toString();
        }
        return null;
    }

    @Override
    public String getFingerPrint() {
        try {
            return Build.FINGERPRINT;
        } catch (Exception ignore) {
        }
        return "";
    }

    @Override
    public String getDeviceName() {
        try {
            if (PermissionUtils.selfPermissionGranted(mContext, Manifest.permission.BLUETOOTH)) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    return bluetoothAdapter.getName();
                }
            }
        } catch (Exception ignore) {
        }
        return "";
    }

    @Override
    public String getIP() {
        try {
            //获取wifi服务
            WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            //判断wifi是否开启
            if (!wifiManager.isWifiEnabled()) {
                //获取移动网络地址
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            return inetAddress.getHostAddress();
                        }
                    }
                }
            } else {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int ipAddress = wifiInfo.getIpAddress();
                if (ipAddress != 0) {
                    return intToIp(ipAddress);
                } else {
                    return "";
                }
            }
        } catch (Exception ignore) {

        }
        return "";
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }

    @Override
    public void setDeviceId(final String device_id) {
        if (!TextUtils.isEmpty(device_id)) {
            final PrefHelper prefHelper = PrefHelper.getInstance(mContext);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //存储到sp文件中
                        prefHelper.setDeviceID(device_id);
                        //存储到SD卡上
                        FileUtils.getInstance().writeDeviceId(device_id);
                        //Android 6.0以下存储到setting provider中

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && mContext.checkCallingOrSelfPermission(Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
                            Settings.System.putString(mContext.getContentResolver(), FileUtils.LM_DEVICE_ID, device_id);
                        }
                    } catch (Exception ignore) {
                        LMLogger.debugExceptionError(ignore);
                    }
                }
            }).start();
        }
    }

    @Override
    public void setDeviceInfo(final String deviceInfo) {
        if (!TextUtils.isEmpty(deviceInfo)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String exist_device_info = FileUtils.getInstance().readDeviceInfo();
                        //不相同时才会重新写入数据
                        if (!TextUtils.equals(exist_device_info, deviceInfo)) {
                            //存储到SD卡上
                            FileUtils.getInstance().writeDeviceInfo(exist_device_info, deviceInfo);
                            //Android 6.0以下存储到setting provider中
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && mContext.checkCallingOrSelfPermission(Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
                                Settings.System.putString(mContext.getContentResolver(), FileUtils.LM_DEVICE_INFO, deviceInfo);
                            }
                        }
                    } catch (Exception ignore) {
                        LMLogger.debugExceptionError(ignore);
                    }
                }
            }).start();
        }
    }

    @Override
    public String isDeviceInfoUpdate() {
        final PrefHelper prefHelper = PrefHelper.getInstance(mContext);
        String imei = prefHelper.getIMEI();
        String imsi = prefHelper.getIMSI();
        String mac = prefHelper.getMac();
        if (TextUtils.isEmpty(imei) && !TextUtils.isEmpty(getIMEI())) {
            return "1";
        }
        if (TextUtils.isEmpty(imsi) && !TextUtils.isEmpty(getIMSI())) {
            return "1";
        }
        if (TextUtils.isEmpty(mac) && !TextUtils.isEmpty(getMAC())) {
            return "1";
        }
        return "0";
    }

    @Override
    public String getDeviceId() {
        PrefHelper prefHelper = PrefHelper.getInstance(mContext);
        //从sp文件中查找
        String device_id = prefHelper.getDeviceID();
        if (!TextUtils.isEmpty(device_id) && !PrefHelper.NO_STRING_VALUE.equals(device_id)) {
            return device_id;
        }
        //从SD卡上读取
        device_id = FileUtils.getInstance().readDeviceId();
        if (!TextUtils.isEmpty(device_id) && !PrefHelper.NO_STRING_VALUE.equals(device_id)) {
            return device_id;
        }
        //Android 6.0以下存储到setting provider中
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && mContext.checkCallingOrSelfPermission(Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
                device_id = Settings.System.getString(mContext.getContentResolver(), FileUtils.LM_DEVICE_ID);
            }
        } catch (Exception ignore) {
            LMLogger.debugExceptionError(ignore);
        }
        if (!TextUtils.isEmpty(device_id) && !PrefHelper.NO_STRING_VALUE.equals(device_id)) {
            return device_id;
        }
        return "";
    }

    @Override
    public String getApplicationName() {
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            if (applicationInfo != null)
                return (String) mContext.getPackageManager().getApplicationLabel(applicationInfo);
            else
                return "";
        } catch (Exception ignored) {
        }
        return "";
    }

    /**
     * 根据Android的版本判断获取到的SSID是否有双引号
     *
     * @param ssid SSID
     * @return ssid
     */
    private String removeDoubleQuotationMarks(String ssid) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
        }
        return ssid;
    }

    @Override
    public String getBrowserIdentityId() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                ClipboardManager cbm = (ClipboardManager) mContext.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                if (cbm != null && cbm.hasPrimaryClip()) {
                    if (cbm.getPrimaryClip().getItemCount() > 0) {
                        CharSequence temp = cbm.getPrimaryClip().getItemAt(0).getText();
                        LMLogger.debug("剪切板数据== " + temp);
                        if (TextUtils.isEmpty(temp)) {
                            return "";
                        }
                        // 创建 Pattern 对象
                        Pattern r = Pattern.compile(LMConstant.PATTERN_TRANSACTION_TYPE);
                        // 现在创建 matcher 对象
                        Matcher m = r.matcher(temp);
                        if (m.find() && m.groupCount() > 0) {
                            return m.group(1);
                        }
                    }
                }
            }
        } catch (Exception ignore) {
            LMLogger.debugExceptionError(ignore);
        }
        return "";
    }

    @Override
    public void obtainOAIDAsync() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Class mdidSdkHelper = Class.forName("com.bun.miitmdid.core.MdidSdkHelper");
                    LMLogger.debug("MdidSdkHelper is exist");

                    Class sysParamters = Class.forName("com.bun.miitmdid.utils.sysParamters");
                    Method[] sysParamtersMethods = sysParamters.getDeclaredMethods();
                    Object sysParamtersObject = null;
                    for (Method sysParamtersMethod : sysParamtersMethods) {
                        if (sysParamtersMethod.getReturnType() == sysParamters
                                && Modifier.isPublic(sysParamtersMethod.getModifiers())
                                && Modifier.isStatic(sysParamtersMethod.getModifiers())) {
                            sysParamtersObject = sysParamtersMethod.invoke(null);
                            break;
                        }
                    }
                    if (sysParamtersObject != null) {
                        Field[] sysParamtersFields = sysParamters.getDeclaredFields();
                        for (Field sysParamtersField : sysParamtersFields) {
                            if ("sdk_version".equals(sysParamtersField.getName())) {
                                sysParamtersField.setAccessible(true);
                                String miitSdkVersion = (String) sysParamtersField.get(sysParamtersObject);
                                PrefHelper.getInstance(LinkedME.getInstance().getApplicationContext()).setMiitSdkVersion(miitSdkVersion);
                                LMLogger.debug("miit_sdk_version=" + miitSdkVersion);
                            }
                        }
                    }

                    Class iIdentifierListener = Class.forName("com.bun.miitmdid.core.IIdentifierListener");
                    IIdentifierHandler mHandler = new IIdentifierHandler();
                    Object mObj = Proxy.newProxyInstance(LinkedME.class.getClassLoader(), new Class[]{iIdentifierListener}, mHandler);
                    Method mMethod = mdidSdkHelper.getDeclaredMethod("InitSdk", Context.class, boolean.class, iIdentifierListener);
                    mMethod.invoke(null, LinkedME.getInstance().getApplicationContext(), true, mObj);

                } catch (Exception ignore) {
                    LMLogger.debugExceptionError(ignore);
                }
            }
        }).start();
    }

    /**
     * 数据解压缩
     */
    private static String decompress(String data) {
        try {
            if (TextUtils.isEmpty(data)) {
                return "";
            }
            byte[] dataBytes = data.getBytes("iso8859-1");
            ByteArrayInputStream bais = new ByteArrayInputStream(dataBytes);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 解压缩
            decompress(bais, baos);
            dataBytes = baos.toByteArray();
            baos.flush();
            baos.close();
            bais.close();
            return new String(dataBytes, "utf-8");
        } catch (Exception ignore) {
            LMLogger.debugExceptionError(ignore);
        }
        return "";
    }

    static final int BUFFER = 10240;

    /**
     * 数据解压缩
     */
    private static void decompress(InputStream is, OutputStream os)
            throws Exception {
        GZIPInputStream gis = new GZIPInputStream(is);
        int count;
        byte data[] = new byte[BUFFER];
        while ((count = gis.read(data, 0, BUFFER)) != -1) {
            os.write(data, 0, count);
        }

        gis.close();
    }
}
