package net.scoobis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo.ServerType;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.scoobis.ollama.OllamaChatMessage;
import net.scoobis.ollama.OllamaChatRequest;

public class Ollama {
    private static List<OllamaChatMessage> messages;
    private static String message;
    private static String username;
    private static boolean thinking = false;

    public static void resetMessages() {
        messages = new ArrayList<OllamaChatMessage>();
        messages.add(new OllamaChatMessage("SYSTEM", OllamaMc.CONFIG.get().SystemPrompt  + " Do not send messages larger than 230 characters. Do not use the character §. Only use one line."));
    }

    public static Runnable onMessageRunnable = new Runnable() {
        @Override
        public void run() {
            onMessage(message, username);
        }
    };

    public static void onMessage(String message, String username) {
        thinking = true;
        try {
            OllamaMc.LOGGER.info(username + ": " + message);
            URL url = new URI(OllamaMc.CONFIG.get().Host + "api/chat").toURL();
            
            if (username == "") {
                messages.add(new OllamaChatMessage("USER", message));
            } else {
                messages.add(new OllamaChatMessage("USER", "[User: " + username + "] " + message));
            }
            
            OllamaChatRequest chatRequest = new OllamaChatRequest(OllamaMc.CONFIG.get().Model, messages);
            OllamaMc.LOGGER.info(chatRequest.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try(OutputStream os = conn.getOutputStream()) {
                byte[] input = chatRequest.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while (((line) = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            String responseString = jsonResponse.get("message").getAsJsonObject().get("content").getAsString();
            OllamaMc.LOGGER.info(responseString);

            String newLine = System.getProperty("line.separator");
            OllamaMc.LOGGER.info("[" + OllamaMc.CONFIG.get().ModelName + "]: " + responseString);
            responseString = responseString.replaceAll(newLine, "[n]");
            if (!(responseString.contains("§")) && !(responseString.contains(newLine)) && (responseString.length() <= 240)) {
                OllamaMc.CLIENT.player.networkHandler.sendChatMessage("[" + OllamaMc.CONFIG.get().ModelName + "]: " + responseString);
                messages.add(new OllamaChatMessage("ASSISTANT", responseString));
            } else {
                OllamaMc.CLIENT.player.networkHandler.sendChatMessage("[" + OllamaMc.CONFIG.get().ModelName + "]: [error: response didn't follow guidelines]");
            }
        } catch (IOException | URISyntaxException e) {
            OllamaMc.CLIENT.player.networkHandler.sendChatMessage("[" + OllamaMc.CONFIG.get().ModelName + "]: [unknown error: " + e.getMessage() + " ]");
        }
        thinking = false;
    }

    public static void onChatMessage(Text message2, SignedMessage signedMessage, GameProfile profile, MessageType.Parameters params, Instant receptionTimestamp) {
        if (OllamaMc.CONFIG.get().Enabled) {
            message = signedMessage.getSignedContent();
            username = profile.getName();
            if ((message.contains("[" + OllamaMc.CONFIG.get().ModelName + "]") == false) && message.contains(OllamaMc.CONFIG.get().ModelName) && thinking == false) {
                message = message.replaceAll(OllamaMc.CONFIG.get().ModelName, "");
                message = message.replaceAll("  ", " ");
                Executors.newSingleThreadExecutor().execute(onMessageRunnable);
            }
        }
    }

    public static void onChatMessage(Text message2, boolean overlay) {
        ClientPlayerEntity playerEntity = OllamaMc.CLIENT.player;
        if (playerEntity != null && playerEntity.networkHandler != null && playerEntity.networkHandler.getServerInfo().getServerType() == ServerType.OTHER && playerEntity.networkHandler.getServerInfo().isLocal() == false && OllamaMc.CONFIG.get().Enabled && playerEntity.networkHandler.getConnection().isEncrypted() == true) {
            message = message2.getString().toLowerCase();
            if ((message.contains("[" + OllamaMc.CONFIG.get().ModelName.toLowerCase() + "]") == false) && message.contains(OllamaMc.CONFIG.get().ModelName.toLowerCase()) && thinking == false) {
                if (message2.getString().contains("> ") && message2.getString().startsWith("<")) {
                    message = message2.getString().replaceFirst(message2.getString().split("> ")[0], "").replaceFirst(">", "");
                    username = message2.getString().split("> ")[0].replaceFirst("<", "");

                    message = message.replaceAll(OllamaMc.CONFIG.get().ModelName, "");
                    message = message.replaceAll("  ", " ");
                    Executors.newSingleThreadExecutor().execute(onMessageRunnable);
                }
                else if (message2.getString().contains(": ")) {
                    message = message2.getString().replaceFirst(message2.getString().split(": ")[0], "").replaceFirst(": ", "");
                    username = message2.getString().split(": ")[0];

                    message = message.replaceAll(OllamaMc.CONFIG.get().ModelName, "");
                    message = message.replaceAll("  ", " ");
                    Executors.newSingleThreadExecutor().execute(onMessageRunnable);
                } else if (message2.getString().contains(" » ")) {
                    message = message2.getString().replaceFirst(message2.getString().split(" » ")[0], "").replaceFirst(" » ", "");
                    username = message2.getString().split(" » ")[0];

                    message = message.replaceAll(OllamaMc.CONFIG.get().ModelName, "");
                    message = message.replaceAll("  ", " ");
                    Executors.newSingleThreadExecutor().execute(onMessageRunnable);
                }
            }
        }
    }
}
