package io.github.haintrain.soulbind;

import io.github.archemedes.customitem.CustomTag;
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
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static io.github.haintrain.soulbind.SoulbindEnchant.isSoulbound;

@Info(name = "Soulbind", version = "1.0", color = ChatColor.WHITE, displayName = "Soulbind", dependencies = {CrownEmporium.class })
public class Soulbind extends JavaModule implements ObeliskListener{

    private SoulbindEnchant ench = new SoulbindEnchant(101);
    private static transient Soulbind instance;
    private CrownPurchase token1, token5, token10;

    @Override
    public void onEnable() {
        SoulbindEnchant.getBound();
        Obelisk.registerCommands(this, this);
        instance = this;

        registerEvents(new SoulbindListeners(ench, this));

        token1 = CrownAPI.buildPurchase("Token x1", CrownIcon.build("Token x1", Material.LEATHER_BOOTS)
                .setDesc("Test")
                .build(), 500, player -> {
            setToken(player, 1);
        }, null).build();
        CrownAPI.addPurchase(token1);

        token5 = CrownAPI.buildPurchase("Token x5", CrownIcon.build("Token x5", Material.LEATHER_BOOTS)
                .setDesc("Test")
                .build(), 500, player -> {
            setToken(player, 5);
        }, null).build();
        CrownAPI.addPurchase(token5);

        token10 = CrownAPI.buildPurchase("Token x10", CrownIcon.build("Token x10", Material.LEATHER_BOOTS)
                .setDesc("Test")
                .build(), 500, player -> {
            setToken(player, 10);
        }, null).build();
        CrownAPI.addPurchase(token10);
    }

    public void onDisable() {
    }

    public static Soulbind getInstance() {
        return instance;
    }

    public void onPlayerLogin(PlayerLoginEvent event){
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        UserMask u = User.getMask(this, uuid);
        u.setVarTemp("soulbindOverride", false);
    }


    @OCmd(cmd = "soulbind %i", info = "Makes item soulbound to you")
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
            if(player.getInventory().getItemInMainHand() != null){
                if (player.getInventory().getItemInMainHand().getAmount() > 1) {
                    player.sendMessage("Maximum stack size for soulbound items is one.");
                }
                else if(isSoulbound(player.getInventory().getItemInMainHand())) {
                    player.sendMessage("This item is already soulbound");
                }
                else {
                    ItemStack item = player.getInventory().getItemInMainHand();

                    CustomTag tag = CustomTag.getFrom(item);
                    tag.put("token", player.getUniqueId().toString());

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

        if(player.getInventory().getItemInMainHand() != null){
            if (player.getInventory().getItemInMainHand().getAmount() > 1) {
                player.sendMessage("Maximum stack size for soulbound items is one.");
            }
            else if(player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.getByName("Soulbound"))) {
                ItemStack item = player.getInventory().getItemInMainHand();

                Integer lvl = getEnchLevel(item);

                if(lvl > 1) {
                    CustomTag tag = CustomTag.getFrom(item);
                    String uuidToken = tag.get("token");

                    UserMask u = User.getMask(this, UUID.fromString(uuidToken));
                    Integer token = u.getVarElseSetDefault("token", 0);

                    u.setVar("token", token + lvl);


                    player.getInventory().setItemInMainHand(SoulbindEnchant.removeBound(item));
                }
            }
            else {
                player.sendMessage("This item is already unsoulbound");
            }
        }
    }

    @OCmd(cmd = "tokens", info = "View token")
    void getToken(Player player) {
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);

        Integer token = u.getVarElseSetDefault("token", 0);

        player.sendMessage("You have this many soulbind token: " + Integer.toString(token));
    }

    @OCmd(cmd = "override %P", info = "Override soulbound", perm = "soulbind.override")
    void overrideSoulbind(Player player, String args[]) {
        for (Player pl : player.getWorld().getPlayers()) {
            String name = ChatColor.stripColor(pl.getPlayerListName());

            if(name.equals(args[0])){
                UUID uuid = pl.getUniqueId();
                UserMask u = User.getMask(this, uuid);

                Boolean test = u.getVarElseSetDefault("soulbindOverride", false);

                if(!test){
                    u.setVarTemp("soulbindOverride", true);
                    player.sendMessage(ChatColor.BLUE + "Soulbind Override For: " + name + ", has been toggled on");
                }
                else {
                    u.setVarTemp("soulbindOverride", false);
                    player.sendMessage(ChatColor.BLUE + "Soulbind Override For: " + name + ", has been toggled off");
                }
            }
        }
    }


    private void setToken(Player player, Integer num){
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);

        Integer token = u.getVarElseSetDefault("token", 0);
        u.setVar("token", token + num);
    }

    private Integer getEnchLevel(ItemStack item){
        return item.getEnchantmentLevel(ench);
    }
}

