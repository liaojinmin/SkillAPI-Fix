package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.armorstand.ArmorStandInstance;
import com.sucy.skill.api.armorstand.ArmorStandManager;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.listener.MechanicListener;
import com.sucy.skill.task.RemoveTask;
import ink.ptms.adyeshach.core.Adyeshach;
import ink.ptms.adyeshach.core.entity.EntityEquipable;
import ink.ptms.adyeshach.core.entity.EntityInstance;
import ink.ptms.adyeshach.core.entity.EntityTypes;
import ink.ptms.adyeshach.core.entity.manager.FastAPIKt;
import ink.ptms.adyeshach.core.entity.manager.ManagerType;
import ink.ptms.adyeshach.core.entity.type.AdyHuman;
import ink.ptms.adyeshach.core.entity.type.AdyPufferfish;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Summons an armor stand that can be used as a marker or for item display. Applies child components on the armor stand
 */
public class AdyeshachMechanic extends MechanicComponent {

    private static final Vector UP = new Vector(0, 1, 0);

    private static final ItemStack air = new ItemStack(Material.AIR);
    private static final String DURATION = "duration"; // 移除时间
    private static final String NAME = "name"; // 名称
    private static final String TYPE = "type"; // 生物类型

    private static final String TEXTURE = "texture";

    private static final String NAME_VISIBLE = "name-visible"; // 名称是否可见 true、false
    private static final String GRAVITY = "gravity";  // 是否在地面 true、false
    private static final String VISIBLE = "visible"; // 是否隐身 true、false
    private static final String FORWARD = "forward"; // 向前 0.1、0.5
    private static final String UPWARD = "upward"; // 向上 0.1、0.5
    private static final String RIGHT = "right"; // 向右 0.1、0.5

    private static final String USE_ITEM = "use_item";

    private static final String ITEM_TYPE = "item_type";

    private static final String ITEM_NAME = "item_name";

    @Override
    public String getKey() {
        return "adyeshach";
    }

    @Override
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        int duration = (int) (20 * parseValues(caster, DURATION, level, 5));
        String name = settings.getString(NAME, "Armor Stand");
        boolean nameVisible = settings.getBool(NAME_VISIBLE, false);
        boolean gravity = settings.getBool(GRAVITY, false);
        boolean visible = settings.getBool(VISIBLE, true);
        String texture = settings.getString(TEXTURE, "");
        World world = caster.getWorld();
        EntityTypes types = EntityTypes.valueOf(settings.getString(TYPE,"ARMOR_STAND").toUpperCase(Locale.ROOT));
        List<EntityInstance> entityInstanceList = new ArrayList<>();
        final ItemStack itemStack;
        if (settings.getBool(USE_ITEM, false)) {
            String type = settings.getString(ITEM_TYPE, "AIR");
            itemStack = new ItemStack(Material.valueOf(type));
            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(settings.getString(ITEM_NAME, "null"));
            itemStack.setItemMeta(meta);
        } else itemStack = air;
        for (LivingEntity target : targets) {
            Location loc = target.getLocation();
            Vector dir = loc.getDirection().setY(0).normalize();
            Vector side = dir.clone().crossProduct(UP);
            loc.add(
                    dir.multiply(
                            parseValues(caster, FORWARD, level, 0)
                    )
            ).add(0, parseValues(caster, UPWARD, level, 0), 0)
                    .add(side.multiply(parseValues(caster, RIGHT, level, 0)));
            entityInstanceList.add(FastAPIKt.spawnEntity(world, loc, types,  ManagerType.TEMPORARY, (it) -> {
                it.setCustomName(name);
                it.setCustomNameVisible(nameVisible);
                it.setNoGravity(!gravity);
                it.setVisibleDistance(32);
                it.setInvisible(!visible);
                it.setTag("SkillAPI", true);

                if (it instanceof EntityEquipable) {
                    EntityEquipable eq = (EntityEquipable) it;
                    eq.setHelmet(itemStack);
                }
                if (it instanceof AdyHuman) {
                    AdyHuman eq = (AdyHuman) it;
                    eq.setName("");
                    eq.setTexture(texture);
                  //  Adyeshach.INSTANCE.api().getNetworkAPI().getAshcon().getTexture(texture).thenAccept(a -> eq.setTexture(a.value(), a.signature()));
                    //eq.setSkinEnabled(false);
                }
            }));

        }
        SkillAPI.schedule(() -> entityInstanceList.forEach(EntityInstance::remove), duration);
        return targets.size() > 0;
    }
}
