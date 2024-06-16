package com.sucy.skill.utils;

import com.rit.sucy.config.parse.NumberParser;
import com.sucy.skill.SkillAPI;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AttributeParseUtils {

    private static final Pattern intParse = Pattern.compile("(?<![&§])\\d+");

    public static Pair<String, Integer> getAttribute(String lore) {
        for (String attr : SkillAPI.getAttributeManager().getLookupKeys()) {
            String oLore = ChatColor.stripColor(lore).toLowerCase();
            if (oLore.contains(attr)) {
                String normalized = SkillAPI.getAttributeManager().normalize(attr);
                int extra = toInt(oLore);
                if (extra <= 0) {
                    return null;
                }
                return new Pair<>(normalized, extra);
            }
        }
        return null;
    }

    public static Integer toInt(String a) {
        if (a.isEmpty()) {
            return 0;
        }
        Matcher m = intParse.matcher(a);
        if (m.find()) {
            return NumberParser.parseInt(m.group());
        } else return 0;
    }

}
