package io.github.haintrain.soulbind;

import net.minegrid.module.crowns.CrownAPI;
import net.minegrid.module.crowns.CrownEmporium;
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
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Info(name = "Test", version = "1.0", color = ChatColor.WHITE, displayName = "Test", dependencies = {CrownEmporium.class })
public class Soulbind extends JavaModule implements ObeliskListener{

    private static transient Soulbind instance;
    private transient CrownPurchase token;

    @Override
    public void onEnable() {
        Obelisk.registerCommands(this, this);
        instance = this;
        token = CrownAPI.buildPurchase("Test Token", CrownIcon.build("More Test", Material.LEATHER_BOOTS)
                .setDesc("Test")
                .build(), 500, player -> {
            setToken(player);
        }, null).build();
        CrownAPI.addPurchase(token);
    }

    @SuppressWarnings("unchecked")
    public void onDisable() {

    }

    public static Soulbind getInstance() {
        return instance;
    }

    @OCmd(cmd = "soulbind", info = "Makes item soulbound to you")
    void commandBind(Player player) {
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);

        Integer token = u.getVarElseSetDefault("token", 0);

        if(token > 0){
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
                    u.setVar("token", token - 1);

                    player.getInventory().setItemInMainHand(SoulbindEnchant.addBound(item));
                }
            }
        }
        else{
            player.sendMessage("Not enough token");
        }
    }

    @OCmd(cmd = "unsoulbind", info = "Makes item soulbound to you")
    void commandUnbind(Player player) {
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);

        Integer token = u.getVarElseSetDefault("token", 0);

        if(token > 0){
            if(player.getInventory().getItemInMainHand() instanceof ItemStack){
                if (player.getInventory().getItemInMainHand().getAmount() > 1) {
                    player.sendMessage("Maximum stack size for soulbound items is one.");
                }
                else if(player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.getByName("Soulbound"))) {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    ItemMeta itemmeta = item.getItemMeta();
                    ArrayList<String> lore = new ArrayList<String>();
                    item.setItemMeta(itemmeta);

                    player.getInventory().setItemInMainHand(SoulbindEnchant.removeBound(item));
                }
                else {
                    player.sendMessage("This item is already unsoulbound");
                }
            }
        }
        else{
            player.sendMessage("Not enough token");
        }
    }


    void setToken(Player player){
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);

        Integer token = u.getVarElseSetDefault("token", 0);
        u.setVar("token", token + 10);
    }


    @OCmd(cmd = "tokens", info = "View token")
    void getToken(Player player) {
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);

        Integer token = u.getVarElseSetDefault("token", 0);

        player.sendMessage("You have this many soulbind token: " + Integer.toString(token)) ;
    }


    @EventHandler
    void onItemDrop(PlayerDropItemEvent event){
        ItemStack item = event.getItemDrop().getItemStack();

        if(isSoulbound(item)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    void onItemDrag(InventoryDragEvent event){
        ItemStack item = event.getOldCursor();

        if (isSoulbound(item)) {
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
    public void onPlayerInteract(PlayerInteractEvent event){
        ItemStack item = event.getItem();

        if(isSoulbound(item)){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        ItemStack item = event.getCurrentItem();
        Integer slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getClickedInventory();
        Inventory top = player.getOpenInventory().getTopInventory();

        if((isArmor(item.getType()) && (event.getClick() == ClickType.SHIFT_RIGHT || event.getClick() == ClickType.SHIFT_LEFT)) || ((event.getClick() == ClickType.SHIFT_RIGHT || event.getClick() == ClickType.SHIFT_LEFT) && top.getType() != InventoryType.CRAFTING)) {
            if (isSoulbound(item)) {
                event.setCancelled(true);
            }
        }

        if(event.getClickedInventory() != player.getInventory()){
            item = event.getCursor();
            if (isSoulbound(item)) {
                event.setCancelled(true);
            }
        }

        if(event.getSlotType() == InventoryType.SlotType.ARMOR){
            item = event.getCursor();
            if (isSoulbound(item)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    void onPlayerDeath(PlayerDeathEvent event){
        ItemStack [] inventory;
        List<ItemStack> inventoryKeep = new ArrayList<ItemStack>();

        Player player = event.getEntity();
        UserMask u = User.getMask(this, player.getUniqueId());
        Boolean remove = true;

        player.sendMessage(event.getDrops().toString());

        while (remove) {
            remove = false;
            for(ItemStack item : event.getDrops()){
            ItemMeta meta = item.getItemMeta();
            if(isSoulbound(item)) {
                inventoryKeep.add(item);
                event.getDrops().remove(item);
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
        UserMask u = User.getMask(this, player.getUniqueId());

        ArrayList<ItemStack> items = u.getVarElseSetDefault("items", null);
        Optional.ofNullable(items).ifPresent(value -> {for(ItemStack item: value){player.getInventory().addItem(item);}});
    }

    private boolean isSoulbound(ItemStack item){
        return item.containsEnchantment(Enchantment.getByName("Soulbound"));
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

