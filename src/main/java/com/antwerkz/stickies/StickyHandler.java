package com.antwerkz.stickies;

import java.io.IOException;
import java.util.Arrays;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.antwerkz.stickies.CometServlet.CometOperations;
import com.sun.grizzly.comet.CometEvent;
import com.sun.grizzly.comet.CometHandler;

public class StickyHandler implements CometHandler<HttpServletResponse> {
    private static final String BEGIN_SCRIPT_TAG = "<script type='text/javascript'>alert('response!');";
    private static final String END_SCRIPT_TAG = "</script>\n";
    private HttpServletResponse response;
    private CometServlet cometServlet;

    public StickyHandler(final CometServlet cometServlet) {
        this.cometServlet = cometServlet;
    }

    public void write(String text) throws IOException {
        ServletOutputStream writer = response.getOutputStream();
        writer.println(BEGIN_SCRIPT_TAG /*+ text */+ END_SCRIPT_TAG);
//        for (int i = 0; i < 10000; i++) {
//            writer.println(CometServlet.JUNK);
//        }
        writer.flush();
    }

    public void attach(HttpServletResponse attachment) {
        response = attachment;
    }

    public void onEvent(CometEvent event) throws IOException {
        System.out.println("CometServlet$StickyHandler.onEvent: event.getType() = " + event.getType());
        final String[] split = String.valueOf(event.attachment()).split("-");
        System.out.println("CometServlet$StickyHandler.onEvent: split = " + Arrays.toString(split));
        final CometOperations operation;
        try {
            operation = CometOperations.valueOf(split[0].toUpperCase());
            System.out.println("StickyHandler.onEvent: operation = " + operation);
            operation.accept(cometServlet, this, split);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
//            event.getCometContext().resumeCometHandler(this);
    }

    public void onInitialize(CometEvent event) throws IOException {
        System.out.println("CometServlet$StickyHandler.onInitialize");
//            event.getCometContext().resumeCometHandler(this);
    }

    public void onTerminate(CometEvent event) throws IOException {
        System.out.println("CometServlet$StickyHandler.onTerminate");
    }

    public void onInterrupt(CometEvent event) throws IOException {
        System.out.println("CometServlet$StickyHandler.onInterrupt");
    }
}
