package com.microquation.linkedme.android.network;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.log.LMLogger;
import com.microquation.linkedme.android.network.base.ServerResponse;
import com.microquation.linkedme.android.referral.MessageType;
import com.microquation.linkedme.android.referral.PrefHelper;
import com.microquation.linkedme.android.util.Defines;
import com.microquation.linkedme.android.util.LMConstant;
import com.microquation.linkedme.android.util.LMSystemObserver;
import com.microquation.linkedme.android.util.SystemObserver;
import com.microquation.linkedme.android.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * * <p> The server request for registering an app open event to LinkedME API. Handles request
 * creation and execution.
 *
 * </p>
 */
public class ServerRequestRegisterOpen extends ServerRequestInitSession {

    /**
     * <p>Create an instance of {@link ServerRequestRegisterInstall} to notify LinkedME API on app
     * open event.</p>
     *
     * @param context     Current {@link Application} context data associated with new install
     *                    registration.
     * @param sysObserver {@link SystemObserver} instance.
     */
    public ServerRequestRegisterOpen(Context context, LMSystemObserver sysObserver) {
        super(context, Defines.RequestPath.RegisterOpen.getPath());
        JSONObject openPost = new JSONObject();
        try {
            openPost.putOpt(Defines.Jsonkey.LKME_DEVICE_ID.getKey(), LinkedME.getInstance().getDeviceId());
            openPost.putOpt(Defines.Jsonkey.LKME_DEVICE_TYPE.getKey(), SystemObserver.DEVICE_ID_TYPE_ANDROIDID);
            String imei = sysObserver.getIMEI();
            if (TextUtils.isEmpty(imei)) {
                imei = prefHelper.getIMEI();
            } else {
                //存储IMEI号
                prefHelper.setIMEI(imei);
            }
            openPost.putOpt(Defines.Jsonkey.LKME_DEVICE_IMEI.getKey(), imei);

            openPost.putOpt(Defines.Jsonkey.LKME_DEVICE_ANDROID_ID.getKey(), sysObserver.getAndroidId());
            openPost.putOpt(Defines.Jsonkey.LKME_DEVICE_SERIAL_NUMBER.getKey(), sysObserver.getSerialNumber());
            openPost.putOpt(Defines.Jsonkey.LKME_DEVICE_MAC.getKey(), sysObserver.getMAC());
            openPost.putOpt(Defines.Jsonkey.LKME_LOCAL_IP.getKey(), sysObserver.getIP());
            //deferred deep linking 需要改参数
            openPost.putOpt(Defines.Jsonkey.LKME_DEVICE_MODEL.getKey(), sysObserver.getPhoneModel());
            openPost.putOpt(Defines.Jsonkey.LKME_DF_ID.getKey(), prefHelper.getDeviceFingerPrintID());
            openPost.putOpt(Defines.Jsonkey.LKME_IDENTITY_ID.getKey(), prefHelper.getIdentityID());
            openPost.putOpt(Defines.Jsonkey.LKME_IS_REFERRABLE.getKey(), prefHelper.getIsReferrable());
            if (!TextUtils.equals(sysObserver.getAppVersion(), SystemObserver.BLANK)) {
                openPost.putOpt(Defines.Jsonkey.LKME_APP_VERSION.getKey(), sysObserver.getAppVersion());
            }
            openPost.putOpt(Defines.Jsonkey.LKME_APP_VERSION_CODE.getKey(), sysObserver.getAppVersionCode());
            openPost.putOpt(Defines.Jsonkey.LKME_OS_VERSION.getKey(), sysObserver.getOSVersionString());
            openPost.putOpt(Defines.Jsonkey.LKME_SDK_UPDATE.getKey(), sysObserver.getUpdateState(true));
            if (!TextUtils.equals(sysObserver.getOS(), SystemObserver.BLANK)) {
                openPost.putOpt(Defines.Jsonkey.LKME_OS.getKey(), sysObserver.getOS());
            }
            openPost.putOpt(Defines.Jsonkey.LKME_IS_DEBUG.getKey(), LMLogger.isDebug());
            if (!prefHelper.getExternalIntentUri().equals(PrefHelper.NO_STRING_VALUE)) {
                openPost.putOpt(Defines.Jsonkey.External_Intent_URI.getKey(), prefHelper.getExternalIntentUri());
            }
            openPost.putOpt(Defines.Jsonkey.LKME_DEVICE_UPDATE.getKey(), sysObserver.isDeviceInfoUpdate());
            String browserIdentityId = sysObserver.getBrowserIdentityId();
            LMLogger.debug("browserIdentityId从剪切板中获取" + browserIdentityId);
            openPost.putOpt(Defines.Jsonkey.LKME_BROWSER_MISC.getKey(), browserIdentityId);

            setPost(openPost);
        } catch (JSONException ex) {
            ex.printStackTrace();
            constructError = true;
        }

    }

    public ServerRequestRegisterOpen(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }

    @Override
    public void onRequestSucceeded(ServerResponse resp, LinkedME linkedME) {
        try {
            prefHelper.setLinkClickIdentifier(PrefHelper.NO_STRING_VALUE);
            JSONObject respObj = resp.getObject();
            prefHelper.setExternalIntentUri(PrefHelper.NO_STRING_VALUE);
            prefHelper.setExternalIntentExtra(PrefHelper.NO_STRING_VALUE);
            prefHelper.setAppLink(PrefHelper.NO_STRING_VALUE);
            prefHelper.setIdentity(respObj.optString(Defines.Jsonkey.LKME_IDENTITY.getKey()));
            if (respObj.optBoolean(Defines.Jsonkey.LKME_CLICKED_LINKEDME_LINK.getKey())) {
                if (TextUtils.equals(prefHelper.getInstallParams(), PrefHelper.NO_STRING_VALUE)) {
                    if (prefHelper.getIsReferrable() == 1) {
                        JSONObject params = new JSONObject(respObj, new String[]{
                                Defines.Jsonkey.LKME_IS_FIRST_SESSION.getKey(),
                                Defines.Jsonkey.LKME_CLICKED_LINKEDME_LINK.getKey(),
                                Defines.Jsonkey.Params.getKey()
                        });
                        prefHelper.setInstallParams(params.toString());
                    }
                }
            }
            //判断响应中是否包含is_first_session及clicked_linkedme_link两个字段
            if (respObj.has(Defines.Jsonkey.LKME_IS_FIRST_SESSION.getKey())
                    && respObj.has(Defines.Jsonkey.LKME_CLICKED_LINKEDME_LINK.getKey())) {
                JSONObject params = new JSONObject(respObj, new String[]{
                        Defines.Jsonkey.LKME_IS_FIRST_SESSION.getKey(),
                        Defines.Jsonkey.LKME_CLICKED_LINKEDME_LINK.getKey(),
                        Defines.Jsonkey.Params.getKey()
                });
                prefHelper.setSessionParams(params.toString());
                // 返回了参数，可以清空剪切板了
                Intent broadIntent = new Intent();
                broadIntent.setAction(LMConstant.BROAD_MAIN_ACTION);
                broadIntent.putExtra(LMConstant.BROAD_CODE, MessageType.MSG_GET_PARAMS);
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(LinkedME.getInstance().getApplicationContext());
                localBroadcastManager.sendBroadcast(broadIntent);
            } else {
                prefHelper.setSessionParams(PrefHelper.NO_STRING_VALUE);
            }
            if (LMLogger.isDebug() && respObj.optBoolean(Defines.Jsonkey.LKME_IS_TEST_URL.getKey())) {
                Intent broadIntent = new Intent();
                broadIntent.setAction(LMConstant.BROAD_MAIN_ACTION);
                broadIntent.putExtra(LMConstant.BROAD_CODE, MessageType.MSG_INTEGRATE_SUCCESS);
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(LinkedME.getInstance().getApplicationContext());
                localBroadcastManager.sendBroadcast(broadIntent);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    @Override
    public void handleFailure(int statusCode, String causeMsg) {
    }

    @Override
    public boolean handleErrors(Context context) {
        if (!super.doesAppHasInternetPermission(context)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isGetRequest() {
        return false;
    }

}
