package io.github.haintrain.soulbind;

import net.minegrid.obelisk.api.Module;
import net.minegrid.obelisk.api.User;
import net.minegrid.obelisk.api.UserMask;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.haintrain.soulbind.SoulbindEnchant.isSoulbound;

public class SoulbindListeners implements Listener{

    private SoulbindEnchant ench;
    private Module mod;

    public SoulbindListeners(SoulbindEnchant ench, Module mod){
        this.ench = ench;
        this.mod = mod;
    }

    @EventHandler
    void onItemDrop(PlayerDropItemEvent event){
        ItemStack item = event.getItemDrop().getItemStack();
        Player player = event.getPlayer();

        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(mod, uuid);
        Boolean override = u.getVarElseSetDefault("soulbindOverride", false);

        if(isSoulbound(item) && !override){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot throw out soulbound items!");
        }
    }

    @EventHandler
    void onItemDrag(InventoryDragEvent event){
        ItemStack item = event.getOldCursor();
        Player player = (Player) event.getWhoClicked();

        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(mod, uuid);
        Boolean override = u.getVarElseSetDefault("soulbindOverride", false);

        if (isSoulbound(item) && !override) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(ChatColor.DARK_PURPLE + "You cannot move soulbound items to chests or storage.");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Player player = event.getPlayer();

        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(mod, uuid);
        Boolean override = u.getVarElseSetDefault("soulbindOverride", false);

        if(isSoulbound(item) && !override){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "This item can not be placed, it is bound too tightly.");
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(event.getItem() != null) {
            ItemStack item = event.getItem();

            if (isSoulbound(item)) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ChatColor.RED + "This item can not be used, it is bound too tightly.");
            }
        }
    }

    @EventHandler
    void onPlayerDamageEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            if (isSoulbound(player.getInventory().getItemInMainHand())) {
                event.setCancelled(true);
                event.getDamager().sendMessage(ChatColor.RED + "This item can not be used, it is bound too tightly.");
            }
        }
    }

    @EventHandler
    void onBowShoot(final EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Integer first = player.getInventory().first(Material.ARROW);
            ItemStack item = player.getInventory().getItem(first);
            if (isSoulbound(item)){
                event.setCancelled(true);
                event.getEntity().sendMessage(ChatColor.RED + "Why did you even soulbind an arrow? You can't use it");
                player.updateInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();

        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(mod, uuid);

        ItemStack item = event.getCurrentItem();
        Inventory top = player.getOpenInventory().getTopInventory();
        Boolean override = u.getVarElseSetDefault("soulbindOverride", false);

        if(!override) {
            if (item == null) {
                return;
            }

            if ((isArmor(item.getType()) && (event.getClick() == ClickType.SHIFT_RIGHT || event.getClick() == ClickType.SHIFT_LEFT)) || ((event.getClick() == ClickType.SHIFT_RIGHT || event.getClick() == ClickType.SHIFT_LEFT) && top.getType() != InventoryType.CRAFTING)) {
                if (isSoulbound(item)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.DARK_PURPLE + "You cannot move soulbound items to chests or storage or equip as armor.");
                }
            }
            else if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                item = event.getCursor();
                if (isSoulbound(item)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.DARK_PURPLE + "You cannot move soulbound items to chests or storage.");
                }
            }
            else if (event.getAction() == InventoryAction.HOTBAR_SWAP || event.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD)  {
                int num = event.getHotbarButton();
                item = player.getInventory().getItem(num);
                if (isSoulbound(item)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.DARK_PURPLE + "You cannot move soulbound items to chests or storage.");
                }
            }
            else if (event.getClickedInventory() != player.getInventory()) {
                item = event.getCursor();
                if (isSoulbound(item)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.DARK_PURPLE + "You cannot move soulbound items to chests or storage.");
                }
            }
        }
    }


    @EventHandler
    void onPlayerDeath(PlayerDeathEvent event){
        Boolean playerKilled = false;


        if(event.getEntity().getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK){
            if(event.getEntity() instanceof Player){
                playerKilled = true;
            }
        }

        List<ItemStack> inventoryKeep = new ArrayList<>();

        Player player = event.getEntity();
        UserMask u = User.getMask(mod, player.getUniqueId());
        Boolean remove = true;

        player.sendMessage(event.getDrops().toString());

        while (remove) {
            remove = false;
            for(ItemStack item : event.getDrops()){
                if(isSoulbound(item)) {
                    Integer lvl = getEnchLevel(item);

                    switch(lvl) {
                        case 1:
                            if(!playerKilled) {
                                inventoryKeep.add(item);
                                event.getDrops().remove(item);
                            }
                            item.removeEnchantment(ench);
                            break;
                        case 2:
                            item.removeEnchantment(ench);
                            inventoryKeep.add(item);
                            event.getDrops().remove(item);
                            break;
                        case 3:
                            inventoryKeep.add(item);
                            event.getDrops().remove(item);
                            break;
                        case 4:
                            inventoryKeep.add(item);
                            event.getDrops().remove(item);
                            break;
                    }

                    remove = true;
                    break;
                }
            }
        }

        if (inventoryKeep.size() > 0) {
            u.setVar("items", inventoryKeep);
            player.sendMessage(inventoryKeep.toString());
        }
    }

    @EventHandler
    public void onPlayerSpawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UserMask u = User.getMask(mod, player.getUniqueId());

        ArrayList<ItemStack> items = u.getVarElseSetDefault("items", null);
        Optional.ofNullable(items).ifPresent(value -> {for(ItemStack item: value){player.getInventory().addItem(item);}});
    }

    private Integer getEnchLevel(ItemStack item){
        return item.getEnchantmentLevel(ench);
    }


    private boolean isArmor(Material mat) {
        switch (mat) {
            case JACK_O_LANTERN:
            case GOLD_HELMET:
            case GOLD_BOOTS:
            case GOLD_CHESTPLATE:
            case GOLD_LEGGINGS:
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_BOOTS:
            case LEATHER_LEGGINGS:
            case CHAINMAIL_HELMET:
            case CHAINMAIL_BOOTS:
            case CHAINMAIL_CHESTPLATE:
            case CHAINMAIL_LEGGINGS:
            case IRON_HELMET:
            case IRON_BOOTS:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case DIAMOND_HELMET:
            case DIAMOND_BOOTS:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case ELYTRA:
                return true;
            default:
                return false;
        }
    }
}
