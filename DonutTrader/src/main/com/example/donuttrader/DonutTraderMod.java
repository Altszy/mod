package com.example.donuttrader;

import com.example.donuttrader.config.ModConfig;
import com.example.donuttrader.hf.HighFrequencyTrader;
import com.example.donuttrader.handler.WebhookHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class DonutTraderMod implements ClientModInitializer {
    public static final String MOD_ID = "donut-trader";
    public static ModConfig CONFIG = new ModConfig();
    public static boolean isRunning = false;
    
    private HighFrequencyTrader hfTrader;
    private WebhookHandler webhook;
    
    @Override
    public void onInitializeClient() {
        CONFIG.load();
        
        webhook = new WebhookHandler();
        webhook.initialize(CONFIG.discordWebhookUrl);
        
        hfTrader = new HighFrequencyTrader(webhook);
        
        // Keybind R - Toggle ON/OFF
        KeyBinding toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.donuttrader.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.donuttrader"
        ));
        
        // Keybind Y - Status
        KeyBinding statusKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.donuttrader.status",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "category.donuttrader"
        ));
        
        // Keybind U - Turbo Mode
        KeyBinding turboKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.donuttrader.turbo",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            "category.donuttrader"
        ));
        
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey.wasPressed()) {
                isRunning = !isRunning;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal(
                        "§6[DonutTrader] §f" + (isRunning ? "§a§lON" : "§c§lOFF")
                    ), false);
                }
            }
            
            if (statusKey.wasPressed() && client.player != null) {
                client.player.sendMessage(Text.literal(
                    "§b[Status] " + hfTrader.getStatus()
                ), false);
            }
            
            if (turboKey.wasPressed() && client.player != null) {
                hfTrader.toggleTurbo();
                client.player.sendMessage(Text.literal(
                    "§e[Turbo] " + (hfTrader.isTurbo() ? "§aENABLED" : "§cDISABLED")
                ), false);
            }
            
            if (isRunning) {
                hfTrader.tick(client);
            }
        });
    }
}
