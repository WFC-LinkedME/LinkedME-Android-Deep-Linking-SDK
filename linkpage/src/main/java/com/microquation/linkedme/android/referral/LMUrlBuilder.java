package com.microquation.linkedme.android.referral;

import android.content.Context;

import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.callback.LMLinkCreateListener;
import com.microquation.linkedme.android.network.base.ServerRequest;
import com.microquation.linkedme.android.network.base.ServerRequestFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 创建深度链接抽象类
 * </p>
 */
@SuppressWarnings("rawtypes")
public abstract class LMUrlBuilder<T extends LMUrlBuilder> {

    /* 深度链接参数 */
    protected JSONObject params;
    /* 渠道号 */
    protected String channel;
    /* 功能 */
    protected String feature;
    /* 阶段 */
    protected String stage;
    /* 别名 */
    protected String alias;
    /* 深度链接类型，只能使用一次或无限次使用 */
    protected int type = LinkedME.LINK_TYPE_UNLIMITED_USE;
    /* 深度链接有效期 */
    protected int duration = 0;
    /* 标签 */
    protected ArrayList<String> tags;
    /* Application context. */
    private final Context context;

    /**
     * <p>
     * 创建 {@link LMUrlBuilder} 实例
     * </p>
     *
     * @param context {@link android.app.Activity} context
     */
    protected LMUrlBuilder(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * <p> 添加单条深度链接标签</p>
     *
     * @param tag {@link String} 类型标签.
     * @return 泛型对象
     */
    @SuppressWarnings("unchecked")
    public T addTag(String tag) {
        if (this.tags == null) {
            tags = new ArrayList<>();
        }
        this.tags.add(tag);
        return (T) this;
    }

    /**
     * <p>添加多个标签</p>
     *
     * @param tags 标签列表
     * @return 泛型对象
     */
    @SuppressWarnings("unchecked")
    public T addTags(List<String> tags) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.addAll(tags);
        return (T) this;
    }

    /**
     * <p> 添加深度链接绑定的参数 </p>
     *
     * @param key   参数key
     * @param value 参数key对应的参数值
     * @return 泛型对象
     */
    @SuppressWarnings("unchecked")
    public T addParameters(String key, String value) {
        try {
            if (this.params == null) {
                this.params = new JSONObject();
            }
            this.params.put(key, value);
        } catch (JSONException ignore) {

        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addParameters(String key, JSONArray value) {
        try {
            if (this.params == null) {
                this.params = new JSONObject();
            }
            this.params.put(key, value);
        } catch (JSONException ignore) {

        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T addParameters(String key, Map<String, ?> value) {
        try {
            if (this.params == null) {
                this.params = new JSONObject();
            }
            this.params.putOpt(key, new JSONObject(value));
        } catch (JSONException ignore) {

        }
        return (T) this;
    }

    ///------------------------- Link Build methods---------------------------///

    protected String getUrl() {
        String shortUrl = null;
        ServerRequest req = ServerRequestFactory.createDefaultCreateUrl(context, alias, type, duration, tags,
                channel, feature, stage, LMUtil.formatAndStringifyLinkParam(params), null, false);
        shortUrl = LinkedME.getInstance().generateShortLinkInternal(req);
        return shortUrl;
    }

    protected void generateUrl(LMLinkCreateListener callback) {
        ServerRequest req = ServerRequestFactory.createDefaultCreateUrl(context, alias, type, duration, tags,
                channel, feature, stage, LMUtil.formatAndStringifyLinkParam(params), callback, true);
        LinkedME.getInstance().generateShortLinkInternal(req);
    }
}
