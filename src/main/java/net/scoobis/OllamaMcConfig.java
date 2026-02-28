package net.scoobis;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@Config(name = "ollama-mc")
public class OllamaMcConfig implements ConfigData {
    boolean Enabled = true;
    String Model = "llama3.1:latest";
    String ModelName = "@ollama";
    String Host = "http://localhost:11434/";
    String SystemPrompt = "You are an LLM within the game minecraft.";
}