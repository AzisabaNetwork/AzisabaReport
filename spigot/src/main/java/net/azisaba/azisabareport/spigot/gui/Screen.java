package net.azisaba.azisabareport.spigot.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public abstract class Screen implements InventoryHolder {
    protected final Inventory inventory;

    public Screen(int size, @Nullable String title) {
        this.inventory = title != null ? Bukkit.createInventory(this, size, title) : Bukkit.createInventory(this, size);
    }

    public final void setItem(int slot, @NotNull Material type, int amount, @NotNull String displayName, @NotNull String @Nullable ... lore) {
        ItemStack item = new ItemStack(type, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(displayName);
            if (lore != null) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        inventory.setItem(slot, item);
    }

    @Override
    public final @NotNull Inventory getInventory() {
        return inventory;
    }
}
