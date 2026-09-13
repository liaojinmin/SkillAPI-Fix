package com.sucy.skill.task;

import com.sucy.skill.api.attribute.mob.MobAttribute;
import com.sucy.skill.api.attribute.mob.MobAttributeData;
import com.sucy.skill.thread.AbstractRepeatThread;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.UUID;

public class MobAttributeTask extends AbstractRepeatThread {

    public MobAttributeTask() {
        super(0, 20 * 30);
    }

    @Override
    public void run() {
        ArrayList<UUID> uuids = new ArrayList<>();
        for (MobAttributeData data: MobAttribute.getAllData()) {
            UUID uuid = data.getUuid();
            if (Bukkit.getEntity(uuid) == null || Bukkit.getEntity(uuid).isDead()) {
                uuids.add(uuid);
            }
        }
        uuids.forEach(MobAttribute::delData);
    }
}
