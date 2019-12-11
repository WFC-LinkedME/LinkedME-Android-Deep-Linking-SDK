package com.microquation.linkedme.android.network.base;

import android.content.Context;

import com.microquation.linkedme.android.callback.LMLinkCreateListener;
import com.microquation.linkedme.android.network.ServerRequestCreateUrl;
import com.microquation.linkedme.android.network.ServerRequestRegisterClose;
import com.microquation.linkedme.android.network.ServerRequestRegisterInstall;
import com.microquation.linkedme.android.network.ServerRequestRegisterOpen;
import com.microquation.linkedme.android.util.LMSystemObserver;

import java.util.Collection;

/**
 * ServerRequest构造工厂 Created by chenhao on 16-3-8.
 */
public final class ServerRequestFactory {


    public static ServerRequest createDefaultCreateUrl(Context context, String alias, int type, int duration, Collection<String> tags, String channel, String feature, String stage, String params, LMLinkCreateListener callback, boolean async) {
        return new ServerRequestCreateUrl(context, alias, type, duration, tags, channel, feature, stage, params, callback, async);
    }


    public static ServerRequest createDefaultRegisterClose(Context context) {
        return new ServerRequestRegisterClose(context);
    }

    public static ServerRequest createDefaultRegisterInstall(Context context, String installID, LMSystemObserver sysObserver) {
        return new ServerRequestRegisterInstall(context, sysObserver, installID);
    }

    public static ServerRequest createDefaultRegisterOpen(Context context, LMSystemObserver sysObserver) {
        return new ServerRequestRegisterOpen(context, sysObserver);
    }

}
