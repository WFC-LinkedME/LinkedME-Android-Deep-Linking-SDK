package com.microquation.linkedme.android.referral;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.microquation.linkedme.android.util.Defines;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

/**
 * 深度链接相关参数对象
 */
public class LMLinkData extends JSONObject {

    /**
     * 标签
     */
    private Collection<String> tags;

    /**
     * 别名
     */
    private String alias;

    private int type;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 功能
     */
    private String feature;

    /**
     * 阶段
     */
    private String stage;

    /**
     * 参数
     */
    private String params;

    /**
     * 深度链接有效期限
     */
    private int duration;

    public LMLinkData() {
        super();
    }

    public void putTags(Collection<String> tags) throws JSONException {
        if (tags != null) {
            this.tags = tags;
            this.put(Defines.LinkParam.Tags.getKey(), TextUtils.join(",", tags));
        }
    }

    public Collection<String> getTags() {
        return tags;
    }

    public void putAlias(String alias) throws JSONException {
        if (alias != null) {
            this.alias = alias;
            this.put(Defines.LinkParam.Alias.getKey(), alias);
        }
    }

    public String getAlias() {
        return alias;
    }

    public void putType(int type) throws JSONException {
        if (type != 0) {
            this.type = type;
            this.put(Defines.LinkParam.Type.getKey(), type);
        }
    }

    public int getType() {
        return type;
    }

    public void putDuration(int duration) throws JSONException {
        if (duration > 0) {
            this.duration = duration;
            this.put(Defines.LinkParam.Duration.getKey(), duration);
        }
    }

    public int getDuration() {
        return duration;
    }

    public void putChannel(String channel) throws JSONException {
        if (channel != null) {
            this.channel = channel;
            this.put(Defines.LinkParam.Channel.getKey(), channel);
        }
    }

    public String getChannel() {
        return channel;
    }

    public void putFeature(String feature) throws JSONException {
        if (feature != null) {
            this.feature = feature;
            this.put(Defines.LinkParam.Feature.getKey(), feature);
        }
    }

    public String getFeature() {
        return feature;
    }

    public void putStage(String stage) throws JSONException {
        if (stage != null) {
            this.stage = stage;
            this.put(Defines.LinkParam.Stage.getKey(), stage);
        }
    }

    public String getStage() {
        return stage;
    }

    public void putParams(String params) throws JSONException {
        this.params = params;
        this.put(Defines.LinkParam.Params.getKey(), params);
    }

    public String getParams() {
        return params;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LMLinkData other = (LMLinkData) obj;
        if (alias == null) {
            if (other.alias != null)
                return false;
        } else if (!alias.equals(other.alias))
            return false;
        if (channel == null) {
            if (other.channel != null)
                return false;
        } else if (!channel.equals(other.channel))
            return false;
        if (feature == null) {
            if (other.feature != null)
                return false;
        } else if (!feature.equals(other.feature))
            return false;
        if (params == null) {
            if (other.params != null)
                return false;
        } else if (!params.equals(other.params))
            return false;
        if (stage == null) {
            if (other.stage != null)
                return false;
        } else if (!stage.equals(other.stage))
            return false;
        if (type != other.type)
            return false;
        if (duration != other.duration)
            return false;

        if (tags == null) {
            if (other.tags != null)
                return false;
        } else if (!tags.toString().equals(other.tags.toString()))
            return false;

        return true;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public int hashCode() {
        int result = 1;
        int prime = 19;

        result = prime * result + this.type;
        result = prime * result
                + ((alias == null) ? 0 : alias.toLowerCase().hashCode());
        result = prime * result
                + ((channel == null) ? 0 : channel.toLowerCase().hashCode());
        result = prime * result
                + ((feature == null) ? 0 : feature.toLowerCase().hashCode());
        result = prime * result
                + ((stage == null) ? 0 : stage.toLowerCase().hashCode());
        result = prime * result
                + ((params == null) ? 0 : params.toLowerCase().hashCode());
        result = prime * result + this.duration;

        if (this.tags != null) {
            for (String tag : this.tags) {
                result = prime * result + tag.toLowerCase().hashCode();
            }
        }

        return result;
    }

}
