package com.microquation.linkedme.android.callback;

import android.content.Intent;

import com.microquation.linkedme.android.log.LMErrorCode;
import com.microquation.linkedme.android.util.LinkProperties;

/**
 * 深度链接回调结果监听，用于用户自己处理深度链接结果
 *
 * Created by LinkedME06 on 09/02/2017.
 */

public abstract class LMDLResultListener {
    public void dlResult(Intent handledIntent, LMErrorCode lmErrorCode) {

    }

    public void dlParams(LinkProperties linkProperties) {

    }
}
