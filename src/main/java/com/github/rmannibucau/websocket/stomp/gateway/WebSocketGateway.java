package com.github.rmannibucau.websocket.stomp.gateway;

import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.gateway.MessageListenerHaltException;
import asia.stampy.common.gateway.StampyHandlerHelper;
import asia.stampy.common.heartbeat.HeartbeatContainer;
import asia.stampy.common.heartbeat.StampyHeartbeatContainer;
import asia.stampy.common.message.StampyMessage;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.server.listener.validate.ServerMessageValidationListener;
import asia.stampy.server.listener.version.VersionListener;
import com.github.rmannibucau.websocket.stomp.handler.NoopStampyAcknowledgementHandler;
import com.github.rmannibucau.websocket.stomp.listener.AcknowledgementListenerAndInterceptor;
import com.github.rmannibucau.websocket.stomp.listener.ConnectResponseListener;
import com.github.rmannibucau.websocket.stomp.listener.ConnectStateListener;
import com.github.rmannibucau.websocket.stomp.listener.HeartbeatListener;
import com.github.rmannibucau.websocket.stomp.listener.NoSecurityListener;
import com.github.rmannibucau.websocket.stomp.listener.ReceiptListener;
import com.github.rmannibucau.websocket.stomp.listener.TransactionListener;

import javax.websocket.CloseReason;
import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketGateway extends AbstractStampyMessageGateway {
    private final StampyHeartbeatContainer heartbeatContainer = new HeartbeatContainer();
    private final Map<HostPort, Session> sessions = new ConcurrentHashMap<>();
    private final StampyHandlerHelper helper = new StampyHandlerHelper();
    private final Collection<OnCloseListener> closeListeners = new ArrayList<>();

    public WebSocketGateway(final int heartbeat, final int ackTimeout) {
        setAutoShutdown(false);
        setHeartbeat(heartbeat);

        final AcknowledgementListenerAndInterceptor acknowledgementListenerAndInterceptor =
            new AcknowledgementListenerAndInterceptor(this, new NoopStampyAcknowledgementHandler(), ackTimeout);

        addMessageListener(new NoSecurityListener());
        addMessageListener(new ServerMessageValidationListener());
        addMessageListener(new VersionListener());
        addMessageListener(new ConnectStateListener(this));
        addMessageListener(new HeartbeatListener(this, heartbeatContainer));
        addMessageListener(new TransactionListener(this));
        addMessageListener(acknowledgementListenerAndInterceptor);
        addMessageListener(new ReceiptListener(this));
        addMessageListener(new ConnectResponseListener(this));

        addOutgoingMessageInterceptor(acknowledgementListenerAndInterceptor);
    }

    public void addCloseListener(final OnCloseListener onCloseListener) {
        closeListeners.add(onCloseListener);
    }

    public void open( final Session session) {
        final HostPort hostPort = createHostPort(session);
        sessions.put(hostPort, session);
    }

    public void message(final HostPort hostPort, final StampyMessage<?> stampy) throws Exception {
        try {
            notifyMessageListeners(stampy, hostPort);
        } catch (final MessageListenerHaltException e) {
            // halting
        } catch (final Exception e) {
            helper.handleUnexpectedError(hostPort, stampy.toStompMessage(false), stampy, e);
        }
    }

    public void close(final Session session) {
        final HostPort hostPort = createHostPort(session);
        sessions.remove(hostPort);
        for (final OnCloseListener listener : closeListeners) {
            listener.close(session);
        }
    }

    public HostPort createHostPort(final Session session) {
        final String sessionId = session.getId();
        return new HostPort(session.getId(), -1) {
            @Override
            public String toString() {
                return sessionId;
            }
        };
    }

    @Override
    public void broadcastMessage(final String stompMessage) throws InterceptException {
        for (final HostPort s : sessions.keySet()) {
            sendMessage(stompMessage, s);
        }
    }

    @Override
    public void sendMessage(final String stompMessage, final HostPort hostPort) throws InterceptException {
        final Session session = sessions.get(hostPort);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(stompMessage);
            } catch (final IOException e) {
                throw new InterceptException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void closeConnection(final HostPort hostPort) {
        final Session session = sessions.get(hostPort);
        if (session != null && session.isOpen()) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "bye"));
            } catch (final IOException e) {
                // no-op
            }
        }
    }

    @Override
    public void connect() throws Exception {
        // no-op
    }

    @Override
    public void shutdown() throws Exception {
        for (final Session s : sessions.values()) {
            s.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "shutdown"));
        }
        sessions.clear();
    }

    @Override
    public boolean isConnected(final HostPort hostPort) {
        return sessions.containsKey(hostPort);
    }

    @Override
    public Set<HostPort> getConnectedHostPorts() {
        return sessions.keySet();
    }

    public StampyHeartbeatContainer getHeartbeatContainer() {
        return heartbeatContainer;
    }
}
