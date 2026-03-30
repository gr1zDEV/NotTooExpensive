package com.example.nottooexpensive.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;

import java.lang.reflect.Method;

/**
 * Ensures anvils never become "Too Expensive!" for survival players by capping any
 * computed cost above 39 down to 39.
 *
 * <p>Why two handlers:
 * <ul>
 *     <li>PrepareAnvilEvent updates the preview cost/result shown in the anvil UI.</li>
 *     <li>InventoryClickEvent is a safety net right before taking the result, so the charged
 *     level amount remains capped to the same value even if another plugin modifies it later.</li>
 * </ul>
 */
public final class AnvilCostCapListener implements Listener {

    private static final int VANILLA_TOO_EXPENSIVE_THRESHOLD = 40;
    private static final int MAX_ALLOWED_COST = 39;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();

        // Preserve all normal mechanics. Only adjust values that would trigger "Too Expensive!".
        capAnvilCostIfNeeded(anvil);

        // Some server implementations expose a configurable max repair cost.
        // Keeping it at 39 further aligns the UI with the intended cap when available.
        setMaximumRepairCostIfSupported(anvil, MAX_ALLOWED_COST);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAnvilResultClick(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getType() != InventoryType.ANVIL) {
            return;
        }

        // We only care about taking the output slot from the anvil container.
        if (event.getSlotType() != InventoryType.SlotType.RESULT || event.getRawSlot() != 2) {
            return;
        }

        HumanEntity clicker = event.getWhoClicked();
        if (clicker.getGameMode() == GameMode.CREATIVE) {
            // Creative mode behavior should remain natural/vanilla.
            return;
        }

        if (!(event.getView().getTopInventory() instanceof AnvilInventory anvil)) {
            return;
        }

        // Safety-net pass to ensure the charged value cannot exceed 39.
        capAnvilCostIfNeeded(anvil);
        setMaximumRepairCostIfSupported(anvil, MAX_ALLOWED_COST);
    }

    private static void capAnvilCostIfNeeded(AnvilInventory anvil) {
        int currentCost = anvil.getRepairCost();

        // Keep vanilla values 0..39 unchanged; only cap values that would be "Too Expensive!".
        if (currentCost >= VANILLA_TOO_EXPENSIVE_THRESHOLD) {
            anvil.setRepairCost(MAX_ALLOWED_COST);
        }
    }

    /**
     * Uses reflection so this plugin remains broadly compatible across Bukkit/Paper forks where
     * this method may or may not exist.
     */
    private static void setMaximumRepairCostIfSupported(AnvilInventory anvil, int value) {
        try {
            Method method = anvil.getClass().getMethod("setMaximumRepairCost", int.class);
            method.invoke(anvil, value);
        } catch (ReflectiveOperationException ignored) {
            // Method not available on this implementation/version; capped repair cost still works.
        }
    }
}
