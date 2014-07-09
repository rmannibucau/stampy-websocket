package com.github.rmannibucau.websocket.test.stomp.endpoint.listener;

import asia.stampy.client.message.subscribe.SubscribeHeader;
import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.common.gateway.HostPort;
import asia.stampy.common.message.interceptor.InterceptException;
import asia.stampy.server.message.message.MessageMessage;
import com.github.rmannibucau.websocket.stomp.gateway.WebSocketGateway;
import com.github.rmannibucau.websocket.stomp.listener.BaseSubscribeListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class SubscribeListener extends BaseSubscribeListener {
    private final AtomicLong idGenerator = new AtomicLong(Long.MIN_VALUE);

    // in practise key would be hostport + subscription but fine for a test
    private final Map<HostPort, Sender> threads = new ConcurrentHashMap<>();

    public SubscribeListener(final WebSocketGateway webSocketGateway) {
        super(webSocketGateway);
    }

    @Override
    protected void onSessionClose(final HostPort hostPort) {
        final Sender sender = threads.remove(hostPort);
        if (sender != null) {
            sender.running.set(false);
            try {
                sender.join();
            } catch (final InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

    @Override
    protected void onUnsubscribe(final HostPort hostPort, final UnsubscribeMessage unsubscribe) {
        onSessionClose(hostPort);
    }

    @Override
    protected void onSubscribe(final HostPort hostPort, final SubscribeMessage subscribe) {
        final Sender sender = new Sender(subscribe.getHeader(), hostPort);
        threads.put(hostPort, sender);
        sender.start();
    }

    private class Sender extends Thread {
        private final AtomicBoolean running = new AtomicBoolean(true);
        private final SubscribeHeader headers;
        private final HostPort hostPort;

        private Sender(final SubscribeHeader headers, final HostPort hostPort) {
            this.headers = headers;
            this.hostPort = hostPort;
        }

        @Override
        public void run() {
            int i = 0;
            while (running.get()) {
                try {
                    final String msg = "{ \"message\":\"test " + i++ + "\"}";
                    final MessageMessage pushMessage = new MessageMessage(headers.getDestination(), Long.toString(idGenerator.incrementAndGet()), headers.getId());
                    // real life Message will inherit from MessageMessage and override getObjectArrayAsString(Object body) to return JSon for instance
                    pushMessage.setBodyEncoding("text/json");
                    pushMessage.setBody(msg);
                    pushMessage.getHeader().setContentLength(msg.length());
                    pushMessage.getHeader().setContentType("text/json");
                    gateway.sendMessage(pushMessage, hostPort);
                    if (i > 5) { // don't use CPU that much
                        try {
                            sleep(100);
                        } catch (final InterruptedException e) {
                            Thread.interrupted();
                        }
                    }
                } catch (final InterceptException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
