package com.github.rmannibucau.websocket.stomp.listener;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.server.listener.connect.AbstractConnectStateListener;
import com.github.rmannibucau.websocket.stomp.gateway.OnCloseListener;
import com.github.rmannibucau.websocket.stomp.gateway.WebSocketGateway;

import javax.websocket.Session;

public class ConnectStateListener extends AbstractConnectStateListener<WebSocketGateway> {
    public ConnectStateListener(final WebSocketGateway webSocketGateway) {
        setGateway(webSocketGateway);
    }

    @Override
    protected void ensureCleanup() {
        getGateway().addCloseListener(new OnCloseListener() {
            @Override
            public void close(final Session session) {
                final HostPort hostPort = getGateway().createHostPort(session);
                if (connectedClients.contains(hostPort)) {
                    connectedClients.remove(hostPort);
                }
            }
        });
    }
}
