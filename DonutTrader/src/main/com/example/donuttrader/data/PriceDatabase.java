package com.example.donuttrader.data;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import java.util.*;

public class PriceDatabase {
    public static class ItemData {
        public final String id;
        public final int marketPrice;
        public final int minPrice;
        public final int maxPrice;
        public final String category;
        
        public ItemData(String id, int market, int min, int max, String cat) {
            this.id = id;
            this.marketPrice = market;
            this.minPrice = min;
            this.maxPrice = max;
            this.category = cat;
        }
    }
    
    private static final Map<String, ItemData> DB = new HashMap<>();
    
    static {
        // HIGH TIER - Big profits
        add("minecraft:enchanted_golden_apple", 50000, 35000, 48000, "HIGH");
        add("minecraft:netherite_sword", 75000, 50000, 72000, "HIGH");
        add("minecraft:netherite_chestplate", 90000, 65000, 85000, "HIGH");
        add("minecraft:netherite_helmet", 60000, 40000, 55000, "HIGH");
        add("minecraft:netherite_leggings", 80000, 55000, 75000, "HIGH");
        add("minecraft:netherite_boots", 50000, 35000, 48000, "HIGH");
        
        // MID TIER - Fast volume
        add("minecraft:end_crystal", 12000, 8000, 11500, "MID");
        add("minecraft:totem_of_undying", 15000, 10000, 14000, "MID");
        add("minecraft:experience_bottle", 8000, 5000, 7500, "MID");
        
        // FAST FLIP - High volume
        add("minecraft:golden_apple", 3000, 1800, 2800, "FAST");
        add("minecraft:ender_pearl", 2000, 1200, 1800, "FAST");
        add("minecraft:obsidian", 1500, 800, 1400, "FAST");
    }
    
    private static void add(String id, int m, int min, int max, String c) {
        DB.put(id, new ItemData(id, m, min, max, c));
    }
    
    public static Optional<ItemData> getData(String id) {
        return Optional.ofNullable(DB.get(id));
    }
    
    public static boolean hasEntry(String id) {
        return DB.containsKey(id);
    }
    
    public static int calculateOptimalSellPrice(String itemId) {
        Optional<ItemData> d = getData(itemId);
        if (d.isEmpty()) return 0;
        return (int)(d.get().marketPrice * 0.95);
    }
    
    public static int calculatePotentialProfit(String itemId, int buyPrice) {
        return calculateOptimalSellPrice(itemId) - buyPrice;
    }
    
    public static int getProfitPercent(String itemId, int buyPrice) {
        int profit = calculatePotentialProfit(itemId, buyPrice);
        if (buyPrice == 0) return 0;
        return (profit * 100) / buyPrice;
    }
}
