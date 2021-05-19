package fr.lernejo.navy_battle;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;

public class RequestHandler {
    private final HttpExchange exchange;

    public RequestHandler(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public String readToString() throws IOException {
        return new String(this.exchange.getRequestBody().readAllBytes());
    }

    public JSONObject getJSONObject() throws IOException {
        try {
            return new JSONObject(readToString());
        } catch (JSONException e) {
            sendString(400, e.toString());
            throw new JSONException(e);
        }
    }

    public void sendString(int status, String test) throws IOException {
        byte[] bytes = test.getBytes();
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) { // (1)
            os.write(bytes);
        }
        exchange.close();
    }

    public void sendJSON(int status, JSONObject object) throws IOException {
        exchange.getResponseHeaders().set("Content-type", "application/json");
        sendString(status, object.toString());
    }
}
