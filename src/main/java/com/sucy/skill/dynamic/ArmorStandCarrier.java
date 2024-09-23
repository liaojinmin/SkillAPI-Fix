package com.sucy.skill.dynamic;

import me.neon.libs.carrier.CarrierBase;
import me.neon.libs.carrier.PacketAPI;
import me.neon.libs.carrier.PacketHandler;
import me.neon.libs.carrier.minecraft.meta.ArmorStandMeta;
import me.neon.libs.util.BoundingBox;
import org.bukkit.Location;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic
 *
 * @author 老廖
 * @since 2024/5/3 12:34
 */
public class ArmorStandCarrier extends CarrierBase {

    private final BoundingBox box;

    private Location loc;

    private String name = "ArmorStandCarrier";

    private ItemStack itemStack = null;

    public ArmorStandCarrier(Location loc, ArmorStandMeta meta) {
        this.loc = loc;
        this.box = BoundingBox.Companion.of(loc, loc);
        setCarrierMeta(meta);
    }

    @NotNull
    @Override
    public BoundingBox getBoundingBox() {
        return box;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public void setDisplayName(@NotNull String s) {
        name = s;
    }

    public void addItemsStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        registerSpawn(it -> {
            PacketAPI.INSTANCE.getEntityOperatorHandler().sendEquipment(it, getEntityId(), EquipmentSlot.HEAD, itemStack);
        });

    }

    @NotNull
    @Override
    public Location getEyeLocation() {
        return loc.clone().add(0.0, 1.2, 0.0);
    }

    @NotNull
    @Override
    public Location getLocation() {
        return loc;
    }

    public void setLocation(Location location) {
        this.loc = location;
    }

    @Override
    public boolean getDisplayNameVisible() {
        return false;
    }

    @Override
    public void setDisplayNameVisible(boolean b) {

    }
}
