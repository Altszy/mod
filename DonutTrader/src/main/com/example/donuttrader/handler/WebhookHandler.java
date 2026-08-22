package com.example.donuttrader.handler;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.minecraft.item.ItemStack;
import java.awt.Color;
import java.time.Instant;

public class WebhookHandler {
    private WebhookClient client;
    private boolean enabled = false;
    
    public void initialize(String url) {
        if (url == null || url.isEmpty() || url.contains("https://discord.com/api/webhooks/1462715101185310864/a12xl5rMTM8ew1kY0QoYBi5lbD65yvu5irZ2KCqD6IKsYwVpG0Lw3ucTIbH3M3PWFlo8")) {
            enabled = false;
            return;
        }
        try {
            WebhookClientBuilder builder = new WebhookClientBuilder(url);
            builder.setWait(false);
            this.client = builder.build();
            enabled = true;
        } catch (Exception e) {
            enabled = false;
        }
    }
    
    public void sendBuyNotification(ItemStack item, int buy, int sell, int profitPct, String seller) {
        if (!enabled) return;
        
        int profit = sell - buy;
        
        WebhookEmbedBuilder embed = new WebhookEmbedBuilder()
            .setColor(Color.GREEN.getRGB())
            .setTitle("✅ Auto-Buy")
            .setDescription("**" + item.getName().getString() + "**")
            .addField(new WebhookEmbedBuilder.EmbedField(false, "Buy", "$" + format(buy)))
            .addField(new WebhookEmbedBuilder.EmbedField(false, "Sell", "$" + format(sell)))
            .addField(new WebhookEmbedBuilder.EmbedField(false, "Profit", "$" + format(profit) + " (" + profitPct + "%)"))
            .setTimestamp(Instant.now());
        
        WebhookMessageBuilder msg = new WebhookMessageBuilder()
            .setUsername("DonutTrader")
            .addEmbed(embed.build());
        
        client.send(msg.build());
    }
    
    public void sendSellNotification(ItemStack item, int price, int hold) {
        if (!enabled) return;
        
        WebhookEmbedBuilder embed = new WebhookEmbedBuilder()
            .setColor(Color.BLUE.getRGB())
            .setTitle("🏷️ Sold")
            .setDescription("**" + item.getName().getString() + "**")
            .addField(new WebhookEmbedBuilder.EmbedField(false, "Price", "$" + format(price)))
            .addField(new WebhookEmbedBuilder.EmbedField(false, "Hold Time", hold + "m"))
            .setTimestamp(Instant.now());
        
        WebhookMessageBuilder msg = new WebhookMessageBuilder()
            .setUsername("DonutTrader")
            .addEmbed(embed.build());
        
        client.send(msg.build());
    }
    
    private String format(int n) {
        if (n >= 1000000) return String.format("%.1fM", n/1000000.0);
        if (n >= 1000) return String.format("%.1fK", n/1000.0);
        return String.valueOf(n);
    }
}
