package com.lankabuild.cms.util;

import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpUtil {

    public static String readBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int read;
        while ((read = is.read(data)) != -1) {
            buffer.write(data, 0, read);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    public static void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        String json = JsonUtil.toJson(body);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, String> body = new HashMap<>();
        body.put("error", message);
        sendJson(exchange, statusCode, body);
    }

    /** Extracts a numeric path segment, e.g. /api/projects/12 -> 12 */
    public static Integer extractIdFromPath(String path, String prefix) {
        try {
            String remainder = path.substring(prefix.length());
            remainder = remainder.replaceAll("^/+", "").replaceAll("/+$", "");
            if (remainder.isEmpty() || remainder.contains("/")) return null;
            return Integer.parseInt(remainder);
        } catch (Exception e) {
            return null;
        }
    }

    public static String getBearerToken(HttpExchange exchange) {
        String header = exchange.getRequestHeaders().getFirst("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
