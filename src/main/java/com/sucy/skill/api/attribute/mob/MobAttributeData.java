package com.sucy.skill.api.attribute.mob;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.event.AttributeEntityAddEvent;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.hook.mythic.MythicManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobAttributeData {

    private PlayerData owner = null;

    private final Entity entity;

    public final HashMap<String, Double> map = new HashMap<>();

    public final ConcurrentHashMap<String,  Double> timerMap = new ConcurrentHashMap<>();

    public final ConcurrentHashMap<String, HashMap<String, Double>> temp = new ConcurrentHashMap<>();

    public MobAttributeData(LivingEntity entity) {
        this.entity = entity;
    }

    @Nullable
    public PlayerData getOwner() {
        if (owner == null) {
            LivingEntity ow = MythicManager.INSTANCE.getSummonAscription().get(entity.getUniqueId());
            if (ow != null) {
                owner = SkillAPI.getPlayerData(ow.getUniqueId());
            }
        }
        return owner;
    }

    public LivingEntity getEntity() {
        if (entity == null || entity.isDead()) {
            return null;
        }
        return (LivingEntity) entity;
    }

    public void addAttribute(String attribute, double value, long timer) {
        LivingEntity livingEntity = getEntity();
        if (livingEntity == null) {
            return;
        }
        AttributeEntityAddEvent event = AttributeAPI.attributeEntityAdd(livingEntity, attribute, value);
        if (timer <= 0) {
            Double v = map.computeIfAbsent(event.getAttribute(), (k) -> 0.0);
            map.put(event.getAttribute(), v + event.getValue());
        } else {
            Double v = timerMap.computeIfAbsent(event.getAttribute(), (k) -> 0.0);
            timerMap.put(event.getAttribute(), v + event.getValue());

            Bukkit.getScheduler().runTaskLater(SkillAPI.singleton(), () -> {
                timerMap.remove(event.getAttribute());
            }, timer);
        }
    }

    public void addAttribute(String attribute, double value) {
        addAttribute(attribute, value, -1);
    }

    public void tempAddAttribute(String taskID, String string, double value) {
        HashMap<String, Double> map = temp.computeIfAbsent(taskID, k -> new HashMap<>());
        double old = map.getOrDefault(string, 0.0);
        value += old;
        if (value <= 0) {
            value = 0.0;
        }
        map.put(string, value);
        temp.put(taskID, map);
    }

    public void tempRemove(String taskID){
        temp.remove(taskID);
    }

    /**
     * 这里只是用来转发 请从AttributeAPI获取属性
     *
     * @param attribute 属性名
     * @return 返回的存储的数值
     */
    public double getAttribute(String attribute) {
        if (attribute == null) return 0.0;
        double temps = 0.0;
        for (HashMap<String, Double> value : temp.values()) {
            temps += value.getOrDefault(attribute, 0.0);
        }
        temps += timerMap.getOrDefault(attribute, 0.0);
        temps += map.getOrDefault(attribute, 0.0);
        // owner = 召唤它的玩家，可直接取得该玩家的属性容器
        PlayerData ow = getOwner();
        if (ow != null) {
           // System.out.println("owner != null old: "+temps + " attribute: "+attribute);
            temps += ow.getGlobalAttribute(attribute, null);
           // System.out.println("  addValue: "+temps);
        }
        return temps;
    }

    public UUID getUuid() {
        return entity.getUniqueId();
    }

    public String getDisplay() {
        return entity.getName();
    }

    @Override
    public String toString() {
        return "MobAttributeData{" +
                "uuid=" + entity.getUniqueId().toString() + ","+
                "map=" + map + ","+
                "temp=" + temp +
                '}';
    }
}
