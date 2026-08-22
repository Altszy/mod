package com.example.donuttrader.hf;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class FastPriceParser {
    private static final boolean[] IS_DIGIT = new boolean[256];
    
    static {
        for (int i = '0'; i <= '9'; i++) IS_DIGIT[i] = true;
    }
    
    public static int parse(ItemStack stack) {
        if (!stack.hasNbt()) return -1;
        
        NbtCompound display = stack.getNbt().getCompound("display");
        if (display == null || !display.contains("Lore", 9)) return -1;
        
        NbtList lore = display.getList("Lore", 8);
        
        // Check from bottom up (price usually at end)
        for (int i = lore.size() - 1; i >= Math.max(0, lore.size() - 3); i--) {
            String line = lore.getString(i);
            int price = extractPrice(line);
            if (price > 0) return price;
        }
        return -1;
    }
    
    private static int extractPrice(String json) {
        int price = 0;
        boolean found = false;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (IS_DIGIT[c]) {
                price = price * 10 + (c - '0');
                found = true;
            } else if (found && c == ',') {
                continue;
            } else if (found && !IS_DIGIT[c]) {
                return price;
            }
        }
        return found ? price : -1;
    }
}
