package com.github.rmannibucau.websocket.stomp.gateway;

import javax.websocket.Session;

public interface OnCloseListener {
    void close(Session session);
}
