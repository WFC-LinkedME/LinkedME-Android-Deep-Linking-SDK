package com.microquation.linkedme.android.network;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.DisplayMetrics;

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
 * * <p> The server request for registering an app install to LinkedME API. Handles request creation
 * and execution. </p>
 */
public class ServerRequestRegisterInstall extends ServerRequestInitSession {

    /**
     * <p>Create an instance of {@link ServerRequestRegisterInstall} to notify LinkedME API on a
     * new install.</p>
     *
     * @param context     Current {@link Application} context data associated with new install
     *                    registration.
     * @param sysObserver {@link SystemObserver} instance.
     * @param installID   installation ID.                                   .
     */
    public ServerRequestRegisterInstall(Context context, LMSystemObserver sysObserver, String installID) {

        super(context, Defines.RequestPath.RegisterInstall.getPath());

        JSONObject installPost = new JSONObject();
        try {
            String imei = sysObserver.getIMEI();
            String imsi = sysObserver.getIMSI();
            String mac = sysObserver.getMAC();
            if (!installID.equals(PrefHelper.NO_STRING_VALUE)) {
                installPost.put(Defines.Jsonkey.LinkClickID.getKey(), installID);
            }
            installPost.putOpt(Defines.Jsonkey.LKME_DEVICE_ID.getKey(), LinkedME.getInstance().getDeviceId());
            installPost.putOpt(Defines.Jsonkey.LKME_DEVICE_NAME.getKey(), sysObserver.getDeviceName());
            installPost.putOpt(Defines.Jsonkey.LKME_DEVICE_TYPE.getKey(), SystemObserver.DEVICE_ID_TYPE_ANDROIDID);
            installPost.putOpt(Defines.Jsonkey.LKME_DEVICE_IMEI.getKey(), imei);
            if (TextUtils.isEmpty(imei)) {
                imei = prefHelper.getIMEI();
            } else {
                //存储IMEI号
                prefHelper.setIMEI(imei);
            }
            installPost.putOpt(Defines.Jsonkey.LKME_IMSI.getKey(), imsi);
            //存储IMSI号
            prefHelper.setIMSI(imsi);
            installPost.putOpt(Defines.Jsonkey.LKME_DEVICE_ANDROID_ID.getKey(), sysObserver.getAndroidId());
            installPost.putOpt(Defines.Jsonkey.LKME_DEVICE_SERIAL_NUMBER.getKey(), sysObserver.getSerialNumber());
            installPost.putOpt(Defines.Jsonkey.LKME_DEVICE_MAC.getKey(), mac);
            //存储mac地址
            prefHelper.setMac(mac);
            installPost.putOpt(Defines.Jsonkey.LKME_LOCAL_IP.getKey(), sysObserver.getIP());
            installPost.putOpt(Defines.Jsonkey.LKME_DEVICE_FINGERPRINT.getKey(), sysObserver.getFingerPrint());

            if (!TextUtils.equals(sysObserver.getPhoneBrand(), SystemObserver.BLANK)) {
                installPost.putOpt(Defines.Jsonkey.LKME_DEVICE_BRAND.getKey(), sysObserver.getPhoneBrand());
            }
            if (!TextUtils.equals(sysObserver.getPhoneModel(), SystemObserver.BLANK)) {
                installPost.putOpt(Defines.Jsonkey.LKME_DEVICE_MODEL.getKey(), sysObserver.getPhoneModel());
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                installPost.putOpt(Defines.Jsonkey.LKME_HAS_BLUETOOTH.getKey(), sysObserver.getBluetoothPresent());
            }
            installPost.putOpt(Defines.Jsonkey.LKME_HAS_NFC.getKey(), sysObserver.getNFCPresent());
            installPost.putOpt(Defines.Jsonkey.LKME_HAS_SIM.getKey(), sysObserver.getTelephonePresent());
            if (!TextUtils.equals(sysObserver.getOS(), SystemObserver.BLANK)) {
                installPost.putOpt(Defines.Jsonkey.LKME_OS.getKey(), sysObserver.getOS());
            }

            installPost.putOpt(Defines.Jsonkey.LKME_OS_VERSION_INT.getKey(), sysObserver.getOSVersion());

            installPost.putOpt(Defines.Jsonkey.LKME_OS_VERSION.getKey(), String.valueOf(sysObserver.getOSVersionString()));
            DisplayMetrics dMetrics = sysObserver.getScreenDisplay();
            installPost.putOpt(Defines.Jsonkey.LKME_SCREEN_DPI.getKey(), dMetrics.densityDpi);
            installPost.putOpt(Defines.Jsonkey.LKME_SCREEN_HEIGHT.getKey(), dMetrics.heightPixels);
            installPost.putOpt(Defines.Jsonkey.LKME_SCREEN_WIDTH.getKey(), dMetrics.widthPixels);
            installPost.putOpt(Defines.Jsonkey.LKME_IS_WIFI.getKey(), sysObserver.getWifiConnected());
            installPost.putOpt(Defines.Jsonkey.LKME_IS_REFERRABLE.getKey(), prefHelper.getIsReferrable());
            installPost.putOpt(Defines.Jsonkey.LKME_IS_DEBUG.getKey(), LMLogger.isDebug());
            installPost.putOpt(Defines.Jsonkey.LKME_GoogleAdvertisingID.getKey(), sysObserver.getAdvertisingId());
            if (!prefHelper.getExternalIntentUri().equals(PrefHelper.NO_STRING_VALUE)) {
                installPost.putOpt(Defines.Jsonkey.External_Intent_URI.getKey(), prefHelper.getExternalIntentUri());
            }
            if (!TextUtils.equals(sysObserver.getCarrier(), SystemObserver.BLANK)) {
                installPost.putOpt(Defines.Jsonkey.LKME_CARRIER.getKey(), sysObserver.getCarrier());
            }
            if (!TextUtils.equals(sysObserver.getAppVersion(), SystemObserver.BLANK)) {
                installPost.putOpt(Defines.Jsonkey.LKME_APP_VERSION.getKey(), sysObserver.getAppVersion());
            }
            installPost.putOpt(Defines.Jsonkey.LKME_APP_VERSION_CODE.getKey(), sysObserver.getAppVersionCode());
            installPost.putOpt(Defines.Jsonkey.LKME_SDK_UPDATE.getKey(), sysObserver.getUpdateState(true));
            String browserIdentityId = sysObserver.getBrowserIdentityId();
            LMLogger.debug("browserIdentityId从剪切板中获取" + browserIdentityId);
            installPost.putOpt(Defines.Jsonkey.LKME_BROWSER_MISC.getKey(), browserIdentityId);
            setPost(installPost);

        } catch (JSONException ex) {
            ex.printStackTrace();
            constructError = true;
        }

    }

    public ServerRequestRegisterInstall(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }

    @Override
    public void onRequestSucceeded(ServerResponse resp, LinkedME linkedME) {
        try {
            JSONObject respObj = resp.getObject();
            prefHelper.setUserURL(respObj.optString(Defines.Jsonkey.LKME_LINK.getKey()));
            prefHelper.setLinkClickIdentifier(PrefHelper.NO_STRING_VALUE);
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

            //存储device_id
            SystemObserver.getInstance(LinkedME.getInstance().getApplicationContext()).setDeviceId(respObj.optString(Defines.Jsonkey.LKME_DEVICE_ID.getKey()));
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
