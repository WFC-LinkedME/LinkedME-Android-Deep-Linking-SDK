package com.microquation.linkedme.android.network;

import com.microquation.linkedme.android.referral.LMLinkData;

/**
 * 创建深度链接接口
 * Created by chenhao on 16-3-8.
 */
public interface LMCreateUrl {

    LMLinkData getLinkPost();

    void onUrlAvailable(String url);

    String getLongUrl();

    void handleDuplicateURLError();

    boolean isAsync();
}
