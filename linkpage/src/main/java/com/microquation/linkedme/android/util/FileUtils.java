package com.microquation.linkedme.android.util;

import android.os.Environment;
import android.text.TextUtils;

import com.microquation.linkedme.android.log.LMLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 文件操作帮助类
 *
 * Created by LinkedME06 on 16/7/19.
 */
public class FileUtils {

    private final static String LMDEVICE = "LMDevice";
    private final static String LMDEVICE_HIDDEN = ".lm_device";
    public final static String LM_DEVICE_ID = "lm_device_id";
    private final static String LM_DEVICE_ID_HIDDEN = ".lm_device_id";
    private final static String LM_DEVICE_INFO_HIDDEN = ".lm_device_info";
    public final static String LM_DEVICE_INFO = "lm_device_info";
    public final static String LM_BROWSER_IDENTITY_ID = "lm_browser_identity_id";

    //类加载时就初始化
    private static final FileUtils instance = new FileUtils();

    private FileUtils() {
    }

    public static FileUtils getInstance() {
        return instance;
    }

    public boolean writeFile(final String content, String fileName) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            /* 创建一个读写锁 */
            ReadWriteLock rwlock = new ReentrantReadWriteLock();
            rwlock.writeLock().lock();
            FileOutputStream fileOutputStream = null;
            try {
                File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + LMDEVICE_HIDDEN);
                if (!fileDir.exists()) {
                    fileDir.mkdirs();
                }
                File file = new File(fileDir, fileName);
                fileOutputStream = new FileOutputStream(file);
                IOUtils.writeStr(fileOutputStream, content);
                return true;
            } catch (FileNotFoundException ex) {
                LMLogger.debug("应用程序未被授予读写文件权限,但不影响使用!");
            } catch (IOException e) {
            } finally {
                rwlock.writeLock().unlock();
                IOUtils.closeQuietly(fileOutputStream);
            }
        }
        return false;
    }

    /**
     * 从文件中读取device_id
     *
     * @return 设备信息
     */
    public String readFile(String fileName) {
        String text = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            /* 创建一个读写锁 */
            ReadWriteLock rwlock = new ReentrantReadWriteLock();
            rwlock.readLock().lock();
            FileInputStream fileInputStream = null;
            try {
                File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + LMDEVICE_HIDDEN);
                File file = new File(fileDir, fileName);
                if (file.exists()) {
                    fileInputStream = new FileInputStream(file);
                    text = IOUtils.readStr(fileInputStream);
                } else {
                    return "";
                }
            } catch (FileNotFoundException ex) {
                LMLogger.debug("应用程序未被授予读写文件权限,但不影响使用!");
            } catch (IOException e) {
            } finally {
                IOUtils.closeQuietly(fileInputStream);
                rwlock.readLock().unlock();
            }
        }
        return text;
    }


    /**
     * 将device_id写入到SD卡中,以保存设备的唯一标识
     *
     * @param content 写入的内容
     */
    public void writeDeviceId(final String content) {
        writeFile(content, LM_DEVICE_ID_HIDDEN);
    }

    /**
     * 从文件中读取device_id
     *
     * @return 设备信息
     */
    public String readDeviceId() {
        String device_id = "";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            /* 创建一个读写锁 */
            ReadWriteLock rwlock = new ReentrantReadWriteLock();
            rwlock.readLock().lock();
            FileInputStream fileInputStream = null;
            try {
                File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + LMDEVICE_HIDDEN);
                String fileName = LM_DEVICE_ID_HIDDEN;
                if (!fileDir.exists()) {
                    fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + LMDEVICE);
                    fileName = LM_DEVICE_ID;
                }
                File file = new File(fileDir, fileName);
                if (file.exists()) {
                    fileInputStream = new FileInputStream(file);
                    device_id = IOUtils.readStr(fileInputStream);
                } else {
                    return "";
                }
            } catch (FileNotFoundException ex) {
                LMLogger.debug("应用程序未被授予读写文件权限,但不影响使用!");
            } catch (IOException e) {
            } finally {
                IOUtils.closeQuietly(fileInputStream);
                rwlock.readLock().unlock();
            }
        }
        return device_id;
    }

    /**
     * 写入设备信息
     *
     * @param existDeviceInfo 已存设备信息
     * @param deviceInfo      设备信息
     */
    public void writeDeviceInfo(String existDeviceInfo, String deviceInfo) {
        deviceInfo = formatDeviceInfo(existDeviceInfo, deviceInfo);
        writeFile(deviceInfo, LM_DEVICE_INFO_HIDDEN);
    }

    /**
     * 获取完整的deviceInfo
     *
     * @param existDeviceInfo 已存储的deviceInfo
     * @param deviceInfo      新的deviceInfo
     * @return 完整的deviceInfo
     */
    private String formatDeviceInfo(String existDeviceInfo, String deviceInfo) {
        if (TextUtils.isEmpty(existDeviceInfo)) {
            return deviceInfo;
        }
        String[] existDeviceInfoArr = existDeviceInfo.split(",");
        String[] deviceInfoArr = deviceInfo.split(",");
        //如果数组长度不同，则使用最新的设备信息
        if (existDeviceInfoArr.length != deviceInfoArr.length) {
            return deviceInfo;
        }
        //比较每个值的不同，取长度最长的值为新值
        for (int i = 0; i < deviceInfoArr.length; i++) {
            String exist_info = existDeviceInfoArr[i];
            String info = deviceInfoArr[i];
            if (info.length() >= exist_info.length()) {
                deviceInfoArr[i] = info;
            } else {
                deviceInfoArr[i] = exist_info;
            }
        }
        return TextUtils.join(",", deviceInfoArr);
    }

    /**
     * 从文件中读取设备信息
     *
     * @return 设备信息
     */
    public String readDeviceInfo() {
        return readFile(LM_DEVICE_INFO_HIDDEN);
    }

    /**
     * 从文件中读取剪切板指纹信息
     *
     * @return 剪切板指纹信息
     */
    public String readBrowserIdentityId() {
        String browserIdentityId = readFile(LM_BROWSER_IDENTITY_ID);
        if (TextUtils.isEmpty(browserIdentityId)) {
            return "";
        }
        // 清空文件内容
        writeFile("", LM_BROWSER_IDENTITY_ID);
        return browserIdentityId;
    }

    /**
     * 写入Browser Identity ID信息
     *
     * @param browserIdentityId 剪切板指纹信息
     */
    public boolean writeBrowserIdentityId(String browserIdentityId) {
        return writeFile(browserIdentityId, LM_BROWSER_IDENTITY_ID);
    }

}
