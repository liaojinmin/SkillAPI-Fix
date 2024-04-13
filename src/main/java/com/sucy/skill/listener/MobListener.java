package com.sucy.skill.listener;

import com.sucy.skill.api.attribute.mob.MobAttribute;
import com.sucy.skill.api.attribute.mob.MobAttributeData;
import com.sucy.skill.utils.AttributeParseUtils;
import com.sucy.skill.utils.Pair;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.event.EventHandler;

import java.util.List;

public class MobListener extends SkillAPIListener {

    @EventHandler
    public static void onMobSpawn(MythicMobSpawnEvent event) {
        List<String> attribute = event.getMobType().getConfig().getStringList("skillAPI-attribute");
        List<String> attribute2 = event.getMobType().getConfig().getStringList("skillAPI-timerAttribute");
        MobAttributeData data = MobAttribute.getData(event.getEntity().getUniqueId(), true);
        attribute.forEach(it -> {
          //  System.out.println("属性: "+it);
            Pair<String, Integer> pair = AttributeParseUtils.getAttribute(it);
            if (data != null && pair != null && pair.key != null) {
                data.addAttribute(pair.key, pair.value);
            }
        });
        attribute2.forEach(it -> {
            //  System.out.println("属性: "+it);
            String[] av = it.split(";");
            if (av.length == 2) {
                long timer = Long.parseLong(av[1]);
                Pair<String, Integer> pair = AttributeParseUtils.getAttribute(av[0]);
                if (data != null && pair != null && pair.key != null) {
                    data.addAttribute(pair.key, pair.value, timer);
                }
            }
        });
        // 处理韧性值

    }
}
