package com.antwerkz.stickies;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import javax.servlet.http.HttpServletResponse;

import com.antwerkz.stickies.LongPollingServlet.CometOperations;
import com.sun.grizzly.comet.CometEvent;
import com.sun.grizzly.comet.CometHandler;

public class StickyHandler implements CometHandler<HttpServletResponse> {
    private HttpServletResponse response;
    private LongPollingServlet longPollingServlet;

    public StickyHandler(final LongPollingServlet longPollingServlet) {
        this.longPollingServlet = longPollingServlet;
    }

    public void write(String text) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.write(text);
        writer.flush();
    }

    public void attach(HttpServletResponse attachment) {
        response = attachment;
    }

    public void onEvent(CometEvent event) throws IOException {
        System.out.println("LongPollingServlet$StickyHandler.onEvent: event.getType() = " + event.getType());
        final String[] split = String.valueOf(event.attachment()).split("-");
        System.out.println("LongPollingServlet$StickyHandler.onEvent: split = " + Arrays.toString(split));
        CometOperations.valueOf(split[0].toUpperCase()).accept(longPollingServlet, this, split);
//            event.getCometContext().resumeCometHandler(this);
    }

    public void onInitialize(CometEvent event) throws IOException {
        System.out.println("LongPollingServlet$StickyHandler.onInitialize");
//            event.getCometContext().resumeCometHandler(this);
    }

    public void onTerminate(CometEvent event) throws IOException {
        System.out.println("LongPollingServlet$StickyHandler.onTerminate");
    }

    public void onInterrupt(CometEvent event) throws IOException {
        System.out.println("LongPollingServlet$StickyHandler.onInterrupt");
    }
}
