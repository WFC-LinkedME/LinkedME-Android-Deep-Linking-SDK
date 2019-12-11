package com.microquation.linkedme.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;

import com.microquation.linkedme.android.callback.LMDLResultListener;
import com.microquation.linkedme.android.callback.LMLinkCreateListener;
import com.microquation.linkedme.android.indexing.LMUniversalObject;
import com.microquation.linkedme.android.log.LMErrorCode;
import com.microquation.linkedme.android.log.LMLogger;
import com.microquation.linkedme.android.network.LMCreateUrl;
import com.microquation.linkedme.android.network.ReqRespEntry;
import com.microquation.linkedme.android.network.base.LMRemoteInterface;
import com.microquation.linkedme.android.network.base.ServerRequest;
import com.microquation.linkedme.android.network.base.ServerRequestQueue;
import com.microquation.linkedme.android.network.base.ServerResponse;
import com.microquation.linkedme.android.referral.LMLinkData;
import com.microquation.linkedme.android.referral.MessageType;
import com.microquation.linkedme.android.referral.PrefHelper;
import com.microquation.linkedme.android.util.Defines;
import com.microquation.linkedme.android.util.LMConstant;
import com.microquation.linkedme.android.util.LMSystemObserver;
import com.microquation.linkedme.android.util.LinkProperties;
import com.microquation.linkedme.android.util.StringUtils;
import com.microquation.linkedme.android.util.SystemObserver;
import com.microquation.linkedme.android.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microquation.linkedme.android.network.ServerRequestValidate.isCreateUrl;
import static com.microquation.linkedme.android.network.ServerRequestValidate.isInitSession;
import static com.microquation.linkedme.android.network.ServerRequestValidate.isRegisterClose;
import static com.microquation.linkedme.android.network.ServerRequestValidate.isRegisterInstall;
import static com.microquation.linkedme.android.network.base.ServerRequestFactory.createDefaultRegisterClose;
import static com.microquation.linkedme.android.network.base.ServerRequestFactory.createDefaultRegisterInstall;
import static com.microquation.linkedme.android.network.base.ServerRequestFactory.createDefaultRegisterOpen;

public class LinkedME {

    public static final String TAG = LinkedME.class.getName();

    public static final String OG_TITLE = "$og_title";
    public static final String OG_DESC = "$og_description";
    public static final String OG_IMAGE_URL = "$og_image_url";
    public static final int LINK_TYPE_UNLIMITED_USE = 0;
    public static final String LM_LINKPROPERTIES = "lmLinkProperties";
    public static final String LM_UNIVERSALOBJECT = "lmUniversalObject";

    private static final int DEF_AUTO_DEEP_LINK_REQ_CODE = 1501;
    private volatile static LinkedME SINGLETON;

    // 是否为true 都会监控 activity 的生命周期，唯一的区别在于不会主动请求open接口
    private boolean isAutoSessionMode;
    /**
     * 注册{@link Activity}生命周期回调时实用的标识
     */
    private static boolean isActivityLifeCycleCallbackRegistered = false;

    final Object lock;
    private final ConcurrentHashMap<String, String> instrumentationExtraData;
    private JSONObject deeplinkDebugParams;
    private LMRemoteInterface kRemoteInterface;
    private LMSystemObserver systemObserver;
    /**
     * Application Context
     */
    private Context applicationContext;
    private Semaphore semaphore;
    private ServerRequestQueue requestQueue;

    // 是否有请求正在执行
    private boolean hasRequesting;
    private boolean hasNetwork;
    private Map<LMLinkData, String> linkCache;
    private String device_id;
    /**
     * 记录当前session的初始化状态,默认值为{@link LinkedME.SESSION_STATE#UNINITIALISED}
     */
    private SESSION_STATE initState = SESSION_STATE.UNINITIALISED;
    /**
     * 当前Activity的引用(注意内存泄漏问题)
     */
    private WeakReference<Activity> currentActivityReference_;

    //是否立即执行深度链接跳转
    private boolean deepLinksImmediate = true;
    //自动跳转延迟时间
    private int autoDLTimeOut = 200;
    //深度链接回调监听
    private LMDLResultListener lmdlResultListener;
    //深度链接参数回调监听
    private LMDLResultListener lmdlParamsListener;
    //处理回调数据的activity
    private String handleActivityName;
    //当应用退到后台后是否重置自动跳转为false，默认不重置
    // 当用户在application中初始化设置为false的时候，用户是希望重置为false的
    private boolean resetImmediateStatus = false;
    //APP退到后台后最后一个activity
    private String lastActivityName;
    //如果应用是从应用宝打开，再次唤起后台APP时会直接将应用从后台拉取到前台，此时应自动跳转到详情页，默认是true
    private boolean autoDLLaunchFromYYB = true;
    //如果满足autoDLLaunchFromYYB这个状态，则自动跳转
    private boolean dlLaunchFromYYB = false;
    //自动跳转时携带activity的名称的key
    private String autoDLActivityKey = "lm_act_ref_name";
    //是否跳转到详情页，默认为false，跳转后置为true
    private boolean jumpStatus = false;
    //标识是否通过微下载唤起APP
    private boolean isLaunchFromYYB = false;
    private boolean isAddClipListener = false;
    private ClipboardManager.OnPrimaryClipChangedListener onPrimaryClipChangedListener = null;
    private Handler mainHandler;
    private HandlerThread handlerThread;
    private Handler threadHandler;
    // 是否通过TaskId唤起
    private boolean openByTaskId = false;
    private String appKey;

    /**
     * <p>{@link LinkedME}内部构造方法,用于初始化主要的组件</p> <p>使用 {@link #getInstance(Context)}完成初始化过程.</p>
     *
     * @param context {@link Context} Application Context
     */
    private LinkedME(Context context, String key, boolean autoSession) {
        this.applicationContext = context;
        this.appKey = key;
        this.isAutoSessionMode = autoSession;
        kRemoteInterface = new LMRemoteInterface();
        systemObserver = SystemObserver.getInstance(context);
        requestQueue = ServerRequestQueue.getInstance(applicationContext);
        semaphore = new Semaphore(1);
        lock = new Object();
        hasRequesting = false;
        hasNetwork = true;
        linkCache = new HashMap<>();
        instrumentationExtraData = new ConcurrentHashMap<>();
        mainHandler = new Handler(Looper.getMainLooper());
        initLMHandler();
        initBroadcastReceiver(context);
        if (handlerThread == null) {
            handlerThread = new HandlerThread("LMREQUEST");
            handlerThread.start();
            initReqHandler(handlerThread.getLooper());
        }
        if (!isAutoSessionMode) {
            context.registerReceiver(screenOffReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
            addCloseSessionListener(context);
        }
        // sdk初始化时清空session，防止因close接口没有被及时调用而导致无法调用open请求
        clearSession();
        systemObserver.obtainOAIDAsync();
    }

    private BroadcastReceiver screenOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                switch (action) {
                    case Intent.ACTION_SCREEN_OFF:
                        closeSessionInternal();
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private void addCloseSessionListener(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            context.getApplicationContext().registerComponentCallbacks(new ComponentCallbacks2() {
                @Override
                public void onTrimMemory(int level) {
                    switch (level) {
                        case TRIM_MEMORY_UI_HIDDEN:
                            //当按home键的时候,此方法会被调用
                            closeSessionInternal();
                            LMLogger.info("close session called");
                            break;
                    }
                }

                @Override
                public void onConfigurationChanged(Configuration newConfig) {

                }

                @Override
                public void onLowMemory() {

                }
            });
        }
    }

    private PrefHelper getPrefHelper() {
        return PrefHelper.getInstance(applicationContext);
    }

    public String getAppKey() {
        return appKey;
    }

    /**
     * <p>异步任务执行请求服务。执行网络任务需要在后台线程执行,并且请求是按一定顺序执行的。
     * 是在异步同步模式下处理请求执行的。只能在主线程调用,并且结果应该在主线程发布。</p>
     */
    private void initReqHandler(Looper looper) {
        if (threadHandler == null) {
            threadHandler = new Handler(looper) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case ReqRespEntry.REQ_OPEN: {
                            try {
                                ServerRequest thisReq = (ServerRequest) msg.obj;
                                //更新队列中请求等待的时间,GAL及SRAL并未添加到队列中
                                addExtraInstrumentationData(thisReq.getRequestPath() + "-" + Defines.Jsonkey.Queue_Wait_Time.getKey(), String.valueOf(thisReq.getQueueWaitTime()));

                                if (thisReq.isGAdsParamsRequired()) {
                                    thisReq.updateGAdsParams(systemObserver);
                                }
                                //如果是初始化的请求，则传递browseIdentityId
                                if (isInitSession(thisReq)) {
                                    String browserIdentityId = thisReq.getPost().optString(Defines.Jsonkey.LKME_BROWSER_MISC.getKey(), "");
                                    // 剪切板中的数据之前可能已经被程序读取，所以如果从剪切板中没有获取到数据，则从sp文件等位置读取
                                    if (TextUtils.isEmpty(browserIdentityId)) {
                                        browserIdentityId = getBrowserIdentityId();
                                        thisReq.getPost().putOpt(Defines.Jsonkey.LKME_BROWSER_MISC.getKey(), browserIdentityId);
                                    }
                                }
                                if (thisReq.isGetRequest()) {
                                    ReqRespEntry reqRespEntry = new ReqRespEntry();
                                    reqRespEntry.setServerRequest(thisReq);
                                    reqRespEntry.setServerResponse(kRemoteInterface.make_restful_get(thisReq.getRequestUrl(), thisReq.getGetParams(), thisReq.getRequestUrl(), getPrefHelper().getTimeout()));
                                    Message msgResp = mainHandler.obtainMessage(ReqRespEntry.RESP_OPEN, reqRespEntry);
                                    mainHandler.sendMessage(msgResp);
                                } else {
                                    ReqRespEntry reqRespEntry = new ReqRespEntry();
                                    reqRespEntry.setServerRequest(thisReq);
                                    reqRespEntry.setServerResponse(kRemoteInterface.make_restful_post(thisReq.getPostWithInstrumentationValues(instrumentationExtraData), thisReq.getRequestUrl(), thisReq.getRequestPath(), getPrefHelper().getTimeout()));
                                    Message msgResp = mainHandler.obtainMessage(ReqRespEntry.RESP_OPEN, reqRespEntry);
                                    mainHandler.sendMessage(msgResp);
                                }
                            } catch (Exception ignore) {

                            }
                            break;
                        }
                        default:
                            break;
                    }
                }
            };
        }

    }


    /**
     * 主线程调用
     */
    private void initLMHandler() {
        mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ReqRespEntry.RESP_OPEN: {
                        try {
                            ReqRespEntry reqRespEntry = (ReqRespEntry) msg.obj;
                            ServerRequest thisReq = reqRespEntry.getServerRequest();
                            ServerResponse serverResponse = reqRespEntry.getServerResponse();
                            if (serverResponse != null) {
                                int status = serverResponse.getStatusCode();
                                hasNetwork = true;
                                //请求失败
                                if (status != HttpURLConnection.HTTP_OK) {
                                    //如果是初始化的请求，则标记会话未被初始化
                                    if (isInitSession(thisReq)) {
                                        initState = SESSION_STATE.UNINITIALISED;
                                    }
                                    // 请求返回结果标识该请求为不合规请求，则从队列中移除该请求
                                    if (status == HttpURLConnection.HTTP_CONFLICT) {
                                        requestQueue.remove(thisReq);
                                        if (isCreateUrl(thisReq) && thisReq instanceof LMCreateUrl) {
                                            ((LMCreateUrl) thisReq).handleDuplicateURLError();
                                        } else {
                                            LMLogger.info("LinkedME API Error: Conflicting resource error code from API");
                                            handleFailure(0, status);
                                        }
                                    } else {
                                        // 网络错误或者LinkedME初始化失败，队列中所有被挂起的，失败后需要重新请求的都需要重试
                                        hasNetwork = false;
                                        // 收集队列中所有的请求
                                        ArrayList<ServerRequest> requestToFail = new ArrayList<>();
                                        for (int i = 0; i < requestQueue.getSize(); i++) {
                                            requestToFail.add(requestQueue.peekAt(i));
                                        }
                                        // 首先移除那些失败后不需要重试的请求
                                        for (ServerRequest req : requestToFail) {
                                            if (req == null || !req.shouldRetryOnFail()) {
                                                requestQueue.remove(req);
                                            }
                                        }
                                        // 设置network为0，标识请求能被重新开始
                                        hasRequesting = false;

                                        // 调用请求失败的回调
                                        for (ServerRequest req : requestToFail) {
                                            if (req != null) {
                                                req.handleFailure(status, serverResponse.getFailReason());
                                                if (isInitSession(req)) {
                                                    dlResultFailure();
                                                    paramsCallback();
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // 缓存生成的深度链接
                                    if (isCreateUrl(thisReq) && thisReq instanceof LMCreateUrl) {
                                        if (serverResponse.getObject() != null) {
                                            String url = serverResponse.getObject().optString("url");
                                            // cache the link
                                            linkCache.put(((LMCreateUrl) thisReq).getLinkPost(), url);
                                        }
                                    }

                                    if (thisReq.isPersist()) {
                                        //因为GAL及SRAL并没有加入到队列中，我以不需要移除
                                        requestQueue.dequeue();
                                    }

                                    // 如果请求改变了session，则需要更新session_id
                                    if (isInitSession(thisReq)) {
                                        if (serverResponse.getObject() != null) {
                                            boolean updateRequestsInQueue = false;
                                            if (serverResponse.getObject().has(Defines.Jsonkey.LKME_SESSION_ID.getKey()) &&
                                                    !TextUtils.isEmpty(serverResponse.getObject().getString(Defines.Jsonkey.LKME_SESSION_ID.getKey()))) {
                                                //install接口和open接口请求成功后，会从服务器端获取SessionID并存储到sp文件中，close接口调用成功后会将该SessionID重置
                                                getPrefHelper().setSessionID(serverResponse.getObject().getString(Defines.Jsonkey.LKME_SESSION_ID.getKey()));
                                                updateRequestsInQueue = true;
                                            }
                                            if (serverResponse.getObject().has(Defines.Jsonkey.LKME_IDENTITY_ID.getKey()) &&
                                                    !TextUtils.isEmpty(serverResponse.getObject().getString(Defines.Jsonkey.LKME_IDENTITY_ID.getKey()))) {
                                                String new_Identity_Id = serverResponse.getObject().getString(Defines.Jsonkey.LKME_IDENTITY_ID.getKey());
                                                if (!getPrefHelper().getIdentityID().equals(new_Identity_Id)) {
                                                    //On setting a new identity Id clear the link cache
                                                    linkCache.clear();
                                                    getPrefHelper().setIdentityID(serverResponse.getObject().getString(Defines.Jsonkey.LKME_IDENTITY_ID.getKey()));
                                                    updateRequestsInQueue = true;
                                                }
                                            }
                                            if (serverResponse.getObject().has(Defines.Jsonkey.DeviceFingerprintID.getKey()) &&
                                                    !TextUtils.isEmpty(serverResponse.getObject().getString(Defines.Jsonkey.DeviceFingerprintID.getKey()))) {
                                                getPrefHelper().setDeviceFingerPrintID(serverResponse.getObject().getString(Defines.Jsonkey.DeviceFingerprintID.getKey()));
                                                updateRequestsInQueue = true;
                                            }

                                            //将lkme_link深度链接存到sp文件中
                                            if (serverResponse.getObject().has(Defines.LinkParam.Params.getKey()) &&
                                                    !TextUtils.isEmpty(serverResponse.getObject().getString(Defines.LinkParam.Params.getKey()))) {
                                                JSONObject params = convertParamsStringToDictionary(serverResponse.getObject().getString(Defines.LinkParam.Params.getKey()));
                                                getPrefHelper().setLMLink(params.getString(Defines.LinkParam.LKME_Link.getKey()));
                                            }

                                            if (updateRequestsInQueue) {
                                                updateAllRequestsInQueue();
                                            }

                                            if (isInitSession(thisReq)) {
                                                LMLogger.info("post init session status ===  " + initState);
                                                // 初始化接口需要设置详细参数
                                                initState = SESSION_STATE.INITIALISED;
                                                thisReq.onRequestSucceeded(serverResponse, SINGLETON);
                                                // 自动深度链接跳转
                                                LMLogger.info("处理方式：" + deepLinksImmediate);
                                                LMLogger.info("lmdlResultListener = " + lmdlResultListener
                                                        + ", lmdlParamsListener = " + lmdlParamsListener
                                                        + ", deepLinksImmediate = " + deepLinksImmediate
                                                        + ", dlLaunchFromYYB = " + dlLaunchFromYYB);
                                                if (lmdlResultListener != null) {
                                                    JSONObject latestParams = getLatestReferringParams();
                                                    //Check if the application is launched by clicking a LinkedME link.
                                                    if (!latestParams.optBoolean(Defines.Jsonkey.LKME_CLICKED_LINKEDME_LINK.getKey(), false)) {
                                                        dlResultFailure();
                                                    } else if (latestParams.length() > 0) {
                                                        Intent intent = new Intent();
                                                        addHandleIntentData(intent, latestParams, LinkProperties.getReferredLinkProperties());
                                                        lmdlResultListener.dlResult(intent, null);
                                                    } else {
                                                        dlResultFailure();
                                                    }
                                                } else if (lmdlParamsListener != null) {
                                                    paramsCallback();
                                                } else if (deepLinksImmediate || dlLaunchFromYYB || isOpenByTaskId()) {
                                                    if (isOpenByTaskId()) {
                                                        setOpenByTaskId(false);
                                                    }
                                                    LMLogger.info("open api auto jump deepLinksImmediate = " + deepLinksImmediate + "dlLaunchFromYYB = " + dlLaunchFromYYB);
                                                    checkForAutoDeepLinkConfiguration();
                                                }
                                            } else {
                                                thisReq.onRequestSucceeded(serverResponse, SINGLETON);
                                            }
                                        }
                                    } else {
                                        //Publish success to listeners
                                        thisReq.onRequestSucceeded(serverResponse, SINGLETON);
                                    }
                                }
                                if (hasNetwork && initState != SESSION_STATE.UNINITIALISED) {
                                    //处理下一个请求
                                    processNextQueueItem();
                                }
                            }
                        } catch (JSONException ex) {
                            hasRequesting = false;
                            ex.printStackTrace();
                        } finally {
                            hasRequesting = false;
                        }

                        break;
                    }
                    default:
                        break;
                }
            }
        };
    }


    private void initBroadcastReceiver(Context context) {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    int messageType = intent.getIntExtra(LMConstant.BROAD_CODE, MessageType.MSG_INVALID);
                    switch (messageType) {
                        case MessageType.MSG_HTTP_SERVER_OPEN_APP:
                            final String startType = intent.getStringExtra(LMConstant.BROAD_ARG1);
                            final String uriScheme = intent.getStringExtra(LMConstant.BROAD_ARG2);
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    LMLogger.debug("open App");
                                    try {
                                        if (TextUtils.equals(LMConstant.START_TYPE_SELF, startType)) {
                                            if (LinkedME.getInstance().getCurrentActivity() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                                ActivityManager activityManager = (ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
                                                int taskId = LinkedME.getInstance().getCurrentActivity().getTaskId();
                                                LMLogger.debug("task id == " + taskId);
                                                if (taskId != -1) {
                                                    LinkedME.getInstance().setOpenByTaskId(true);
                                                    activityManager.moveTaskToFront(taskId, ActivityManager.MOVE_TASK_WITH_HOME);
                                                }
                                            }
                                        } else {
                                            if (!TextUtils.isEmpty(uriScheme)) {
                                                Intent intentZero = Intent.parseUri(uriScheme, Intent.URI_INTENT_SCHEME);
                                                intentZero.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                applicationContext.startActivity(intentZero);
                                            }
                                        }
                                    } catch (Exception e) {
                                        LMLogger.debugExceptionError(e);
                                    }
                                }
                            });
                            break;
                        case MessageType.MSG_INTEGRATE_SUCCESS:
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Activity currentActivity = LinkedME.getInstance().getCurrentActivity();
                                    if (currentActivity != null) {
                                        AlertDialog alertDialog = new AlertDialog.Builder(currentActivity).create();
                                        alertDialog.setMessage("您的SDK已正确集成！\n（该提示只在扫描测试二维码时出现）");
                                        alertDialog.setTitle("温馨提示");
                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
                                                "OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog,
                                                                        int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        alertDialog.show();
                                    }
                                }
                            });
                            break;
                        case MessageType.MSG_GET_PARAMS:
                            mainHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // 由于剪切板数据写入滞后，所以需要延迟清空剪切板数据
                                    clearClipboard();
                                }
                            }, 1000);

                            break;
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(LMConstant.BROAD_MAIN_ACTION);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, filter);

    }

    private void clearClipboard() {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                ClipboardManager cbm = (ClipboardManager) applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                if (cbm != null && cbm.hasPrimaryClip()) {
                    if (cbm.getPrimaryClip().getItemCount() > 0) {
                        CharSequence temp = cbm.getPrimaryClip().getItemAt(0).getText();
                        LMLogger.debug("需要清空的剪切板数据== " + temp);
                        if (TextUtils.isEmpty(temp)) {
                            return;
                        }
                        // 创建 Pattern 对象
                        Pattern r = Pattern.compile(LMConstant.PATTERN_TRANSACTION_TYPE);
                        // 现在创建 matcher 对象
                        Matcher m = r.matcher(temp);
                        if (m.find() && m.groupCount() > 0) {
                            // 清空剪切板
                            ClipData clipData = ClipData.newPlainText("", "");
                            cbm.setPrimaryClip(clipData);
                        }
                    }
                }
            }
            handleBrowserIdentityId();
        } catch (Exception ignore) {
            LMLogger.debugExceptionError(ignore);
        }
    }

    // 处理剪切板数据，防止唤起已在后台的应用后，退到后台再进入前台通过剪切板数据重复跳转的问题
    private void handleBrowserIdentityId() {
        String originUriScheme = getPrefHelper().getOriginUriScheme();
        String browserIdentityId = getPrefHelper().getBrowserIdentityId();
        if (!TextUtils.isEmpty(originUriScheme) && !TextUtils.isEmpty(browserIdentityId)) {
            String[] browserIdentityIdArr = browserIdentityId.split(LMConstant.BROWSER_IDENTITY_ID_SPLIT);
            if (browserIdentityIdArr.length > 1) {
                String[] browserDeeplinkArr = browserIdentityIdArr[1].split("/");
                if (browserDeeplinkArr.length > 2) {
                    String browserClickId = browserDeeplinkArr[1];
                    String browserTimestamp = browserDeeplinkArr[2];
                    if (!TextUtils.isEmpty(browserClickId)) {
                        Uri originUri = Uri.parse(originUriScheme);
                        String originClickId = originUri.getQueryParameter(Defines.Jsonkey.LinkClickID.getKey());
                        String originTimestamp = originUri.getQueryParameter(Defines.Jsonkey.LinkRandom.getKey());
                        if (TextUtils.isEmpty(originTimestamp)) {
                            originTimestamp = originUri.getQueryParameter(LMConstant.LM_TIMESTAMP);
                        }
                        if (!TextUtils.isEmpty(originClickId) && TextUtils.equals(browserClickId, originClickId)) {
                            if (!TextUtils.isEmpty(browserTimestamp) && !TextUtils.isEmpty(originTimestamp)) {
                                if (Long.valueOf(browserTimestamp) - Long.valueOf(originTimestamp) < 1000) {
                                    // browserIdentityId数据无效
                                    String invalidBrowserIdentityId = browserIdentityId.replace(browserIdentityIdArr[1], StringUtils.EMPTY);
                                    getPrefHelper().setBrowserIdentityId(invalidBrowserIdentityId);
                                }
                            }
                        }
                    }

                }
            }
        }

    }

    /**
     * <p>初始化SDK</p>
     *
     * @return LinkedME 实例
     */
    public static LinkedME getInstance(Context context) {
        return getInstance(context, getKeyFromXml(context));
    }

    /**
     * 兼容之前key的配置方案
     */
    private static String getKeyFromXml(Context context) {
        String key = null;
        try {
            ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (applicationInfo.metaData != null) {
                key = applicationInfo.metaData.getString(LMConstant.META_APP_KEY);
            }
        } catch (PackageManager.NameNotFoundException e) {
            LMLogger.undefinedError(e);
        }

        return key;
    }

    /**
     * <p>初始化SDK</p>
     *
     * @return LinkedME 实例
     */
    public static LinkedME getInstance(Context context, String key) {
        return getInstance(context, key, true);
    }

    /**
     * <p>初始化SDK</p>
     *
     * @return LinkedME 实例
     */
    public static LinkedME getInstance(Context context, String key, boolean autoSession) {
        if (TextUtils.isEmpty(verifyAndGetKey(context, key))) {
            LMLogger.info("LinkedME Key 不能为空！");
            return null;
        }
        if (SINGLETON == null) {
            SINGLETON = LinkedME.initInstance(context, key, autoSession);
        }
        if (autoSession) {
            if (!isActivityLifeCycleCallbackRegistered
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                SINGLETON.setActivityLifeCycleObserver((Application) context.getApplicationContext());
            }
        }
        return SINGLETON;
    }

    /**
     * 校验并获取key
     *
     * @return appKey
     */
    private static String verifyAndGetKey(Context context, String key) {
        if (TextUtils.isEmpty(key)) {
            String metaDataKey = "linkedme.sdk.appKey";
            try {
                final ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
                if (applicationInfo.metaData != null) {
                    key = applicationInfo.metaData.getString(metaDataKey);
                }
            } catch (Exception ignore) {
                LMLogger.all(null, "解析AndroidManifest文件异常", ignore);
            }
        }

        return key;
    }

    /**
     * <p>获取当前的{@link LinkedME}实例,请确认调用{@link LinkedME#getInstance(Context)}完成了{@link
     * LinkedME}的初始化过程</p>
     *
     * @return 当前的{@link LinkedME}实例
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static LinkedME getInstance() {
        if (SINGLETON == null) {
            LMLogger.info("LinkedMe没有初始化.[如果您调整后依然看到这个提示,请尝试使用getInstance(Context context, String appKey).进行初始化工作");
        } else {
            if (SINGLETON.isAutoSessionMode && !isActivityLifeCycleCallbackRegistered) {
                LMLogger.info("LinkedMe没有初始化成功. 请确保您的Application继承自LMApp或者您已经在您的Application#onCreate中初始化LinkedMe.");
            }
        }
        return SINGLETON;
    }

    /**
     * <p>初始化LinkedMe</p>
     *
     * @param context {@link Context}.
     * @return 返回LinkMe单例
     */
    private static LinkedME initInstance(Context context, String key, boolean autoSession) {
        return new LinkedME(context.getApplicationContext(), key, autoSession);
    }

    /**
     * <p>重置当前session状态</p>
     */
    public void resetUserSession() {
        initState = SESSION_STATE.UNINITIALISED;
        getPrefHelper().setSessionID(PrefHelper.NO_STRING_VALUE);
        getPrefHelper().setSessionParams(PrefHelper.NO_STRING_VALUE);
    }

    /**
     * <p>清除深度链接跳转数据，防止应用多次跳转中转页面，默认不清除跳转数据，可能会用到跳转数据</p>
     */
    public void clearSessionParams() {
        getPrefHelper().setSessionParams(PrefHelper.NO_STRING_VALUE);
    }

    /**
     * <p>获取LinkProperties{@link LinkProperties}值，包含深度链接对应的用户跳转参数</p>
     *
     * @return LinkProperties对象，无跳转参数则返回null
     */
    public LinkProperties getLinkProperties() {
        return LinkProperties.getReferredLinkProperties();
    }

    /**
     * <p>获取LMUniversalObject{@link LMUniversalObject}值，包含深度链接对应的相关跳转参数</p>
     *
     * @return LMUniversalObject对象，无跳转参数则返回null
     */
    public LMUniversalObject getLMUniversalObject() {
        return LMUniversalObject.getReferredLinkedMeUniversalObject();
    }

    /**
     * <p>设置链接最大自动重试次数,默认值为3,参数定义 {@link PrefHelper#MAX_RETRIES}</p>
     *
     * @param retryCount 链接重试次数
     */
    public void setRetryCount(int retryCount) {
        if (getPrefHelper() != null && retryCount > 0) {
            getPrefHelper().setRetryCount(retryCount);
        }
    }

    /**
     * <p>为超时的链接设置重试前的等待时间,默认为0ms,即不等待。参数定义 {@link PrefHelper#KEY_RETRY_INTERVAL}</p>
     *
     * @param retryInterval 重试等待时间
     */
    public void setRetryInterval(int retryInterval) {
        if (getPrefHelper() != null && retryInterval > 0) {
            getPrefHelper().setRetryInterval(retryInterval);
        }
    }

    /**
     * <p>设置网络请求超时时间,默认为5500ms,参数定义 {@link PrefHelper#TIMEOUT}</p> <p>可以在网络环境不佳的情况下,更快的给予用户响应</p>
     *
     * @param timeout 网络请求超时时间
     */
    public void setNetworkTimeout(int timeout) {
        if (getPrefHelper() != null && timeout > 0) {
            getPrefHelper().setTimeout(timeout);
        }
    }

    /**
     * <p>设置打印网络请求日志</p>
     */
    public LinkedME setDebug() {
        LMLogger.setDebug(true);
        return this;
    }

    /**
     * <p>设置深度链接唤起APP后是否立即立即解析深度链接，实现跳转，该设置只针对自动深度链接跳转有效。 若在Application中初始化的时候设置为false，需要在特定网页开始跳转，则在该特定页面调用该方法并传入true。建议在主页将该值置为true
     * </p>
     *
     * @param immediate true:打开APP后立即跳转，false:打开APP后等待通知跳转 默认为true
     */
    public synchronized LinkedME setImmediate(boolean immediate) {
        LMLogger.info("调用了setImmediate(" + immediate + ") 方法。");
        LMLogger.info("autoDLLaunchFromYYB : " + autoDLLaunchFromYYB);
        if (!autoDLLaunchFromYYB) {
            LMLogger.info("限制应用自动跳转！");
            return this;
        }
        if (!immediate) {
            //如果有被设置为false，则说明用户不想直接跳转，有相关限制
            autoResetImmediateFalse();
        }
        if (immediate && !deepLinksImmediate) {
            LMLogger.info("调用了setImmediate(boolean immediate) 方法并开始处理跳转逻辑。");
            checkForAutoDeepLinkConfiguration();
        }
        deepLinksImmediate = immediate;
        return this;
    }

    /**
     * 设置自动跳转的超时时间，无特殊需求请不要更改该值，默认为200毫秒
     *
     * @param autoDLTimeOut 自动跳转超时时间，单位为毫秒，默认200毫秒
     */
    public LinkedME setAutoDLTimeOut(int autoDLTimeOut) {
        if (autoDLTimeOut == 0) {
            //最低10毫秒的延迟
            autoDLTimeOut = 10;
        } else if (autoDLTimeOut < 0) {
            autoDLTimeOut = 200;
        }
        this.autoDLTimeOut = autoDLTimeOut;
        return this;
    }

    /**
     * 可以设置深度链接的调试参数. 调试模式下的这些参数将会在其他的触发深度链接的页面于initSession的回调函数中返回. 这个方法需要在调用initSession的
     * Activity的onCreate(Bundle)中返回
     *
     * @param debugParams {@link JSONObject}
     */
    public void setDeepLinkDebugMode(JSONObject debugParams) {
        deeplinkDebugParams = debugParams;
    }

    /**
     * <p>初始化session</p>
     *
     * @param intentData 通过包含部分来源链接数据的{@link Uri}来引导初始化工作.
     * @param activity   当前{@link Activity}
     * @return 标识传入的data是否用于初始化session工作, 如Uri格式错误将导致此函数返回false
     */
    public void initSessionWithData(Uri intentData, Activity activity) {
        if (activity == null) {
            return;
        }
        if (intentData == null) {
            intentData = activity.getIntent().getData();
        }
        //未进行跳转状态重置
        jumpStatus = false;
        setHandleStatus(false);
        if (intentData != null) {
            getPrefHelper().setOriginUriScheme(intentData.toString());
        }
        // intentData.toString().contains("?hs_from=") 处理多次用微信唤起的问题
        if ((intentData == null || intentData.toString().contains("?" + LMConstant.HS_FROM + "=")) && isOpenByTaskId()) {
            // 通过Http Server唤起，剪切板获取不到数据，在oppo、vivo手机intentData也为空的情况下
            String httpServerUriScheme = getPrefHelper().getAndClearHttpServerUriScheme();
            if (!TextUtils.isEmpty(httpServerUriScheme)) {
                intentData = Uri.parse(httpServerUriScheme);
            }
        }
        // 判断是否通过Http Server唤起逻辑
        if (intentData != null && intentData.isHierarchical() && isLKMEHSData(intentData)) {
            String oldUriScheme = getPrefHelper().getUriScheme();
            String from = "&" + LMConstant.HS_FROM + "=" + intentData.getQueryParameter(LMConstant.HS_FROM);
            String intentStrOrigin = intentData.toString().replace(from, "");
            if (!TextUtils.isEmpty(oldUriScheme)) {
                if (TextUtils.equals(oldUriScheme, intentStrOrigin)) {
                    // 相同则代表已经跳转，无需重复跳转
                    LMLogger.debug("Uri Scheme相同");
                    intentData = null;
                } else {
                    // 不相同判断参数中的间隔时间是否大于3秒，大于3秒跳转
                    String timestamp = intentData.getQueryParameter(LMConstant.LM_TIMESTAMP);
                    String timestampReplace = "&" + LMConstant.LM_TIMESTAMP + "=" + timestamp;
                    String intentStr = intentStrOrigin.replace(timestampReplace, "");
                    Uri oldUri = Uri.parse(oldUriScheme);
                    String oldTimestamp = oldUri.getQueryParameter(LMConstant.LM_TIMESTAMP);
                    String oldTimestampReplace = "&" + LMConstant.LM_TIMESTAMP + "=" + oldTimestamp;
                    String oldIntentStr = oldUriScheme.replace(oldTimestampReplace, "");
                    if (timestamp != null && oldTimestamp != null && TextUtils.equals(intentStr, oldIntentStr)) {
                        // 判断时间是否超过3秒
                        if (Long.valueOf(timestamp) > (Long.valueOf(oldTimestamp) + 3000)) {
                            LMLogger.debug("Uri Scheme相同，时间是否超过3秒");
                            // 超过3秒执行
                            getPrefHelper().setUriScheme(intentStrOrigin);
                        } else {
                            // 小于3秒不执行
                            LMLogger.debug("Uri Scheme相同，时间是否小于3秒");
                            intentData = null;
                        }
                    } else {
                        // uri scheme 不相同
                        LMLogger.debug("Uri Scheme不相同");
                        getPrefHelper().setUriScheme(intentStrOrigin);
                    }
                }
            } else {
                // 存储scheme
                LMLogger.debug("Old Uri Scheme不存在");
                getPrefHelper().setUriScheme(intentStrOrigin);
            }
        }

        if (intentData != null && intentData.isHierarchical()) {
            String timestamp = "&" + LMConstant.LM_TIMESTAMP + "=" + intentData.getQueryParameter(LMConstant.LM_TIMESTAMP);
            String intentStrOrigin = intentData.toString().replace(timestamp, "");
            intentData = Uri.parse(intentStrOrigin);
        }
        readAndStripParam(intentData, activity);
        initUserSessionInternal(activity);
    }

    private void initUserSessionInternal(Activity activity) {
        if (activity != null) {
            currentActivityReference_ = new WeakReference<>(activity);
        }
        //如果未初始化或者正在初始化
        // 如果正在初始化，则设置新的回调
        if (initState == SESSION_STATE.UNINITIALISED || (!hasSession() && initState != SESSION_STATE.INITIALISING)) {
            //如果未被初始化，则将已存在的或者创建一个新的install或者open请求，并放到请求队列头部
            initState = SESSION_STATE.INITIALISING;
            initializeSession();
        }
    }

    /**
     * <p>关闭当前session. 将会在Activity栈中最后一个Activity调用 Activity的onStop()时调用. </p>
     *
     * @see LinkedMEActivityLifeCycleObserver
     * @see #setActivityLifeCycleObserver(Application)
     */
    private void closeSessionInternal() {
        dlLaunchFromYYB = false;
        //退到后台后重置为不自动跳转
        if (resetImmediateStatus) {
            deepLinksImmediate = false;
        }
        // 重置监听，防止从后台返回到前台时重复调用监听
        if (lmdlParamsListener != null) {
            lmdlParamsListener = null;
        }
        if (lmdlResultListener != null) {
            lmdlResultListener = null;
        }
        executeClose();
        // 在退到后台的时候监听剪切板中的内容
        addClipboardListener();
    }


    /**
     * <p>安全的关闭当前的session</p>
     *
     * <p>如果当前网络不可用,这个函数将把请求队列前端的Open/Install移除请求队列(由于此时LinkedMe并没有获得一个可用的session,因此这个行为等效于关闭session);将直接发起关闭session的请求</p>
     */
    private void executeClose() {
        LMLogger.info("executeClose status start ===  " + initState);
        if (initState != SESSION_STATE.UNINITIALISED) {
            if (hasNetwork) {
                if (!requestQueue.containsClose()) {
                    ServerRequest req = createDefaultRegisterClose(applicationContext);
                    handleNewRequest(req);
                }
                LMLogger.info("executeClose status central ===  " + initState);
            } else {
                clearSession();
            }
            initState = SESSION_STATE.UNINITIALISED;
        } else {
            clearSession();
        }
        LMLogger.info("executeClose status end ===  " + initState);
    }

    public void clearSession() {
        // close的时候既要清空sessionParams也要清空sessionId
        PrefHelper.getInstance(applicationContext).setSessionParams(PrefHelper.NO_STRING_VALUE);
        PrefHelper.getInstance(applicationContext).setSessionID(PrefHelper.NO_STRING_VALUE);
    }

    private boolean readAndStripParam(Uri data, Activity activity) {
        LMLogger.info("调用了readAndStripParam() 方法。");
        // 当是从外部intents开启的应用时,获取intent uri 和 extra 用于分析。
        try {
            //判断是否属于linkedme的Uri scheme或者http请求
            if (isLKMEData(data)) {
                LMLogger.info("调用了readAndStripParam() 方法并且是深度链接跳转，uri 为：" + data);
                getPrefHelper().setExternalIntentUri(data.toString());
            }
            if (activity != null && activity.getIntent() != null && activity.getIntent().getExtras() != null) {
                Bundle bundle = activity.getIntent().getExtras();
                Set<String> extraKeys = bundle.keySet();

                if (extraKeys.size() > 0) {
                    JSONObject extrasJson = new JSONObject();
                    for (String key : extraKeys) {
                        extrasJson.put(key, bundle.get(key));
                    }
                    getPrefHelper().setExternalIntentExtra(extrasJson.toString());
                }
            }
        } catch (Exception ignore) {
            LMLogger.undefinedError(ignore);
        }

        //检查是否是深度链接或者App links
        if (data != null && data.isHierarchical() && activity != null) {
            if (data.getQueryParameter(Defines.Jsonkey.LinkClickID.getKey()) != null) {
                LMLogger.info("调用了readAndStripParam() 方法且是uri scheme方式。");
                //uri-scheme模式
                getPrefHelper().setLinkClickIdentifier(data.getQueryParameter(Defines.Jsonkey.LinkClickID.getKey()));
                //此处是为了去除Uri scheme携带的关于深度链接的相关参数包括lkme参数
                String paramString = Defines.Jsonkey.LinkClickID.getKey() + "=" + data.getQueryParameter(Defines.Jsonkey.LinkClickID.getKey()) + "&" +
                        Defines.Jsonkey.LinkLKME.getKey() + "=" + data.getQueryParameter(Defines.Jsonkey.LinkLKME.getKey());
                String uriString = data.toString();//activity.getIntent().getDataString();
                if (data.getQuery() != null && data.getQuery().length() == paramString.length()) {
                    paramString = "\\?" + paramString;
                } else if ((uriString.length() - paramString.length()) == uriString.indexOf(paramString)) {
                    paramString = "&" + paramString;
                } else {
                    paramString = paramString + "&";
                }
                Uri newData = Uri.parse(uriString.replaceFirst(paramString, ""));
                activity.getIntent().setData(newData);
                return true;
            } else {
                LMLogger.info("调用了readAndStripParam() 方法且是app links方式。");
                // 检查被点击的url是否是一个指向该应用的APP links
                String scheme = data.getScheme();
                if (scheme != null) {
                    if ((activity.getIntent().getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
                        if ((scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))
                                && data.getHost() != null && data.getHost().length() > 0 && data.getQueryParameter(Defines.Jsonkey.AppLinkUsed.getKey()) == null) {
                            getPrefHelper().setAppLink(data.toString());
                            String uriString = data.toString();
                            if (isLKMEData(data)) {
                                uriString = uriString.replace(data.getHost(), "");
                            }
                            activity.getIntent().setData(Uri.parse(uriString));
                            return false;
                        }
                        LMLogger.info("通过App links 启动！");

                    }
                }
            }
        }
        return false;
    }

    /**
     * 判断是否属于linkedme的Uri scheme或者http请求
     *
     * @param data Uri
     * @return true:属于 false:不属于
     */
    private boolean isLKMEData(Uri data) {
        if (data != null && data.isHierarchical()) {
            try {
                //判断是否属于linkedme的Uri scheme或者http请求
                return null != data.getQueryParameter(Defines.Jsonkey.LinkLKME.getKey())
                        || Defines.Jsonkey.LinkLKMECC.getKey().equals(data.getHost())
                        || Defines.Jsonkey.LinkWWWLKMECC.getKey().equals(data.getHost());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 判断是否属于linkedme的 http server 发起的 Uri scheme或者http请求
     *
     * @param data Uri
     * @return true:属于 false:不属于
     */
    private boolean isLKMEHSData(Uri data) {
        //判断是否属于linkedme的Uri scheme或者http请求
        if (isLKMEData(data)) {
            return data.toString().contains(LMConstant.HS_FROM);
        }
        return false;
    }


    /**
     * <p>获取首次打开时的链接参数，如果设置了追加的测试参数，测试参数也会随之返回</p>
     *
     * @return A {@link JSONObject} 链接参数
     */
    public JSONObject getFirstReferringParams() {
        String storedParam = getPrefHelper().getInstallParams();
        JSONObject firstReferringParams = convertParamsStringToDictionary(storedParam);
        firstReferringParams = appendDebugParams(firstReferringParams);
        return firstReferringParams;
    }

    /**
     * <p>获取最近一次打开APP的深度链接参数，如果设置了追加的测试参数，测试参数也会随之返回</p>
     *
     * @return A {@link JSONObject} 链接参数
     */
    public JSONObject getLatestReferringParams() {
        String storedParam = getPrefHelper().getSessionParams();
        JSONObject latestParams = convertParamsStringToDictionary(storedParam);
        latestParams = appendDebugParams(latestParams);
        return latestParams;
    }

    /**
     * 追加调试参数
     *
     * @param originalParams A {@link JSONObject} 原始深度链接参数
     * @return 将被追加的{@link JSONObject}.
     */
    private JSONObject appendDebugParams(JSONObject originalParams) {
        try {
            if (originalParams != null && deeplinkDebugParams != null) {
                if (deeplinkDebugParams.length() > 0) {
                    LMLogger.info("当前使用调试模式参数");
                }
                Iterator<String> keys = deeplinkDebugParams.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    originalParams.put(key, deeplinkDebugParams.get(key));
                }
            }
        } catch (Exception ignore) {
        }
        return originalParams;
    }

    /**
     * 获取追加的测试参数
     *
     * @return 追加的测试参数
     */
    public JSONObject getDeeplinkDebugParams() {
        if (deeplinkDebugParams != null && deeplinkDebugParams.length() > 0) {
            LMLogger.info("当前使用调试模式参数");
        }
        return deeplinkDebugParams;
    }


    //-----------------Generate Short URL      -------------------------------------------//

    /**
     * <p><strong>内部方法,请勿直接使用</strong></p> <p>通过给定的{@link ServerRequest}生成深度链接</p>
     *
     * @param req 给定的{@link ServerRequest}
     * @return 对应的深度链接, 这个方法为SDK内部调用, 如果生成深度链接, 请使用 {@link LMUniversalObject#generateShortUrl(Context,
     * LinkProperties, LMLinkCreateListener)}完成
     */
    public String generateShortLinkInternal(ServerRequest req) {
        if (!req.isConstructError() && !req.handleErrors(applicationContext) && req instanceof LMCreateUrl) {
            if (linkCache.containsKey(((LMCreateUrl) req).getLinkPost())) {
                //从缓存中获取深度链接
                String url = linkCache.get(((LMCreateUrl) req).getLinkPost());
                ((LMCreateUrl) req).onUrlAvailable(url);
                return url;
            } else {
                if (((LMCreateUrl) req).isAsync()) {
                    generateShortLinkAsync(req);
                } else {
                    return generateShortLinkSync(req);
                }
            }
        }
        return null;
    }


    /**
     * 同步生成深度链接
     *
     * @param req 生成深度链接请求
     * @return 生成的深度链接
     */
    private String generateShortLinkSync(ServerRequest req) {
        if (initState == SESSION_STATE.INITIALISED) {
            ServerResponse response = null;
            try {
                int timeOut = getPrefHelper().getTimeout() + 2000;
                response = new getShortLinkTask().execute(req).get(timeOut, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ignore) {
            }
            if (req instanceof LMCreateUrl) {
                String url = ((LMCreateUrl) req).getLongUrl();
                if (response != null && response.getStatusCode() == HttpURLConnection.HTTP_OK) {
                    url = response.getObject().optString("url");
                    if (((LMCreateUrl) req).getLinkPost() != null) {
                        linkCache.put(((LMCreateUrl) req).getLinkPost(), url);
                    }
                }
                return url;
            }
        } else {
            LMLogger.info("LinkedME Warning: 用户session未被初始化！");
        }
        return null;
    }

    /**
     * 异步生成深度链接
     *
     * @param req 生成深度链接请求
     */
    private void generateShortLinkAsync(ServerRequest req) {
        handleNewRequest(req);
    }

    /**
     * 将params转化为json数据格式
     */
    private JSONObject convertParamsStringToDictionary(String paramString) {
        if (paramString.equals(PrefHelper.NO_STRING_VALUE)) {
            return new JSONObject();
        } else {
            try {
                return new JSONObject(paramString);
            } catch (JSONException e) {
                byte[] encodedArray = Base64.decode(paramString.getBytes(), Base64.NO_WRAP);
                try {
                    return new JSONObject(new String(encodedArray));
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    return new JSONObject();
                }
            }
        }
    }

    /**
     * 创建一个新的请求
     */
    private void createReq(ServerRequest req) {
        if (threadHandler != null) {
            Message msg = threadHandler.obtainMessage(ReqRespEntry.REQ_OPEN, req);
            threadHandler.sendMessage(msg);
        }
    }

    /**
     * <p>处理队列中的一个请求</p>
     */
    private void processNextQueueItem() {
        try {
            semaphore.acquire();
            if (!hasRequesting && requestQueue.getSize() > 0) {
                ServerRequest req = requestQueue.peek();
                if (req != null) {
                    if (!req.isPersist()) {
                        requestQueue.dequeue();
                    }
                    //除了Install请求,所有请求都需要一个有效的IdentityID
                    if (!(isRegisterInstall(req)) && !hasIdentityId()) {
                        //不是一个Install接口,并且没有Identity_id,这两个是矛盾的,所以这是一个错误的请求
                        LMLogger.info("LinkedME 错误: 用户session没有被初始化!");
                        hasRequesting = false;
                        handleFailure(requestQueue.getSize() - 1, LMErrorCode.ERR_NO_SESSION);
                    } else if (!isInitSession(req) && !hasSession()) {
                        //除了open和install接口，其他所有接口都需要包含session才可以执行
                        hasRequesting = false;
                        handleFailure(requestQueue.getSize() - 1, LMErrorCode.ERR_NO_SESSION);
                    } else {
                        hasRequesting = true;
                        createReq(req);
                    }
                } else {
                    requestQueue.remove(null); //移除无效的请求
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            semaphore.release();
        }
    }

    private void handleFailure(int index, int statusCode) {
        ServerRequest req;
        if (index >= requestQueue.getSize()) {
            req = requestQueue.peekAt(requestQueue.getSize() - 1);
        } else {
            req = requestQueue.peekAt(index);
        }
        handleFailure(req, statusCode);
    }

    private void handleFailure(final ServerRequest req, int statusCode) {
        if (req == null)
            return;
        req.handleFailure(statusCode, "");
        if (isInitSession(req)) {
            dlResultFailure();
            paramsCallback();
        }
    }

    /**
     * 更新所有请求的sessionId,identityId和deviceFingerPrintId
     */
    private void updateAllRequestsInQueue() {
        try {
            for (int i = 0; i < requestQueue.getSize(); i++) {
                ServerRequest req = requestQueue.peekAt(i);
                if (req.getPost() != null) {
                    Iterator<?> keys = req.getPost().keys();
                    while (keys.hasNext()) {
                        String key = (String) keys.next();
                        if (key.equals(Defines.Jsonkey.SessionID.getKey())) {
                            req.getPost().put(key, getPrefHelper().getSessionID());
                        } else if (key.equals(Defines.Jsonkey.IdentityID.getKey())) {
                            req.getPost().put(key, getPrefHelper().getIdentityID());
                        } else if (key.equals(Defines.Jsonkey.DeviceFingerprintID.getKey())) {
                            req.getPost().put(key, getPrefHelper().getDeviceFingerPrintID());
                        }
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否包含sessionID
     *
     * @return true: 包含 false:不包含
     */
    private boolean hasSession() {
        return !TextUtils.isEmpty(getPrefHelper().getSessionID());
    }

    private boolean hasDeviceFingerPrint() {
        return !TextUtils.isEmpty(getPrefHelper().getDeviceFingerPrintID());
    }

    /**
     * <p>判断是否有identityId</p>
     *
     * @return true:不是一次安装 false:是一次安装
     */
    private boolean hasIdentityId() {
        return !TextUtils.isEmpty(getPrefHelper().getIdentityID());
    }

    private void bringRequestToFront(ServerRequest req) {
        if (hasRequesting) {
            // TODO: 2019-10-11 lipeng 如果正在发生的请求不需要持久化（参见ServerRequest的persist），则并不能将该请求提到最前面
            requestQueue.insert(req, 1);
        } else {
            requestQueue.insert(req, 0);
        }
    }


    private void createInstallOrOpenRequest(ServerRequest req) {
        // 如果队列中不包含Open/Install请求,则添加到队列中
        if (!requestQueue.containsInstallOrOpen()) {
            bringRequestToFront(req);
        } else {
            //如果队列中已经有install或者open请求，将该请求置于请求队列头，并且确保重新绑定了回调，
            // 因为当open或者install接口挂起，APP被中断的时候，回调会被清除
            requestQueue.moveInstallOrOpenToFront(req, hasRequesting);
        }
        processNextQueueItem();
    }

    private void initializeSession() {
        //未设置linkedme_key或者未初始化
        if (TextUtils.isEmpty(getAppKey())) {
            initState = SESSION_STATE.UNINITIALISED;
            LMLogger.info("未设置linkedme_key或者未初始化");
            return;
        }
        if (hasIdentityId() && systemObserver.getUpdateState(true) == SystemObserver.STATE_NO_CHANGE) {
            createInstallOrOpenRequest(createDefaultRegisterOpen(applicationContext, systemObserver));
        } else {
            createInstallOrOpenRequest(createDefaultRegisterInstall(applicationContext, getPrefHelper().getLinkClickIdentifier(), systemObserver));
        }
    }

    /**
     * 处理非install和open的请求。检查session是否初始化并且如果需要初始化session请求，则添加install或者open请求到请求队列头部
     *
     * @param req 需要被处理的请求
     */
    private void handleNewRequest(ServerRequest req) {
        // 当请求需要session，而该请求即将发生时，session并没有初始化，则添加open或install请求至请求队列头部
        if (initState != SESSION_STATE.INITIALISED && !isInitSession(req)) {
            if (isRegisterClose(req) && initState == SESSION_STATE.UNINITIALISED) {
                LMLogger.info("LinkedME 没有完成session初始化，不需要关闭。");
                //只有在未初始化的情况下才返回不调用close接口，用于解决"有时分享到微信后留在微信中，无法跳转到详情页"的情况
                return;
            } else {
                Activity currentActivity = null;
                if (currentActivityReference_ != null) {
                    currentActivity = currentActivityReference_.get();
                }
                initUserSessionInternal(currentActivity);
            }
        }

        requestQueue.enqueue(req);
        req.onRequestQueued();
        processNextQueueItem();
    }

    /**
     * 对于Android 14及以上版本会自动管理session，无需用户手动关闭
     *
     * @param application {@link Application}
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setActivityLifeCycleObserver(Application application) {
        try {
            LinkedMEActivityLifeCycleObserver activityLifeCycleObserver = new LinkedMEActivityLifeCycleObserver();
            application.unregisterActivityLifecycleCallbacks(activityLifeCycleObserver);
            application.registerActivityLifecycleCallbacks(activityLifeCycleObserver);
            isActivityLifeCycleCallbackRegistered = true;
        } catch (NoSuchMethodError | NoClassDefFoundError ignore) {
            isActivityLifeCycleCallbackRegistered = false;
            LMLogger.error(LMErrorCode.ERR_API_LVL_14_NEEDED, null, ignore);
        }
    }

    /**
     * 检查是否开启了自动跳转，若开启了自动跳转功能，则自动跳转
     */
    private synchronized void checkForAutoDeepLinkConfiguration() {
        if (jumpStatus && !dlLaunchFromYYB) {
            //若已跳转，则不重复处理
            return;
        }
        final JSONObject latestParams = getLatestReferringParams();
        LMLogger.info("参数原始数据为：" + latestParams);
        String deepLinkActivity = null;
        int deepLinkActivityReqCode = DEF_AUTO_DEEP_LINK_REQ_CODE;

        try {
            final LinkProperties lmLinkProperties = LinkProperties.getReferredLinkProperties();
            if (lmLinkProperties != null) {
                //如果不包含自动跳转Activity Key
                String autoDLActValue = getAutoDLActValue(lmLinkProperties);
                if (TextUtils.isEmpty(autoDLActValue)) {
                    if (!TextUtils.isEmpty(handleActivityName)) {
                        LMLogger.info("设置的中间处理页面为：" + handleActivityName);
                        //用户手动设置了处理页面
                        deepLinkActivity = handleActivityName;
                    } else {
                        LMLogger.info("请设置参数接收页面");
                        throw new RuntimeException("未设置参数接收页面");
                    }
                } else {
                    //用户配置了自动跳转，无需用户添加跳转逻辑
                    deepLinkActivity = autoDLActValue;
                }
                if (deepLinkActivity != null && currentActivityReference_ != null) {

                    final int finalDeepLinkActivityReqCode = deepLinkActivityReqCode;
                    //添加延迟打开功能，防止后台应用拉到前台时界面展示底色
                    final String finalDeepLinkActivity = deepLinkActivity;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final Activity currentActivity = currentActivityReference_.get();
                                Intent intent = null;
                                if (currentActivity != null) {
                                    intent = new Intent(currentActivity, Class.forName(finalDeepLinkActivity));
                                } else {
                                    LMLogger.info("LinkedME Warning: 当前Activity已被销毁，采用Application Context跳转！");
                                    intent = new Intent(applicationContext, Class.forName(finalDeepLinkActivity));
                                }
                                addHandleIntentData(intent, latestParams, lmLinkProperties);
                                LMLogger.info("开始跳转到中间页面！");
                                if (currentActivity != null) {
                                    currentActivity.startActivityForResult(intent, finalDeepLinkActivityReqCode);
                                } else {
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    applicationContext.startActivity(intent);
                                }
                                //跳转成功后重置以下两种状态
                                jumpStatus = true;
                                dlLaunchFromYYB = false;
                            } catch (ClassNotFoundException e) {
                                LMLogger.info("LinkedME Warning: 请确保自动深度链接Activity正确配置！并没有找到该Activity" + finalDeepLinkActivityReqCode);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                LMLogger.info("LinkedME Warning: 数据解析错误！");
                            } catch (Exception ignore) {
                                LMLogger.debugExceptionError(ignore);
                            }
                        }
                    }, autoDLTimeOut);

                } else {
                    LMLogger.info("无接收深度链接跳转参数的中转页面。");
                }
            } else {
                LMLogger.info("无任何参数！");
            }
        } catch (Exception ignore) {
            LMLogger.undefinedError(ignore);
        }
    }

    /**
     * 为intent添加数据
     *
     * @param intent       Intent
     * @param latestParams 深度链接数据
     * @throws JSONException JSONException
     */
    private void addHandleIntentData(Intent intent, JSONObject latestParams, LinkProperties lmLinkProperties) throws JSONException {

        //原始深度链接参数
//        intent.putExtra(Defines.Jsonkey.ReferringData.getKey(), latestParams.toString());
        //添加深度链接相关参数
        LMUniversalObject lmUniversalObject = LMUniversalObject.getReferredLinkedMeUniversalObject();
        if (lmLinkProperties == null) {
            LMLogger.info("跳转无相关参数！");
        } else {
            LMLogger.info("跳转的参数为：" + lmLinkProperties.getControlParams());
            //将用户参数放置到intent中
            Map<String, String> controlParams = lmLinkProperties.getControlParamsArrayMap();
            if (controlParams != null && !controlParams.isEmpty()) {
                for (String key : controlParams.keySet()) {
                    intent.putExtra(key, controlParams.get(key));
                }
            }
        }
        intent.putExtra(LinkedME.LM_LINKPROPERTIES, lmLinkProperties);
        intent.putExtra(LinkedME.LM_UNIVERSALOBJECT, lmUniversalObject);
        // Add individual parameters in the data
        Iterator<?> keys = latestParams.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            intent.putExtra(key, latestParams.getString(key));
        }
    }

    /**
     * 获取用户自定义跳转Activity的值
     *
     * @return 有则返回值，无则返回null
     */
    private String getAutoDLActValue(LinkProperties lmLinkProperties) {
        if (lmLinkProperties != null) {
            Map<String, String> controlParams = lmLinkProperties.getControlParamsArrayMap();
            if (controlParams != null) {
                return controlParams.get(autoDLActivityKey);
            }
        }
        return null;
    }

    /**
     * 更新请求在队列中等待的时间
     */
    public void addExtraInstrumentationData(String key, String value) {
        instrumentationExtraData.put(key, value);
    }

    /**
     * 用于标识session的初始化状态
     */
    private enum SESSION_STATE {
        INITIALISED, INITIALISING, UNINITIALISED
    }

    /**
     * <p>该类监听记录Activity{@link Activity}的生命周期，以至于决定什么时候开启和关闭Session</p>
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private class LinkedMEActivityLifeCycleObserver implements Application.ActivityLifecycleCallbacks {
        private int activityCnt_ = 0; // 记录活跃Activity数目
        private Uri launcherData = null;
        //是否已经判断了由微下载唤起
        private boolean checkLaunchFromYYB = false;
        // 判断应用是否被系统进程杀掉后从后台唤起，4.0及一下不支持该问题修复
        private boolean isRecoveredBySystem = false;


        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
            LMLogger.info("onCreated " + activity.getClass().getSimpleName() + "  activityCnt_ = " + activityCnt_);
            if (!isAutoSessionMode) {
                return;
            }
            //处理第一个activity被创建后在onCreate()中随即被finish的情况
            if (activityCnt_ < 1 && launcherData == null) {
                launcherData = activity.getIntent().getData();
                if (!TextUtils.isEmpty(getPrefHelper().getOriginUriScheme())
                        && launcherData != null
                        && TextUtils.equals(launcherData.toString(), getPrefHelper().getOriginUriScheme())
                        && activity.getIntent().getSourceBounds() != null) {
                    isRecoveredBySystem = true;
                }
            }
            LMLogger.debug("isRecoveredBySystem==" + isRecoveredBySystem);
            if (activityCnt_ < 1 && !checkLaunchFromYYB) {
                isLaunchFromYYB = isLaunchFromYYB(activity.getIntent());
                checkLaunchFromYYB = true;
            }
            if (activityCnt_ > 0 && checkLaunchFromYYB) {
                checkLaunchFromYYB = false;
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
            LMLogger.info("onResumed " + activity.getClass().getSimpleName() + "  activityCnt_ = " + activityCnt_ + " getIntent() = " + activity.getIntent());

            if (isAutoSessionMode && activityCnt_ < 1) { // 检查是否是第一个Activity，如果是，则开始一个Session
                //设置自动应用宝自动跳转
                LMLogger.info("应用宝自动跳转参数：autoDLLaunchFromYYB = " + autoDLLaunchFromYYB
                        + ", isLaunchFromYYB = " + isLaunchFromYYB);
                if (autoDLLaunchFromYYB && isLaunchFromYYB && TextUtils.equals(activity.getClass().getName(), lastActivityName)) {
                    dlLaunchFromYYB = true;
                }
                LMLogger.info("应用宝自动跳转参数处理后：dlLaunchFromYYB = " + dlLaunchFromYYB);
                Uri intentData = null;
                if (activity.getIntent() != null) {
                    LMLogger.info("onStarted--onStarted " + activity.getIntent().getDataString());
                    Uri currentData = activity.getIntent().getData();
                    // 如果是从最近任务列表中打开的app，则判断是否是linkedme的uri scheme，如果是则将
                    // data置为null，防止重复打开详情页面
                    LMLogger.info("最近任务列表 = " + isLaunchedFromRecents(activity.getIntent())
                            + ", LinkedME Intent = " + isLKMEData(currentData)
                            + ", isRecoveredBySystem = " + isRecoveredBySystem);
                    if ((isLaunchedFromRecents(activity.getIntent()) && isLKMEData(currentData)) || isRecoveredBySystem) {
                        currentData = null;
                        launcherData = null;
                        activity.getIntent().setData(currentData);
                        isRecoveredBySystem = false;
                    }
                    if (currentData != null) {
                        // 以下处理某些用户在接收uri scheme的activity的onCreate()方法中finish掉后，无法获取data的情况

                        // 我个人并没有复现
                        if (isLKMEData(launcherData) &&
                                launcherData.toString().startsWith(activity.getIntent().getDataString())) {
                            activity.getIntent().setData(launcherData);
                            intentData = launcherData;
                            LMLogger.info("Uri Scheme接收页面在onCreate()中调用了finish()方法，同时将Uri Data传递到下一个页面");
                        } else {
                            intentData = currentData;
                        }
                    } else {
                        activity.getIntent().setData(launcherData);
                        intentData = launcherData;
                        LMLogger.info("onStarted--onCreated " + activity.getIntent().getDataString());
                    }
                    launcherData = null;
                }
                initSessionWithData(intentData, activity);
            }
            activityCnt_++;
        }

        @Override
        public void onActivityResumed(Activity activity) {
            LMLogger.info("onResumed " + activity.getClass().getSimpleName() + ",intent=" + activity.getIntent().getDataString());
            currentActivityReference_ = new WeakReference<>(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {
            LMLogger.info("onPaused " + activity.getClass().getSimpleName() + "  activityCnt_ = " + activityCnt_);
        }

        @Override
        public void onActivityStopped(Activity activity) {
            LMLogger.info("onStop " + activity.getClass().getSimpleName() + "  activityCnt_ = " + activityCnt_);
            activityCnt_--;
            if (activityCnt_ < 1) {
                lastActivityName = activity.getClass().getName();
                closeSessionInternal();
                LMLogger.info("close session called");
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            LMLogger.info("onDestroyed " + activity.getClass().getSimpleName());
            if (currentActivityReference_ != null && currentActivityReference_.get() == activity) {
                currentActivityReference_.clear();
            }
        }

    }

    /**
     * 异步创建深度链接
     */
    private class getShortLinkTask extends AsyncTask<ServerRequest, Void, ServerResponse> {
        @Override
        protected ServerResponse doInBackground(ServerRequest... serverRequests) {
            return kRemoteInterface.createCustomUrlSync(serverRequests[0].getPost());
        }
    }


    /**
     * 用户可从此处获取手机上存储的device_id
     */
    public String getDeviceId() {
        if (!TextUtils.isEmpty(device_id) && !PrefHelper.NO_STRING_VALUE.equals(device_id)) {
            return device_id;
        }
        device_id = systemObserver.getDeviceId();
        return device_id;
    }

    /**
     * 是否处理了服务器返回的结果数据
     */
    public boolean isHandleStatus() {
        return getPrefHelper().getHandleStatus();
    }

    /**
     * 是否处理了服务器返回的结果数据
     */
    public void setHandleStatus(boolean status) {
        getPrefHelper().setHandleStatus(status);
    }

    /**
     * 获取当前SDK的版本号
     */
    public static String getSDKVersion() {
        return LMRemoteInterface.SDK_VERSION;
    }

    /**
     * 获得当前所在的activity
     */
    private Activity getCurrentActivity() {
        if (currentActivityReference_ != null) {
            return currentActivityReference_.get();
        } else {
            return null;
        }
    }


    /**
     * 获取Application Context
     *
     * @return Application Context
     */
    public Context getApplicationContext() {
        return applicationContext;
    }

    /**
     * 判断是否由深度链接唤起，但是该判断并不完全准确，具体查看todo，该方法只能在被唤起的第一个activity内调用
     *
     * @param intent Intent
     * @return true：是深度链接唤起 false：不是深度链接唤起
     */
    @Deprecated
    public boolean isDeepLinkUri(Intent intent) {
        return isOpenFromDL(intent);
    }

    private boolean isOpenFromDL(Intent intent) {
        if (intent != null) {
            // TODO: 10/02/2017 这里是有问题的，用户可能先前点击了深度链接
            if (intent.getSourceBounds() != null) {
                // 通过点击桌面快捷方式打开应用
                return false;
            }
            if (intent.getData() != null) {
                //通过uri scheme方式或者app links方式跳转过来
                return isLKMEData(intent.getData());
            }
            if (intent.getExtras() != null) {
                //从应用宝跳转过来
                return true;
            }
            if (TextUtils.isEmpty(intent.getPackage())) {
                //如果不包含包名，一定不是通过微下载唤起APP的
                return false;
            }
            //QQ浏览器微下载唤起APP不包含platformId，Extras为null，包含package name，但很多APP唤起应用时都会添加
            //package name，那么我就认为Extras为null，且包含package name均一律认为为微下载唤起的APP
            if (intent.getExtras() == null && !TextUtils.isEmpty(intent.getPackage())) {
                return true;
            }
        }
        // TODO: 10/02/2017 从应用程序点击打开APP也有可能先前点击了深度链接
        return false;
    }

    /**
     * 判断APP是否由微下载唤起从未唤起的APP
     *
     * @param intent {@link Intent}
     * @return true 由微下载唤起，false 相反
     */
    private boolean isLaunchFromYYB(Intent intent) {
        if (intent != null) {
            boolean isLaunch = false;
            Set<String> categories = intent.getCategories();
            if (categories == null) {
                return false;
            }
            for (String category : categories) {
                if (TextUtils.equals(category, Intent.CATEGORY_LAUNCHER)) {
                    isLaunch = true;
                    break;
                }
            }
            if (!isLaunch) {
                return false;
            }
            if (intent.getData() != null) {
                //通过uri scheme方式或者app links方式跳转过来
                return false;
            }
            //微信 appKey == platformId, value ===  wechat
            if (intent.getExtras() != null && intent.getExtras().containsKey("platformId")) {
                return true;
            }
            if (intent.getSourceBounds() != null) {
                // 通过点击桌面快捷方式打开应用
                return false;
            }
            if (TextUtils.isEmpty(intent.getPackage())) {
                //如果不包含包名，一定不是通过微下载唤起APP的
                return false;
            }
            //QQ浏览器微下载唤起APP不包含platformId，Extras为null，包含package name，但很多APP唤起应用时都会添加
            //package name，那么我就认为Extras为null，且包含package name均一律认为为微下载唤起的APP
            if (intent.getExtras() == null && !TextUtils.isEmpty(intent.getPackage())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 设置深度链接结果回调监听，因为该监听只会在结果返回的时候调用一次，所以该监听需要在launcher页面设置，否则应用宝无法获取参数并跳转到指定页面
     * 因为在应用退到后台后，会销毁lmdlResultListener对象，所以存在如果从后台通过包名唤起应用，则无法监听到数据的问题，
     * 应用宝的微下载链接从2019-01-01开始，全部通过uri scheme唤起应用，所以不存在通过包名唤起应用的情况
     *
     * @param lmdlResultListener 结果回调监听
     */
    public void setDeepLinkListener(LMDLResultListener lmdlResultListener) {
        this.lmdlResultListener = lmdlResultListener;
    }

    /**
     * 深度链接参数监听，
     */
    public void setParamsCallback(LMDLResultListener lmdlParamsListener) {
        this.lmdlParamsListener = lmdlParamsListener;
        paramsCallback();
    }

    /**
     * 参数接口回调
     */
    private void paramsCallback() {
        if (lmdlParamsListener == null) {
            LMLogger.info("lmdlParamsListener 不能为null");
            return;
        }
        JSONObject jsonObject = getLatestReferringParams();
        if (jsonObject.isNull("params")) {
            LMLogger.info("Params no data ");
            lmdlParamsListener.dlParams(null);
        } else {
            String params = jsonObject.optString("params");
            if (TextUtils.isEmpty(params)) {
                //无数据
                LMLogger.info("Params no data ");
                lmdlParamsListener.dlParams(null);
            } else {
                //有数据
                LMLogger.info("Params: " + params);
                lmdlParamsListener.dlParams(LinkProperties.getReferredLinkProperties());
                //取出数据后清除数据
                LinkedME.getInstance().clearSessionParams();
            }
        }
    }


    /**
     * 深度链接结果获取失败
     */
    private void dlResultFailure() {
        if (lmdlResultListener != null && !jumpStatus) {
            lmdlResultListener.dlResult(null, new LMErrorCode("LinkedME 提示信息：", LMErrorCode.ERR_LINKEDME_NOT_DEEPLINK));
        }
    }

    /**
     * 设置处理深度链接数据的Activity的引用名称（例如：com.microquation.sample.activity.MiddleActivity）
     *
     * @param handleActivityName 处理深度链接数据的Activity的引用名称（例如：com.microquation.sample.activity.MiddleActivity）
     * @return {@link LinkedME}
     */
    public LinkedME setHandleActivity(String handleActivityName) {
        this.handleActivityName = handleActivityName;
        return this;
    }

    /**
     * 当应用退到后台后重置自动跳转为false
     *
     * @return {@link LinkedME}
     */
    private LinkedME autoResetImmediateFalse() {
        this.resetImmediateStatus = true;
        return this;
    }

    /**
     * 如果应用是通过微下载打开，再次唤起后台APP时会直接将应用从后台拉取到前台， 此时应用会自动跳转到详情页，不会受任何限制，调用该方法关闭自动跳转到详情页
     * 展示完广告或者登录后一定要调用LinkedME.getInstance().setImmediate(true);跳转到详情页
     */
    private void disableDLLaunchFromYYB() {
        autoDLLaunchFromYYB = false;
        dlLaunchFromYYB = false;
    }

    /**
     * 如果应用是通过微下载打开，再次唤起后台APP时会直接将应用从后台拉取到前台， 调用该方法允许应用自动跳转到详情页，与{@link
     * LinkedME#disableDLLaunchFromYYB()}相对
     */
    private void enableDLLaunchFromYYB() {
        autoDLLaunchFromYYB = true;
    }

    /**
     * 设置自动跳转，无需用户编写跳转逻辑时包含Activity名称的key字段
     *
     * @param autoDLActivityKey 包含Activity名称的key字段
     * @return {@link LinkedME}
     */
    public LinkedME setAutoDLActivityKey(String autoDLActivityKey) {
        this.autoDLActivityKey = autoDLActivityKey;
        return this;
    }

    /**
     * 设置跳转限制
     */
    @Deprecated
    public void addJumpConstraint() {
        disableDLLaunchFromYYB();
    }

    /**
     * 移除跳转限制
     */
    @Deprecated
    public void removeJumpConstraint() {
        enableDLLaunchFromYYB();
    }

    /**
     * 保存BrowserIdentityId
     */
    private void saveBrowserIdentityId() {
        if (systemObserver != null) {
            final String browserIdentityId = systemObserver.getBrowserIdentityId();
            if (!TextUtils.isEmpty(browserIdentityId)) {
                LMLogger.debug("browserIdentityId保存到SP文件中");
                String oldUriScheme = getPrefHelper().getUriScheme();
                String newBrowserIdentityId = browserIdentityId;
                if (!TextUtils.isEmpty(oldUriScheme)) {
                    try {
                        Uri oldUri = Uri.parse(oldUriScheme);
                        if (oldUri != null && oldUri.isHierarchical()) {
                            String clickId = oldUri.getQueryParameter(Defines.Jsonkey.LinkClickID.getKey());
                            if (clickId != null && browserIdentityId.contains(clickId)) {
                                String[] browserIdentityIdArr = browserIdentityId.split(LMConstant.BROWSER_IDENTITY_ID_SPLIT);
                                if (browserIdentityIdArr.length > 1) {
                                    newBrowserIdentityId = browserIdentityId.replace(browserIdentityIdArr[1], StringUtils.EMPTY);
                                }
                            }
                        }
                    } catch (Exception e) {
                        LMLogger.debugExceptionError(e);
                    }

                }
                getPrefHelper().setBrowserIdentityId(newBrowserIdentityId);
            }
        }
    }

    /**
     * 获取BrowserIdentityId
     */
    private String getBrowserIdentityId() {
        //获取browseIdentityId，先从剪切板中获取，如果剪切板中获取不到，则从文件中及sp文件中获取
        String browserIdentityId = getPrefHelper().getBrowserIdentityId();
        LMLogger.debug("browserIdentityId从SP文件中获取" + browserIdentityId);
        LMLogger.debug("browserIdentityId的值为" + browserIdentityId);
        return browserIdentityId;
    }

    private void removeClipboardListener() {
        // 在App打开的时候移除监听剪切板中的内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && isAddClipListener) {
            try {
                ClipboardManager cbm = (ClipboardManager) applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                if (cbm != null && onPrimaryClipChangedListener != null) {
                    LMLogger.debug("browserIdentityId移除了监听");
                    cbm.removePrimaryClipChangedListener(onPrimaryClipChangedListener);
                    onPrimaryClipChangedListener = null;
                    isAddClipListener = false;
                }
            } catch (Exception ignore) {
                LMLogger.debugExceptionError(ignore);
            }
        }
    }

    private void addClipboardListener() {
        removeClipboardListener();
        LMLogger.debug("准备添加监听");
        // 在退到后台的时候监听剪切板中的内容
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && !isAddClipListener) {
            try {
                ClipboardManager cbm = (ClipboardManager) applicationContext.getSystemService(Context.CLIPBOARD_SERVICE);
                if (cbm != null) {
                    onPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
                        @Override
                        public void onPrimaryClipChanged() {
                            LMLogger.debug("监听到了数据");
                            saveBrowserIdentityId();
                        }
                    };
                    cbm.addPrimaryClipChangedListener(onPrimaryClipChangedListener);
                    LMLogger.debug("browserIdentityId添加了监听");
                    isAddClipListener = true;
                }
            } catch (Exception ignore) {
                // 此处添加catch是由于中华万年历反馈个别杂牌手机
                // 在调用addPrimaryClipChangedListener时会报空指针异常，因此此处捕获忽略掉
                LMLogger.debugExceptionError(ignore);
            }
        }
    }

    /**
     * 判断是否从最近任务列表中打开App
     *
     * @param intent Intent
     * @return true:是 false:否
     */
    private boolean isLaunchedFromRecents(Intent intent) {
        return (intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY;
    }

    private boolean isOpenByTaskId() {
        return openByTaskId;
    }

    private void setOpenByTaskId(boolean openByTaskId) {
        this.openByTaskId = openByTaskId;
    }

}