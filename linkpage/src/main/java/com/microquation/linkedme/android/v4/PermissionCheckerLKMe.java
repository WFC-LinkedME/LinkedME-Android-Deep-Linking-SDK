package com.microquation.linkedme.android.v4;

import android.content.Context;
import android.content.pm.PackageManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * V4支持库
 *
 * Created by LinkedME06 on 02/06/2017.
 */

public final class PermissionCheckerLKMe {
    /**
     * Permission result: The permission is granted.
     */
    public static final int PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED;

    /**
     * Permission result: The permission is denied.
     */
    public static final int PERMISSION_DENIED = PackageManager.PERMISSION_DENIED;

    /**
     * Permission result: The permission is denied because the app op is not allowed.
     */
    public static final int PERMISSION_DENIED_APP_OP = PackageManager.PERMISSION_DENIED - 1;

    @Retention(RetentionPolicy.SOURCE)
    public @interface PermissionResult {
    }

    private PermissionCheckerLKMe() {
        /* do nothing */
    }

    /**
     * Checks whether a given package in a UID and PID has a given permission and whether the app op
     * that corresponds to this permission is allowed.
     *
     * @param context     Context for accessing resources.
     * @param permission  The permission to check.
     * @param pid         The process id for which to check.
     * @param uid         The uid for which to check.
     * @param packageName The package name for which to check. If null the the first package for the
     *                    calling UID will be used.
     * @return The permission check result which is either {@link #PERMISSION_GRANTED} or {@link
     * #PERMISSION_DENIED} or {@link #PERMISSION_DENIED_APP_OP}.
     */
    public static int checkPermission(Context context, String permission,
                                      int pid, int uid, String packageName) {
        if (context.checkPermission(permission, pid, uid) == PackageManager.PERMISSION_DENIED) {
            return PERMISSION_DENIED;
        }

        String op = AppOpsManagerCompat.permissionToOp(permission);
        if (op == null) {
            return PERMISSION_GRANTED;
        }

        if (packageName == null) {
            String[] packageNames = context.getPackageManager().getPackagesForUid(uid);
            if (packageNames == null || packageNames.length <= 0) {
                return PERMISSION_DENIED;
            }
            packageName = packageNames[0];
        }

        if (AppOpsManagerCompat.noteProxyOp(context, op, packageName)
                != AppOpsManagerCompat.MODE_ALLOWED) {
            return PERMISSION_DENIED_APP_OP;
        }

        return PERMISSION_GRANTED;
    }

    /**
     * Checks whether your app has a given permission and whether the app op that corresponds to
     * this permission is allowed.
     *
     * @param context    Context for accessing resources.
     * @param permission The permission to check.
     * @return The permission check result which is either {@link #PERMISSION_GRANTED} or {@link
     * #PERMISSION_DENIED} or {@link #PERMISSION_DENIED_APP_OP}.
     */
    public static int checkSelfPermission(Context context,
                                          String permission) {
        return checkPermission(context, permission, android.os.Process.myPid(),
                android.os.Process.myUid(), context.getPackageName());
    }

//    /**
//     * Checks whether the IPC you are handling has a given permission and whether the app op that
//     * corresponds to this permission is allowed.
//     *
//     * @param context     Context for accessing resources.
//     * @param permission  The permission to check.
//     * @param packageName The package name making the IPC. If null the the first package for the
//     *                    calling UID will be used.
//     * @return The permission check result which is either {@link #PERMISSION_GRANTED} or {@link
//     * #PERMISSION_DENIED} or {@link #PERMISSION_DENIED_APP_OP}.
//     */
//    public static int checkCallingPermission(Context context,
//                                             String permission, String packageName) {
//        if (Binder.getCallingPid() == Process.myPid()) {
//            return PackageManager.PERMISSION_DENIED;
//        }
//        return checkPermission(context, permission, Binder.getCallingPid(),
//                Binder.getCallingUid(), packageName);
//    }
//
//    /**
//     * Checks whether the IPC you are handling or your app has a given permission and whether the
//     * app op that corresponds to this permission is allowed.
//     *
//     * @param context    Context for accessing resources.
//     * @param permission The permission to check.
//     * @return The permission check result which is either {@link #PERMISSION_GRANTED} or {@link
//     * #PERMISSION_DENIED} or {@link #PERMISSION_DENIED_APP_OP}.
//     */
//    public static int checkCallingOrSelfPermission(Context context,
//                                                   String permission) {
//        String packageName = (Binder.getCallingPid() == Process.myPid())
//                ? context.getPackageName() : null;
//        return checkPermission(context, permission, Binder.getCallingPid(),
//                Binder.getCallingUid(), packageName);
//    }
}
