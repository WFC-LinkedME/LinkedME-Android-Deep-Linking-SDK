package com.microquation.linkedme.android.network;

import com.microquation.linkedme.android.network.base.ServerRequest;
import com.microquation.linkedme.android.network.base.ServerResponse;

public class ReqRespEntry {

    public static final int REQ_OPEN = 10001;
    public static final int RESP_OPEN = 10002;

    private ServerRequest serverRequest;

    public ServerRequest getServerRequest() {
        return serverRequest;
    }

    public void setServerRequest(ServerRequest serverRequest) {
        this.serverRequest = serverRequest;
    }

    public ServerResponse getServerResponse() {
        return serverResponse;
    }

    public void setServerResponse(ServerResponse serverResponse) {
        this.serverResponse = serverResponse;
    }

    private ServerResponse serverResponse;
}
