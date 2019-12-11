package com.microquation.linkedme.android.referral;

import android.content.Context;

import com.microquation.linkedme.android.callback.LMLinkCreateListener;

import org.json.JSONObject;

/**
 * <p> 创建短链 </p>
 */
public class LMShortLinkBuilder extends LMUrlBuilder<LMShortLinkBuilder> {
    public LMShortLinkBuilder(Context context) {
        super(context);
    }

    /**
     * <p> 设置链接别名</p>
     *
     * @param alias 链接别名
     * @return LMShortLinkBuilder
     */
    public LMShortLinkBuilder setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * <p> 设置渠道</p>
     *
     * @param channel 渠道
     * @return LMShortLinkBuilder
     */
    public LMShortLinkBuilder setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    /**
     * <p> 设置深度链接有效期限</p>
     *
     * @param duration 深度链接有效期限
     * @return LMShortLinkBuilder
     */
    public LMShortLinkBuilder setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    /**
     * <p> 设置功能名称</p>
     *
     * @param feature 功能名称
     * @return LMShortLinkBuilder
     */
    public LMShortLinkBuilder setFeature(String feature) {
        this.feature = feature;
        return this;
    }

    /**
     * <p> 设置链接相关参数</p>
     *
     * @param parameters 链接相关参数
     * @return LMShortLinkBuilder
     */
    public LMShortLinkBuilder setParameters(JSONObject parameters) {
        this.params = parameters;
        return this;
    }

    /**
     * <p>设置阶段名称</p>
     *
     * @param stage 阶段名称
     * @return LMShortLinkBuilder
     */
    public LMShortLinkBuilder setStage(String stage) {
        this.stage = stage;
        return this;
    }

    /**
     * <p>设置链接类型</p>
     *
     * @param type 连接类型
     * @return LMShortLinkBuilder
     */
    public LMShortLinkBuilder setType(int type) {
        this.type = type;
        return this;
    }

    /**
     * <p>生成深度链接</p>
     */
    public void generateShortUrl(LMLinkCreateListener callback) {
        super.generateUrl(callback);
    }
}
