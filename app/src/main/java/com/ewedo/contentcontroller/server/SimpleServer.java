package com.ewedo.contentcontroller.server;

import com.ewedo.contentcontroller.bean.SimpleResponse;
import com.ewedo.libserver.NanoHTTPD;
import com.google.gson.Gson;

/**
 * Created by fozei on 17-11-3.
 */

public class SimpleServer extends NanoHTTPD {
    private SimpleResponse response;

    public SimpleServer(String hostname, int port) {

        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Gson gson = new Gson();

        if (response == null) {
            response = new SimpleResponse();
            response.setState(400);
            response.setMessage("NO RESPONSE");
            return newFixedLengthResponse(gson.toJson(response));
        }

        return newFixedLengthResponse(gson.toJson(response));
    }

    public void setResponse(SimpleResponse response) {
        this.response = response;
    }
}
