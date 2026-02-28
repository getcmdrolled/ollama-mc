package net.scoobis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import net.minecraft.text.Text;

public class Ollama {
    private static List<OllamaChatMessage> messages;
    private static String message;
    private static OllamaMcConfig config = OllamaMc.CONFIG.get();
    private static OllamaAPI ollamaAPI = new OllamaAPI(config.Host);
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static Future<?> currentExecution;

    public static void resetMessages() {
        messages = new ArrayList<>();
        messages.add(new OllamaChatMessage(OllamaChatMessageRole.SYSTEM, config.SystemPrompt));
        if (currentExecution != null) currentExecution.cancel(true);
    }

    public static void updateConfig() {
        config = OllamaMc.CONFIG.get();
        ollamaAPI = new OllamaAPI(config.Host);
    }

    public static Runnable onMessageRunnable = new Runnable() {
        @Override
        public void run() {
            onMessage(message.replaceAll(config.ModelName, "").replaceAll(" {2}", " "));
        }
    };

    public static void onMessage(String message) {
        if (OllamaMc.CLIENT.player == null) return;
        messages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, message));
        OllamaChatRequest chatRequest = new OllamaChatRequest(config.Model, false, messages);
        try {
            OllamaChatResult result = ollamaAPI.chat(chatRequest);
            String responseString = result.getResponseModel().getMessage().getContent();
            responseString = responseString.replaceAll(System.lineSeparator(), "\\\\n").replaceAll("§", "");

            if (responseString.length() > 252 - config.ModelName.length()) responseString = responseString.substring(0, 240 - config.ModelName.length()) + " [truncated]";
            OllamaMc.CLIENT.player.networkHandler.sendChatMessage("[" + config.ModelName + "]: " + responseString);
            messages.add(new OllamaChatMessage(OllamaChatMessageRole.ASSISTANT, responseString));
        } catch (Exception e) {
            OllamaMc.CLIENT.player.networkHandler.sendChatMessage("[" + config.ModelName + " ERROR]: " + e.getMessage());
            OllamaMc.LOGGER.error(e.getMessage());
        }
    }

    public static void onChatMessage(Text message2) {
        message = message2.getString();
        if (!message.contains("[" + config.ModelName) && message.contains(config.ModelName) && config.Enabled) {
            OllamaMc.LOGGER.info(message);
            currentExecution = executor.submit(onMessageRunnable);
        }
    }
}
