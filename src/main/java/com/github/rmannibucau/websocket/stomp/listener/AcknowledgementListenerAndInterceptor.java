package com.github.rmannibucau.websocket.stomp.listener;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.server.listener.subscription.AbstractAcknowledgementListenerAndInterceptor;
import asia.stampy.server.listener.subscription.StampyAcknowledgementHandler;
import com.github.rmannibucau.websocket.stomp.gateway.OnCloseListener;
import com.github.rmannibucau.websocket.stomp.gateway.WebSocketGateway;

import javax.websocket.Session;

public class AcknowledgementListenerAndInterceptor extends AbstractAcknowledgementListenerAndInterceptor<WebSocketGateway> {
    public AcknowledgementListenerAndInterceptor(final WebSocketGateway webSocketGateway,
                                                 final StampyAcknowledgementHandler handler,
                                                 final int ackTimeout) {
        setGateway(webSocketGateway);
        setHandler(handler);
        setAckTimeoutMillis(ackTimeout);
    }

    @Override
    protected void ensureCleanup() {
        getGateway().addCloseListener(new OnCloseListener() {
            @Override
            public void close(final Session session) {
                final HostPort hostPort = getGateway().createHostPort(session);
                if (messages.containsKey(hostPort)) {
                    messages.remove(hostPort);
                }
            }
        });
    }
}
