package com.github.rmannibucau.websocket.stomp.handler;

import asia.stampy.server.listener.subscription.StampyAcknowledgementHandler;

public class NoopStampyAcknowledgementHandler implements StampyAcknowledgementHandler {
    @Override
    public void ackReceived(final String id, final String receipt, final String transaction) throws Exception {
        // no-op
    }

    @Override
    public void nackReceived(final String id, final String receipt, final String transaction) throws Exception {
        // no-op
    }

    @Override
    public void noAcknowledgementReceived(final String id) {
        // no-op
    }
}
