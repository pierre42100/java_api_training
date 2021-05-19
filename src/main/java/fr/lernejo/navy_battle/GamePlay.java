package fr.lernejo.navy_battle;

import fr.lernejo.navy_battle.prototypes.ServerInfo;

import java.io.IOException;

public class GamePlay {

    private static ServerInfo remoteServer;

    /**
     * Start the game (the other peer requested to start the game)
     */
    public static void startGame(RequestHandler handler) throws IOException {
        try {
            if (remoteServer != null) {
                handler.sendString(400, "I'm sorry, I have already another player dude!");
                return;
            }

            remoteServer = ServerInfo.fromJSON(handler.getJSONObject());

            handler.sendJSON(202, Launcher.getLocalServerInfo().toJSON());

        } catch (Exception e) {
            e.printStackTrace();
            handler.sendString(400, e.getMessage());
        }
    }

}
