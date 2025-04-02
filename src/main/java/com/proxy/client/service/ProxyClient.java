package com.proxy.client.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class ProxyClient {
//    private static final String PROXY_SERVER_HOST = "server";
    private static final String PROXY_SERVER_HOST = "localhost";
    private static final int PROXY_SERVER_PORT = 9091;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final BlockingQueue<HttpRequest> requestQueue = new LinkedBlockingQueue<>();

public ProxyClient() throws IOException {
    connectToServer();
    startRequestProcessor();
}

    private void connectToServer() throws IOException {
        while (true) {
            try {
                System.out.println("Connecting to Proxy Server...");
                this.socket = new Socket(PROXY_SERVER_HOST, PROXY_SERVER_PORT);
                this.out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                System.out.println("Connected to Proxy Server.");
                break;
            } catch (IOException e) {
                System.err.println("Failed to connect, retrying...");
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }

    public synchronized String forwardRequest(String method, String url, String body) throws IOException {
        try {
            out.println(method + " " + url);
            System.out.println("URL recieved : "+url);
            if ("POST".equals(method)) {
                out.println(body);
            }
            out.flush();

            StringBuilder response = new StringBuilder();

            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                response.append(line).append("\n");
                if(line.contains("Error fetching URL:")){
                    response.append("Please check url "+url+ " and try again...");
                    return response.toString();
                }
            }
            return response.toString();
        } catch (SocketException e) {
            System.err.println("Connection reset, reconnecting...");
            connectToServer();
            return forwardRequest(method, url, body);
        }
    }


    //Queuing requests
    public void enqueueRequest(String method, String url, String body, HttpServletResponse response) {
        try {
            requestQueue.put(new HttpRequest(method, url, body, response));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void startRequestProcessor() {
        new Thread(() -> {
            while (true) {
                try {
                    HttpRequest request = requestQueue.take(); // Takes the next request (blocking)
                    processRequest(request);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void processRequest(HttpRequest request) throws IOException {
        try {
            out.println(request.method + " " + request.url);
            if ("POST".equals(request.method)) {
                out.println(request.body);
            }
            out.flush();

            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                responseBuilder.append(line).append("\n");
            }

            request.response.getWriter().write(responseBuilder.toString());
            request.response.getWriter().flush();
        } catch (SocketException e) {
            System.err.println("Connection reset, reconnecting...");
            connectToServer();
            processRequest(request); // Retry the same request
        }
    }

    private static class HttpRequest {
        String method, url, body;
        HttpServletResponse response;

        public HttpRequest(String method, String url, String body, HttpServletResponse response) {
            this.method = method;
            this.url = url;
            this.body = body;
            this.response = response;
        }
    }

}