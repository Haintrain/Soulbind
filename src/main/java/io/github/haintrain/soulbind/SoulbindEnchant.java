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
        return 2    ;
    }

    @Override
    public String getName() {
        return "Soulbound";
    }

    @Override
    public int getStartLevel() {
        return 1;
    }

    @Override
    public boolean isCursed() {
        return false;
    }

    @Override
    public boolean isTreasure() {
        return false;
    }


    public static Enchantment getBound()
    {
        if (bound != null) return bound;

        if(Enchantment.getByName("Glow") != null)
            return Enchantment.getByName("Glow");

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

        bound = new SoulbindEnchant(255);
        Enchantment.registerEnchantment(bound);
        return bound;
    }

    public static ItemStack addBound(ItemStack item)
    {
        Enchantment glow = getBound();

        if(!item.containsEnchantment(glow))
            item.addUnsafeEnchantment(glow, 1);

        return item;
    }

    public static ItemStack removeBound(ItemStack item)
    {
        Enchantment glow = getBound();

        if(item.containsEnchantment(glow))
            item.removeEnchantment(glow);

        return item;
    }
}