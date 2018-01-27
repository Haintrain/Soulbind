package io.github.haintrain.soulbind;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class SoulbindEnchant extends Enchantment{

    private static Enchantment bound;

    public SoulbindEnchant(Integer id) {
        super(id);
    }

    @Override
    public boolean canEnchantItem(ItemStack item) {
        return true;
    }

    @Override
    public boolean conflictsWith(Enchantment other) {
        return false;
    }

    @Override
    public EnchantmentTarget getItemTarget() {
        return null;
    }

    @Override
    public int getMaxLevel() {
        return 4;
    }

    @Override
    public String getName() {
        return "Soulbound";
    }

    @Override
    public int getStartLevel() { return 1; }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }


    public static Enchantment getBound() {
        if (bound != null) return bound;

        if(Enchantment.getByName("Soulbound") != null)
            return Enchantment.getByName("Soulbound");

        try
        {
            Field f = Enchantment.class.getDeclaredField("acceptingNew");
            f.setAccessible(true);
            f.set(null, true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        bound = new SoulbindEnchant(101);
        Enchantment.registerEnchantment(bound);
        return bound;
    }

    public static ItemStack addBound(ItemStack item, Integer lvl) {
        Enchantment ench = getBound();

        if(!item.containsEnchantment(ench))
            item.addUnsafeEnchantment(ench, lvl);

        return item;
    }

    public static ItemStack removeBound(ItemStack item) {
        Enchantment ench = getBound();

        if(item.containsEnchantment(ench))
            item.removeEnchantment(ench);

        return item;
    }

    public static boolean isSoulbound(ItemStack item){
        Enchantment ench = getBound();

        return item.containsEnchantment(ench);
    }

}