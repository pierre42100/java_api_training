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
    private ServerInfo localServer;
    private ServerInfo remoteServer;

    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.err.println("Usage: Launcher [port] {server_url}");
                System.exit(-1);
            }

            int serverPort = Integer.parseInt(args[0]);
            System.out.println("Starting to listen on port " + serverPort);

            new Launcher().startServer(serverPort, args.length > 1 ? args[1] : null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Start the server
     */
    private void startServer(int port, String connectURL) throws IOException {
        localServer = new ServerInfo(
            UUID.randomUUID().toString(),
            "http://localhost:" + port,
            "Pierre is the best coder forever. I can only beat you!"
        );

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.createContext("/ping", this::handlePing);
        server.createContext("/api/game/start", s -> startGame(new RequestHandler(s)));
        server.start();
    }


    /**
     * Handle simple ping
     */
    private void handlePing(HttpExchange exchange) throws IOException {
        String body = "Hello";
        exchange.sendResponseHeaders(200, body.length());
        try (OutputStream os = exchange.getResponseBody()) { // (1)
            os.write(body.getBytes());
        }
    }

    /**
     * Start the game (the other peer requested to start the game)
     */
    public void startGame(RequestHandler handler) throws IOException {
        try {
            if (remoteServer != null) {
                handler.sendString(400, "I'm sorry, I have already another player dude!");
                return;
            }

            remoteServer = ServerInfo.fromJSON(handler.getJSONObject());

            handler.sendJSON(202, localServer.toJSON());

        } catch (Exception e) {
            e.printStackTrace();
            handler.sendString(400, e.getMessage());
        }
    }
}
