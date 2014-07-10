package com.github.rmannibucau.websocket.stomp;

import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.StampyHandlerHelper;
import asia.stampy.common.gateway.StampyMessageListener;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.parsing.StompMessageParser;
import com.github.rmannibucau.websocket.stomp.gateway.WebSocketGateway;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.util.logging.Level;
import java.util.logging.Logger;

// easier to inherit than annotated version which is more application oriented
// than library oriented
public abstract class BaseStampyEndpoint extends Endpoint {
    private final Logger logger = Logger.getLogger(getClass().getName());

    protected final WebSocketGateway gateway;
    private final StompMessageParser parser = new StompMessageParser();
    private final StampyHandlerHelper helper = new StampyHandlerHelper();

    public BaseStampyEndpoint() {
        this.gateway = new WebSocketGateway(getHearbeat(), getAckTimeout());
        for (final StampyMessageListener listener : getStampyListeners()) { // after having gateway valued
            this.gateway.addMessageListener(listener);
        }
    }

    @Override
    public void onOpen(final Session session, final EndpointConfig config) {
        gateway.open(session);

        final HostPort hostPort = gateway.createHostPort(session);

        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(final String message) {
                gateway.getHeartbeatContainer().reset(hostPort);

                if (helper.isHeartbeat(message)) {
                    return;
                }

                try {
                    final StampyMessage<?> stampyMessage = parser.parseMessage(message);
                    gateway.message(hostPort, stampyMessage);
                } catch (final Exception e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }
        });
    }

    @Override
    public void onError(final Session session, final Throwable throwable) {
        logger.log(Level.SEVERE, throwable == null ? null : throwable.getMessage(), throwable);
    }

    @Override
    public void onClose(final Session session, final CloseReason closeReason) {
        gateway.close(session);
    }

    public int getHearbeat() {
        return 1000;
    }

    public int getAckTimeout() {
        return 200;
    }

    public StampyMessageListener[] getStampyListeners() {
        return new StampyMessageListener[0];
    }
}
