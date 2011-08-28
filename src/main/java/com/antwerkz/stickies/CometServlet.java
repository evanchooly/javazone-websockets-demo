package com.antwerkz.stickies;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sun.grizzly.comet.CometContext;
import com.sun.grizzly.comet.CometEngine;
import com.sun.grizzly.comet.CometHandler;
import com.sun.grizzly.websockets.WebSocketEngine;

public class CometServlet extends HttpServlet {
    private Map<String, Note> notes = new HashMap<String, Note>();
    private CometContext<StickyHandler> context;
//    final static String JUNK = "<!-- Comet is a programming technique that enables web " +
//        "servers to send data to the client without having any need " +
//        "for the client to request it. -->\n";
    final static String JUNK = "                                                       " +
        "                                                       " +
        "                                                       " +
        "                                                       " +
        "                                                       ";

    @Override
    @SuppressWarnings("unchecked")
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        WebSocketEngine.getEngine().register("/stickies", new StickiesApplication());

        final String contextPath = config.getServletContext().getContextPath() + "/comet";
        context = CometEngine.getEngine().register(contextPath);
        context.setBlockingNotification(true);
        context.setExpirationDelay(5 * 30 * 1000);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setContentType("text/html");
        response.setHeader("Cache-Control", "private");
        response.setHeader("Pragma", "no-cache");
        final PrintWriter writer = response.getWriter();
//        For IE, Safari and Chrome, we must output some junk to enable streaming
//        for (int i = 0; i < 10; i++) {
//            writer.println(JUNK);
//        }
        writer.flush();
        StickyHandler handler = new StickyHandler(this);
        handler.attach(writer);
        context.addCometHandler(handler);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        response.setContentType("text/html");
        response.setHeader("Cache-Control", "private");
        request.setCharacterEncoding("UTF-8");

        final String text = read(request.getInputStream());
        System.out.println("CometServlet.doPost: text = " + text);
        context.notify(text);
        response.getOutputStream().println("success");
    }

    private String read(ServletInputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream);
        char[] buff = new char[8192];
        StringBuilder builder = new StringBuilder();
        int read;
        while ((read = reader.read(buff)) != -1) {
            builder.append(buff, 0, read);
        }
        return builder.toString();
    }

    private void createNote() throws IOException {
        System.out.println("CometServlet.createNote");
        Note note = new Note();
        notes.put(note.getId(), note);
        broadcast(null, "create-" + note.toString());
    }

    private void saveNote(StickyHandler handler, String[] params) throws IOException {
        System.out.println("CometServlet.saveNote");
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
        broadcast(handler, "save-" + note.toString());
    }

    private void deleteNote(StickyHandler handler, String[] params) throws IOException {
        notes.remove(params[1]);
        broadcast(handler, "delete-" + params[1]);
    }

    @SuppressWarnings("unchecked")
    private void broadcast(StickyHandler origin, String message) throws IOException {
        System.out.println("CometServlet.broadcast: message = " + message);
        final Set<CometHandler> handlers = context.getCometHandlers();
        for (CometHandler cometHandler : handlers) {
            if (!cometHandler.equals(origin)) {
                StickyHandler handler = (StickyHandler) cometHandler;
                handler.write(message);
            }
        }
    }

    enum CometOperations {
        CREATE {
            @Override
            void accept(CometServlet servlet, StickyHandler handler, String[] params) throws IOException {
                System.out.println("CometServlet$CometOperations.accept");
                servlet.createNote();
            }
        },
        SAVE {
            @Override
            void accept(CometServlet servlet, StickyHandler socket, String[] params) throws IOException {
                System.out.println("CometServlet$CometOperations.accept");
                servlet.saveNote(socket, params);
            }
        },
        DELETE {
            @Override
            void accept(CometServlet servlet, StickyHandler socket, String[] params) throws IOException {
                System.out.println("CometServlet$CometOperations.accept");
                servlet.deleteNote(socket, params);
            }
        };

        abstract void accept(CometServlet servlet, StickyHandler handler, String[] params) throws IOException;

    }
}