package com.microquation.linkedme.android.v4;

import android.content.Context;
import android.os.Process;

/**
 * Created by LinkedME06 on 16/9/6.
 */
public class ContextCompatLKMe {
    /**
     * Determine whether <em>you</em> have been granted a particular permission.
     *
     * @param permission The name of the permission being checked.
     *
     * @return {@link android.content.pm.PackageManager#PERMISSION_GRANTED} if you have the
     * permission, or {@link android.content.pm.PackageManager#PERMISSION_DENIED} if not.
     *
     * @see android.content.pm.PackageManager#checkPermission(String, String)
     */
    public static int checkSelfPermission(Context context, String permission) {
        if (permission == null) {
            throw new IllegalArgumentException("permission is null");
        }

        return context.checkPermission(permission, android.os.Process.myPid(), Process.myUid());
    }

}
