package com.github.rmannibucau.websocket.stomp.listener;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.SecurityMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;

public class NoSecurityListener implements SecurityMessageListener {
    @Override
    public StompMessageType[] getMessageTypes() {
        return null;
    }

    @Override
    public boolean isForMessage(final StampyMessage<?> message) {
        return false;
    }

    @Override
    public void messageReceived(final StampyMessage<?> message, final HostPort hostPort) throws Exception {
        // no-op
    }
}
