package com.github.rmannibucau.websocket.stomp.listener;

import asia.stampy.server.listener.receipt.AbstractReceiptListener;
import com.github.rmannibucau.websocket.stomp.gateway.WebSocketGateway;

public class ReceiptListener extends AbstractReceiptListener<WebSocketGateway> {
    public ReceiptListener(final WebSocketGateway webSocketGateway) {
        setGateway(webSocketGateway);
    }
}
