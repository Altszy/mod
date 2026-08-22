package com.example.donuttrader.hf;

import com.example.donuttrader.DonutTraderMod;
import com.example.donuttrader.data.PriceDatabase;
import com.example.donuttrader.handler.WebhookHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class HighFrequencyTrader {
    private final WebhookHandler webhook;
    private final AtomicInteger hourlyProfit = new AtomicInteger(0);
    private final AtomicInteger hourlyTrades = new AtomicInteger(0);
    private final List<PurchasedItem> inventory = new ArrayList<>();
    
    private int tickCounter = 0;
    private int buyCooldown = 0;
    private boolean turboMode = false;
    private long startTime = 0;
    private long lastHourReset = System.currentTimeMillis();
    private Random random = new Random();
    
    public HighFrequencyTrader(WebhookHandler webhook) {
        this.webhook = webhook;
    }
    
    public void tick(MinecraftClient client) {
        if (startTime == 0) startTime = System.currentTimeMillis();
        tickCounter++;
        
        // Reset hourly
        if (System.currentTimeMillis() - lastHourReset >= 3600000) {
            hourlyProfit.set(0);
            hourlyTrades.set(0);
            lastHourReset = System.currentTimeMillis();
        }
        
        if (buyCooldown > 0) buyCooldown--;
        
        if (client.currentScreen instanceof GenericContainerScreen) {
            scanAndBuy(client);
        } else {
            sellItems(client);
        }
    }
    
    private void scanAndBuy(MinecraftClient client) {
        if (buyCooldown > 0) return;
        if (!(client.currentScreen instanceof GenericContainerScreen)) return;
        
        GenericContainerScreen screen = (GenericContainerScreen) client.currentScreen;
        String title = screen.getTitle().getString().toLowerCase();
        if (!title.contains("auction") && !title.contains("ah")) return;
        
        var handler = screen.getScreenHandler();
        Deal bestDeal = null;
        
        // Scan all slots quickly
        for (var slot : handler.slots) {
            if (!slot.hasStack()) continue;
            
            ItemStack stack = slot.getStack();
            Identifier id = Registries.ITEM.getId(stack.getItem());
            String itemId = id.toString();
            
            if (!PriceDatabase.hasEntry(itemId)) continue;
            
            int price = FastPriceParser.parse(stack);
            if (price <= 0) continue;
            
            var data = PriceDatabase.getData(itemId).get();
            int profit = PriceDatabase.calculatePotentialProfit(itemId, price);
            int profitPct = PriceDatabase.getProfitPercent(itemId, price);
            
            int minProfit = turboMode ? 10 : 15;
            
            if (profitPct >= minProfit && profit >= 30000 && price <= data.maxPrice) {
                if (bestDeal == null || profit > bestDeal.profit) {
                    bestDeal = new Deal(slot.id, stack, itemId, price, profit, profitPct);
                }
            }
        }
        
        if (bestDeal != null) {
            executeBuy(client, bestDeal);
        }
    }
    
    private void executeBuy(MinecraftClient client, Deal deal) {
        if (!(client.currentScreen instanceof GenericContainerScreen)) return;
        
        GenericContainerScreen screen = (GenericContainerScreen) client.currentScreen;
        
        // Click to buy
        client.interactionManager.clickSlot(
            screen.getScreenHandler().syncId,
            deal.slotId,
            0,
            SlotActionType.PICKUP,
            client.player
        );
        
        int sellPrice = PriceDatabase.calculateOptimalSellPrice(deal.itemId);
        inventory.add(new PurchasedItem(deal.stack, deal.itemId, deal.price, sellPrice));
        
        hourlyProfit.addAndGet(deal.profit);
        hourlyTrades.incrementAndGet();
        
        // Notify every 5 trades
        if (hourlyTrades.get() % 5 == 0) {
            client.player.sendMessage(Text.literal(String.format(
                "§a[HF] §f%d trades §7| §6%s §7| §a%s/h",
                hourlyTrades.get(),
                formatMoney(hourlyProfit.get()),
                formatMoney(hourlyProfit.get())
            )), true);
        }
        
        if (deal.profit >= 100000) {
            webhook.sendBuyNotification(deal.stack, deal.price, sellPrice, deal.profitPercent, "Snipe");
        }
        
        // Cooldown: 2-4 ticks normal, 1-2 ticks turbo
        buyCooldown = turboMode ? 1 + random.nextInt(2) : 2 + random.nextInt(3);
    }
    
    private void sellItems(MinecraftClient client) {
        if (inventory.isEmpty()) return;
        if (client.player == null) return;
        
        PurchasedItem item = inventory.get(0);
        int holdTime = (int)((System.currentTimeMillis() - item.buyTime) / 60000);
        
        // Dynamic pricing
        int sellPrice = item.sellPrice;
        if (holdTime > 10) sellPrice = (int)(sellPrice * 0.95);
        if (holdTime > 20) sellPrice = (int)(sellPrice * 0.90);
        
        client.player.networkHandler.sendCommand("ah sell " + sellPrice);
        inventory.remove(0);
        
        webhook.sendSellNotification(item.stack, sellPrice, holdTime);
    }
    
    public void toggleTurbo() {
        turboMode = !turboMode;
    }
    
    public boolean isTurbo() {
        return turboMode;
    }
    
    public String getStatus() {
        long runtime = Math.max(1, (System.currentTimeMillis() - startTime) / 60000);
        long profitRate = (hourlyProfit.get() * 60) / runtime;
        
        return String.format(
            "§a%s§7|§f%d§7t|§6%s§7/h",
            turboMode ? "TURBO" : "NORMAL",
            hourlyTrades.get(),
            formatMoney(profitRate)
        );
    }
    
    private String formatMoney(long amount) {
        if (amount >= 1000000) return String.format("%.1fM", amount/1000000.0);
        if (amount >= 1000) return String.format("%.1fK", amount/1000.0);
        return String.valueOf(amount);
    }
    
    private record Deal(int slotId, ItemStack stack, String itemId, int price, int profit, int profitPercent) {}
    
    private static class PurchasedItem {
        final ItemStack stack;
        final String itemId;
        final int buyPrice;
        final int sellPrice;
        final long buyTime;
        
        PurchasedItem(ItemStack s, String id, int bp, int sp) {
            this.stack = s;
            this.itemId = id;
            this.buyPrice = bp;
            this.sellPrice = sp;
            this.buyTime = System.currentTimeMillis();
        }
    }
}
