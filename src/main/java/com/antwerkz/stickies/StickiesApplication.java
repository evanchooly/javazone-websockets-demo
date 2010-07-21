package com.antwerkz.stickies;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.grizzly.websockets.DataFrame;
import com.sun.grizzly.websockets.WebSocket;
import com.sun.grizzly.websockets.WebSocketApplication;
import com.sun.grizzly.websockets.WebSocketEngine;

public class StickiesApplication extends WebSocketApplication {
    static final Logger logger = Logger.getLogger(WebSocketEngine.WEBSOCKET);

    public void onMessage(WebSocket socket, DataFrame frame) throws IOException {
        final String data = frame.getTextPayload();
        System.out.println("StickiesApplication.onMessage: data = " + data);
        broadcast(socket, data);
    }

    public void onConnect(WebSocket socket) {
    }

    private void broadcast(WebSocket original, String text) throws IOException {
        logger.info("Broadcasting : " + text);
        for (WebSocket webSocket : getWebSockets()) {
            if(!webSocket.equals(original)) {
                send(webSocket, text);
            }
        }

    }

    private void send(WebSocket socket, String text) throws IOException {
        try {
            socket.send(text);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Removing chat client: " + e.getMessage(), e);
            onClose(socket);
        }
    }
}