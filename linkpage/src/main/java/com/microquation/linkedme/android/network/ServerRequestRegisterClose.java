package com.microquation.linkedme.android.network;

import android.content.Context;
import android.util.Log;

import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.network.base.ServerRequest;
import com.microquation.linkedme.android.network.base.ServerResponse;
import com.microquation.linkedme.android.referral.PrefHelper;
import com.microquation.linkedme.android.util.Defines;
import com.microquation.linkedme.android.util.LMSystemObserver;
import com.microquation.linkedme.android.util.SystemObserver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * * <p> 关闭session请求
 * </p>
 */
public class ServerRequestRegisterClose extends ServerRequest {

    public ServerRequestRegisterClose(Context context) {
        super(context, Defines.RequestPath.RegisterClose.getPath());
        JSONObject closePost = new JSONObject();
        try {
            closePost.putOpt(Defines.Jsonkey.LKME_DEVICE_ID.getKey(), LinkedME.getInstance().getDeviceId());
            closePost.putOpt(Defines.Jsonkey.LKME_DF_ID.getKey(), prefHelper.getDeviceFingerPrintID());
            closePost.putOpt(Defines.Jsonkey.LKME_IDENTITY_ID.getKey(), prefHelper.getIdentityID());
            closePost.putOpt(Defines.Jsonkey.LKME_SESSION_ID.getKey(), prefHelper.getSessionID());
            setPost(closePost);
        } catch (JSONException ex) {
            ex.printStackTrace();
            constructError = true;
        }
    }

    public ServerRequestRegisterClose(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }

    @Override
    public boolean handleErrors(Context context) {
        clearSession();
        if (!super.doesAppHasInternetPermission(context)) {
            Log.i(LinkedME.TAG, "无联网权限，请添加联网权限！");
            return true;
        }
        return false;
    }

    @Override
    public void onRequestSucceeded(ServerResponse resp, LinkedME linkedME) {
        clearSession();
        String device_info = "";
        //格式为：device_id,imei,android_id,serial_number,mac,imsi
        LMSystemObserver systemObserver = SystemObserver.getInstance(LinkedME.getInstance().getApplicationContext());
        device_info += systemObserver.getDeviceId() + ",";
        device_info += systemObserver.getIMEI() + ",";
        device_info += systemObserver.getAndroidId() + ",";
        device_info += systemObserver.getSerialNumber() + ",";
        device_info += systemObserver.getMAC() + ",";
        device_info += systemObserver.getIMSI();
        SystemObserver.getInstance(LinkedME.getInstance().getApplicationContext()).setDeviceInfo(device_info);
    }

    @Override
    public void handleFailure(int statusCode, String causeMsg) {
        clearSession();
    }

    @Override
    public boolean isGetRequest() {
        return false;
    }

    public void clearSession() {
        // close的时候既要清空sessionParams也要清空sessionId
        prefHelper.setSessionParams(PrefHelper.NO_STRING_VALUE);
        prefHelper.setSessionID(PrefHelper.NO_STRING_VALUE);
    }
}
