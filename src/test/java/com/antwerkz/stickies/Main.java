package com.antwerkz.stickies;

import java.io.IOException;

import com.sun.grizzly.arp.DefaultAsyncHandler;
import com.sun.grizzly.http.SelectorThread;
import com.sun.grizzly.tcp.StaticResourcesAdapter;
import com.sun.grizzly.util.Utils;
import com.sun.grizzly.websockets.WebSocketAsyncFilter;
import com.sun.grizzly.websockets.WebSocketEngine;

public class Main {
    public static void main(String[] args) throws IOException, InstantiationException, InterruptedException {
        final SelectorThread thread = setUpThread();
        try {
            while(true) {
                Thread.sleep(1000);
            }
        } finally {
            thread.stopEndpoint();
        }

    }

    private static SelectorThread setUpThread() throws IOException, InstantiationException {
        final SelectorThread st = new SelectorThread();
        st.setSsBackLog(8192);
        st.setCoreThreads(2);
        st.setMaxThreads(2);
        st.setPort(8080);
        st.setDisplayConfiguration(Utils.VERBOSE_TESTS);
        st.setAdapter(new StaticResourcesAdapter("src/main/webapp"));
//        final ServletAdapter adapter = new ServletAdapter(new CometServlet());
//        adapter.addRootFolder("src/main/webapp");
//        st.setAdapter(adapter);
        st.setAsyncHandler(new DefaultAsyncHandler());
        st.setEnableAsyncExecution(true);
        st.getAsyncHandler().addAsyncFilter(new WebSocketAsyncFilter());
        st.setTcpNoDelay(true);
        st.listen();

        WebSocketEngine.getEngine().register("/stickies", new StickiesApplication());

        return st;
    }
}
