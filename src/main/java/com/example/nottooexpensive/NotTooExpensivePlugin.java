package com.example.nottooexpensive;

import com.example.nottooexpensive.listener.AnvilCostCapListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main entrypoint for NotTooExpensive.
 */
public final class NotTooExpensivePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new AnvilCostCapListener(), this);
        getLogger().info("NotTooExpensive enabled: anvil costs above 39 will be capped to 39.");
    }

    @Override
    public void onDisable() {
        getLogger().info("NotTooExpensive disabled.");
    }
}
