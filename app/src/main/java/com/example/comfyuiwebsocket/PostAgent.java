package com.example.comfyuiwebsocket;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostAgent {
    private static final String SERVER_ADDRESS = "legal-picked-primate.ngrok-free.app";
    public static String sendPostRequest(String payload) throws Exception {
        URL url = new URL("https://" + SERVER_ADDRESS + "/prompt" );
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("Accept", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = payload.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = con.getResponseCode();
        System.out.println("POST Response Code :: " + responseCode);
        // Read the response from the server
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        }
        // Print the server response JSON
        //System.out.println("Server response JSON: " + response.toString());
        // Parse the response JSON and output the prompt_id
        ObjectMapper Mapper = new ObjectMapper();
        String out1 = response.toString();
        //System.out.println(out1);
        JsonNode rootNode = Mapper.readTree(out1);
        if (rootNode.has("prompt_id")) {
            String promptId = rootNode.get("prompt_id").asText();
            System.out.println("Prompt ID: " + promptId);
            return promptId;
        } else {
            System.out.println("Prompt ID not found in the response.");
            return "No ID";
        }
        // Optionally, handle the response from the server
    }
}
