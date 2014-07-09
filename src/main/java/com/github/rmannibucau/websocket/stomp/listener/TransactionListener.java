package com.github.rmannibucau.websocket.stomp.listener;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.server.listener.transaction.AbstractTransactionListener;
import com.github.rmannibucau.websocket.stomp.gateway.OnCloseListener;
import com.github.rmannibucau.websocket.stomp.gateway.WebSocketGateway;

import javax.websocket.Session;

public class TransactionListener extends AbstractTransactionListener<WebSocketGateway> {
    public TransactionListener(final WebSocketGateway webSocketGateway) {
        setGateway(webSocketGateway);
    }

    @Override
    protected void ensureCleanup() {
        getGateway().addCloseListener(new OnCloseListener() {
            @Override
            public void close(final Session session) {
                final HostPort hostPort = getGateway().createHostPort(session);
                if (activeTransactions.containsKey(hostPort)) {
                    activeTransactions.remove(hostPort);
                }
            }
        });
    }
}
