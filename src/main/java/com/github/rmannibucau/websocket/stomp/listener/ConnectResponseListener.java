package com.github.rmannibucau.websocket.stomp.listener;

import asia.stampy.server.listener.connect.AbstractConnectResponseListener;
import com.github.rmannibucau.websocket.stomp.gateway.WebSocketGateway;

public class ConnectResponseListener extends AbstractConnectResponseListener<WebSocketGateway> {
    public ConnectResponseListener(final WebSocketGateway webSocketGateway) {
        setGateway(webSocketGateway);
    }
}
