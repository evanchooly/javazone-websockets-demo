package com.antwerkz.stickies;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.sun.grizzly.websockets.WebSocketEngine;

public class WebSocketsServlet extends HttpServlet {
    @Override
    public void init(ServletConfig config) throws ServletException {
        WebSocketEngine.getEngine().register(new StickiesApplication());
    }
}
