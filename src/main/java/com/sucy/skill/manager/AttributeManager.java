package com.sucy.skill.manager;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.util.DamageLoreRemover;
import com.sucy.skill.api.util.Data;
import com.sucy.skill.data.formula.Formula;
import com.sucy.skill.data.formula.value.CustomValue;
import com.sucy.skill.dynamic.ComponentType;
import com.sucy.skill.dynamic.EffectComponent;

import com.sucy.skill.gui.IconHolder;
import com.sucy.skill.log.LogType;
import com.sucy.skill.log.Logger;
import me.neon.libs.taboolib.chat.HexColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles loading and accessing individual
 * attributes from the configuration.
 */
public class AttributeManager {
    // Keys for supported stat modifiers
    public static final String HEALTH             = "health";
    public static final String MANA               = "mana";
    public static final String MANA_REGEN         = "mana-regen";
    public static final String PHYSICAL_DAMAGE    = "physical-damage";
    public static final String MELEE_DAMAGE       = "melee-damage";
    public static final String PROJECTILE_DAMAGE  = "projectile-damage";
    public static final String PHYSICAL_DEFENSE   = "physical-defense";
    public static final String MELEE_DEFENSE      = "melee-defense";
    public static final String PROJECTILE_DEFENSE = "projectile-defense";
    public static final String SKILL_DAMAGE       = "skill-damage";
    public static final String SKILL_DEFENSE      = "skill-defense";
    public static final String MOVE_SPEED         = "move-speed";
    public static final String ATTACK_SPEED       = "attack-speed";
    public static final String ARMOR              = "armor";
    public static final String LUCK               = "luck";
    public static final String ARMOR_TOUGHNESS    = "armor-toughness";
    public static final String EXPERIENCE         = "exp";
    public static final String HUNGER             = "hunger";
    public static final String HUNGER_HEAL        = "hunger-heal";
    public static final String COOLDOWN           = "cooldown";
    public static final String KNOCKBACK_RESIST   = "knockback-resist";

    public static Double money = 10000.0;
    public static String accept = "已成功洗点...";
    public static String deny = "没有足够的金币...";
    public static String hasLevel = "加点成功...";
    public static String notLevel = "已达最大等级...";
    public static String notPoints = "没有可退回的属性点...";

    private final List<Attribute> show = new ArrayList<>();
    private final HashMap<String, Attribute>       attributes  = new LinkedHashMap<>();
    private final HashMap<String, Attribute>       lookup      = new HashMap<>();
    private final HashMap<String, List<Attribute>> byStat      = new HashMap<>();
    private final HashMap<String, List<Attribute>> byComponent = new HashMap<>();

    /**
     * Sets up the attribute manager, loading the attribute
     * data from the configuration. This is handled by SkillAPI
     * automatically so other plugins should not instantiate
     * this class.
     *
     * @param api SkillAPI reference
     */
    public AttributeManager(SkillAPI api) {
        load(api);
    }

    /**
     * Retrieves an attribute template
     *
     * @param key attribute key
     *
     * @return template for the attribute
     */
    public Attribute getAttribute(String key) {
        return lookup.get(key.toLowerCase());
    }

    public HashMap<String, Attribute> getAttributes() {
        return attributes;
    }

    public List<Attribute> getScreenAttributes() {
        return show;
    }

    public List<Attribute> forStat(final String key) {
        return byStat.get(key.toLowerCase());
    }

    public List<Attribute> forComponent(final EffectComponent component, final String key) {
        return byComponent.get(component.getKey() + "-" + key.toLowerCase());
    }

    /**
     * Retrieves the available attribute keys. This
     * does not include display names for attributes.
     *
     * @return set of available attribute keys
     */
    public Set<String> getKeys() {
        return attributes.keySet();
    }

    /**
     * Retrieves the available attribute keys including
     * both display names and config keys.
     *
     * @return display name and config keys for attributes
     */
    public Set<String> getLookupKeys() {
        return lookup.keySet();
    }

    /**
     * Normalizes a config key or name into the config key
     * for a unified identifier to store stats under.
     *
     * @param key key to normalize
     * @return config key
     */
    public String normalize(String key) {
        final Attribute attribute = lookup.get(key.toLowerCase());
        if (attribute == null) { throw new IllegalArgumentException("Invalid attribute - " + key); }
       // System.out.println("key: "+key);
        return attribute.getKey();
    }

    /**
     * Loads attribute data from the config
     *
     * @param api SkillAPI reference
     */
    private void load(SkillAPI api) {
        File file = new File(api.getDataFolder(), "attributes.yml");
        if (!file.exists()) {
            api.saveResource("attributes.yml", true);
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new File(api.getDataFolder(), "attributes.yml"));
        Logger.log(LogType.ATTRIBUTE_LOAD, 1, "正在加载自定义属性...");
        if (yaml.get("settings") == null) {
            yaml.set("settings", new YamlConfiguration() {{
                set("money", 1000.0);
                set("accept", "已成功洗点...");
                set("deny", "你没有足够的金币...");
                set("notPoints", "没有可退回的属性点...");
                set("notLevel", "已达最大等级...");
                set("hasLevel", "加点成功...");
                set("show", new ArrayList<String>());
            }});
            try {
                yaml.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        List<String> open = new ArrayList<>();
        for (String key : yaml.getKeys(false)) {
            if (key.equalsIgnoreCase("settings")) {
                ConfigurationSection settings = yaml.getConfigurationSection(key);
                money = settings.getDouble("money", 1000.0);
                accept = HexColor.INSTANCE.colored(settings.getString("accept"));
                deny = HexColor.INSTANCE.colored(settings.getString("deny"));
                notPoints = HexColor.INSTANCE.colored(settings.getString("notPoints"));
                notLevel = HexColor.INSTANCE.colored(settings.getString("notLevel"));
                hasLevel = HexColor.INSTANCE.colored(settings.getString("hasLevel"));
                open.addAll(settings.getStringList("show"));
            } else {
                Logger.log(LogType.ATTRIBUTE_LOAD, 2, "  - " + key);
                Attribute attribute = new Attribute(yaml.getConfigurationSection(key), key);
                attributes.put(attribute.getKey(), attribute);
                lookup.put(attribute.getKey(), attribute);
                lookup.put(attribute.getName().toLowerCase(), attribute);
            }
        }
        for (Attribute attribute : attributes.values()) {
            if (open.contains(attribute.key)) {
                show.add(attribute);
            }
        }
    }

    /**
     * A single attribute template
     */
    public class Attribute implements IconHolder {
        private static final String DISPLAY   = "display";
        private static final String GLOBAL    = "global";
        private static final String CONDITION = "condition";
        private static final String MECHANIC  = "mechanic";
        private static final String TARGET    = "target";
        private static final String STATS     = "stats";
        private static final String MAX       = "max";

        // Attribute description
        private final String    key;
        private final String    display;
        public final int iconPriority;
        private final ItemStack icon;
        private final int       max;

        // Dynamic global modifiers
        private final Map<ComponentType, Map<String, AttributeValue[]>> dynamicModifiers = new EnumMap<>(ComponentType.class);

        // General stat modifiers
        private final HashMap<String, Formula> statModifiers = new HashMap<>();

        /**
         * Creates a new attribute, loading the settings from the given
         * config data.
         *
         * @param data config data to load from
         * @param key  the key the attribute was labeled under
         */
        public Attribute(ConfigurationSection data, String key) {
            this.key = key.toLowerCase();
            this.display = data.getString(DISPLAY, key).toLowerCase();
            this.iconPriority = data.getInt("icon-priority", 0);
            this.icon = Data.parseIconAtSection(data);
            this.max = Integer.parseInt(data.getString(MAX, "999"));
            // Load dynamic global settings
            ConfigurationSection globals = data.getConfigurationSection(GLOBAL);
            if (globals != null) {
                loadGroup(globals.getConfigurationSection(CONDITION), ComponentType.CONDITION);
                loadGroup(globals.getConfigurationSection(MECHANIC), ComponentType.MECHANIC);
                loadGroup(globals.getConfigurationSection(TARGET), ComponentType.TARGET);
            }

            // Load stat settings
            ConfigurationSection stats = data.getConfigurationSection(STATS);
            if (stats != null) {
                for (String stat : stats.getKeys(false)) {
                    loadStatModifier(stats, stat);
                }
            }
        }

        /**
         * Retrieves the config key of the attribute
         *
         * @return config key of the attribute
         */
        public String getKey() {
            return key;
        }

        /**
         * Retrieves the name for the attribute
         *
         * @return name of the attribute
         */
        public String getName() {
            return display;
        }


        public String getIconDisplay(PlayerData data) {
            return filter(data, icon.getItemMeta().getDisplayName());
        }

        public List<String> getIconLore(PlayerData data) {
            List<String> lore = icon.getItemMeta().getLore();
            if (lore != null) {
                lore.replaceAll(text -> filter(data, text));
            }
            return lore;
        }

        @Override
        public ItemStack getIcon(PlayerData data) {
            ItemStack item = icon.clone();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(filter(data, meta.getDisplayName()));
            List<String> lore = meta.getLore();
            if (lore != null) {
                lore.replaceAll(text -> filter(data, text));
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
            return DamageLoreRemover.removeAttackDmg(item);
        }

        @Override
        public boolean isAllowed(final Player player) {
            return true;
        }

        /**
         * Filters a line of the icon according to the player data
         *
         * @param data player data to use
         * @param text line of text to filter
         *
         * @return filtered line
         */
        private String filter(PlayerData data, String text) {
            return text.replace("{amount}", "" + data.getInvestedAttribute(key))
                    .replace("{total}", "" + data.getAttribute(key));
        }


        /**
         * Retrieves the max amount the attribute can be raised to
         *
         * @return max attribute amount
         */
        public int getMax() {
            return max;
        }

        /**
         * Modifies a dynamic condition's value
         *
         * @param component component to modify for
         * @param key       key of the value to modify
         * @param value     base value
         * @param amount    amount of attribute points
         *
         * @return modified value
         */
        public double modify(EffectComponent component, String key, double value, double amount) {
            key = component.getKey() + "-" + key.toLowerCase();
            final Map<String, AttributeValue[]> map = dynamicModifiers.get(component.getType());
            if (map.containsKey(key)) {
                AttributeValue[] list = map.get(key);
                for (AttributeValue attribValue : list) {
                    if (attribValue.passes(component)) {
                        //if (key.contains("heal")) {
                           // System.out.println("key " + key + " value " + value + " amount " + amount + " 结果: " + a);
                    //    }
                        return attribValue.apply(value, amount);
                    }
                }
            }
            return value;
        }

        /**
         * Modifies a stat value
         *
         * @param key    key of the stat
         * @param base   base value of the stat
         * @param amount amount of attribute points
         *
         * @return modified stat value
         */
        public double modifyStat(String key, double base, double amount) {
            if (statModifiers.containsKey(key)) {
                double a = statModifiers.get(key).compute(base, amount);
                //System.out.println("key "+key + " base " + base +" amount " + amount+ " 结果: " + a);
                return a;
                //return statModifiers.get(key).compute(base, amount);
            }
            return base;
        }

        /**
         * Loads a dynamic group globals settings into the given map
         *
         * @param data   config data to load from
         * @param type the component type to load for
         */
        private void loadGroup(ConfigurationSection data, ComponentType type) {
            if (data == null) { return; }

            final Map<String, AttributeValue[]> target = dynamicModifiers.computeIfAbsent(type, t -> new HashMap<>());
            for (String key : data.getKeys(false)) {
                final String lower = key.toLowerCase();
                Logger.log(LogType.ATTRIBUTE_LOAD, 2, "    SkillMod: " + key);
                final String value = data.getString(key);
                final String[] formulas = value.split("\\|");
                final AttributeValue[] values = new AttributeValue[formulas.length];
                int i = 0;
                for (final String formula : formulas) {
                    values[i++] = new AttributeValue(formula);
                }
                target.put(lower, values);

                if (!byComponent.containsKey(lower)) {
                    byComponent.put(lower, new ArrayList<>());
                }
                byComponent.get(lower).add(this);
            }
        }

        /**
         * Loads a stat modifier from the config data
         *
         * @param data config data to load from
         * @param key  key of the stat modifier
         */
        private void loadStatModifier(ConfigurationSection data, String key) {
            if (data.contains(key)) {
                Logger.log(LogType.ATTRIBUTE_LOAD, 2, "    StatMod: " + key);
                statModifiers.put(key,
                        new Formula(data.getString(key, "v"), new CustomValue("v"), new CustomValue("a")));

                if (!byStat.containsKey(key)) {
                    byStat.put(key, new ArrayList<>());
                }
                byStat.get(key).add(this);
            }
        }
    }

    /**
     * Represents one formula modifier for an attribute
     * that can have conditions
     */
    public class AttributeValue {
        private Formula formula;
        private HashMap<String, String> conditions = new HashMap<>();

        /**
         * Loads the attribute value that starts with the formula
         * and can have as many conditions as desired after
         *
         * @param data data string for the value
         */
        public AttributeValue(String data) {
            String[] pieces = data.split(":");
            formula = new Formula(pieces[0], new CustomValue("v"), new CustomValue("a"));
            for (int i = 1; i < pieces.length; i++) {
                String[] sides = pieces[i].split("=");
                conditions.put(sides[0], sides[1]);
                Logger.log(LogType.ATTRIBUTE_LOAD, 3, "      Condition: " + sides[0] + " / " + sides[1]);
            }
        }

        /**
         * Checks whether or not the formula should be applied to the component
         *
         * @param component component to check for conditions against
         *
         * @return true if passes the conditions
         */
        public boolean passes(EffectComponent component) {
            for (String key : conditions.keySet()) {
                if (!component.getSettings().getString(key).equalsIgnoreCase(conditions.get(key))) { return false; }
            }
            return true;
        }

        /**
         * Checks the conditions for the given component
         *
         * @param value  base value
         * @param amount amount of attribute points
         *
         * @return the modified value if the conditions passed or the base value if they failed
         */
        public double apply(double value, double amount) {
            return formula.compute(value, amount);
        }
    }
}
