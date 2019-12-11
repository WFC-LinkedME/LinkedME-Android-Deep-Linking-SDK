package com.microquation.linkedme.android.util;

import android.text.TextUtils;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Copy from renyang@linkedme.cc on 16-3-7.
 */
public class MD5Utils {
    public static final String md5_key = "linkedme2017nble";
    private static ThreadLocal<MessageDigest> MD5 = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (Exception ignored) {
            }
            return null;
        }
    };


    private static String md5(String src) {
        return md5(src.getBytes());
    }


    private static String md5(byte[] bytes) {
        if (bytes.length == 0) {
            return "";
        } else {
            MessageDigest md5 = MD5.get();
            md5.reset();
            md5.update(bytes);
            byte[] digest = md5.digest();
            return encodeHex(digest);
        }
    }

    private static String encodeHex(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length + bytes.length);
        for (byte b : bytes) {
            if (((int) b & 0xff) < 0x10) {
                buf.append("0");
            }
            buf.append(Long.toString((int) b & 0xff, 16));
        }
        return buf.toString();
    }

    public static String encrypt(JSONObject json, String key) {
        List<String> values = new ArrayList<>();
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            values.add(json.opt(keys.next()).toString());
        }
        Collections.sort(values);
        return md5(TextUtils.join("&", values) + key);
    }

    public static String encrypt(String input) {
        return md5(input);
    }

}
