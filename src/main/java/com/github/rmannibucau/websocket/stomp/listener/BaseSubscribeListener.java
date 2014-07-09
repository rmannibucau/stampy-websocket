package com.github.rmannibucau.websocket.stomp.listener;

import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;
import com.github.rmannibucau.websocket.stomp.gateway.OnCloseListener;
import com.github.rmannibucau.websocket.stomp.gateway.WebSocketGateway;

import javax.websocket.Session;

public abstract class BaseSubscribeListener implements StampyMessageListener {
    private static final StompMessageType[] STOMP_MESSAGE_TYPES = new StompMessageType[]{
        StompMessageType.SUBSCRIBE, StompMessageType.UNSUBSCRIBE
    };

    protected final WebSocketGateway gateway;

    public BaseSubscribeListener(final WebSocketGateway webSocketGateway) {
        gateway = webSocketGateway;
        gateway.addCloseListener(new OnCloseListener() {
            @Override
            public void close(final Session session) {
                final HostPort hostPort = gateway.createHostPort(session);
                onSessionClose(hostPort);
            }
        });
    }

    @Override
    public StompMessageType[] getMessageTypes() {
        return STOMP_MESSAGE_TYPES;
    }

    @Override
    public boolean isForMessage(final StampyMessage<?> message) {
        final StompMessageType type = message.getMessageType();
        return type == STOMP_MESSAGE_TYPES[0] || type == STOMP_MESSAGE_TYPES[1];
    }

    @Override
    public void messageReceived(final StampyMessage<?> message, final HostPort hostPort) throws Exception {
        switch (message.getMessageType()) {
            case SUBSCRIBE:
                onSubscribe(hostPort, SubscribeMessage.class.cast(message));
                break;

            case UNSUBSCRIBE:
                onUnsubscribe(hostPort, UnsubscribeMessage.class.cast(message));
                break;

            default:
        }
    }

    protected abstract void onUnsubscribe(final HostPort hostPort, final UnsubscribeMessage unsubscribe);
    protected abstract void onSubscribe(final HostPort hostPort, final SubscribeMessage subscribe);
    protected abstract void onSessionClose(HostPort hostPort);
}
