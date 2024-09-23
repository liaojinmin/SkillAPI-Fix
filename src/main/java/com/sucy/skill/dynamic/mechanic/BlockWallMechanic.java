package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.dynamic.ArmorStandCarrier;
import me.neon.libs.carrier.minecraft.meta.ArmorStandMeta;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic.mechanic
 *
 * @author 老廖
 * @since 2024/7/2 19:15
 */
public class BlockWallMechanic extends MechanicComponent {

    private static final String BLOCK   = "block";
    private static final String SECONDS = "seconds";
    private static final String LENGTH = "length";
    private static final String HEIGHT = "height";
    private static final String NAME = "name"; // 盔甲架名称
    private static final HashSet<Location> pending = new HashSet<>();
    private static final Map<Integer, List<RevertTask>> tasks = new HashMap<>();
    private static final ArmorStandMeta meta = new ArmorStandMeta(false, false, true, false, true, false);

    @Override
    public String getKey() {
        return "block wall";
    }

    @Override
    public boolean execute(LivingEntity caster, SkillContext context, int level, List<LivingEntity> targets) {
        final int length = settings.getInt(LENGTH, 0);
        int height = settings.getInt(HEIGHT, 1);
        final long ticks = (long) (20 * parseValues(caster, SECONDS, level, 5));
        final String[] name = settings.getString(NAME, "Armor Stand Packet").split(",");
        Material block = Material.STONE;
        try {
            block = Material.valueOf(settings.getString(BLOCK, "STONE")
                    .toUpperCase().replace(' ', '_'));
        } catch (Exception ignored) {}
        final HashMap<Location, Boolean> map = new HashMap<>();
        for (LivingEntity target : targets) {
            final Location location = target.getLocation();
            //location = location.add(location.getDirection().normalize());
            float yaw = location.getYaw();
            final Vector leftDir = getDirectionVector(yaw + 90);
            final Vector rightDir = getDirectionVector(yaw - 90);
            boolean put = false;
            while (height >= 1) {
                if (height == 1) {
                    put = true;
                }
                for (int index = 0; index <= length; index++) {
                    Location _lo = location.clone().add(leftDir.getX() * index, height, leftDir.getZ() * index);
                    if (!pending.contains(_lo.getBlock().getLocation())) {
                        map.put(_lo, put);
                    }
                }
                for (int index = 0; index <= length; index++) {
                    Location _lo = location.clone().add(rightDir.getX() * index, height, rightDir.getZ() * index);
                    if (!pending.contains(_lo.getBlock().getLocation())) {
                        map.put(_lo, put);
                    }
                }
                height--;
            }
        }
        final List<WallData> old = new ArrayList<>();
        int index = 0;
        for (Map.Entry<Location, Boolean> entry : map.entrySet()) {
            Block _block = entry.getKey().getBlock();
            Location blockLocation = _block.getLocation();
            pending.add(blockLocation);

            WallData wallData;
            if (entry.getValue()) {
                final ArmorStandCarrier carrier = new ArmorStandCarrier(entry.getKey(), meta);
                if (index >= name.length) {
                    carrier.setDisplayName(name[name.length-1]);
                } else {
                    carrier.setDisplayName(name[index]);
                }
                wallData = new WallData(carrier, blockLocation, _block, _block.getType());
                index++;
            } else {
                wallData = new WallData(null, blockLocation, _block, _block.getType());
            }
            old.add(wallData);
        }
        // 设置方块为指定样式
        for (WallData data : old) {
            BlockState state = data.block.getState();
            state.setType(block);
            state.setData(new MaterialData(block));
            state.update(true, false);
            if (data.carrier != null) {
                data.carrier.setDead(false);
            }
        }
        final RevertTask task = new RevertTask(caster, old);
        task.runTaskLater(SkillAPI.singleton(), ticks);
        tasks.computeIfAbsent(caster.getEntityId(), ArrayList::new).add(task);
        return true;
    }

    @Override
    protected void doCleanUp(final LivingEntity caster) {
        final List<RevertTask> casterTasks = tasks.remove(caster.getEntityId());
        if (casterTasks != null) {
            casterTasks.forEach(task -> {
                task.revert();
                task.cancel();
            });
        }
    }

    private Vector getDirectionVector(Float yaw) {
        double radians = Math.toRadians(yaw);
        return new Vector(-Math.sin(radians), 0.0, Math.cos(radians));
    }

    public static boolean isPending(Location loc) {
        return pending.contains(loc);
    }

    private static class RevertTask extends BukkitRunnable {

        private final LivingEntity caster;

        private final List<WallData> data;

        public RevertTask(LivingEntity caster, List<WallData> data) {
            this.data = data;
            this.caster = caster;
        }

        @Override
        public void run() {
            revert();
            tasks.get(caster.getEntityId()).remove(this);
        }

        private void revert() {
            for (WallData a : data) {
                if (a.carrier != null) {
                    a.carrier.setDead(true);
                }
                if (pending.remove(a.blockLocation)) {
                    a.block.setType(a.oldMaterial);
                }
            }
        }
    }

    private static class WallData {
        @Nullable
        final ArmorStandCarrier carrier;

        @NotNull
        final Location blockLocation;

        final Block block;

        final Material oldMaterial;

        public WallData(@Nullable ArmorStandCarrier carrier, @NotNull Location blockLocation, Block block, Material oldMaterial) {
            this.carrier = carrier;
            this.blockLocation = blockLocation;
            this.block = block;
            this.oldMaterial = oldMaterial;
        }
    }
}
