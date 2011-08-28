package com.antwerkz.stickies;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.websockets.WebSocket;
import com.sun.grizzly.websockets.WebSocketApplication;
import com.sun.grizzly.websockets.WebSocketEngine;

public class StickiesApplication extends WebSocketApplication {
    static final Logger logger = Logger.getLogger(WebSocketEngine.WEBSOCKET);
    public static final AtomicInteger ids = new AtomicInteger(0);
    private Map<String, Note> notes = new HashMap<String, Note>();

    @Override
    public void onMessage(WebSocket socket, String frame) {
        final String[] bits = frame.split("-");
        Operations.valueOf(bits[0].toUpperCase()).accept(this, socket, bits);
    }

    @Override
    public void onConnect(WebSocket socket) {
        super.onConnect(socket);
        for (Note note : notes.values()) {
            socket.send("create-" + note.toString());
        }
    }

    @Override
    public boolean isApplicationRequest(Request request) {
        return request.requestURI().equals("/stickies");
    }

    private void broadcast(WebSocket original, String text) {
        for (WebSocket webSocket : getWebSockets()) {
            if (!webSocket.equals(original)) {
                send(webSocket, text);
            }
        }

    }

    private void send(WebSocket socket, String text) {
        socket.send(text);
    }

    private void createNote(WebSocket socket, String[] params) {
        Note note = new Note();
        notes.put(note.getId(), note);
        broadcast(null, "create-" + note.toString());
    }

    private void saveNote(WebSocket socket, String[] params) {
        String[] pieces = params[1].split(",");
        Map<String, String> map = new HashMap<String, String>();
        for (String s : pieces) {
            String[] data = s.split(":");
            map.put(data[0], data.length == 2 ? data[1] : "");
        }
        Note note = notes.get(map.get("id"));
        note.setText(map.get("text"));
        note.setTimestamp(String.valueOf(System.currentTimeMillis()));
        note.setLeft(map.get("left"));
        note.setTop(map.get("top"));
        note.setzIndex(map.get("zIndex"));
        broadcast(socket, "save-" + note.toString());
    }

    private void deleteNote(WebSocket socket, String[] params) {
        notes.remove(params[1]);
        broadcast(socket, "delete-" + params[1]);
    }

    enum Operations {
        CREATE {
            @Override
            void accept(StickiesApplication app, WebSocket socket, String[] params) {
                app.createNote(socket, params);
            }
        },
        SAVE {
            @Override
            void accept(StickiesApplication app, WebSocket socket, String[] params) {
                app.saveNote(socket, params);
            }
        },
        DELETE {
            @Override
            void accept(StickiesApplication app, WebSocket socket, String[] params) {
                app.deleteNote(socket, params);
            }
        };

        abstract void accept(StickiesApplication app, WebSocket socket, String[] params);
    }
}