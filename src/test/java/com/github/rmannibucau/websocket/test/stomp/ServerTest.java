package com.github.rmannibucau.websocket.test.stomp;

import asia.stampy.client.message.connect.ConnectMessage;
import asia.stampy.client.message.disconnect.DisconnectMessage;
import asia.stampy.client.message.subscribe.SubscribeMessage;
import asia.stampy.client.message.unsubscribe.UnsubscribeMessage;
import asia.stampy.common.gateway.AbstractStampyMessageGateway;
import asia.stampy.common.message.StampyMessage;
import com.github.rmannibucau.websocket.stomp.BaseStampyEndpoint;
import com.github.rmannibucau.websocket.stomp.StampyDecoder;
import com.github.rmannibucau.websocket.test.stomp.endpoint.Server;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static javax.websocket.CloseReason.CloseCodes.GOING_AWAY;
import static org.apache.ziplock.JarLocation.jarLocation;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class ServerTest {
    @Deployment(testable = false)
    public static Archive<?> war() {
        return ShrinkWrap.create(WebArchive.class, "server.war")
                .addPackages(true, BaseStampyEndpoint.class.getPackage()) // lib
                .addPackages(true, Server.class.getPackage()) // tests endpoints
                .addAsLibraries(
                    // stampy
                    jarLocation(StampyMessage.class), jarLocation(AbstractStampyMessageGateway.class));
    }

    @ArquillianResource
    private URL context;

    @Test
    public void manualStompClient() throws Exception { // ie not a Stampy one
        final WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        final Semaphore latch = new Semaphore(0);
        final Session session = container.connectToServer(
                new Client(latch),
                new URI("ws", null, context.getHost(), context.getPort(), context.getPath() + "touch", null, null));

        final RemoteEndpoint.Basic remote = session.getBasicRemote();

        remote.sendText(new ConnectMessage("1.2", context.getHost()).toStompMessage(false));

        final String id = UUID.randomUUID().toString();
        remote.sendText(new SubscribeMessage("/endpoint", id).toStompMessage(false));
        assertTrue(latch.tryAcquire(1 + 10, 1, TimeUnit.MINUTES));  // CONNECTED + n MESSAGEs
        remote.sendText(new UnsubscribeMessage(id).toStompMessage(false));

        final DisconnectMessage disconnectMessage = new DisconnectMessage();
        disconnectMessage.getHeader().setReceipt("end");
        remote.sendText(disconnectMessage.toStompMessage(false));
        assertTrue(latch.tryAcquire(1, 1, TimeUnit.MINUTES));  // RECEIPT

        session.close(new CloseReason(GOING_AWAY, "bye"));
        assertTrue(latch.tryAcquire(1, 1, TimeUnit.MINUTES));  // server closes the connection
    }

    @ClientEndpoint(
        subprotocols = "stomp",
        decoders = StampyDecoder.class
    )
    public static class Client {
        private final Semaphore latch;

        public Client(final Semaphore latch) {
            this.latch = latch;
        }

        @OnMessage
        public void onMessage(final StampyMessage<?> msg) {
            assertNotNull(msg);
            latch.release();
        }

        @OnClose
        public void close() {
            latch.release();
        }
    }
}
