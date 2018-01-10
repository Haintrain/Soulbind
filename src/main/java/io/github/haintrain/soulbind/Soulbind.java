package io.github.haintrain.soulbind;

import net.minegrid.module.crowns.CrownAPI;
import net.minegrid.module.crowns.CrownIcon;
import net.minegrid.module.crowns.CrownPurchase;
import net.minegrid.obelisk.api.*;
import net.minegrid.obelisk.api.command.OCmd;
import net.minegrid.obelisk.api.command.ObeliskListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;

@Info(name = "Test", version = "1.0", color = ChatColor.WHITE, displayName = "Test")
public class Soulbind extends JavaModule implements ObeliskListener{

    private static transient Soulbind instance;
    private transient CrownPurchase token;

    @Override
    public void onEnable() {
        Obelisk.registerCommands(this, this);
        instance = this;
        token = CrownAPI.buildPurchase("Test token", CrownIcon.build("More Test", Material.LEATHER_BOOTS)
                .setDesc("Test")
                .build(), 500, player -> {
            setToken(player);
        }, null).build();
        CrownAPI.addPurchase(token);
    }

    @SuppressWarnings("unchecked")
    public void onDisable() {

    }

    public static boolean isSoulbound(ItemStack item){
        return item.containsEnchantment(Enchantment.getByName("Soulbound"));
    }

    public static Soulbind getInstance() {
        return instance;
    }

    @SuppressWarnings("deprecation")
    @OCmd(cmd = "soulbind", info = "Makes item soulbound to you")
    void commandBind(Player player) {
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);

        Integer tokens = u.getVarElseSetDefault("tokens", 0);

        if(tokens > 0){
            if(player.getInventory().getItemInMainHand() instanceof ItemStack){
                if (player.getInventory().getItemInMainHand().getAmount() > 1) {
                    player.sendMessage("Maximum stack size for soulbound items is one.");
                }
                else if(player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.getByName("Soulbound"))) {
                    player.sendMessage("This item is already soulbound");
                }
                else {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    ItemMeta itemmeta = item.getItemMeta();
                    ArrayList<String> lore = new ArrayList<String>();
                    item.setItemMeta(itemmeta);
                    u.setVar("tokens", tokens - 1);

                    player.getInventory().setItemInMainHand(SoulbindEnchant.addBound(item));
                }
            }
        }
        else{
            player.sendMessage("Not enough tokens");
        }

    }

    @OCmd(cmd = "tokens", info = "View tokens")
    void getToken(Player player) {
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);

        Integer tokens = u.getVarElseSetDefault("tokens", 0);

        player.sendMessage("You have this many soulbind tokens: " + Integer.toString(tokens)) ;
    }

    void setToken(Player player){
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);

        Integer tokens = u.getVarElseSetDefault("tokens", 0);
        u.setVar("tokens", tokens + 1);
    }

    @EventHandler
    void onItemDrop(PlayerDropItemEvent event){
        ItemStack item = event.getItemDrop().getItemStack();

        if(isSoulbound(item)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onMoveDrop(InventoryMoveItemEvent event){
        ItemStack item = event.getItem();

        if(isSoulbound(item)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if(isSoulbound(item)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onPlayerDeath(PlayerDeathEvent event){
        ItemStack [] inventory;
        ArrayList<ItemStack> inventoryKeep = new ArrayList<ItemStack>();

        Player player = event.getEntity();
        UserMask u = User.getMask(this, player.getUniqueId());


        for(ItemStack item : event.getDrops()){
            ItemMeta meta = item.getItemMeta();
            if(isSoulbound(item)){
                inventoryKeep.add(item);
                event.getDrops().remove(item);
            }
        }

        if (inventoryKeep.size() > 0) {
            u.setVar("items", inventoryKeep);
        }
    }



    @EventHandler
    public void onPlayerSpawn(PlayerRespawnEvent event) {

        Player player = event.getPlayer();
        UserMask u = User.getMask(this, player.getUniqueId());

        ArrayList<ItemStack> items = u.getVarElseSetDefault( "items", null);
        Optional.ofNullable(items).ifPresent(value -> {for(ItemStack item: value){player.getInventory().addItem(item);}});
    }
}

