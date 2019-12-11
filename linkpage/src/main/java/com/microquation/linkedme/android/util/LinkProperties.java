package com.microquation.linkedme.android.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.log.LMLogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * <p>
 * 这个类中定义了与link相关的一些参数 使用这个类来定义deep link 的具体属性，例如 channel，feature
 * </p>
 */
public class LinkProperties implements Parcelable {

    public static final String PARAMS_ANDROID_LINK = "$android_deeplink_path";
    public static final String PARAMS_IOS_LINK = "$ios_deeplink_key";


    private final ArrayList<String> tags;
    private String feature;
    private String alias;
    private String stage;
    private int matchDuration;
    private final Map<String, String> controlParams;
    private String channel;

    private String lkme_link;
    private boolean lkme_new_user;
    private String h5_url;

    /**
     * 新建一个实例{@link LinkProperties}
     */
    public LinkProperties() {
        tags = new ArrayList<>();
        feature = "Share";
        controlParams = new HashMap<>();
        alias = "";
        stage = "";
        matchDuration = 0;
        channel = "";
        lkme_link = "";
        lkme_new_user = false;
        h5_url = "";
    }

    /**
     * <p> 设置链接的别名 </p>
     *
     * @param alias Link别名  用来标记一个链接 <p> For example: http://lkme.cc/AUSTIN28. Should not exceed 128
     *              characters </p>
     * @return LinkProperties对象
     */
    public LinkProperties setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * <p>为deep link增加一个tag
     * </p>
     *
     * @param tag {@link String} 为deep link新增的tag
     * @return LinkProperties 对象
     */
    public LinkProperties addTag(String tag) {
        this.tags.add(tag);
        return this;
    }

    /**
     * <p>为deep link的行为添加控制参数
     *
     * @param key   自定义key值
     * @param value 自定义key对应的值
     * @return LinkProperties 对象
     */
    public LinkProperties addControlParameter(String key, String value) {
        this.controlParams.put(key, value);
        return this;
    }

    public LinkProperties setAndroidPathControlParameter(String value) {
        return addControlParameter(PARAMS_ANDROID_LINK, value);
    }

    public LinkProperties setIOSKeyControlParameter(String value) {
        return addControlParameter(PARAMS_IOS_LINK, value);
    }

    /**
     * <p> 为deep link 添加feature</p>
     *
     * @param feature 功能
     * @return LinkProperties 对象
     */
    public LinkProperties setFeature(String feature) {
        this.feature = feature;
        return this;
    }

    /**
     * <p> 设置deep link链接失效时间.</p>
     *
     * @param duration 链接失效时间
     * @return LinkProperties 对象
     */
    public LinkProperties setDuration(int duration) {
        this.matchDuration = duration;
        return this;
    }

    /**
     * <p>设置一个app或者用户的阶段</p>
     *
     * @param stage 阶段名称
     * @return LinkProperties 对象
     */
    public LinkProperties setStage(String stage) {
        this.stage = stage;
        return this;
    }

    /**
     * <p> 设置deep link的渠道 </p>
     *
     * @param channel 渠道名称
     * @return LinkProperties 对象
     */
    public LinkProperties setChannel(String channel) {
        this.channel = channel;
        return this;
    }

    /**
     * <p> 设置处理的深度链接 </p>
     *
     * @param lkme_link 深度链接
     * @return LinkProperties对象
     */
    public LinkProperties setLMLink(String lkme_link) {
        this.lkme_link = lkme_link;
        return this;
    }

    /**
     * <p> 设置deep link的渠道 </p>
     *
     * @param lkme_new_user 新用户状态
     * @return LinkProperties对象
     */
    public LinkProperties setLMNewUser(boolean lkme_new_user) {
        this.lkme_new_user = lkme_new_user;
        return this;
    }

    /**
     * <p> 设置分享的链接地址，用于iOS点击右上角lkme.cc时跳转的地址 </p>
     *
     * @param h5_url 分享的链接地址
     * @return LinkProperties对象
     */
    public LinkProperties setH5Url(String h5_url) {
        this.h5_url = h5_url;
        return this;
    }


    /**
     * 从 {@link LinkProperties}，得到这个deep link 的tag
     *
     * @return 标签
     */
    public ArrayList<String> getTags() {
        return tags;
    }

    /**
     * 从 {@link LinkProperties}，得到这个deep link 的控制参数
     *
     * @return 与该深度链接相关的控制参数
     */
    public HashMap<String, String> getControlParams() {
        HashMap<String, String> toHashMap = new HashMap<>();
        toHashMap.putAll(controlParams);
        return toHashMap;
    }

    /**
     * 从 {@link LinkProperties}，得到这个deep link 的控制参数
     *
     * @return 与该深度链接相关的控制参数，以ArrayMap的形式返回
     */
    public Map<String, String> getControlParamsArrayMap() {
        return controlParams;
    }

    /**
     * <p>
     * 得到deep link链接失效时间
     * </p>
     *
     * @return 链接失效时间
     */
    public int getMatchDuration() {
        return matchDuration;
    }

    /**
     * <p> 得到链接的别名 </p>
     *
     * @return 链接别名
     */
    public String getAlias() {
        return alias;
    }

    /**
     * <p> 得到链接的feature.</p>
     *
     * @return 功能
     */
    public String getFeature() {
        return feature;
    }

    /**
     * <p> 得到deep link标记的一个app或者用户的stage.</p>
     *
     * @return 阶段 process
     */
    public String getStage() {
        return stage;
    }

    /**
     * <p> 得到deep link 的渠道</p>
     *
     * @return 渠道
     */
    public String getChannel() {
        return channel;
    }

    /**
     * <p> 获取deep link的深度链接,例如 http://lkme.cc/AfC/IEGyekes7 </p>
     *
     * @return 字符串格式的深度链接
     */
    public String getLMLink() {
        return lkme_link;
    }

    /**
     * <p> 获取是否是新用户 </p>
     *
     * @return true:新用户 false:老用户
     */
    public boolean isLMNewUser() {
        return lkme_new_user;
    }

    /**
     * <p> 获取h5_url链接 </p>
     *
     * @return 链接
     */
    public String getH5Url() {
        return h5_url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator CREATOR = new Creator() {
        public LinkProperties createFromParcel(Parcel in) {
            return new LinkProperties(in);
        }

        public LinkProperties[] newArray(int size) {
            return new LinkProperties[size];
        }
    };

    /**
     * 根据链接参数创建一个{@link LinkProperties}对象
     *
     * @return 一个 {@link LinkProperties} 对象
     */
    public static LinkProperties getReferredLinkProperties() {
        LinkProperties linkProperties = null;
        LinkedME linkedMEInstance = LinkedME.getInstance();
        if (linkedMEInstance != null && linkedMEInstance.getLatestReferringParams() != null) {
            JSONObject latestParam = linkedMEInstance.getLatestReferringParams();
            LMLogger.info("开始解析用户数据：" + latestParam);
            try {
                if (latestParam.optBoolean(Defines.Jsonkey.Clicked_LINKEDME_Link.getKey(), false)) {
                    JSONObject params = latestParam.optJSONObject(Defines.Jsonkey.Params.getKey());
                    linkProperties = new LinkProperties();
                    JSONArray channels = params.optJSONArray(Defines.LinkParam.Channel.getKey());
                    if (channels != null && channels.length() > 0) {
                        linkProperties.setChannel(channels.optString(0));
                    }
                    JSONArray features = params.optJSONArray(Defines.LinkParam.Feature.getKey());
                    if (features != null && features.length() > 0) {
                        linkProperties.setFeature(features.optString(0));
                    }
                    JSONArray stages = params.optJSONArray(Defines.LinkParam.Stage.getKey());
                    if (stages != null && stages.length() > 0) {
                        linkProperties.setStage(stages.optString(0));
                    }

                    String link = params.optString(Defines.LinkParam.LKME_Link.getKey());
                    if (!TextUtils.isEmpty(link)) {
                        linkProperties.setLMLink(link);
                    }
                    linkProperties.setLMNewUser(params.optBoolean(Defines.LinkParam.LKME_NewUser.getKey()));
                    linkProperties.setH5Url(params.optString(Defines.LinkParam.LKME_H5Url.getKey()));

                    linkProperties.setDuration(params.optInt(Defines.LinkParam.Duration.getKey()));
                    JSONArray tags = params.optJSONArray(Defines.LinkParam.Tags.getKey());
                    if (tags != null && tags.length() > 0) {
                        for (int i = 0, length = tags.length(); i < length; i++) {
                            linkProperties.addTag(tags.optString(i));
                        }
                    }
                    JSONObject controls = params.optJSONObject(Defines.Jsonkey.LKME_CONTROLL.getKey());
                    if (controls != null) {
                        Iterator<String> keys = controls.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            linkProperties.addControlParameter(key, controls.optString(key));
                        }
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return linkProperties;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(feature);
        dest.writeString(alias);
        dest.writeString(stage);
        dest.writeString(channel);
        dest.writeInt(matchDuration);
        dest.writeString(lkme_link);
        dest.writeByte((byte) (lkme_new_user ? 1 : 0));
        dest.writeString(h5_url);
        dest.writeSerializable(tags);

        int controlParamSize = controlParams.size();
        dest.writeInt(controlParamSize);
        for (Map.Entry<String, String> stringStringEntry : controlParams.entrySet()) {
            dest.writeString(stringStringEntry.getKey());
            dest.writeString(stringStringEntry.getValue());
        }
    }

    private LinkProperties(Parcel in) {
        this();
        feature = in.readString();
        alias = in.readString();
        stage = in.readString();
        channel = in.readString();
        matchDuration = in.readInt();
        lkme_link = in.readString();
        lkme_new_user = in.readByte() != 0;
        h5_url = in.readString();
        @SuppressWarnings("unchecked")
        ArrayList<String> tagsTemp = (ArrayList<String>) in.readSerializable();
        tags.addAll(tagsTemp);
        for (int i = 0, controlPramSize = in.readInt(); i < controlPramSize; i++) {
            controlParams.put(in.readString(), in.readString());
        }
    }


    @Override
    public String toString() {
        return "LinkProperties{" +
                "tags=" + tags +
                ", feature='" + feature + '\'' +
                ", alias='" + alias + '\'' +
                ", stage='" + stage + '\'' +
                ", matchDuration=" + matchDuration +
                ", controlParams=" + controlParams +
                ", channel='" + channel + '\'' +
                ", link='" + lkme_link + '\'' +
                ", new_user='" + lkme_new_user + '\'' +
                ", h5_url='" + h5_url + '\'' +
                '}';
    }
}
