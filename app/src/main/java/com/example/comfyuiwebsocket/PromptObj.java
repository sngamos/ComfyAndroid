package com.example.comfyuiwebsocket;

import android.content.Context;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


public class PromptObj {
    String CharacterName;
    String emotion;
    PromptObj(String CharacterName, String emotion){
        this.CharacterName = CharacterName;
        this.emotion = emotion;

    }
    public static String PosCombinePrompt(String CharacterName,String emotion){
        if (CharacterName == "Ganyu"){
            return String.format("<lora:ganyu_ned2_offset:1> <lora:japaneseDollLikeness_v10:0.45> masterpiece, (photorealistic:1.5), best quality, beautiful lighting, real life,\n\nganyu \\(genshin impact\\), 1girl, architecture, bangs,medium breasts, bare shoulders, bell, black gloves, black pantyhose, (blue hair), blush, chinese knot, detached sleeves, east asian architecture, flower knot, gloves, horns, long hair, looking at viewer, neck bell, night, outdoors, pantyhose, purple eyes, sidelocks, solo, tassel,  white sleeves, (ulzzang-6500:0.5)\n\n, intricate, high detail, sharp focus, dramatic, beautiful girl , (RAW photo, 8k uhd, film grain), caustics, subsurface scattering, reflections, (%s :1.6), triangle face",emotion);
        }else{
            return "male";
        }
    }
    public static  String negCombinePrompt(String CharacterName,String additionalPrompts){
        return String.format("(painting by bad-artist-anime:0.9), (painting by bad-artist:0.9), watermark, text, error, blurry, jpeg artifacts, cropped, worst quality, low quality, normal quality, jpeg artifacts, signature, watermark, username, artist name, (worst quality, low quality:1.4), bad anatomy, nudity, nipples, bad fingers, bad hands, full body, bad mouth, bad lips, %s",additionalPrompts);
    }
    public String readJsonAndModify(Context context,String ClientID) throws Exception {

        InputStream is = context.getResources().openRawResource(R.raw.workflow_api);
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        // Convert the InputStream to a String.
        String jsonContent = new String(buffer, "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();

        // Example modification
        Map<String, Object> originalJsonMap = objectMapper.readValue(jsonContent, HashMap.class);

        // Example of modifying the "inputs" section for key "15" to change positive & negative prompts
        Map<String, Object> inputs15 = (Map<String, Object>) ((Map<String, Object>) originalJsonMap.get("15")).get("inputs");
        String posPromptString = PosCombinePrompt(this.CharacterName,this.emotion); //promptText is the variable that stores the emotion as a String
        inputs15.put("positive", posPromptString); // Directly use promptText

        String negPromptString = negCombinePrompt(this.CharacterName,""); //add additional negative prompts here
        inputs15.put("negative",negPromptString);


        // Modifying input 16 for seed
        Map<String, Object> inputs16 = (Map<String, Object>) ((Map<String, Object>) originalJsonMap.get("16")).get("inputs");
        int newSeed = (int) (Math.random() * 10001);
        inputs16.replace("seed", newSeed);
        // Further modifications as needed...

        // Wrap the modified structure under a "prompt" key
        Map<String, Object> wrappedJsonMap = new HashMap<>();
        wrappedJsonMap.put("prompt", originalJsonMap);
        wrappedJsonMap.put("client_id",ClientID);
        //System.out.println(wrappedJsonMap);

        return objectMapper.writeValueAsString(wrappedJsonMap);
    }
}


