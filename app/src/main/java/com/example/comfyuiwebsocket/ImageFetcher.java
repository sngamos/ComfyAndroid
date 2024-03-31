package com.example.comfyuiwebsocket;
import android.content.Context;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ImageFetcher {
    private final OkHttpClient client = new OkHttpClient();
    private final String serverAddress;
    private final Context context; // Context is needed to access internal storage
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface ImageFetchListener {
        void onImageSaved(String imagePath);
        void onError(Exception e);
    }

    public ImageFetcher(Context context, String serverAddress) {
        this.context = context;
        this.serverAddress = serverAddress;
    }

    public void fetchImages(String promptId, ImageFetchListener listener) {
        executor.submit(() -> {
            try {
                String historyUrl = serverAddress + "/history/" + promptId;
                Request historyRequest = new Request.Builder().url(historyUrl).build();
                try (Response historyResponse = client.newCall(historyRequest).execute()) {
                    if (!historyResponse.isSuccessful()) throw new IOException("Unexpected code " + historyResponse);

                    String jsonData = historyResponse.body().string();
                    JSONObject jsonObject = new JSONObject(jsonData);
                    JSONObject history = jsonObject.getJSONObject(promptId); // Assuming this exists
                    JSONObject outputs = history.getJSONObject("outputs");

                    for (Iterator<String> it = outputs.keys(); it.hasNext(); ) {
                        String node_id = it.next();
                        JSONObject nodeOutput = outputs.getJSONObject(node_id);
                        if (nodeOutput.has("images")) {
                            JSONArray images = nodeOutput.getJSONArray("images");
                            for (int j = 0; j < images.length(); j++) {
                                JSONObject image = images.getJSONObject(j);
                                // Check for 'type': 'output'
                                if ("output".equals(image.getString("type"))) {
                                    String imageUrl = serverAddress + "/view?filename=" + image.getString("filename")
                                            + "&subfolder=" + image.optString("subfolder", "") + "&type=" + image.getString("type");
                                    Request imageRequest = new Request.Builder().url(imageUrl).build();
                                    try (Response imageResponse = client.newCall(imageRequest).execute()) {
                                        if (!imageResponse.isSuccessful()) continue; // Skip if the image fetch wasn't successful

                                        // Save the first 'output' type image to internal storage and notify
                                        Bitmap bitmap = BitmapFactory.decodeStream(imageResponse.body().byteStream());
                                        File imageFile = new File(context.getFilesDir(), promptId + "_output.png");
                                        try (FileOutputStream out = new FileOutputStream(imageFile)) {
                                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                            listener.onImageSaved(imageFile.getAbsolutePath());
                                            return; // Stop after saving the first 'output' type image
                                        } catch (IOException e) {
                                            Log.e("ImageSave", "Error saving image", e);
                                        }
                                    }
                                    break; // Break from the images loop once the first 'output' type image is processed
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("ImageFetch", "Error fetching images", e);
                listener.onError(e);
            }
        });
    }


}
