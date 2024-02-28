package org.rammex.moreitems.events;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.rammex.moreitems.Moreitems;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class FunnelPickupEvent implements Listener {
    Moreitems plugin;

    public FunnelPickupEvent(Moreitems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();

        if (block.getType() == Material.HOPPER) {
            ItemStack item = event.getItemInHand();
            ItemMeta meta = item.getItemMeta();
            if (meta.getDisplayName().equalsIgnoreCase(plugin.getItemConf().getString("entonnoir.name"))) {
                int radius = Integer.parseInt(meta.getLore().get(1).split(": ")[1].split(" ")[0]);

                // Créer un armor stand invisible au-dessus du hopper
                Location location = block.getLocation().add(0.5, 1, 0.5);
                ArmorStand armorStand = block.getWorld().spawn(location, ArmorStand.class);
                armorStand.setCustomName("Hopper x " + radius);
                armorStand.setCustomNameVisible(true);
                armorStand.setGravity(false);
                armorStand.setVisible(false);
                armorStand.setMarker(true);

                Collection<Entity> entities = block.getWorld().getNearbyEntities(block.getLocation(), radius, radius, radius);
                Collection<Item> items = new ArrayList<>();

                for (Entity entity : entities) {
                    if (entity instanceof Item) {
                        items.add((Item) entity);
                    }
                }

                Hopper hopper = (Hopper) block.getState();
                for (Item droppedItem : items) {
                    hopper.getInventory().addItem(droppedItem.getItemStack());
                    droppedItem.remove();
                }
            }
        }
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();

        if (block.getType() == Material.HOPPER) {
            Collection<Entity> entities = block.getWorld().getNearbyEntities(block.getLocation(), 1, 2, 1);
            for (Entity entity : entities) {
                if (entity instanceof ArmorStand) {
                    ArmorStand armorStand = (ArmorStand) entity;
                    if (armorStand.getCustomName() != null && armorStand.getCustomName().startsWith("Hopper x ")) {
                        armorStand.remove();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        plugin.getLogger().info("Item spawned");

        int chunkRadius = 1; // Change this to increase the search radius
        Chunk centerChunk = item.getLocation().getChunk();
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                Chunk chunk = centerChunk.getWorld().getChunkAt(centerChunk.getX() + dx, centerChunk.getZ() + dz);
                for (BlockState blockState : chunk.getTileEntities()) {
                    if (blockState instanceof Hopper) {
                        Hopper hopper = (Hopper) blockState;
                        plugin.getLogger().info("Hopper found");
                        Collection<Entity> nearbyEntities = hopper.getWorld().getNearbyEntities(hopper.getLocation(), 1, 2, 1);
                        for (Entity nearbyEntity : nearbyEntities) {
                            if (nearbyEntity instanceof ArmorStand) {
                                ArmorStand armorStand = (ArmorStand) nearbyEntity;
                                if (armorStand.getCustomName() != null && armorStand.getCustomName().startsWith("Hopper x ")) {
                                    plugin.getLogger().info("Armor stand found");
                                    int radius = Integer.parseInt(armorStand.getCustomName().split("x ")[1]);
                                    Collection<Entity> itemsInRange = hopper.getWorld().getNearbyEntities(hopper.getLocation(), radius, radius, radius);
                                    for (Entity entityInRange : itemsInRange) {
                                        if (entityInRange instanceof Item) {
                                            Item itemInRange = (Item) entityInRange;
                                            plugin.getLogger().info("Item in range");
                                            if (isHopperNotFull(hopper, itemInRange)) {
                                                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                                    HashMap<Integer, ItemStack> remaining = hopper.getInventory().addItem(itemInRange.getItemStack());
                                                    if (remaining.isEmpty()) {
                                                        plugin.getLogger().info("Item successfully added to hopper");
                                                        itemInRange.remove();
                                                    } else {
                                                        plugin.getLogger().info("Failed to add item to hopper");
                                                    }
                                                }, 40L); // 40 ticks = 2 seconds delay
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isHopperNotFull(Hopper hopper, Item itemInRange) {
        Inventory inventory = hopper.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item == null || (item.isSimilar(itemInRange.getItemStack()) && item.getAmount() < item.getMaxStackSize())) {
                return true;
            }
        }
        return false;
    }
}