package net.scoobis.ollama;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OllamaChatRequest {
    private String model;
    private List<OllamaChatMessage> messages;
    private boolean stream;
    @JsonSerialize(using = BooleanToJsonFormatFlagSerializer.class)
    @JsonProperty(value = "format")
    private Boolean returnFormatJson;
    
    public OllamaChatRequest(String model, List<OllamaChatMessage> messages) {
        this.model = model;
        this.messages = messages;
        this.stream = false;
    }

    public String toString() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
