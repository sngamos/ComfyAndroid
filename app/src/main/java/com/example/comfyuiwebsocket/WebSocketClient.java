package com.example.comfyuiwebsocket;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketClient {
    private WebSocket webSocket;

    public WebSocketClient(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                // Connection opened
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // Handle text messages
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // Handle binary messages (e.g., images)
                // This is where you would convert the ByteString to a Bitmap and update your UI
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                webSocket.close(1000, null);
                // Handle the closing of the connection
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                // Handle failures
            }
        });

        // Shutting down the ExecutorService is not necessary when the application is still running and needs to make other requests.
    }

    public void sendTextMessage(String message) {
        webSocket.send(message);
    }

}

