package net.scoobis.ollama;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.NonNull;

@Data
public class OllamaChatMessage {
    @NonNull
    private String role;
    @NonNull
    private String content;

    public OllamaChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}