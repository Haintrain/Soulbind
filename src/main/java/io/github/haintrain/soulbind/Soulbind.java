package io.github.haintrain.soulbind;

import io.github.archemedes.customitem.CustomTag;
import net.minegrid.module.crowns.CrownAPI;
import net.minegrid.module.crowns.CrownEmporium;
import net.minegrid.module.crowns.CrownIcon;
import net.minegrid.module.crowns.CrownPurchase;
import net.minegrid.obelisk.api.*;
import net.minegrid.obelisk.api.command.OCmd;
import net.minegrid.obelisk.api.command.OCmdMod;
import net.minegrid.obelisk.api.command.ObeliskListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.security.Permission;
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
        Obelisk.registerCommands(new SoulbindCommand(ench, this), this);
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

        for(PermissionAttachmentInfo perm: player.getEffectivePermissions()){
            String permission = perm.getPermission();
            if(permission.startsWith("soulbind.bind")){
                String[] permSplit = permission.split(".");
                int max = Integer.parseInt(permSplit[2]);

                u.setVarTemp("bindMax", max);
            }
        }

        Integer bindCurrent = u.getVarElseSetDefault("bindCurrent", 0);
        u.setVar("bindCurrent", bindCurrent);
    }

    private void setToken(Player player, Integer num){
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(this, uuid);

        Integer token = u.getVarElseSetDefault("token", 0);
        u.setVar("token", token + num);
    }
}

