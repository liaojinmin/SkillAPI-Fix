package com.sucy.skill.dynamic;

import com.germ.germplugin.api.GermSrcManager;
import com.germ.germplugin.api.RootType;
import com.germ.germplugin.api.dynamic.effect.GermEffectEntity;
import me.neon.libs.carrier.CarrierBase;
import me.neon.libs.carrier.PacketAPI;
import me.neon.libs.carrier.PacketHandler;
import me.neon.libs.carrier.minecraft.meta.ArmorStandMeta;
import me.neon.libs.util.BoundingBox;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
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

    private GermEffectEntity effectEntity;

    private Location loc;

    private String name = "ArmorStandCarrier";

    private ItemStack itemStack = null;

    private final EntityType type;


    @NotNull
    @Override
    public EntityType getEntityType() {
        return type;
    }

    public ArmorStandCarrier(Location loc, ArmorStandMeta meta) {
        this.loc = loc;
        this.type = EntityType.ARMOR_STAND;
        this.box = BoundingBox.Companion.of(loc, loc);
        if (meta != null)
            setCarrierMeta(meta);
    }

    public ArmorStandCarrier(Location loc, EntityType type, ArmorStandMeta meta) {
        this.loc = loc;
        this.type = type;
        this.box = BoundingBox.Companion.of(loc, loc);
        if (type == EntityType.ARMOR_STAND && meta != null)
            setCarrierMeta(meta);
    }

    public ArmorStandCarrier(Location loc, EntityType type) {
        this.loc = loc;
        this.type = type;
        this.box = BoundingBox.Companion.of(loc, loc);
    }

    public void setEffectEntity(String config) {
        YamlConfiguration yaml = GermSrcManager.getGermSrcManager().getSrc(config, RootType.EFFECT);
        if (yaml != null) {
           effectEntity = (GermEffectEntity) GermEffectEntity.getGermEffectPart(this.getUniqueId().toString(), yaml);
          // effectEntity.bind
        }

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

    public GermEffectEntity getEffectEntity() {
        return effectEntity;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}
