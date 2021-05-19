package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import fr.lernejo.navy_battle.prototypes.ServerInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.Executors;

public class Launcher {

    private static int serverPort;
    private static final UUID serverID = UUID.randomUUID();
    private static String remoteServerURL = null;

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Usage: Launcher [port] {server_url}");
                System.exit(-1);
            }

            serverPort = Integer.parseInt(args[0]);
            System.out.println("Starting to listen on " + getLocalServerInfo().getUrl());

            if (args.length > 1)
                remoteServerURL = args[1];

            startServer(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startServer(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.createContext("/ping", Launcher::handlePing);
        server.createContext("/api/game/start", s -> GamePlay.startGame(new RequestHandler(s)));
        server.start();
    }

    private static void handlePing(HttpExchange exchange) throws IOException {
        String body = "Hello";
        exchange.sendResponseHeaders(200, body.length());
        try (OutputStream os = exchange.getResponseBody()) { // (1)
            os.write(body.getBytes());
        }
    }

    public static ServerInfo getLocalServerInfo() {
        return new ServerInfo(
            serverID.toString(),
            "http://localhost:" + serverPort + "/",
            "Pierre is the best coder forever. I can only beat you!"
        );
    }

    public static String getRemoteServerURL() {
        return remoteServerURL;
    }
}
