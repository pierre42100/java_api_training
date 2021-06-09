package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import fr.lernejo.navy_battle.prototypes.*;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.Executors;

public class Launcher {
    private final HttpClient client = HttpClient.newHttpClient();

    private final Option<GameMap> localMap = new Option<>();
    private final Option<GameMap> remoteMap = new Option<>();
    private final Option<ServerInfo> localServer = new Option<>();
    private final Option<ServerInfo> remoteServer = new Option<>();

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
        localServer.set(new ServerInfo(
            UUID.randomUUID().toString(),
            "http://localhost:" + port,
            "Pierre is the best coder forever. I can only beat you!"
        ));

        if (connectURL != null)
            new Thread(() -> this.requestStart(connectURL)).start();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.createContext("/ping", this::handlePing);
        server.createContext("/api/game/start", s -> startGame(new RequestHandler(s)));
        server.createContext("/api/game/fire", s -> handleFire(new RequestHandler(s)));
        server.start();
    }


    /**
     * Handle simple ping
     */
    private void handlePing(HttpExchange exchange) throws IOException {
        String body = "OK";
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
            remoteServer.set(ServerInfo.fromJSON(handler.getJSONObject()));
            localMap.set(new GameMap(true));
            remoteMap.set(new GameMap(false));
            System.out.println("Will fight against " + remoteServer.get().getUrl());

            handler.sendJSON(202, localServer.get().toJSON());

            fire();

        } catch (Exception e) {
            e.printStackTrace();
            handler.sendString(400, e.getMessage());
        }
    }

    /**
     * Request the server to be started
     */
    public void requestStart(String server) {
        try {
            localMap.set(new GameMap(true));
            remoteMap.set(new GameMap(false));
            var response = sendPOSTRequest(server + "/api/game/start", this.localServer.get().toJSON());

            this.remoteServer.set(ServerInfo.fromJSON(response));
            System.out.println("Will fight against " + remoteServer.get().getUrl());

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start game!");
        }
    }

    /**
     * Fire on adversary
     */
    public void fire() throws IOException, InterruptedException {
        Coordinates coordinates = remoteMap.get().getNextPlaceToHit();
        var response =
            sendGETRequest(remoteServer.get().getUrl() + "/api/game/fire?cell=" + coordinates.toString());

        if (!response.getBoolean("shipLeft")) {
            System.out.println("Hourray we won the game!!! Pierre is the best!!!");
            System.out.println("The play is over!!!!");
            System.out.println("Adversary map:");
            remoteMap.get().printMap();

            System.out.println("Our map:");
            localMap.get().printMap();
            return;
        }

        var result = FireResult.fromAPI(response.getString("consequence"));

        if (result == FireResult.MISS)
            remoteMap.get().setCell(coordinates, GameCell.MISSED_FIRE);
        else
            remoteMap.get().setCell(coordinates, GameCell.SUCCESSFUL_FIRE);
    }

    /**
     * Handle fire request
     */
    public void handleFire(RequestHandler handler) throws IOException {
        try {
            String cell = handler.getQueryParameter("cell");
            var pos = new Coordinates(cell);

            var res = localMap.get().hit(pos);

            var response = new JSONObject();
            response.put("consequence", res.toAPI());
            response.put("shipLeft", localMap.get().hasShipLeft());

            handler.sendJSON(200, response);

            if (localMap.get().hasShipLeft()) {
                fire();
            } else {
                System.out.println("We have lost the game :(");
            }
        } catch (Exception e) {
            e.printStackTrace();
            handler.sendString(400, e.getMessage());
        }
    }

    /**
     * Send POST request
     */
    public JSONObject sendPOSTRequest(String url, JSONObject obj) throws IOException, InterruptedException {
        HttpRequest requetePost = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .setHeader("Accept", "application/json")
            .setHeader("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
            .build();

        var response = client.send(requetePost, HttpResponse.BodyHandlers.ofString());
        return new JSONObject(response.body());
    }

    /**
     * Send GET request
     */
    public JSONObject sendGETRequest(String url) throws IOException, InterruptedException {
        HttpRequest requeteGET = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .setHeader("Accept", "application/json")
            .GET()
            .build();

        var response = client.send(requeteGET, HttpResponse.BodyHandlers.ofString());
        return new JSONObject(response.body());
    }
}
