package com.microquation.linkedme.android.v4;

import android.app.AppOpsManager;
import android.content.Context;

/**
 * Created by LinkedME06 on 02/06/2017.
 */

class AppOpsManagerCompat23 {
    public static String permissionToOp(String permission) {
        return AppOpsManager.permissionToOp(permission);
    }

    public static int noteOp(Context context, String op, int uid, String packageName) {
        AppOpsManager appOpsManager = context.getSystemService(AppOpsManager.class);
        return appOpsManager.noteOp(op, uid, packageName);
    }

    public static int noteProxyOp(Context context, String op, String proxiedPackageName) {
        AppOpsManager appOpsManager = context.getSystemService(AppOpsManager.class);
        return appOpsManager.noteProxyOp(op, proxiedPackageName);
    }
}