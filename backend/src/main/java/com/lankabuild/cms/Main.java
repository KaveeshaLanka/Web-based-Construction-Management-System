package com.lankabuild.cms;

import com.lankabuild.cms.handler.AuthHandler;
import com.lankabuild.cms.handler.ProjectHandler;
import com.lankabuild.cms.handler.TaskHandler;
import com.lankabuild.cms.handler.UserHandler;   // <-- was .service.UserHandler
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        server.createContext("/api/auth", withCors(new AuthHandler()));
        server.createContext("/api/projects", withCors(new ProjectHandler()));
        server.createContext("/api/milestones", withCors(new ProjectHandler()));
        server.createContext("/api/tasks", withCors(new TaskHandler()));
        server.createContext("/api/users", withCors(new UserHandler()));

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("=================================================");
        System.out.println(" LankaBuild Construction Management System API");
        System.out.println(" Server running at: http://localhost:" + PORT);
        System.out.println("=================================================");
    }

    /** Wraps a handler so browser-based frontends (running on a different port) can call the API. */
    private static HttpHandler withCors(HttpHandler handler) {
        return exchange -> {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            handler.handle(exchange);
        };
    }
}