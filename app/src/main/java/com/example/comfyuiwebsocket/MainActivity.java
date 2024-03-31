package com.example.comfyuiwebsocket;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MainActivity extends AppCompatActivity {
    private OkHttpClient client = new OkHttpClient();
    private EditText editTextInput;
    private ImageView imageView;
    private WebSocket webSocket;
    private String serverAddress = "https://legal-picked-primate.ngrok-free.app";
    private String websocketAddress = serverAddress.replace("https://", "wss://");
    private String clientId = UUID.randomUUID().toString();
    private String promptId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextInput = findViewById(R.id.editTextInput);
        imageView = findViewById(R.id.imageView);
        Button buttonSend = findViewById(R.id.buttonSend);
        connectWebSocket();
        buttonSend.setOnClickListener(view -> {
            String userInput = editTextInput.getText().toString();
            if (webSocket != null && !userInput.isEmpty()) {
                try {
                    String PromptID = sendInputToServer(userInput);
                    Log.d("prompt id",PromptID);
                    this.promptId = PromptID;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }
    private void connectWebSocket() {
        Request request = new Request.Builder().url(websocketAddress + "/ws?clientId=" + clientId).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                super.onOpen(webSocket, response);
                Log.d("WebSocket", "Connection opened");
            }
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                // Handle messages received from the server
                Log.d("WebSocket", "Receiving : " + text);
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    String messageType = jsonObject.getString("type");
                    JSONObject data = jsonObject.optJSONObject("data");
                    if ("executing".equals(messageType) && data != null) {
                        boolean nodeIsNull = data.isNull("node");
                        String receivedPromptId = data.optString("prompt_id", "");
                        Log.d("recieved prompt id",receivedPromptId);
                        // Check if execution is complete
                        if (nodeIsNull && promptId.equals(receivedPromptId)) {
                            Log.d("WebSocket", "Execution done for prompt ID: " + promptId);
                            handleImageFetching(promptId);

                        }
                    }
                } catch (JSONException e) {
                    Log.e("WebSocket", "Error parsing message", e);
                }
            }
        });

        // Keep the WebSocket open
        client.dispatcher().executorService().shutdown();
    }

    private String sendInputToServer(String userInput) throws Exception {
        // Prepare your prompt as a JSON string
        PromptObj promptobj = new PromptObj("Ganyu",userInput);
        String prompt = promptobj.readJsonAndModify(this, clientId);
        Log.d("prompt",prompt);
        //Log.d("websocket prompt",promptJson);
        ExecutorService executor = Executors.newCachedThreadPool(); // Create an executor
        Future<String> future = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                PostAgent agent = new PostAgent();
                String promptID = agent.sendPostRequest(prompt);
                return promptID;
            }
        });
        // Get the result from the Future object. Note: This call is blocking.
        Log.d("WebSocket", "Sending : " + userInput);
        return future.get();
    }
    private void handleImageFetching(String promptId) {
        // Assuming 'this' is a Context (e.g., Activity)
        ImageFetcher imageFetcher = new ImageFetcher(this, this.serverAddress);

        imageFetcher.fetchImages(promptId, new ImageFetcher.ImageFetchListener() {
            @Override
            public void onImageSaved(String imagePath) {
                // This callback will be on a background thread. To update the UI, switch to the main thread.
                runOnUiThread(() -> {
                    // Display the image in imageView
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    imageView.setImageBitmap(bitmap);
                });
            }

            @Override
            public void onError(Exception e) {
                // Handle any errors here, e.g., by showing a toast. Remember to switch to the main thread for UI operations.
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Error fetching image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close WebSocket connection when the activity is destroyed
        if (webSocket != null) {
            webSocket.close(1000, "Activity Destroyed");
        }
    }

}
