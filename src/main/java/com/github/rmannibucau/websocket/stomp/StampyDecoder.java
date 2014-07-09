package com.github.rmannibucau.websocket.stomp;

import asia.stampy.common.gateway.StampyHandlerHelper;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.parsing.StompMessageParser;
import asia.stampy.common.parsing.UnparseableException;

import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

public class StampyDecoder implements Decoder.Text<StampyMessage<?>> {
    private final StompMessageParser parser = new StompMessageParser();
    private final StampyHandlerHelper helper = new StampyHandlerHelper();

    @Override
    public StampyMessage<?> decode(final String s) throws DecodeException {
        try {
            return parser.parseMessage(s);
        } catch (final UnparseableException e) {
            throw new DecodeException(s, e.getMessage(), e);
        }
    }

    @Override
    public boolean willDecode(final String s) {
        return !helper.isHeartbeat(s);
    }

    @Override
    public void init(final EndpointConfig endpointConfig) {
        // no-op
    }

    @Override
    public void destroy() {
        // no-op
    }
}
