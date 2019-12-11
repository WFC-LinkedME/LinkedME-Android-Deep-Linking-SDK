package com.microquation.linkedme.android.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import com.microquation.linkedme.android.log.LMLogger;
import com.microquation.linkedme.android.v4.ContextCompatLKMe;
import com.microquation.linkedme.android.v4.PermissionCheckerLKMe;

/**
 * 权限帮助类
 *
 * Created by LinkedME06 on 29/03/2017.
 */

public class PermissionUtils {

    /**
     * 检查应用是否有某个权限
     *
     * @param context    {@link Context}
     * @param permission {@link java.security.Permission}
     * @return true:已分配该权限 false:无该权限
     */
    public static boolean selfPermissionGranted(Context context, String permission) {
        // Android 6.0 以前，全部默认授权
        boolean result = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (getTargetSDKVersion(context) >= Build.VERSION_CODES.M) {
                    // targetSdkVersion >= 23, 使用Context#checkSelfPermission
                    result = ContextCompatLKMe.checkSelfPermission(context.getApplicationContext(), permission)
                            == PackageManager.PERMISSION_GRANTED;
                } else {
                    // targetSdkVersion < 23, 需要使用 PermissionChecker
                    result = PermissionCheckerLKMe.checkSelfPermission(context.getApplicationContext(), permission)
                            == PackageManager.PERMISSION_GRANTED;
                }
            } catch (Exception ignore) {
                LMLogger.info("请将支持库版本升级到23及以后");
            }
        }
        return result;
    }

    /**
     * 获取应用targetSDKVersion
     *
     * @param context {@link Context}
     * @return {@link Integer} success:targetSDKVersion fail:-1
     */
    public static int getTargetSDKVersion(Context context) {
        int targetSdkVersion = -1;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            targetSdkVersion = info.applicationInfo.targetSdkVersion;
        } catch (Exception ignore) {
            LMLogger.debugExceptionError(ignore);
        }
        return targetSdkVersion;
    }

}
