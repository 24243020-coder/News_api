import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class NewsApp {
    private static final int PORT = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "8080"));
    private static final String API_KEY = System.getenv().getOrDefault("NEWSAPI_KEY", "");
    private static final String BASE_URL = "https://newsapi.org/v2/top-headlines";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
        server.createContext("/", NewsApp::handleHome);
        server.createContext("/api/news/headlines", NewsApp::handleHeadlines);
        server.setExecutor(null);
        server.start();

        System.out.println("Java News App is running on port " + PORT);
        System.out.println("Home: http://localhost:" + PORT + "/");
        System.out.println("Headlines: http://localhost:" + PORT + "/api/news/headlines?country=us");
    }

    private static void handleHome(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"Only GET is supported\"}");
            return;
        }
        send(exchange, 200,
                "{\"application\":\"Java News Application\",\"status\":\"running\","
                        + "\"headlinesEndpoint\":\"/api/news/headlines?country=us\"}");
    }

    private static void handleHeadlines(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"Only GET is supported\"}");
            return;
        }
        if (API_KEY.isBlank() || API_KEY.equals("YOUR_NEWSAPI_KEY")) {
            send(exchange, 503,
                    "{\"error\":\"NewsAPI key missing. Set NEWSAPI_KEY environment variable.\"}");
            return;
        }

        try {
            String query = exchange.getRequestURI().getRawQuery();
            String country = getParam(query, "country");
            if (country == null || country.isBlank()) country = "us";
            String category = getParam(query, "category");

            StringBuilder url = new StringBuilder(BASE_URL)
                    .append("?country=").append(encode(country))
                    .append("&apiKey=").append(encode(API_KEY));
            if (category != null && !category.isBlank()) {
                url.append("&category=").append(encode(category));
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url.toString()))
                    .timeout(Duration.ofSeconds(25))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = CLIENT.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            String body = response.body();
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                send(exchange, 200, body);
            } else {
                String safeBody = body == null ? "" : body;
                send(exchange, response.statusCode(),
                        "{\"error\":\"NewsAPI request failed\",\"status\":"
                                + response.statusCode() + ",\"details\":"
                                + jsonString(safeBody) + "}");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            send(exchange, 502, "{\"error\":\"Request to NewsAPI was interrupted\"}");
        } catch (Exception e) {
            send(exchange, 502, "{\"error\":\"Could not retrieve news from NewsAPI\"}");
        }
    }

    private static String getParam(String rawQuery, String key) {
        if (rawQuery == null || rawQuery.isBlank()) return null;
        for (String pair : rawQuery.split("&")) {
            String[] parts = pair.split("=", 2);
            if (decode(parts[0]).equals(key)) {
                return parts.length > 1 ? decode(parts[1]) : "";
            }
        }
        return null;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String decode(String value) {
        return java.net.URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String jsonString(String value) {
        String escaped = value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
        return "\"" + escaped + "\"";
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
