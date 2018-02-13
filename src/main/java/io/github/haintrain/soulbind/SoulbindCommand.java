package io.github.haintrain.soulbind;


import io.github.archemedes.customitem.CustomTag;
import net.minegrid.obelisk.api.Module;
import net.minegrid.obelisk.api.User;
import net.minegrid.obelisk.api.UserMask;
import net.minegrid.obelisk.api.command.OCmd;
import net.minegrid.obelisk.api.command.OCmdMod;
import net.minegrid.obelisk.api.command.ObeliskListener;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.UUID;

import static io.github.haintrain.soulbind.SoulbindEnchant.isSoulbound;

@OCmdMod(baseCmd = "soulbind/sb")
public class SoulbindCommand implements ObeliskListener{

    private SoulbindEnchant ench;
    private Module mod;

    public SoulbindCommand(SoulbindEnchant ench, Module mod){
        this.ench = ench;
        this.mod = mod;
    }

    @OCmd(cmd = "bind *", info = "Makes item soulbound to you")
    void commandBind(Player player, String args[]) {
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(mod, uuid);

        Integer token = u.getVarElseSetDefault("token", 0);

        player.sendMessage("test");


        if(token > 0 && args[0] == "token"){

            player.sendMessage("test1");
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

                    u.setVar("token", token - 1);



                    player.getInventory().setItemInMainHand(SoulbindEnchant.addBound(item, 1));
                }
            }
        }
        else if(args[0] == "token"){
            player.sendMessage("Not enough token");
        }

        Integer bindMax = u.getVarElseSetDefault("bindMax", 0);
        Integer bindCurrent = u.getVarElseSetDefault("bindCurrent", 0);

        if(bindCurrent < bindMax && args[0] == "natural"){

            player.sendMessage("test2");
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

                    u.setVar("bindCurrent", bindCurrent + 1);

                    player.getInventory().setItemInMainHand(SoulbindEnchant.addBound(item, 2));
                }
            }
        }
    }

    @OCmd(cmd = "unbind", info = "Makes item soulbound to you")
    void commandUnbind(Player player, String args[]) {

        if(player.getInventory().getItemInMainHand() != null){
            if (player.getInventory().getItemInMainHand().getAmount() > 1) {
                player.sendMessage("Maximum stack size for soulbound items is one.");
            }
            else if(player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.getByName("Soulbound"))) {
                ItemStack item = player.getInventory().getItemInMainHand();

                Integer lvl = getEnchLevel(item);

                if(lvl == 1) {
                    CustomTag tag = CustomTag.getFrom(item);
                    String uuidToken = tag.get("token");

                    UserMask u = User.getMask(mod, UUID.fromString(uuidToken));

                    player.getInventory().setItemInMainHand(SoulbindEnchant.removeBound(item));
                }
                else if(lvl == 2) {
                    CustomTag tag = CustomTag.getFrom(item);
                    String uuidToken = tag.get("token");

                    UserMask u = User.getMask(mod, UUID.fromString(uuidToken));
                    Integer bindCurrent = u.getVarElseSetDefault("currentBind", 0);

                    u.setVar("bindCurrent", bindCurrent + 1);


                    player.getInventory().setItemInMainHand(SoulbindEnchant.removeBound(item));
                }

            }
            else {
                player.sendMessage("This item is already unsoulbound");
            }
        }
    }

    @OCmd(cmd = "tokens", info = "View token")
    void getToken(Player player, String args[]) {
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(mod, uuid);

        Integer token = u.getVarElseSetDefault("token", 0);

        player.sendMessage("You have this many soulbind tokens: " + Integer.toString(token));
    }

    @OCmd(cmd = "natural", info = "View token")
    void getNatural(Player player, String args[]) {
        UUID uuid = player.getUniqueId();
        UserMask u = User.getMask(mod, uuid);

        Integer bindMax = u.getVarElseSetDefault("bindMax", 0);
        Integer bindCurrent = u.getVarElseSetDefault("bindCurrent", 0);

        player.sendMessage("You have this many soulbind tokens: " + Integer.toString(bindCurrent) + " out of " + Integer.toString(bindMax));
    }

    @OCmd(cmd = "perms", info = "Debug pemrs")
    void getPerms(Player player, String args[]){
        for(PermissionAttachmentInfo perm: player.getEffectivePermissions()){
            String permission = perm.getPermission();
            if(permission.startsWith("soulbind.bind.")){


            }

            player.sendMessage(permission);
        }
    }

    @OCmd(cmd = "override %P", info = "Override soulbound", perm = "soulbind.override")
    void overrideSoulbind(Player player, String args[]) {
        for (Player pl : player.getWorld().getPlayers()) {
            String name = ChatColor.stripColor(pl.getPlayerListName());

            if(name.equals(args[0])){
                UUID uuid = pl.getUniqueId();
                UserMask u = User.getMask(mod, uuid);

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

    private Integer getEnchLevel(ItemStack item){
        return item.getEnchantmentLevel(ench);
    }

}
