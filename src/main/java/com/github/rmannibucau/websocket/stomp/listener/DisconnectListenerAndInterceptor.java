package com.github.rmannibucau.websocket.stomp.listener;

import asia.stampy.client.listener.disconnect.AbstractDisconnectListenerAndInterceptor;
import com.github.rmannibucau.websocket.stomp.gateway.WebSocketGateway;

public class DisconnectListenerAndInterceptor extends AbstractDisconnectListenerAndInterceptor<WebSocketGateway> {
    public DisconnectListenerAndInterceptor(final WebSocketGateway gateway, final boolean closeOnDisconnect) {
        setGateway(gateway);
        setCloseOnDisconnectMessage(closeOnDisconnect);
    }
}
