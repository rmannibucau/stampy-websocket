package com.github.rmannibucau.websocket.stomp.listener;

import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import asia.stampy.server.listener.heartbeat.AbstractHeartbeatListener;
import com.github.rmannibucau.websocket.stomp.gateway.WebSocketGateway;

public class HeartbeatListener extends AbstractHeartbeatListener<WebSocketGateway> {
    public HeartbeatListener(final WebSocketGateway webSocketGateway, final StampyHeartbeatContainer heartbeatContainer) {
        setGateway(webSocketGateway);
        setHeartbeatContainer(heartbeatContainer);
    }
}
