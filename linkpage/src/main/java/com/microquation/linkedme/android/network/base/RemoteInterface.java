package com.microquation.linkedme.android.network.base;

import android.content.Context;
import android.os.Build;
import android.os.NetworkOnMainThreadException;
import android.text.TextUtils;
import android.util.Log;

import com.microquation.linkedme.BuildConfig;
import com.microquation.linkedme.android.LinkedME;
import com.microquation.linkedme.android.log.LMErrorCode;
import com.microquation.linkedme.android.log.LMLogger;
import com.microquation.linkedme.android.referral.PrefHelper;
import com.microquation.linkedme.android.util.AESCipher;
import com.microquation.linkedme.android.util.Defines;
import com.microquation.linkedme.android.util.MD5Utils;
import com.microquation.linkedme.android.util.SystemObserver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * <p>网络链接请求类</p>
 */
class RemoteInterface {
    public static final String LINKEDME_KEY = "linkedme_key";
    public static final int NO_CONNECTIVITY_STATUS = -1009;
    public static final int NO_LINKEDME_KEY_STATUS = -1234;
    public static final String SDK_VERSION = BuildConfig.SDK_VERSION;
    private static final int DEFAULT_TIMEOUT = 5000;
    private int lastRoundTripTime = 0;

    public RemoteInterface() {
        this(null);
    }

    public RemoteInterface(Context context) {
        this.mPool = new ByteArrayPool(DEFAULT_POOL_SIZE);
    }


    /**
     * <p>将返回的结果转换为json对象</p>
     *
     * @param inStream   http连接输入流
     * @param statusCode http响应状态码
     * @param tag        用于日志或者分析的标记字符串{@link String}
     * @return {@link ServerResponse} 对象
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1: Status
     * Codes</a>
     */
    private ServerResponse processEntityForJSON(InputStream inStream, int statusCode, String tag) {
        BufferedReader rd = null;
        ServerResponse result = new ServerResponse(tag, statusCode);
        try {
            if (inStream != null) {
                rd = new BufferedReader(new InputStreamReader(inStream));
                String line = rd.readLine();
                if ((tag.contains(Defines.RequestPath.RegisterInstall.getPath())
                        || tag.contains(Defines.RequestPath.RegisterOpen.getPath()))
                        || tag.contains(Defines.RequestPath.RegisterClose.getPath())) {
                    LMLogger.info("returned" + line);
                } else {
                    LMLogger.debug("returned" + line);
                }
                if (line != null) {
                    try {
                        JSONObject jsonObj = new JSONObject(line);
                        result.setPost(jsonObj);
                    } catch (JSONException ex) {
                        try {
                            JSONArray jsonArray = new JSONArray(line);
                            result.setPost(jsonArray);
                        } catch (JSONException ex2) {
                            LMLogger.info("JSON exception: " + ex2.getMessage());
                        }
                    }
                }
            }
        } catch (IOException ex) {
            LMLogger.info("IO exception: " + ex.getMessage());
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (rd != null) {
                    rd.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                LMLogger.info("IO exception: " + e.getMessage());
            }
        }
        return result;
    }

    /**
     * <p>将返回的结果转换为json对象</p>
     *
     * @param httpResponse HttpURLConnection响应
     * @param statusCode   http响应状态码
     * @param tag          用于日志或者分析的标记字符串{@link String}
     * @return {@link ServerResponse} 对象
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP/1.1: Status
     * Codes</a>
     */
    private ServerResponse processEntityForImage(HttpURLConnection httpResponse, int statusCode, String tag) {
        try {
            byte[] data = contentToBytes(httpResponse);
            return new ServerResponse(tag, statusCode, data);
        } catch (IOException ex) {
            LMLogger.info("IO exception: " + ex.getMessage());
        }
        return new ServerResponse(tag, statusCode);
    }

    private static int DEFAULT_POOL_SIZE = 4096;
    protected final ByteArrayPool mPool;

    /**
     * Reads the contents of inputStream into a byte[].
     */
    private byte[] contentToBytes(HttpURLConnection httpResponse) throws IOException {
        PoolingByteArrayOutputStream bytes =
                new PoolingByteArrayOutputStream(mPool, httpResponse.getContentLength());
        byte[] buffer = null;
        InputStream in = null;
        try {
            in = httpResponse.getInputStream();
            if (in == null) {
                throw new IOException();
            }
            buffer = mPool.getBuf(1024);
            int count;
            while ((count = in.read(buffer)) != -1) {
                bytes.write(buffer, 0, count);
            }
            return bytes.toByteArray();
        } finally {
            if (in != null) {
                in.close();
            }
            mPool.returnBuf(buffer);
            bytes.close();
        }
    }

    /**
     * <p>创建GET请求</p>
     *
     * @param url     请求的URL字符串{@link String}。
     * @param params  请求参数{@link JSONObject}格式
     * @param tag     用于日志或者分析的标记字符串{@link String}
     * @param timeout {@link Integer} 类型值,设置请求超时的毫秒数
     * @return {@link ServerResponse} 对象
     */
    public ServerResponse make_restful_get(String url, JSONObject params, String tag, int timeout) {
        return make_restful_get(url, params, tag, timeout, 0);
    }

    /**
     * post 请求通用参数
     *
     * @param post        请求参数{@link JSONObject}格式
     * @param retryNumber 请求重试的次数
     * @return true:成功
     */
    private boolean addPostCommonParams(JSONObject post, int retryNumber) {
        try {
            String linkedmeKey = LinkedME.getInstance().getAppKey();
            if (TextUtils.isEmpty(linkedmeKey)) {
                return false;
            }
            PrefHelper prefHelper = PrefHelper.getInstance(LinkedME.getInstance().getApplicationContext());
            post.put(LINKEDME_KEY, LinkedME.getInstance().getAppKey());
            post.put(Defines.Jsonkey.LKME_SDK_VERSION.getKey(), "android" + SDK_VERSION);

            post.put(Defines.Jsonkey.LKME_DEVICE_ID.getKey(), LinkedME.getInstance().getDeviceId());

            post.putOpt(Defines.Jsonkey.LKME_OS.getKey(), SystemObserver.getInstance(LinkedME.getInstance().getApplicationContext()).getOS());
            String imei = SystemObserver.getInstance(LinkedME.getInstance().getApplicationContext()).getIMEI();
            if (TextUtils.isEmpty(imei)) {
                imei = prefHelper.getIMEI();
            } else {
                //存储IMEI号
                prefHelper.setIMEI(imei);
            }
            post.put(Defines.Jsonkey.LKME_IMEI.getKey(), imei);
            post.putOpt(Defines.Jsonkey.LKME_IMEI_MD5.getKey(), MD5Utils.encrypt(SystemObserver.getInstance(LinkedME.getInstance().getApplicationContext()).getIMEI()));
            post.put(Defines.Jsonkey.LKME_ANDROID_ID.getKey(), SystemObserver.getInstance(LinkedME.getInstance().getApplicationContext()).getAndroidId());
            post.putOpt(Defines.Jsonkey.LKME_ANDROID_ID_MD5.getKey(), MD5Utils.encrypt(SystemObserver.getInstance(LinkedME.getInstance().getApplicationContext()).getAndroidId()));
            post.put(Defines.Jsonkey.LKME_IMSI.getKey(), SystemObserver.getInstance(LinkedME.getInstance().getApplicationContext()).getIMSI());
            post.put(Defines.Jsonkey.LKME_MIIT_SDK_VERSION.getKey(), prefHelper.getMiitSdkVersion());
            post.put(Defines.Jsonkey.LKME_MIIT_SUPPORT.getKey(), prefHelper.getIsSupport());
            post.put(Defines.Jsonkey.LKME_AAID.getKey(), prefHelper.getAAID());
            post.put(Defines.Jsonkey.LKME_OAID.getKey(), prefHelper.getOAID());
            post.put(Defines.Jsonkey.LKME_UDID.getKey(), prefHelper.getUDID());
            post.put(Defines.Jsonkey.LKME_VAID.getKey(), prefHelper.getVAID());

            post.put(Defines.Jsonkey.LKME_APP_NAME.getKey(), SystemObserver.getInstance(LinkedME.getInstance().getApplicationContext()).getApplicationName());

            post.put(Defines.Jsonkey.LKME_RETRY_TIMES.getKey(), retryNumber);
            post.put(Defines.Jsonkey.LKME_TIMESTAMP.getKey(), System.currentTimeMillis());
            post.put(Defines.Jsonkey.LKME_SIGN.getKey(), MD5Utils.encrypt(post, "qeradszmxcoiusdj"));
        } catch (JSONException ignore) {
        }
        return true;
    }

    /**
     * get 请求通用参数
     */
    private boolean addGetCommonParams(JSONObject post, int retryNumber) {
        try {
            String linkedmeKey = LinkedME.getInstance().getAppKey();
            if (TextUtils.isEmpty(linkedmeKey)) {
                return false;
            }
            post.put(LINKEDME_KEY, LinkedME.getInstance().getAppKey());
            post.put(Defines.Jsonkey.LKME_SDK_VERSION.getKey(), "android" + SDK_VERSION);

            post.put(Defines.Jsonkey.LKME_DEVICE_ID.getKey(), LinkedME.getInstance().getDeviceId());

            post.putOpt(Defines.Jsonkey.LKME_OS.getKey(), SystemObserver.getInstance(LinkedME.getInstance().getApplicationContext()).getOS());

            post.put(Defines.Jsonkey.LKME_APP_NAME.getKey(), SystemObserver.getInstance(LinkedME.getInstance().getApplicationContext()).getApplicationName());

            post.put(Defines.Jsonkey.LKME_TIMESTAMP.getKey(), System.currentTimeMillis());
            post.put(Defines.Jsonkey.LKME_RETRY_TIMES.getKey(), retryNumber);
            post.put(Defines.Jsonkey.LKME_SIGN.getKey(), MD5Utils.encrypt(post, "qeradszmxcoiusdj"));
        } catch (JSONException ignore) {
        }
        return true;
    }


    /**
     * 创建GET请求
     *
     * @param baseUrl     请求的URL字符串{@link String}。
     * @param params      请求参数{@link JSONObject}格式
     * @param tag         用于日志或者分析的标记字符串{@link String}
     * @param timeout     {@link Integer} 类型值,设置请求超时的毫秒数
     * @param retryNumber 重试次数
     * @return {@link ServerResponse} 对象
     */
    private ServerResponse make_restful_get(String baseUrl, JSONObject params, String tag, int timeout, int retryNumber) {
        String modifiedUrl = baseUrl;
        JSONObject getParameters = new JSONObject();
        HttpURLConnection connection = null;
        if (timeout <= 0) {
            timeout = DEFAULT_TIMEOUT;
        }
        if (baseUrl.contains("lm_type=image")) {
            //图片请求，不处理
        } else if (addGetCommonParams(getParameters, retryNumber)) {
            if (params != null) {
                Iterator keys = params.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    try {
                        getParameters.put(key, params.getString(key));
                    } catch (JSONException ignore) {
                    }
                }
            }
            modifiedUrl += this.convertJSONtoString(getParameters);
        } else {
            return new ServerResponse(tag, NO_LINKEDME_KEY_STATUS);
        }

        try {
            LMLogger.info("getting " + modifiedUrl);
            lastRoundTripTime = 0;
            long reqStartTime = System.currentTimeMillis();
            URL urlObject = new URL(modifiedUrl);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                SSLContext sslcontext = SSLContext.getInstance("TLSv1");
                sslcontext.init(null, null, null);
                SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());
                HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);
            }
            connection = (HttpURLConnection) urlObject.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setDoInput(true);
            lastRoundTripTime = (int) (System.currentTimeMillis() - reqStartTime);
            if (LinkedME.getInstance() != null) {
                LinkedME.getInstance().addExtraInstrumentationData(tag + "-" + Defines.Jsonkey.Last_Round_Trip_Time.getKey(), String.valueOf(lastRoundTripTime));
            }

            if (connection.getResponseCode() >= HttpURLConnection.HTTP_INTERNAL_ERROR &&
                    retryNumber < PrefHelper.getInstance(LinkedME.getInstance().getApplicationContext()).getRetryCount()) {
                try {
                    Thread.sleep(PrefHelper.getInstance(LinkedME.getInstance().getApplicationContext()).getRetryInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                retryNumber++;
                return make_restful_get(baseUrl, params, tag, timeout, retryNumber);
            } else {
                try {
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getErrorStream() != null) {
                        return processEntityForJSON(connection.getErrorStream(),
                                connection.getResponseCode(), tag);
                    } else {
                        if (tag.contains("lm_type=image")) {
                            return processEntityForImage(connection, connection.getResponseCode(), tag);
                        } else {
                            return processEntityForJSON(connection.getInputStream(),
                                    connection.getResponseCode(), tag);
                        }
                    }
                } catch (FileNotFoundException ex) {
                    // In case of Resource conflict getInputStream will throw FileNotFoundException. Handle it here in order to send the right status code
                    LMLogger.info("A resource conflict occurred with this request " + tag);
                    return processEntityForJSON(null, connection.getResponseCode(), tag);
                }
            }
        } catch (SocketException ex) {
            LMLogger.info("Http connect exception: " + ex.getMessage());
            return new ServerResponse(tag, NO_CONNECTIVITY_STATUS);
        } catch (SocketTimeoutException ex) {
            // On socket  time out retry the request for retryNumber of times
            if (retryNumber < PrefHelper.getInstance(LinkedME.getInstance().getApplicationContext()).getRetryCount()) {
                try {
                    Thread.sleep(PrefHelper.getInstance(LinkedME.getInstance().getApplicationContext()).getRetryInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                retryNumber++;
                return make_restful_get(baseUrl, params, tag, timeout, retryNumber);
            } else {
                return new ServerResponse(tag, LMErrorCode.ERR_LINKEDME_REQ_TIMED_OUT);
            }
        } catch (UnknownHostException ex) {
            LMLogger.info("Http connect exception: " + ex.getMessage());
            return new ServerResponse(tag, NO_CONNECTIVITY_STATUS);
        } catch (IOException ex) {
            LMLogger.info("IO exception: " + ex.getMessage());
            return new ServerResponse(tag, HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (NoSuchAlgorithmException ex) {
            LMLogger.info("IO exception: " + ex.getMessage());
            return new ServerResponse(tag, HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (KeyManagementException ex) {
            LMLogger.info("IO exception: " + ex.getMessage());
            return new ServerResponse(tag, HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (Exception ex) {
            LMLogger.info("IO exception: " + ex.getMessage());
            return new ServerResponse(tag, HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

    }
    //endregion

    /**
     * 创建POST请求
     *
     * @param body    一个包含请求数据的 {@link JSONObject} 对象。
     * @param url     请求的URL字符串{@link String}。
     * @param tag     用于日志或者分析的标记字符串{@link String}
     * @param timeout {@link Integer} 类型值,设置请求超时的毫秒数
     * @return {@link ServerResponse} 对象
     */
    public ServerResponse make_restful_post(JSONObject body, String url, String tag, int timeout) {
        return make_restful_post(body, url, tag, timeout, 0);
    }

    /**
     * <p>POST 方法，其他方法通过预置一些值调用该方法。</p>
     *
     * @param body    一个包含请求数据的 {@link JSONObject} 对象。
     * @param url     请求的URL字符串{@link String}。
     * @param tag     用于日志或者分析的标记字符串{@link String}
     * @param timeout {@link Integer} 类型值,设置请求超时的毫秒数
     * @return 返回一个{@link ServerResponse} 对象,代表{@link HttpURLConnection}的响应结果
     */
    private ServerResponse make_restful_post(JSONObject body, String url, String tag, int timeout,
                                             int retryNumber) {
        HttpURLConnection connection = null;
        if (timeout <= 0) {
            timeout = DEFAULT_TIMEOUT;
        }
        JSONObject bodyCopy = new JSONObject();
        try {

            Iterator<String> keys = body.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    bodyCopy.put(key, body.get(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (!addPostCommonParams(bodyCopy, retryNumber)) {
                return new ServerResponse(tag, NO_LINKEDME_KEY_STATUS);
            }
            LMLogger.info("posting to " + url);
            LMLogger.info("Post value = " + bodyCopy.toString(4));

            //把json转化成key-value
            Iterator<String> paramKeys = bodyCopy.keys();
            String postParams = "";
            while (paramKeys.hasNext()) {
                String key = paramKeys.next();
                try {
                    postParams = postParams + String.format("%s=%s&", key, URLEncoder.encode(bodyCopy.get(key).toString(), "utf-8"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (postParams.length() > 0) {
                postParams = postParams.substring(0, postParams.length() - 1);  //去掉最后一个&符号
            }

            URL urlObject = new URL(url);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                SSLContext sslcontext = SSLContext.getInstance("TLSv1");
                sslcontext.init(null, null, null);
                SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());
                HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);
            }
            connection = (HttpURLConnection) urlObject.openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept", "application/json");
//            connection.setRequestProperty("Content-Encoding", "gzip");
            connection.setRequestMethod("POST");
            lastRoundTripTime = 0;
            long reqStartTime = System.currentTimeMillis();
            lastRoundTripTime = (int) (System.currentTimeMillis() - reqStartTime);
            if (LinkedME.getInstance() != null) {
                LinkedME.getInstance().addExtraInstrumentationData(tag + "-" + Defines.Jsonkey.Last_Round_Trip_Time.getKey(), String.valueOf(lastRoundTripTime));
            }
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream());

            outputStreamWriter.write(AESCipher.encryptAES(postParams, MD5Utils.md5_key));
            outputStreamWriter.flush();
            outputStreamWriter.close();
            //重试的次数
            if (connection.getResponseCode() >= HttpURLConnection.HTTP_INTERNAL_ERROR
                    && retryNumber < PrefHelper.getInstance(LinkedME.getInstance().getApplicationContext()).getRetryCount()) {
                try {
                    Thread.sleep(PrefHelper.getInstance(LinkedME.getInstance().getApplicationContext()).getRetryInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                retryNumber++;
                return make_restful_post(bodyCopy, url, tag, timeout, retryNumber);
            } else {
                try {
                    if (connection.getResponseCode() != HttpURLConnection.HTTP_OK && connection.getErrorStream() != null) {
                        return processEntityForJSON(connection.getErrorStream(), connection.getResponseCode(), tag);
                    } else {
                        return processEntityForJSON(connection.getInputStream(), connection.getResponseCode(), tag);
                    }
                } catch (FileNotFoundException ex) {
                    // In case of Resource conflict getInputStream will throw FileNotFoundException. Handle it here in order to send the right status code
                    LMLogger.info("A resource conflict occurred with this request " + tag);
                    return processEntityForJSON(null, connection.getResponseCode(), tag);
                }
            }

        } catch (SocketException ex) {
            LMLogger.info("Http connect exception: " + ex.getMessage());
            return new ServerResponse(tag, NO_CONNECTIVITY_STATUS);
        } catch (UnknownHostException ex) {
            LMLogger.info("Http connect exception: " + ex.getMessage());
            return new ServerResponse(tag, NO_CONNECTIVITY_STATUS);
        } catch (SocketTimeoutException ex) {
            // On socket  time out retry the request for retryNumber of times
            if (retryNumber < PrefHelper.getInstance(LinkedME.getInstance().getApplicationContext()).getRetryCount()) {
                try {
                    Thread.sleep(PrefHelper.getInstance(LinkedME.getInstance().getApplicationContext()).getRetryInterval());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                retryNumber++;
                return make_restful_post(bodyCopy, url, tag, timeout, retryNumber);
            } else {
                return new ServerResponse(tag, LMErrorCode.ERR_LINKEDME_REQ_TIMED_OUT);
            }
        } catch (Exception ex) {
            LMLogger.info("Exception: " + ex.getMessage());
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                if (ex instanceof NetworkOnMainThreadException)
                    Log.i(LinkedME.TAG, "LinkedME Error: Don't call our synchronous methods on the main thread!!!");
            }
            return new ServerResponse(tag, HttpURLConnection.HTTP_INTERNAL_ERROR);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    //endregion


    private String convertJSONtoString(JSONObject json) {
        StringBuilder result = new StringBuilder();
        if (json != null) {
            JSONArray names = json.names();
            if (names != null) {
                boolean first = true;
                int size = names.length();
                for (int i = 0; i < size; i++) {
                    try {
                        String key = names.getString(i);

                        if (first) {
                            result.append("?");
                            first = false;
                        } else {
                            result.append("&");
                        }

                        String value = json.getString(key);
                        result.append(key).append("=").append(URLEncoder.encode(value, "utf-8"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        }

        return result.toString();
    }
}
