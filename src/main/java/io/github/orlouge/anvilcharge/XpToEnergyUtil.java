package io.github.orlouge.anvilcharge;

import net.minecraft.item.ItemStack;
import team.reborn.energy.api.base.SimpleBatteryItem;

public class XpToEnergyUtil {

    public static int getXpCost(long energy) {
        return ((int) energy / AnvilChargeMod.ENERGY_PER_XP) +
               (energy % AnvilChargeMod.ENERGY_PER_XP == 0 ? 0 : 1);
    }

    public static int getLevelCost(int xpCost, int xpLevel, int xpTotal) {
        return xpLevel - (int) Math.floor(getLevelXp(xpTotal - xpCost));
    }

    public static boolean isBattery(ItemStack stack) {
        return stack.getItem() instanceof SimpleBatteryItem;
    }

    public static long getChargeAmount(ItemStack stack, int playerXp) {
        return Math.min(
                getEnergyPerXp(playerXp),
                ((SimpleBatteryItem) stack.getItem()).getEnergyCapacity() - getEnergy(stack)
        );
    }

    public static void addEnergy(ItemStack stack, long energy) {
        setEnergy(stack, getEnergy(stack) + energy);
    }

    private static long getEnergyPerXp(int xp) {
        return xp * AnvilChargeMod.ENERGY_PER_XP;
    }

    private static long getEnergy(ItemStack stack) {
        return ((SimpleBatteryItem) stack.getItem()).getStoredEnergy(stack);
    }

    private static void setEnergy(ItemStack stack, long energy) {
        ((SimpleBatteryItem) stack.getItem()).setStoredEnergy(stack, energy);
    }

    // stolen from the minecraft wiki
    private static double getLevelXp(int xpTotal) {
        double xp = (double) xpTotal;
        if (xp >= 1508) {
            return 18.056 + Math.sqrt(0.222 * (xp - 725.986));
        } else if (xp >= 353) {
            return 8.1 + Math.sqrt(0.4 * (xp - 195.975));
        } else {
            return Math.sqrt(xp + 9) - 3;
        }
    }
}
