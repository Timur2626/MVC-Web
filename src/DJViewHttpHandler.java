import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DJViewHttpHandler implements HttpHandler {

    private final BeatModelInterface model;
    private final byte[] html;

    public DJViewHttpHandler(
            BeatModelInterface model,
            byte[] html
    ) {
        this.model = model;
        this.html = html;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            switch (path) {
                case "/" -> {
                    if (!"GET".equals(method)) {
                        methodNotAllowed(exchange, "GET");
                        return;
                    }

                    send(
                            exchange,
                            200,
                            "text/html; charset=UTF-8",
                            html
                    );
                }

                case "/api/state" -> {
                    if (!"GET".equals(method)) {
                        methodNotAllowed(exchange, "GET");
                        return;
                    }

                    sendState(exchange);
                }

                case "/api/control" -> {
                    if (!"POST".equals(method)) {
                        methodNotAllowed(exchange, "POST");
                        return;
                    }

                    handleCommand(exchange);
                }

                default -> sendJson(
                        exchange,
                        404,
                        "{\"error\":\"Страница не найдена\"}"
                );
            }
        } finally {
            exchange.close();
        }
    }

    private void handleCommand(HttpExchange exchange) throws IOException {
        try {
            String body = new String(
                    exchange.getRequestBody().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            Map<String, String> parameters = parseForm(body);

            String action = parameters.getOrDefault("action", "");

            switch (action) {
                case "start" -> model.on();

                case "stop" -> model.off();

                case "increase" -> {
                    requireRunning();
                    setTempo(model.getBPM() + 1);
                }

                case "decrease" -> {
                    requireRunning();
                    setTempo(model.getBPM() - 1);
                }

                case "set" -> {
                    requireRunning();

                    int bpm = Integer.parseInt(
                            parameters.getOrDefault("bpm", "")
                    );

                    setTempo(bpm);
                }

                default -> throw new IllegalArgumentException();
            }

            sendState(exchange);

        } catch (IllegalArgumentException exception) {
            sendJson(
                    exchange,
                    400,
                    """
                    {
                        "error": "Запустите ритм и укажите целое число от 30 до 300"
                    }
                    """
            );
        }
    }

    private void requireRunning() {
        if (model.getBPM() == 0) {
            throw new IllegalArgumentException();
        }
    }

    private void setTempo(int bpm) {
        if (bpm < 30 || bpm > 300) {
            throw new IllegalArgumentException();
        }

        model.setBPM(bpm);
    }

    private Map<String, String> parseForm(String body) {
        Map<String, String> parameters = new HashMap<>();

        if (body.isEmpty()) {
            return parameters;
        }

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);

            String name = URLDecoder.decode(
                    parts[0],
                    StandardCharsets.UTF_8
            );

            String value = parts.length == 2
                    ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8)
                    : "";

            parameters.put(name, value);
        }

        return parameters;
    }

    private void sendState(HttpExchange exchange) throws IOException {
        String json = "{\"bpm\":" + model.getBPM() + "}";

        sendJson(exchange, 200, json);
    }

    private void methodNotAllowed(
            HttpExchange exchange,
            String allowedMethod
    ) throws IOException {
        exchange.getResponseHeaders().set("Allow", allowedMethod);

        sendJson(
                exchange,
                405,
                "{\"error\":\"Метод не поддерживается\"}"
        );
    }

    private void sendJson(
            HttpExchange exchange,
            int status,
            String json
    ) throws IOException {
        send(
                exchange,
                status,
                "application/json; charset=UTF-8",
                json.getBytes(StandardCharsets.UTF_8)
        );
    }

    private void send(
            HttpExchange exchange,
            int status,
            String contentType,
            byte[] content
    ) throws IOException {
        exchange.getResponseHeaders().set(
                "Content-Type",
                contentType
        );

        exchange.getResponseHeaders().set(
                "Cache-Control",
                "no-store"
        );

        exchange.sendResponseHeaders(status, content.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(content);
        }
    }
}