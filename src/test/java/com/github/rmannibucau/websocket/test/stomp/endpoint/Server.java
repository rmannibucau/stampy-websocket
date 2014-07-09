package com.github.rmannibucau.websocket.test.stomp.endpoint;

import asia.stampy.common.gateway.StampyMessageListener;
import com.github.rmannibucau.websocket.stomp.BaseStampyEndpoint;
import com.github.rmannibucau.websocket.test.stomp.endpoint.listener.SubscribeListener;

import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(
    value = "/touch",
    subprotocols = "stomp"
)
public class Server extends BaseStampyEndpoint {
    @Override
    public StampyMessageListener[] getStampyListeners() {
        return new StampyMessageListener[]{
            //new DebugListener(),
            new SubscribeListener(gateway)
        };
    }
}
