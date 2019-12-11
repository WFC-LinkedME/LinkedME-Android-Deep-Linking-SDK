package com.microquation.linkedme.android.indexing;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.callback.LMLinkCreateListener;
import com.microquation.linkedme.android.referral.LMShortLinkBuilder;
import com.microquation.linkedme.android.util.Defines;
import com.microquation.linkedme.android.util.LinkProperties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * <p> 与应用相关的类,提供了对于分享,深度链接以及内容展示追踪的方法,用于分析分享的内容和深度链接 </p>
 */
public class LMUniversalObject implements Parcelable {
    /* 内容标识 */
    private String canonicalIdentifier;
    /* 内容对应的url */
    private String canonicalUrl;
    /* 内容标题 */
    private String title;
    /* 内容描述 */
    private String description;
    /* 与当前内容相关的图片 */
    private String imageUrl;
    /* 内容的元数据 */
    private final Map<String, String> metadata;
    /* 内容的类型 */
    private String type;
    /* 内容的索引 */
    private CONTENT_INDEX_MODE indexMode;
    /* 与该内容相关的关键字 */
    private final ArrayList<String> keywords;
    /* 失效时间,毫秒数*/
    private long expirationInMilliSec;


    /**
     * 定义内容索引模式 PUBLIC 或者 PRIVATE
     */
    public enum CONTENT_INDEX_MODE {
        PUBLIC, PRIVATE
    }


    /**
     * <p> 创建LMUniversalObject对象. </p>
     */
    public LMUniversalObject() {
        metadata = new HashMap<>();
        keywords = new ArrayList<>();
        canonicalIdentifier = "";
        canonicalUrl = "";
        title = "";
        description = "";
        type = "";
        indexMode = CONTENT_INDEX_MODE.PUBLIC;
        expirationInMilliSec = 0L;
    }


    public LMUniversalObject setCanonicalIdentifier(String canonicalIdentifier) {
        this.canonicalIdentifier = canonicalIdentifier;
        return this;
    }


    public LMUniversalObject setCanonicalUrl(String canonicalUrl) {
        this.canonicalUrl = canonicalUrl;
        return this;
    }

    /**
     * <p> 设置refer内容的标题 </p>
     *
     * @param title 标题
     * @return LMUniversalObject 对象
     */
    public LMUniversalObject setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * <p> 设置refer内容的描述 <p>
     *
     * @param description 描述
     * @return LMUniversalObject 对象
     */
    public LMUniversalObject setContentDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * <p> 设置与内容相关的图片url <p>
     *
     * @param imageUrl 设置与内容相关的图片url
     * @return LMUniversalObject 对象
     */
    public LMUniversalObject setContentImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        return this;
    }

    /**
     * <p> 添加与内容相关的metadata。这些key值通过deep link的形式传递给另一个用户 </p>
     *
     * @param metadata metadata 键值对
     * @return LMUniversalObject 对象
     */
    @SuppressWarnings("unused")
    public LMUniversalObject addContentMetadata(Map<String, String> metadata) {
        this.metadata.putAll(metadata);
        return this;
    }

    /**
     * <p> 添加与内容相关的metadata。这些key值通过deep link的形式传递给另一个用户 </p>
     *
     * @param key   metadata 键
     * @param value metadata 键对应的值
     * @return LMUniversalObject 对象
     */
    public LMUniversalObject addContentMetadata(String key, String value) {
        this.metadata.put(key, value);
        return this;
    }

    /**
     * <p> 设置内容类型 </p>
     *
     * @param type 内容类型
     * @return LMUniversalObject 对象
     */
    public LMUniversalObject setContentType(String type) {
        this.type = type;
        return this;
    }

    /**
     * <p> 在refer的时候设置内容的索引模式 </p>
     *
     * @param indexMode {@link LMUniversalObject.CONTENT_INDEX_MODE} 索引模式
     * @return LMUniversalObject 对象
     */
    public LMUniversalObject setContentIndexingMode(CONTENT_INDEX_MODE indexMode) {
        this.indexMode = indexMode;
        return this;
    }

    /**
     * <p> 为refer的内容添加关键字 </p>
     *
     * @param keywords 与内容相关的关键字
     * @return LMUniversalObject 对象
     */
    @SuppressWarnings("unused")
    public LMUniversalObject addKeyWords(ArrayList<String> keywords) {
        this.keywords.addAll(keywords);
        return this;
    }

    /**
     * <p> 为refer的内容添加关键字 </p>
     *
     * @param keyword 关键字
     * @return LMUniversalObject 对象
     */
    public LMUniversalObject addKeyWord(String keyword) {
        this.keywords.add(keyword);
        return this;
    }

    /**
     * <p> 设置refer导出的时刻 </p>
     *
     * @param expirationDate 过期时间
     * @return LMUniversalObject 对象
     */
    public LMUniversalObject setContentExpiration(Date expirationDate) {
        this.expirationInMilliSec = expirationDate.getTime();
        return this;
    }

    /**
     * @return refer的内容是否内被公共索引 true:是公共索引
     */
    public boolean isPublicallyIndexable() {
        return indexMode == CONTENT_INDEX_MODE.PUBLIC;
    }

    /**
     * @return 得到refer内容的metadata
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @return 得到链接的refer过期毫秒数
     */
    public long getExpirationTime() {
        return expirationInMilliSec;
    }

    /**
     * @return 得到 LinkedMeUniversalObject对象的规范标示
     */
    public String getCanonicalIdentifier() {
        return canonicalIdentifier;
    }

    /**
     * @return 得到LinkedMeUniversalObject的规范url
     */
    public String getCanonicalUrl() {
        return canonicalUrl;
    }

    /**
     * @return 得到refer内容的描述 object
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return 得到refer内容的图片url
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * @return 得到refer内容的标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return 得到refer内容的Type
     */
    public String getType() {
        return type;
    }


    /**
     * @return 得到 {@link LMUniversalObject} 的关键字的jsonArray格式
     */
    public JSONArray getKeywordsJsonArray() {
        JSONArray keywordArray = new JSONArray();
        for (String keyword : keywords) {
            keywordArray.put(keyword);
        }
        return keywordArray;
    }

    /**
     * @return 得到 {@link LMUniversalObject}的关键字
     */
    @SuppressWarnings("unused")
    public ArrayList<String> getKeywords() {
        return keywords;
    }

    /**
     * 创建深度链接
     *
     * @param context        {@link android.app.Activity} context
     * @param linkProperties 深度链接相关参数
     * @param callback       创建深度链接回调
     */
    public void generateShortUrl(Context context, LinkProperties linkProperties, LMLinkCreateListener callback) {
        getLinkBuilder(context, linkProperties).generateShortUrl(callback);
    }

    private LMShortLinkBuilder getLinkBuilder(Context context, LinkProperties linkProperties) {
        LMShortLinkBuilder shortLinkBuilder = new LMShortLinkBuilder(context);
        if (linkProperties.getTags() != null) {
            shortLinkBuilder.addTags(linkProperties.getTags());
        }
        if (linkProperties.getFeature() != null) {
            shortLinkBuilder.setFeature(linkProperties.getFeature());
        }
        if (linkProperties.getAlias() != null) {
            shortLinkBuilder.setAlias(linkProperties.getAlias());
        }
        if (linkProperties.getChannel() != null) {
            shortLinkBuilder.setChannel(linkProperties.getChannel());
        }
        if (linkProperties.getStage() != null) {
            shortLinkBuilder.setStage(linkProperties.getStage());
        }
        if (linkProperties.getMatchDuration() > 0) {
            shortLinkBuilder.setDuration(linkProperties.getMatchDuration());
        }

        shortLinkBuilder.addParameters(Defines.Jsonkey.ContentTitle.getKey(), title);
        shortLinkBuilder.addParameters(Defines.Jsonkey.CanonicalIdentifier.getKey(), canonicalIdentifier);
        shortLinkBuilder.addParameters(Defines.Jsonkey.CanonicalUrl.getKey(), canonicalUrl);
        shortLinkBuilder.addParameters(Defines.Jsonkey.ContentKeyWords.getKey(), getKeywordsJsonArray());
        shortLinkBuilder.addParameters(Defines.Jsonkey.ContentDesc.getKey(), description);
        shortLinkBuilder.addParameters(Defines.Jsonkey.ContentImgUrl.getKey(), imageUrl);
        shortLinkBuilder.addParameters(Defines.Jsonkey.ContentType.getKey(), type);
        shortLinkBuilder.addParameters(Defines.Jsonkey.ContentExpiryTime.getKey(), String.valueOf(expirationInMilliSec));
        shortLinkBuilder.addParameters(Defines.Jsonkey.LKME_METADATA.getKey(), metadata);
        shortLinkBuilder.addParameters(Defines.Jsonkey.LKME_CONTROLL.getKey(), linkProperties.getControlParams());
        return shortLinkBuilder;
    }

    /**
     * 得到与最近的deep link管理的{@link LMUniversalObject}。这个函数会取回用来创建deep link的对象
     * 这个函数只能在LinkedME的session初始化之后调用
     */
    public static LMUniversalObject getReferredLinkedMeUniversalObject() {
        LMUniversalObject LKMEUniversalObject = null;
        LinkedME linkedMEInstance = LinkedME.getInstance();
        try {
            if (linkedMEInstance != null && linkedMEInstance.getLatestReferringParams() != null) {
                // Check if link clicked. Unless deepvlink debug enabled return null if there is no link click
                if (linkedMEInstance.getLatestReferringParams().optBoolean(Defines.Jsonkey.Clicked_LINKEDME_Link.getKey(), false)) {
                    LKMEUniversalObject = createInstance(linkedMEInstance.getLatestReferringParams());
                }
                // If debug params are set then send BUO object even if link click is false
                else if (linkedMEInstance.getDeeplinkDebugParams() != null && linkedMEInstance.getDeeplinkDebugParams().length() > 0) {
                    LKMEUniversalObject = createInstance(linkedMEInstance.getLatestReferringParams());
                }
            }
        } catch (Exception ignore) {
        }
        return LKMEUniversalObject;
    }

    /**
     * 由{@link JSONObject}创建一个新的LinkedMeUniversalObject
     */
    public static LMUniversalObject createInstance(JSONObject jsonObject) {
        JSONObject params = jsonObject.optJSONObject(Defines.Jsonkey.Params.getKey());
        LMUniversalObject LKMEUniversalObject = null;
        try {
            LKMEUniversalObject = new LMUniversalObject();
            LKMEUniversalObject.title = params.optString(Defines.Jsonkey.ContentTitle.getKey());
            LKMEUniversalObject.canonicalIdentifier = params.optString(Defines.Jsonkey.CanonicalIdentifier.getKey());
            LKMEUniversalObject.canonicalUrl = params.optString(Defines.Jsonkey.CanonicalUrl.getKey());
            JSONArray keywords = params.optJSONArray(Defines.Jsonkey.ContentKeyWords.getKey());
            if (keywords != null) {
                for (int i = 0, length = keywords.length(); i < length; i++) {
                    LKMEUniversalObject.addKeyWord(keywords.optString(i));
                }
            }
            LKMEUniversalObject.description = params.optString(Defines.Jsonkey.ContentDesc.getKey());
            LKMEUniversalObject.imageUrl = params.optString(Defines.Jsonkey.ContentImgUrl.getKey());
            LKMEUniversalObject.type = params.optString(Defines.Jsonkey.ContentType.getKey());
            LKMEUniversalObject.expirationInMilliSec = params.optLong(Defines.Jsonkey.ContentExpiryTime.getKey());
            JSONObject metadata = params.optJSONObject(Defines.Jsonkey.LKME_METADATA.getKey());
            Iterator<String> keys = metadata.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                LKMEUniversalObject.addContentMetadata(key, metadata.optString(key));
            }
        } catch (Exception ignore) {
        }
        return LKMEUniversalObject;
    }

    //-------------Object flattening methods--------------------//

    /**
     * 转换为json
     */
    public JSONObject convertToJson() {
        JSONObject buoJsonModel = new JSONObject();
        try {
            buoJsonModel.put(Defines.Jsonkey.ContentTitle.getKey(), title);
            buoJsonModel.put(Defines.Jsonkey.CanonicalIdentifier.getKey(), canonicalIdentifier);
            buoJsonModel.put(Defines.Jsonkey.CanonicalUrl.getKey(), canonicalUrl);
            JSONArray keyWordJsonArray = new JSONArray();
            for (String keyword : keywords) {
                keyWordJsonArray.put(keyword);
            }
            buoJsonModel.put(Defines.Jsonkey.ContentKeyWords.getKey(), keyWordJsonArray);
            buoJsonModel.put(Defines.Jsonkey.ContentDesc.getKey(), description);
            buoJsonModel.put(Defines.Jsonkey.ContentImgUrl.getKey(), imageUrl);
            buoJsonModel.put(Defines.Jsonkey.ContentType.getKey(), type);
            buoJsonModel.put(Defines.Jsonkey.ContentExpiryTime.getKey(), expirationInMilliSec);

            Set<String> metadataKeys = metadata.keySet();
            for (String metadataKey : metadataKeys) {
                buoJsonModel.put(metadataKey, metadata.get(metadataKey));
            }

        } catch (JSONException ignore) {
        }
        return buoJsonModel;
    }

    //---------------------Marshaling and Unmarshaling----------//
    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator CREATOR = new Creator() {
        public LMUniversalObject createFromParcel(Parcel in) {
            return new LMUniversalObject(in);
        }

        public LMUniversalObject[] newArray(int size) {
            return new LMUniversalObject[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(canonicalIdentifier);
        dest.writeString(canonicalUrl);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeString(type);
        dest.writeLong(expirationInMilliSec);
        dest.writeInt(indexMode.ordinal());
        dest.writeSerializable(keywords);

        int metaDataSize = metadata.size();
        dest.writeInt(metaDataSize);
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            dest.writeString(entry.getKey());
            dest.writeString(entry.getValue());
        }
    }

    private LMUniversalObject(Parcel in) {
        this();
        canonicalIdentifier = in.readString();
        canonicalUrl = in.readString();
        title = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        type = in.readString();
        expirationInMilliSec = in.readLong();
        indexMode = CONTENT_INDEX_MODE.values()[in.readInt()];
        @SuppressWarnings("unchecked")
        ArrayList<String> keywordsTemp = (ArrayList<String>) in.readSerializable();
        keywords.addAll(keywordsTemp);
        int metadataSize = in.readInt();
        for (int i = 0; i < metadataSize; i++) {
            metadata.put(in.readString(), in.readString());
        }
    }

    @Override
    public String toString() {
        return "LMUniversalObject{" +
                "canonicalIdentifier='" + canonicalIdentifier + '\'' +
                ", canonicalUrl='" + canonicalUrl + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", metadata=" + metadata +
                ", type='" + type + '\'' +
                ", indexMode=" + indexMode +
                ", keywords=" + keywords +
                ", expirationInMilliSec=" + expirationInMilliSec +
                '}';
    }
}
