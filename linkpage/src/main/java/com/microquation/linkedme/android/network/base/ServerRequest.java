package com.microquation.linkedme.android.network.base;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.network.ServerRequestCreateUrl;
import com.microquation.linkedme.android.network.ServerRequestRegisterClose;
import com.microquation.linkedme.android.network.ServerRequestRegisterInstall;
import com.microquation.linkedme.android.network.ServerRequestRegisterOpen;
import com.microquation.linkedme.android.log.LMErrorCode;
import com.microquation.linkedme.android.referral.PrefHelper;
import com.microquation.linkedme.android.util.Defines;
import com.microquation.linkedme.android.util.LMConstant;
import com.microquation.linkedme.android.util.LMSystemObserver;
import com.microquation.linkedme.android.util.SystemObserver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;


/**
 * <p>定义了LinkedME服务请求框架的抽象类</p>
 */
public abstract class ServerRequest {

    private static final String POST_KEY = "REQ_POST";
    private static final String POST_PATH_KEY = "REQ_POST_PATH";
    protected String requestPath_;
    protected PrefHelper prefHelper;
    //数据格式错误会将该字段置为true，用于将无效的请求从队列中清除
    protected boolean constructError = false;
    long queueWaitTime = 0;
    boolean skipOnTimeOut = false;
    private JSONObject params;
    // 持久化，成功后移除，否则将请求持久化，在适当的时刻尝试
    private boolean persist = false;

    /**
     * <p>创建一个ServerRequest的实例。</p>
     *
     * @param context     Application context
     * @param requestPath 请求路径，参考 {@link Defines.RequestPath}
     */
    public ServerRequest(Context context, String requestPath) {
        requestPath_ = requestPath;
        prefHelper = PrefHelper.getInstance(context);
        params = new JSONObject();
    }

    /**
     * <p>创建一个ServerRequest的实例。</p>
     *
     * @param requestPath 请求路径，参考 {@link Defines.RequestPath}
     * @param post        一个包含要发送数据的{@link JSONObject}对象,该对象是当前请求的键值对参数
     * @param context     Application context
     */
    protected ServerRequest(String requestPath, JSONObject post, Context context) {
        requestPath_ = requestPath;
        params = post;
        prefHelper = PrefHelper.getInstance(context);
    }

    /**
     * <p>将一个以键值对存储的 {@link JSONObject} 对象转化成一个{@link ServerRequest}对象。</p>
     *
     * @param json    {@link JSONObject} 格式的请求数据对象
     * @param context Application context.
     */
    public static ServerRequest fromJSON(JSONObject json, Context context) {
        JSONObject post = null;
        String requestPath = "";
        try {
            if (json.has(POST_KEY)) {
                post = json.getJSONObject(POST_KEY);
            }
        } catch (JSONException e) {
        }

        try {
            if (json.has(POST_PATH_KEY)) {
                requestPath = json.getString(POST_PATH_KEY);
            }
        } catch (JSONException e) {
        }

        if (requestPath != null && requestPath.length() > 0) {
            return getExtendedServerRequest(requestPath, post, context);
        }
        return null;
    }

    /**
     * <p>工厂方法,用于创建特定的服务请求对象。根据请求的路径创建不同的请求</p>
     *
     * @param requestPath 请求路径，参考 {@link Defines.RequestPath}
     * @param post        {@link JSONObject} 格式的请求数据对象
     * @param context     Application context.
     * @return {@link ServerRequest} 对象
     */
    private static ServerRequest getExtendedServerRequest(String requestPath, JSONObject post, Context context) {
        ServerRequest extendedReq = null;

        if (requestPath.equalsIgnoreCase(Defines.RequestPath.GetURL.getPath())) {
            extendedReq = new ServerRequestCreateUrl(requestPath, post, context);
        } else if (requestPath.equalsIgnoreCase(Defines.RequestPath.RegisterClose.getPath())) {
            extendedReq = new ServerRequestRegisterClose(requestPath, post, context);
        } else if (requestPath.equalsIgnoreCase(Defines.RequestPath.RegisterInstall.getPath())) {
            extendedReq = new ServerRequestRegisterInstall(requestPath, post, context);
        } else if (requestPath.equalsIgnoreCase(Defines.RequestPath.RegisterOpen.getPath())) {
            extendedReq = new ServerRequestRegisterOpen(requestPath, post, context);
        }
        return extendedReq;
    }

    /**
     * <p>子类重写该方法，当请求发生错误的时候会调用该方法</p>
     *
     * @param context Application context.
     */
    public abstract boolean handleErrors(Context context);

    /**
     * <p>子类重写该方法，当请求成功后会调用该方法</p>
     *
     * @param response 该请求响应的 {@link ServerResponse} 对象
     * @param linkedME {@link LinkedME} 实例
     */
    public abstract void onRequestSucceeded(ServerResponse response, LinkedME linkedME);

    /**
     * <p>子类重写该方法，当请求失败后会调用该方法</p>
     *
     * @param statusCode 在{@link LMErrorCode}中定义的状态码
     * @param causeMsg   {@link String} 类型的失败原因
     */
    public abstract void handleFailure(int statusCode, String causeMsg);

    /**
     * 设置请求是 GET 还是 POST 请求
     *
     * @return true:GET请求 false:POST请求
     */
    public abstract boolean isGetRequest();

    /**
     * 当请求失败后是否需要重试，默认是不会重试，如果需要在失败后重试，请重写该方法
     *
     * @return true:失败后需要重试
     */
    public boolean shouldRetryOnFail() {
        return false;
    }

    /**
     * <p>获取请求的子路径，参考 {@link Defines.RequestPath} <p>
     *
     * @return 请求的路径
     */
    public final String getRequestPath() {
        return requestPath_;
    }

    /**
     * <p>获取请求的完整路径 </p>
     *
     * @return 请求的完整路径.
     */
    public String getRequestUrl() {
        return LMConstant.LKME_URL + requestPath_;
    }

    /**
     * <p>获取 {@link JSONObject} 格式的请求参数</p>
     *
     * @return {@link JSONObject} 格式的请求参数
     */
    public JSONObject getPost() {
        return params;
    }

    /**
     * <p>设置{@link JSONObject} 格式的请求参数</p>
     *
     * @param post {@link JSONObject} 格式的请求参数
     */
    public void setPost(JSONObject post) {
        params = post;
    }

    /**
     * <p> 指定请求是否需要添加Google Ads Id and LAT值，默认是不添加该参数，当需要的时候请重写该方法。 </p>
     *
     * @return true:需要添加Google Ads Id and LAT参数
     */
    public boolean isGAdsParamsRequired() {
        return false;
    }

    /**
     * 添加额外的请求参数
     */
    public JSONObject getPostWithInstrumentationValues(ConcurrentHashMap<String, String> instrumentationData) {
        return params;
    }

    /**
     * 获取{@link JSONObject}格式的GET请求参数
     */
    public JSONObject getGetParams() {
        return params;
    }

    /**
     * 添加GET请求参数
     *
     * @param paramKey   参数key
     * @param paramValue 参数值
     */
    protected void addGetParam(String paramKey, String paramValue) {
        try {
            params.put(paramKey, paramValue);
        } catch (JSONException ignore) {
        }
    }

    /**
     * 将参数及请求自路径封装成{@link JSONObject}格式
     */
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put(POST_KEY, params);
            json.put(POST_PATH_KEY, requestPath_);
        } catch (JSONException e) {
            return null;
        }
        return json;
    }

    /**
     * 添加Google ads参数，该方法只能在后台线程中调用
     *
     * @param sysObserver {@link SystemObserver} 实例.
     */
    public void updateGAdsParams(final LMSystemObserver sysObserver) {
        try {
            String advertisingId = sysObserver.getAdvertisingId();
            boolean latVal = sysObserver.getLATValue();
            if (!skipOnTimeOut && advertisingId != null && getPost() != null) {
                getPost().put(Defines.Jsonkey.GoogleAdvertisingID.getKey(), advertisingId);
            }
            if (!skipOnTimeOut && getPost() != null) {
                getPost().put(Defines.Jsonkey.LATVal.getKey(), latVal);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /*
     * 检查应用是否添加了internet权限
     *
     * @param context Application context.
     *
     * @return True 如果程序有联网权限.
     */
    protected boolean doesAppHasInternetPermission(Context context) {
        int result = PackageManager.PERMISSION_DENIED;
        try {
            result = context.checkCallingOrSelfPermission(Manifest.permission.INTERNET);
        } catch (Exception ignore) {

        }
        return result == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 当请求被添加到队列中时调用，设置请求等待的时间
     */
    public void onRequestQueued() {
        queueWaitTime = System.currentTimeMillis();
    }

    /**
     * 返回请求在对队列中等待的时间
     *
     * @return {@link Integer} 毫秒数
     */
    public long getQueueWaitTime() {
        long waitTime = 0;
        if (queueWaitTime > 0) {
            waitTime = System.currentTimeMillis() - queueWaitTime;
        }
        return waitTime;
    }

    public boolean isConstructError() {
        return constructError;
    }

    public void setPersist(boolean persist) {
        this.persist = persist;
    }

    public boolean isPersist() {
        return persist;
    }
}
