package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import fr.lernejo.navy_battle.prototypes.Coordinates;
import fr.lernejo.navy_battle.prototypes.FireResult;
import fr.lernejo.navy_battle.prototypes.Option;
import fr.lernejo.navy_battle.prototypes.ServerInfo;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class Server extends AbstractServer {
    private final Option<ServerInfo> localServer = new Option<>();
    private final Option<ServerInfo> remoteServer = new Option<>();
    private final Option<GamePlay> gamePlay = new Option<>();


    @Override
    public void startServer(int port, String connectURL) throws IOException {
        localServer.set(new ServerInfo(
            UUID.randomUUID().toString(),
            "http://localhost:" + port,
            "Pierre is the best coder forever. I can only beat you!"
        ));

        if (connectURL != null)
            new Thread(() -> this.requestStart(connectURL)).start();

        super.startServer(port, connectURL);
    }

    @Override
    public void createContextes(HttpServer server) {
        server.createContext("/ping", this::handlePing);
        server.createContext("/api/game/start", s -> startGame(new RequestHandler(s)));
        server.createContext("/api/game/fire", s -> handleFire(new RequestHandler(s)));
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
            gamePlay.set(new GamePlay());
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
            gamePlay.set(new GamePlay());
            var response = sendPOSTRequest(server + "/api/game/start", this.localServer.get().toJSON());

            this.remoteServer.set(ServerInfo.fromJSON(response).withURL(server));
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
        Coordinates coordinates = gamePlay.get().getNextPlaceToHit();
        var response =
            sendGETRequest(remoteServer.get().getUrl() + "/api/game/fire?cell=" + coordinates.toString());

        if (!response.getBoolean("shipLeft")) {
            gamePlay.get().wonGame();
            return;
        }

        gamePlay.get().setFireResult(coordinates, FireResult.fromAPI(response.getString("consequence")));
    }

    /**
     * Handle fire request
     */
    public void handleFire(RequestHandler handler) throws IOException {
        try {
            var pos = new Coordinates(handler.getQueryParameter("cell"));
            handler.sendJSON(200, new JSONObject().put("consequence", gamePlay.get().hit(pos).toAPI())
                .put("shipLeft", gamePlay.get().localMapShipLeft()));

            if (!gamePlay.get().localMapShipLeft()) {
                System.out.println("We have lost the game :(");
                return;
            }

            fire();
        } catch (Exception e) {
            e.printStackTrace();
            handler.sendString(400, e.getMessage());
        }
    }
}
