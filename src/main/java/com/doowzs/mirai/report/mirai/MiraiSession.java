package com.doowzs.mirai.report.mirai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.io.IOException;

// Cannot become @Component due to circular dependencies
public class MiraiSession {

    private final MiraiConfig config;
    private final MiraiHandler handler;

    private final WebSocketClient client = new StandardWebSocketClient();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private WebSocketSession session;

    public MiraiSession(MiraiConfig config, MiraiHandler handler) {
        this.config = config;
        this.handler = handler;
        getSession();
    }

    private void createSession() {
        if (session == null || !session.isOpen()) {
            try {
                String endpoint = String.format("%s/message", config.getApiUrl());
                session = client.doHandshake(handler, endpoint).get();
            } catch (Exception e) {
                logger.error(String.format("Cannot connect to mirai-api-http-ws, %s", e.getMessage()), e);
            }
        }
    }

    public WebSocketSession getSession() {
        if (session == null) {
            createSession();
        }
        return session;
    }

    public void clearSession() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException ignored) {
            }
        }
        session = null;
    }

}
