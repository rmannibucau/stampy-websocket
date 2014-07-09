package com.github.rmannibucau.websocket.stomp.listener;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.StompMessageType;

import java.util.logging.Logger;

public class DebugListener implements StampyMessageListener {
    private final Logger logger;

    public DebugListener() {
        this(Logger.getLogger(DebugListener.class.getName()));
    }

    public DebugListener(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public StompMessageType[] getMessageTypes() {
        return StompMessageType.values();
    }

    @Override
    public boolean isForMessage(final StampyMessage<?> message) {
        return true;
    }

    @Override
    public void messageReceived(final StampyMessage<?> message, final HostPort hostPort) throws Exception {
        logger.info("Received for: " + hostPort.toString() + "\n" + message.toStompMessage(false));
    }
}
