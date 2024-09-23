package com.sucy.skill.dynamic;

import me.neon.libs.carrier.CarrierAction;
import me.neon.libs.carrier.CarrierEntity;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * SkillAPI-Fix
 * com.sucy.skill.dynamic
 *
 * @author 老廖
 * @since 2024/5/3 12:28
 */
public class PacketEntity implements CarrierEntity {

    private static int index = 19299959 + ThreadLocalRandom.current().nextInt(0, 702);

    private final int id = index++;

    private final UUID uuid = UUID.randomUUID();

    private final HashSet<String> players = new HashSet<>();


    public PacketEntity(Location loc) {

    }


    @Override
    public int getEntityId() {
        return id;
    }

    @NotNull
    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public void close() {

    }

    @Override
    public void destroy(@NotNull Player player) {

    }

    @Override
    public void interact(@NotNull Player player, @NotNull CarrierAction carrierAction, boolean b) {

    }

    @Override
    public void spawn(@NotNull Player player) {

    }
}
