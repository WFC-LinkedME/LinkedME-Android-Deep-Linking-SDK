package com.microquation.linkedme.android.network.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.microquation.linkedme.android.log.LMLogger;
import com.microquation.linkedme.android.network.ServerRequestRegisterInstall;
import com.microquation.linkedme.android.network.ServerRequestRegisterOpen;
import com.microquation.linkedme.android.network.ServerRequestValidate;
import com.microquation.linkedme.android.util.Defines;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * <p>The LinkedME SDK can queue up requests whilst it is waiting for initialization of a session
 * to complete. This allows you to start sending requests to the LinkedME API as soon as your app is
 * opened.</p>
 */
public class ServerRequestQueue {
    private static final String PREF_KEY = "LKMEServerRequestQueue";
    private static final int MAX_ITEMS = 25;
    private volatile static ServerRequestQueue SharedInstance;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private final List<ServerRequest> queue;

    /**
     * <p>Singleton method to return the pre-initialised, or newly initialise and return, a
     * singleton object of the type {@link ServerRequestQueue}.</p>
     *
     * @param c A {@link Context} from which this call was made.
     * @return An initialised {@link ServerRequestQueue} object, either fetched from a
     * pre-initialised instance within the singleton class, or a newly instantiated object where one
     * was not already requested during the current app lifecycle.
     */
    public static ServerRequestQueue getInstance(Context c) {
        if (SharedInstance == null) {
            synchronized (ServerRequestQueue.class) {
                if (SharedInstance == null) {
                    SharedInstance = new ServerRequestQueue(c);
                }
            }
        }
        return SharedInstance;
    }

    /**
     * <p>The main constructor of the ServerRequestQueue class is private because the class uses
     * the Singleton pattern.</p>
     *
     * @param c A {@link Context} from which this call was made.
     */
    @SuppressLint("CommitPrefEdits")
    private ServerRequestQueue(Context c) {
        sharedPref = c.getSharedPreferences("LKME_Server_Request_Queue", Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        queue = retrieve(c);
    }

    /**
     * 将队列中的任务以json数组字符串的形格式保存到SharedPreference中
     */
    private void persist() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (queue) {
                        JSONArray jsonArr = new JSONArray();
                        for (ServerRequest aQueue : queue) {
                            JSONObject json = aQueue.toJSON();
                            if (json != null) {
                                jsonArr.put(json);
                            }
                        }
                        boolean succeeded = false;
                        try {
                            editor.putString(PREF_KEY, jsonArr.toString()).commit();
                            succeeded = true;
                        } catch (ConcurrentModificationException ex) {
                            LMLogger.info("Persisting Queue: Failed to persit queue " + ex.getMessage());
                        } finally {
                            if (!succeeded) {
                                try {
                                    editor.putString(PREF_KEY, jsonArr.toString()).commit();
                                } catch (ConcurrentModificationException ignored) {
                                }
                            }
                        }

                    }
                } catch (Exception ignore) {
                    LMLogger.debugExceptionError(ignore);
                }
            }
        }).start();
    }

    /**
     * <p>创建一个同步(线程安全)的请求列表,同时将SP文件中存储的请求任务添加到请求列表中</p>
     * <p>特别注意！应用打开或者从后台调到前台时，该方法均除去了存储在SP文件中的close接口，否则会造成创建深度链接不成功的情况</p>
     *
     * @param context {@link Context}
     * @return 请求列表
     */
    private List<ServerRequest> retrieve(Context context) {
        List<ServerRequest> result = Collections.synchronizedList(new LinkedList<ServerRequest>());
        String jsonStr = sharedPref.getString(PREF_KEY, null);

        if (jsonStr != null) {
            try {
                JSONArray jsonArr = new JSONArray(jsonStr);
                for (int i = 0; i < Math.min(jsonArr.length(), MAX_ITEMS); i++) {
                    JSONObject json = jsonArr.getJSONObject(i);
                    ServerRequest req = ServerRequest.fromJSON(json, context);
//                    if (req != null) {
                    if (req != null && !ServerRequestValidate.isRegisterClose(req)) {
                        result.add(req);
                    }
                }
            } catch (JSONException ignored) {
            }
        }

        return result;
    }

    /**
     * <p>Gets the number of {@link ServerRequest} objects currently queued up for submission to
     * the LinkedME API.</p>
     *
     * @return An {@link Integer} value indicating the current size of the {@link List} object that
     * forms the logical queue for the class.
     */
    public int getSize() {
        return queue.size();
    }

    /**
     * <p>Adds a {@link ServerRequest} object to the queue.</p>
     *
     * @param request The {@link ServerRequest} object to add to the queue.
     */
    public void enqueue(ServerRequest request) {
        if (request != null) {
            queue.add(request);
            if (getSize() >= MAX_ITEMS) {
                queue.remove(1);
            }
            persist();
        }
    }

    /**
     * <p>Removes the queued {@link ServerRequest} object at position with index 0 within the
     * queue, and returns it as a result.</p>
     *
     * @return The {@link ServerRequest} object at position with index 0 within the queue.
     */
    public ServerRequest dequeue() {
        ServerRequest req = null;
        try {
            req = queue.remove(0);
            persist();
        } catch (IndexOutOfBoundsException ignored) {
        } catch (NoSuchElementException ignored) {
        }
        return req;
    }

    /**
     * <p>Gets the queued {@link ServerRequest} object at position with index 0 within the queue,
     * but unlike {@link #dequeue()}, does not remove it from the queue.</p>
     *
     * @return The {@link ServerRequest} object at position with index 0 within the queue.
     */
    public ServerRequest peek() {
        ServerRequest req = null;
        try {
            req = queue.get(0);
        } catch (IndexOutOfBoundsException ignored) {
        } catch (NoSuchElementException ignored) {
        }
        return req;
    }

    /**
     * <p>Gets the queued {@link ServerRequest} object at position with index specified in the
     * supplied parameter, within the queue. Like {@link #peek()}, the item is not removed from the
     * queue.</p>
     *
     * @param index An {@link Integer} that specifies the position within the queue from which to
     *              pull the {@link ServerRequest} object.
     * @return The {@link ServerRequest} object at the specified index. Returns null if no request
     * exists at that position, or if the index supplied is not valid, for instance if {@link
     * #getSize()} is 6 and index 6 is called.
     */
    public ServerRequest peekAt(int index) {
        ServerRequest req = null;
        try {
            req = queue.get(index);
        } catch (IndexOutOfBoundsException ignored) {
        } catch (NoSuchElementException ignored) {
        }
        return req;
    }

    /**
     * <p>As the method name implies, inserts a {@link ServerRequest} into the queue at the index
     * position specified.</p>
     *
     * @param request The {@link ServerRequest} to insert into the queue.
     * @param index   An {@link Integer} value specifying the index at which to insert the supplied
     *                {@link ServerRequest} object. Fails silently if the index supplied is
     *                invalid.
     */
    public void insert(ServerRequest request, int index) {
        try {
            if (queue.size() < index) {
                index = queue.size();
            }
            queue.add(index, request);
            persist();
        } catch (IndexOutOfBoundsException ignored) {
        }
    }

    /**
     * <p>As the method name implies, removes the {@link ServerRequest} object, at the position
     * indicated by the {@link Integer} parameter supplied.</p>
     *
     * @param index An {@link Integer} value specifying the index at which to remove the {@link
     *              ServerRequest} object. Fails silently if the index supplied is invalid.
     * @return The {@link ServerRequest} object being removed.
     */
    public ServerRequest removeAt(int index) {
        ServerRequest req = null;
        try {
            req = queue.remove(index);
            persist();
        } catch (IndexOutOfBoundsException ignored) {
        }
        return req;
    }

    /**
     * <p>As the method name implies, removes {@link ServerRequest} supplied in the parameter if it
     * is present in the queue.</p>
     *
     * @param request The {@link ServerRequest} object to be removed from the queue.
     * @return A {@link Boolean} whose value is true if the object is removed.
     */
    public boolean remove(ServerRequest request) {
        boolean isRemoved = false;
        try {
            isRemoved = queue.remove(request);
            persist();
        } catch (UnsupportedOperationException ignored) {
        }
        return isRemoved;
    }

    /**
     * <p> Clears all pending requests in the queue </p>
     */
    public void clear() {
        try {
            queue.clear();
            persist();
        } catch (UnsupportedOperationException ignored) {
        }
    }

    /**
     * <p>Determines whether the queue contains a session/app close request.</p>
     *
     * @return A {@link Boolean} value indicating whether or not the queue contains a session close
     * request. <i>True</i> if the queue contains a close request, <i>False</i> if not.
     */
    public boolean containsClose() {
        synchronized (queue) {
            for (ServerRequest req : queue) {
                if (req != null &&
                        req.getRequestPath().equals(Defines.RequestPath.RegisterClose.getPath())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>Determines whether the queue contains an install/register request.</p>
     *
     * @return A {@link Boolean} value indicating whether or not the queue contains an
     * install/register request. <i>True</i> if the queue contains a close request, <i>False</i> if
     * not.
     */
    public boolean containsInstallOrOpen() {
        synchronized (queue) {
            for (ServerRequest req : queue) {
                if (req != null &&
                        ((req instanceof ServerRequestRegisterInstall) || req instanceof ServerRequestRegisterOpen)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * <p>Moves any {@link ServerRequest} of type {@link ServerRequestRegisterInstall}
     * or {@link ServerRequestRegisterOpen} to the front of the queue.</p>
     *
     * @param request       A {@link ServerRequest} of type open or install which need to be moved
     *                      to the front of the queue.
     * @param hasRequesting An {@link Integer} value that indicates whether or not to insert the
     *                      request at the front of the queue or not.
     */
    public void moveInstallOrOpenToFront(ServerRequest request, boolean hasRequesting) {
        synchronized (queue) {
            Iterator<ServerRequest> iter = queue.iterator();
            while (iter.hasNext()) {
                ServerRequest req = iter.next();
                if (req != null && (req instanceof ServerRequestRegisterInstall || req instanceof ServerRequestRegisterOpen)) {
                    //Remove all install or open in queue. Since this method is called each time on Install/open there will be only one
                    //instance of open/Install in queue. So we can break as we see the first open/install
                    iter.remove();
                    break;
                }
            }
        }

        if (hasRequesting) {
            // TODO: 2019-10-11 lipeng 如果正在发生的请求不需要持久化（参见ServerRequest的persist），则并不能将该请求提到最前面
            insert(request, 1);
        } else {
            insert(request, 0);
        }
    }
}
