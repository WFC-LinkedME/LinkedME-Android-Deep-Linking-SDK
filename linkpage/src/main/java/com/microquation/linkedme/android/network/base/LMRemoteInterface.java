package com.microquation.linkedme.android.network.base;

import android.content.Context;

import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.referral.PrefHelper;
import com.microquation.linkedme.android.util.Defines;
import com.microquation.linkedme.android.util.LMConstant;

import org.json.JSONObject;

public class LMRemoteInterface extends RemoteInterface {

    /**
     * <p>初始化构造方法</p>
     */
    public LMRemoteInterface() {
        super();
    }

    /**
     * <p>初始化构造方法</p>
     */
    public LMRemoteInterface(Context context) {
        super(context);
    }

    /**
     * <p>同步创建深度链接</p>
     *
     * @param post A {@link JSONObject} 深度链接相关参数.
     * @return A {@link ServerResponse} 服务器响应 request.
     */
    public ServerResponse createCustomUrlSync(JSONObject post) {
        return make_restful_post(post, LMConstant.LKME_URL + Defines.RequestPath.GetURL.getPath(), Defines.RequestPath.GetURL.getPath(), PrefHelper.getInstance(LinkedME.getInstance().getApplicationContext()).getTimeout());
    }

}
