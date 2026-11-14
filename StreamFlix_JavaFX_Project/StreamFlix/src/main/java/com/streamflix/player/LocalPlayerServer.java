package com.streamflix.player;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * LocalPlayerServer - a tiny web server (built into the JDK) that only listens on THIS computer.
 *
 * WHY WE NEED IT: YouTube refuses to play an embedded video ("Error 153") when the page that holds the
 * video has no proper web address (e.g. a page created with WebEngine.loadContent). So we serve a one-line
 * HTML page from  http://localhost:PORT/player?id=VIDEO_ID  that contains the YouTube iframe.
 * The video itself is still streamed straight from YouTube - nothing is stored in the project.
 *
 * The page also relays messages between the video and our Java code:
 *   Java -> video : play / pause / seek / mute   (postMessage commands)
 *   video -> Java : state changes (playing, paused, ended) and errors, through "javaBridge"
 *
 * PORTION: Video playback  |  OWNER: Pofane Matlali
 */
public final class LocalPlayerServer {

    private static final Pattern VALID_ID = Pattern.compile("[A-Za-z0-9_-]{6,20}");

    private HttpServer server;
    private int port;

    /** Starts the server the first time it is needed. */
    public synchronized void start() throws IOException {
        if (server != null) {
            return;
        }
        // 127.0.0.1 = only this computer can connect (nobody on the network can)
        server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0), 0);
        port = server.getAddress().getPort();
        server.createContext("/player", this::handlePlayer);
        server.setExecutor(Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "player-http");
            t.setDaemon(true);                            // never keeps the app alive after closing
            return t;
        }));
        server.start();
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    /** Address the WebView should load for a given YouTube video id. */
    public String playerUrl(String videoId) {
        try {
            start();
        } catch (IOException e) {
            throw new IllegalStateException("Could not start the local player server", e);
        }
        return "http://localhost:" + port + "/player?id=" + videoId;
    }

    // ------------------------------------------------------------------ request handling

    private void handlePlayer(HttpExchange exchange) throws IOException {
        String id = extractId(exchange.getRequestURI().getQuery());
        byte[] body;
        int status;
        if (id == null || !VALID_ID.matcher(id).matches()) {
            body = "Bad video id".getBytes(StandardCharsets.UTF_8);
            status = 400;
        } else {
            body = buildPage(id).getBytes(StandardCharsets.UTF_8);
            status = 200;
        }
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static String extractId(String query) {
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            if (pair.startsWith("id=")) {
                return pair.substring(3);
            }
        }
        return null;
    }

    private String buildPage(String id) {
        String origin = "http://localhost:" + port;
        String src = "https://www.youtube.com/embed/" + id
                + "?autoplay=1&controls=0&rel=0&modestbranding=1&playsinline=1&iv_load_policy=3"
                + "&disablekb=1&fs=0&enablejsapi=1&origin=" + origin;

        return "<!DOCTYPE html><html><head><meta charset='utf-8'>"
                + "<style>html,body{margin:0;height:100%;background:#000;overflow:hidden}"
                + "iframe{border:0;width:100%;height:100%}</style></head><body>"
                + "<iframe id='yt' src='" + src + "' "
                + "allow='autoplay; encrypted-media; picture-in-picture' referrerpolicy='strict-origin-when-cross-origin'>"
                + "</iframe>"
                + "<script>"
                + "var f=document.getElementById('yt');"
                // ask the YouTube player to start sending us its events
                + "f.onload=function(){f.contentWindow.postMessage(JSON.stringify({event:'listening',id:1,channel:'widget'}),'*');};"
                + "window.addEventListener('message',function(e){"
                + "try{var d=(typeof e.data==='string')?JSON.parse(e.data):e.data;"
                + "if(!d||!window.javaBridge)return;"
                + "if(d.event==='onStateChange'){window.javaBridge.onState(d.info);}"
                + "else if(d.event==='infoDelivery'&&d.info&&d.info.playerState!==undefined){window.javaBridge.onState(d.info.playerState);}"
                + "else if(d.event==='onError'){window.javaBridge.onError(d.info);}"
                + "}catch(x){}});"
                + "</script></body></html>";
    }
}
