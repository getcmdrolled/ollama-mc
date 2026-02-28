package net.scoobis;

import me.shedaniel.autoconfig.AutoConfigClient;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.text.Text;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		Ollama.updateConfig();
		CONFIG.registerSaveListener((manager, data) -> {Ollama.updateConfig(); return ActionResult.SUCCESS;});

		Ollama.resetMessages();

		ClientReceiveMessageEvents.CHAT.register((m, ms, s, p, r) -> Ollama.onChatMessage(m));
		ClientReceiveMessageEvents.GAME.register((m, o) -> Ollama.onChatMessage(m));

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(ClientCommandManager.literal("ollama")
				.then(ClientCommandManager.literal("config").executes(context -> {
					GameMenuScreen gameMenuScreen = new GameMenuScreen(true);
					CLIENT.send(() -> CLIENT.setScreen(AutoConfigClient.getConfigScreen(OllamaMcConfig.class, gameMenuScreen).get()));
					return 1;
				}))
				.then(ClientCommandManager.literal("reset").executes(context -> {
					Ollama.resetMessages();
					context.getSource().sendFeedback(Text.of("Ollama reset"));
					return 1;
				}))
			);
		});
	}
}