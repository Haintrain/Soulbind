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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static io.github.haintrain.soulbind.SoulbindEnchant.isSoulbound;

@Info(name = "Soulbind", version = "1.0", color = ChatColor.WHITE, displayName = "Soulbind", dependencies = {CrownEmporium.class })
public class Soulbind extends JavaModule implements ObeliskListener{

    public SoulbindEnchant ench = new SoulbindEnchant(101);
    private static transient Soulbind instance;
    private transient CrownPurchase token;

    @Override
    public void onEnable() {
        SoulbindEnchant.getBound();
        Obelisk.registerCommands(this, this);
        instance = this;
        token = CrownAPI.buildPurchase("Test Token", CrownIcon.build("More Test", Material.LEATHER_BOOTS)
                .setDesc("Test")
                .build(), 500, player -> {
            setToken(player);
        }, null).build();
        CrownAPI.addPurchase(token);
    }

    public void onDisable() {
    }

    public static Soulbind getInstance() {
        return instance;
    }

    @OCmd(cmd = "soulbind *", info = "Makes item soulbound to you")
    void commandBind(Player player, String args[]) {
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);
        Integer lvl = 1;

        if(args[0].equals("1") || args[0].equals("2") || args[0].equals("3") || args[0].equals("4")) {
            lvl = Integer.parseInt(args[0]);
        }
        else if(args[0].equals("force")){
            return;
        }

        Integer token = u.getVarElseSetDefault("token", 0);

        if(token > 0){
            if(player.getInventory().getItemInMainHand() instanceof ItemStack){
                if (player.getInventory().getItemInMainHand().getAmount() > 1) {
                    player.sendMessage("Maximum stack size for soulbound items is one.");
                }
                else if(isSoulbound(player.getInventory().getItemInMainHand())) {
                    player.sendMessage("This item is already soulbound");
                }
                else {
                    ItemStack item = player.getInventory().getItemInMainHand();
                    ItemMeta itemmeta = item.getItemMeta();
                    ArrayList<String> lore = new ArrayList<String>();
                    item.setItemMeta(itemmeta);
                    u.setVar("token", token - lvl);

                    player.getInventory().setItemInMainHand(SoulbindEnchant.addBound(item, lvl));
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

        if(player.getInventory().getItemInMainHand() instanceof ItemStack){
            if (player.getInventory().getItemInMainHand().getAmount() > 1) {
                player.sendMessage("Maximum stack size for soulbound items is one.");
            }
            else if(player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.getByName("Soulbound"))) {
                ItemStack item = player.getInventory().getItemInMainHand();
                ItemMeta itemmeta = item.getItemMeta();
                ArrayList<String> lore = new ArrayList<String>();
                item.setItemMeta(itemmeta);
                Integer lvl = getEnchLevel(item);
                if(lvl > 1) {
                    u.setVar("token", token + lvl);
                }

                player.getInventory().setItemInMainHand(SoulbindEnchant.removeBound(item));
            }
            else {
                player.sendMessage("This item is already unsoulbound");
            }
        }
    }


    void setToken(Player player){
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);

        Integer token = u.getVarElseSetDefault("token", 0);
        u.setVar("token", token + 1);
    }


    @OCmd(cmd = "tokens", info = "View token")
    void getToken(Player player) {
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);

        Integer token = u.getVarElseSetDefault("token", 0);

        player.sendMessage("You have this many soulbind token: " + Integer.toString(token));
    }


    @EventHandler
    void onItemDrop(PlayerDropItemEvent event){
        ItemStack item = event.getItemDrop().getItemStack();

        if(isSoulbound(item)){
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot throw out soulbound items!");
        }
    }

    @EventHandler
    void onItemDrag(InventoryDragEvent event){
        ItemStack item = event.getOldCursor();

        if (isSoulbound(item)) {
            event.setCancelled(true);
            event.getWhoClicked().sendMessage(ChatColor.DARK_PURPLE + "You cannot move soulbound items to chests or storage.");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if(isSoulbound(item)){
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
        ItemStack item = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        Inventory top = player.getOpenInventory().getTopInventory();

        if(item == null){
            return;
        }

        if((isArmor(item.getType()) && (event.getClick() == ClickType.SHIFT_RIGHT || event.getClick() == ClickType.SHIFT_LEFT)) || ((event.getClick() == ClickType.SHIFT_RIGHT || event.getClick() == ClickType.SHIFT_LEFT) && top.getType() != InventoryType.CRAFTING)) {
            if (isSoulbound(item)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.DARK_PURPLE + "You cannot move soulbound items to chests or storage or equip as armor.");
            }
        }

        if(event.getClickedInventory() != player.getInventory()){
            item = event.getCursor();
            if (isSoulbound(item)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.DARK_PURPLE + "You cannot move soulbound items to chests or storage.");
            }
        }

        if(event.getSlotType() == InventoryType.SlotType.ARMOR){
            item = event.getCursor();
            if (isSoulbound(item)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.DARK_PURPLE + "You cannot move soulbound items to chests or storage.");
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
                Integer lvl = getEnchLevel(item);

                switch(lvl) {
                    case 1:
                        if(playerKilled == false) {
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
        UserMask u = User.getMask(this, player.getUniqueId());

        ArrayList<ItemStack> items = u.getVarElseSetDefault("items", null);
        Optional.ofNullable(items).ifPresent(value -> {for(ItemStack item: value){player.getInventory().addItem(item);}});
    }

    public Integer getEnchLevel(ItemStack item){
        Integer lvl = item.getEnchantmentLevel(ench);
        return lvl;
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

