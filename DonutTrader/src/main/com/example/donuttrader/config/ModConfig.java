package com.example.donuttrader.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(
        FabricLoader.getInstance().getConfigDir().toFile(), 
        "donut-trader.json"
    );
    
    public boolean enabled = true;
    public int scanInterval = 10;
    public boolean autoSell = true;
    public int minProfitPercent = 15;
    public int maxConcurrentItems = 10;
    public String discordWebhookUrl = "YOUR_WEBHOOK_URL_HERE";
    public boolean webhookOnBuy = true;
    public boolean webhookOnSell = true;
    public int minDelayTicks = 2;
    public int maxDelayTicks = 4;
    public boolean onlyHighTier = false;
    public int maxItemValue = 1000000;
    public boolean safetyMode = true;
    
    public void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded != null) {
                    this.discordWebhookUrl = loaded.discordWebhookUrl;
                    this.minProfitPercent = loaded.minProfitPercent;
                    this.safetyMode = loaded.safetyMode;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
