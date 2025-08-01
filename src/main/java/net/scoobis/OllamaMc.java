package net.scoobis;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.text.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

public class OllamaMc implements ModInitializer {
	public static final String MOD_ID = "ollama-mc";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static MinecraftClient CLIENT = MinecraftClient.getInstance();
	public static ConfigHolder<OllamaMcConfig> CONFIG;

	@Override
	public void onInitialize() {
		LOGGER.info("OllamaMC is here!");

		AutoConfig.register(OllamaMcConfig.class, GsonConfigSerializer::new);
		CONFIG = AutoConfig.getConfigHolder(OllamaMcConfig.class);

		Ollama.resetMessages();

		ClientReceiveMessageEvents.CHAT.register(Ollama::onChatMessage);
		ClientReceiveMessageEvents.GAME.register(Ollama::onChatMessage);
		
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("ollama")
				.then(ClientCommandManager.literal("config").executes(context -> {
					GameMenuScreen gameMenuScreen = new GameMenuScreen(true);
					CLIENT.send(() -> CLIENT.setScreen(AutoConfig.getConfigScreen(OllamaMcConfig.class, gameMenuScreen).get()));
					return 1;
				}))
				.then(ClientCommandManager.literal("resetconversation").executes(context -> {
					Ollama.resetMessages();
					context.getSource().sendFeedback(Text.of("Ollama conversation reset"));
					return 1;
				}))
			);
		});
	}
}