package com.microquation.linkedme.android.callback;

import com.microquation.linkedme.android.log.LMErrorCode;

public interface LMLinkCreateListener {
    void onLinkCreate(String url, LMErrorCode error);
}