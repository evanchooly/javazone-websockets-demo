package com.antwerkz.stickies;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.grizzly.comet.CometContext;
import com.sun.grizzly.comet.CometEngine;
import com.sun.grizzly.comet.CometEvent;
import com.sun.grizzly.comet.CometHandler;
import com.sun.grizzly.websockets.WebSocketEngine;

public class LongPollingServlet extends HttpServlet {
    private String contextPath;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        contextPath = config.getServletContext().getContextPath() + "/long_polling";
        CometEngine.getEngine().register(contextPath).setExpirationDelay(5 * 30 * 1000);
        WebSocketEngine.getEngine().register("/stickies", new StickiesApplication());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        StickyHandler handler = new StickyHandler();
        handler.attach(response);
        CometContext context = CometEngine.getEngine().getCometContext(contextPath);
        context.addCometHandler(handler);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        CometContext<?> context = CometEngine.getEngine().getCometContext(contextPath);
        context.notify(null);
//        PrintWriter writer = response.getWriter();
//        writer.write("success");
//        writer.flush();
    }

    private class StickyHandler implements CometHandler<HttpServletResponse> {
        private HttpServletResponse response;

        public void write(String text) throws IOException {
            PrintWriter writer = response.getWriter();
            writer.write(text);
            writer.flush();
        }

        public void attach(HttpServletResponse attachment) {
            response = attachment;
        }

        public void onEvent(CometEvent event) throws IOException {
            write("LongPollingServlet$StickyHandler.onEvent: event = " + event);
        }

        public void onInitialize(CometEvent event) throws IOException {
            write("LongPollingServlet$StickyHandler.onInitialize: event = " + event);
        }

        public void onTerminate(CometEvent event) throws IOException {
            write("LongPollingServlet$StickyHandler.onTerminate: event = " + event);
        }

        public void onInterrupt(CometEvent event) throws IOException {
            write("LongPollingServlet$StickyHandler.onInterrupt: event = " + event);
        }
    }
}
