package com.microquation.linkedme.android.referral;

import android.text.TextUtils;

import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.log.LMLogger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class IIdentifierHandler implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        try {
            if (args.length > 1) {
                Object idSupplier = args[1];
                if (idSupplier != null) {
                    PrefHelper prefHelper = PrefHelper.getInstance(LinkedME.getInstance().getApplicationContext());
                    Method isSupportedMethod = idSupplier.getClass().getDeclaredMethod("isSupported");
                    boolean isSupported = (boolean) isSupportedMethod.invoke(idSupplier);
                    prefHelper.setIsSupport(isSupported);
                    LMLogger.debug("isSupported=" + isSupported);
                    if (isSupported) {
                        Method aaidMethod = idSupplier.getClass().getDeclaredMethod("getAAID");
                        String aaid = (String) aaidMethod.invoke(idSupplier);
                        if (!TextUtils.isEmpty(aaid)) {
                            prefHelper.setAAID(aaid);
                        }
                        LMLogger.debug("aaid=" + aaid);

                        Method oaidMethod = idSupplier.getClass().getDeclaredMethod("getOAID");
                        String oaid = (String) oaidMethod.invoke(idSupplier);
                        if (!TextUtils.isEmpty(oaid)) {
                            prefHelper.setOAID(oaid);
                        }
                        LMLogger.debug("oaid=" + oaid);

                        Method udidMethod = idSupplier.getClass().getDeclaredMethod("getUDID");
                        String udid = (String) udidMethod.invoke(idSupplier);
                        if (!TextUtils.isEmpty(udid)) {
                            prefHelper.setUDID(udid);
                        }
                        LMLogger.debug("udid=" + udid);

                        Method vaidMethod = idSupplier.getClass().getDeclaredMethod("getVAID");
                        String vaid = (String) vaidMethod.invoke(idSupplier);
                        if (!TextUtils.isEmpty(vaid)) {
                            prefHelper.setVAID(vaid);
                        }
                        LMLogger.debug("vaid=" + vaid);

                    }
                }
            }
        } catch (Exception ignore) {
            LMLogger.debugExceptionError(ignore);
        }
        return null;
    }

}
