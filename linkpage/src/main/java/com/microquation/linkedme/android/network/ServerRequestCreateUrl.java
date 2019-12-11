package com.microquation.linkedme.android.network;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.callback.LMLinkCreateListener;
import com.microquation.linkedme.android.network.base.ServerRequest;
import com.microquation.linkedme.android.network.base.ServerResponse;
import com.microquation.linkedme.android.log.LMErrorCode;
import com.microquation.linkedme.android.referral.LMLinkData;
import com.microquation.linkedme.android.referral.PrefHelper;
import com.microquation.linkedme.android.util.Defines;
import com.microquation.linkedme.android.util.MD5Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * <p>
 * 创建深度链接
 * </p>
 */
public class ServerRequestCreateUrl extends ServerRequest implements LMCreateUrl {

    private LMLinkData linkPost;
    private boolean isAsync = true;
    private LMLinkCreateListener callback;
    /* Default long link base url*/
    private static final String DEF_BASE_URL = "https://lkme.cc/i/";

    /**
     * <p>创建深度链接请求构造方法</p>
     *
     * @param context  {@link Application} context
     * @param alias    别名
     * @param type     类型
     * @param duration 深度链接有效时长
     * @param channel  渠道号
     * @param feature  功能
     * @param stage    阶段
     * @param params   参数
     * @param callback 回调
     * @param async    同步创建或异步创建
     */
    public ServerRequestCreateUrl(Context context, final String alias, final int type, final int duration,
                                  final Collection<String> tags, final String channel, final String feature,
                                  final String stage, final String params,
                                  LMLinkCreateListener callback, boolean async) {

        super(context, Defines.RequestPath.GetURL.getPath());

        this.callback = callback;
        isAsync = async;
        linkPost = new LMLinkData();
        try {

            linkPost.putOpt(Defines.Jsonkey.LKME_DEVICE_ID.getKey(), LinkedME.getInstance().getDeviceId());
            linkPost.putOpt(Defines.Jsonkey.LKME_IDENTITY_ID.getKey(), prefHelper.getIdentityID());
            linkPost.putOpt(Defines.Jsonkey.LKME_DF_ID.getKey(), prefHelper.getDeviceFingerPrintID());
            linkPost.putOpt(Defines.Jsonkey.LKME_SESSION_ID.getKey(), prefHelper.getSessionID());
            linkPost.putTags(tags);
            linkPost.putAlias(alias);
            linkPost.putChannel(channel);
            linkPost.putFeature(feature);
            linkPost.putStage(stage);
            linkPost.putParams(params);
            //创建深度链接增加deeplink_md5参数,由客户端生成以减轻服务器端生成md5的压力
            //去除前缀
            String linkedme_key = LinkedME.getInstance().getAppKey().trim().replaceFirst("linkedme_live_", "").replaceFirst("linkedme_test_", "");
            String deeplink_md5_origin = TextUtils.join("&", createObjectArray(linkedme_key, tags, channel, feature, stage, params));
            String deeplink_md5 = MD5Utils.encrypt(deeplink_md5_origin);
            linkPost.put(Defines.Jsonkey.LKME_DEEPLINK_MD5.getKey(), deeplink_md5);
            setPost(linkPost);

        } catch (JSONException ex) {
            ex.printStackTrace();
            constructError = true;
        }

    }

    /**
     * <p>将传入的数据项创建成对象数组用于生成md5串</p>
     *
     * @param object 创建的数据项
     * @return 对象数据
     */
    private Object[] createObjectArray(Object... object) {
        ArrayList<Object> arrayList = new ArrayList<>();
        for (Object element : object) {
            if (element == null) {
                continue;
            }
            //判断是否是collection类型
            if (element instanceof Collection) {
                arrayList.add(TextUtils.join(",", (Iterable) element));
            } else {
                arrayList.add(element);
            }
        }
        return arrayList.toArray();
    }

    public ServerRequestCreateUrl(String requestPath, JSONObject post, Context context) {
        super(requestPath, post, context);
    }

    @Override
    public LMLinkData getLinkPost() {
        return linkPost;
    }

    @Override
    public boolean handleErrors(Context context) {
        if (!super.doesAppHasInternetPermission(context)) {
            if (callback != null) {
                callback.onLinkCreate(null, new LMErrorCode("创建深度链接失败！", LMErrorCode.ERR_NO_INTERNET_PERMISSION));
            }
            return true;
        }
        return !isAsync && !hasUser();
    }

    @Override
    public void onRequestSucceeded(ServerResponse resp, LinkedME linkedME) {
        try {
            final String url = resp.getObject().getString("url");
            if (callback != null) {
                callback.onLinkCreate(url, null);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 缓存中存在相同参数的深度链接调用该方法
     *
     * @param url 深度链接
     */
    @Override
    public void onUrlAvailable(String url) {
        if (callback != null) {
            callback.onLinkCreate(url, null);
        }
    }

    @Override
    public void handleFailure(int statusCode, String causeMsg) {
        if (callback != null) {
            String failedUrl = getLongUrl();
            callback.onLinkCreate(failedUrl, new LMErrorCode("创建深度链接失败！" + causeMsg, statusCode));
        }
    }

    @Override
    public String getLongUrl() {
        String longUrl;
        if (!prefHelper.getUserURL().equals(PrefHelper.NO_STRING_VALUE)) {
            longUrl = generateLongUrlWithParams(prefHelper.getUserURL());
        } else {
            longUrl = generateLongUrlWithParams(DEF_BASE_URL + LinkedME.getInstance().getAppKey());
        }
        return longUrl;
    }

    @Override
    public void handleDuplicateURLError() {
        if (callback != null) {
            callback.onLinkCreate(null, new LMErrorCode("创建深度链接失败！", LMErrorCode.ERR_LINKEDME_DUPLICATE_URL));
        }
    }

    private boolean hasUser() {
        return !prefHelper.getIdentityID().equals(PrefHelper.NO_STRING_VALUE);
    }

    @Override
    public boolean isGetRequest() {
        return false;
    }

    @Override
    public boolean isAsync() {
        return isAsync;
    }

    /**
     * 创建长深度链接
     */
    private String generateLongUrlWithParams(String baseUrl) {
        String longUrl = baseUrl + "?";
        Collection<String> tags = linkPost.getTags();
        if (tags != null) {
            for (String tag : tags) {
                if (tag != null && tag.length() > 0)
                    longUrl = longUrl + Defines.LinkParam.Tags + "=" + tag + "&";
            }
        }
        String alias = linkPost.getAlias();
        if (alias != null && alias.length() > 0) {
            longUrl = longUrl + Defines.LinkParam.Alias + "=" + alias + "&";
        }

        String channel = linkPost.getChannel();
        if (channel != null && channel.length() > 0) {
            longUrl = longUrl + Defines.LinkParam.Channel + "=" + channel + "&";
        }

        String feature = linkPost.getFeature();
        if (feature != null && feature.length() > 0) {
            longUrl = longUrl + Defines.LinkParam.Feature + "=" + feature + "&";
        }

        String stage = linkPost.getStage();
        if (stage != null && stage.length() > 0) {
            longUrl = longUrl + Defines.LinkParam.Stage + "=" + stage + "&";
        }

        long type = linkPost.getType();
        longUrl = longUrl + Defines.LinkParam.Type + "=" + type + "&";

        long duration = linkPost.getDuration();
        longUrl = longUrl + Defines.LinkParam.Duration + "=" + duration + "&";

        String params = linkPost.getParams();
        if (params != null && params.length() > 0) {
            byte[] data = params.getBytes();
            String base64Data = Base64.encodeToString(data, android.util.Base64.NO_WRAP);
            longUrl = longUrl + "source=Android&data=" + base64Data;
        }

        return longUrl;
    }
}
