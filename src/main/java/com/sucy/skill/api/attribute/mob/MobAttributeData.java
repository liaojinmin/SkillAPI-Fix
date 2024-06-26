package com.sucy.skill.api.attribute.mob;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.event.AttributeEntityAddEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 这是非玩家类型的属性中继器，负责怪物属性的实现
 */
public class MobAttributeData {

    private UUID uuid;
    private HashMap<String, Double> map = new HashMap<>();

    private final ConcurrentHashMap<String,  Double> timerMap = new ConcurrentHashMap<>();

    private final HashMap<String, HashMap<String, Double>> temp = new HashMap<>();



    public MobAttributeData(UUID uuid) {
        this.uuid = uuid;
    }

    public LivingEntity getEntity() {
        Entity entity = Bukkit.getEntity(uuid);
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
            if (map.containsKey(event.getAttribute())) {
                double old = map.get(event.getAttribute());
                map.put(event.getAttribute(), event.getValue() + old);
                return;
            }
            map.put(event.getAttribute(), Double.valueOf(event.getValue()));
        } else {
            if (timerMap.containsKey(event.getAttribute())) {
                double old = timerMap.get(event.getAttribute());
                timerMap.put(event.getAttribute(), event.getValue() + old);
                return;
            }
            timerMap.put(event.getAttribute(), Double.valueOf(event.getValue()));
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
        double temps = 0.0;
        for (HashMap<String, Double> value : temp.values()) {
            temps += value.getOrDefault(attribute, 0.0);
        }
        temps += timerMap.getOrDefault(attribute, 0.0);
        return map.getOrDefault(attribute, 0.0) + temps;
    }

    public HashMap<String, Double> getMap() {
        return map;
    }

    public void setMap(HashMap<String, Double> map) {
        this.map = map;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "MobAttributeData{" +
                "uuid=" + uuid + ","+
                "map=" + map + ","+
                "temp=" + temp +
                '}';
    }
}
