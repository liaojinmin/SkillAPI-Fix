package com.sucy.skill.dynamic;

import com.rit.sucy.config.parse.DataSection;
import com.rit.sucy.mobs.MobManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.Settings;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.player.PlayerSkill;
import com.sucy.skill.api.skills.SkillContext;
import com.sucy.skill.log.Logger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A component for dynamic skills which takes care of one effect
 */
public abstract class EffectComponent {
    private static final String TYPE       = "type";
    private static final String ICON_KEY   = "icon-key";
    private static final String COUNTS_KEY = "counts";
    private static final String USE_GLOBAL_CONTEXT = "use-global-context";

    private static boolean passed;

    /**
     * Child components
     */
    protected final ArrayList<EffectComponent> children = new ArrayList<>();

    /**
     * The settings for the component
     */
    protected final Settings settings = new Settings();

    /**
     * Parent class of the component
     */
    protected DynamicSkill skill;

    protected String instanceKey;

    protected boolean useGlobalContext = false;


    public abstract String getKey();

    /**
     * Retrieves the type of the component
     *
     * @return component type
     */
    public abstract ComponentType getType();


    public abstract boolean execute(final SkillContext context, LivingEntity caster, int level, List<LivingEntity> targets);

    /**
     * Retrieves the settings of the dynamic component
     *
     * @return settings of the dynamic component
     */
    public Settings getSettings() {
        return settings;
    }

    public boolean isUseGlobalContext() {
        return useGlobalContext;
    }

    /**
     * Retrieves an attribute value while applying attribute
     * data if enabled and a player is using the skill
     *
     * @param caster   caster of the skill
     * @param key      key of the value to grab
     * @param level    level of the skill
     * @param fallback default value for the attribute
     *
     * @return the value with attribute modifications if applicable
     */
    protected double parseValues(final SkillContext context, LivingEntity caster, String key, int level, double fallback) {
        double base = getNum(context, caster, key + "-base", fallback);
        if (base < 0) return base;
        double scale = getNum(context, caster, key + "-scale", 0);
        double value = base + (level - 1) * scale;

        // Apply global modifiers
        value = AttributeAPI.scaleDynamic(caster, this, key, value);
        return value;
    }

    /**
     * Checks whether or not the last component passed or not
     *
     * @return true if passed, false otherwise
     */
    protected boolean lastPassed() {
        return passed;
    }

    protected boolean executeChildren(final SkillContext context, LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) {
            return false;
        }

        boolean worked = false;
        for (EffectComponent child : children) {
            try {
                boolean counts = !child.settings.getString(COUNTS_KEY, "true").equalsIgnoreCase("false");
                passed = child.execute(context, caster, level, targets);
                worked = (passed && counts) || worked;
            } catch (Exception e) {
                Logger.invalid("执行子组件发生异常 "+e.getMessage());
                e.printStackTrace();
            }
        }
        return worked;
    }

    public void cleanUp(final LivingEntity caster) {
        doCleanUp(caster);
        children.forEach(child -> child.cleanUp(caster));
    }

    protected void doCleanUp(final LivingEntity caster) { }

    /**
     * Gets the skill data for the caster
     *
     * @param caster caster of the skill
     *
     * @return skill data for the caster or null if not found
     */
    protected PlayerSkill getSkillData(LivingEntity caster) {
        if (caster instanceof Player) {
            PlayerData playerData = SkillAPI.getPlayerData(caster.getUniqueId());
            if (playerData == null) return null;
            return playerData.getSkill(skill.getName());
        } else {
            return null;
        }
    }

    protected String filter(final SkillContext context, LivingEntity caster, LivingEntity target, String text) {
        return parseVars(context, caster, target, text);
    }

    protected String parseVars(final SkillContext context, LivingEntity caster, LivingEntity target, String text) {
        return parseVars(context, caster, target, text, false);
    }

    protected String parseVars(final SkillContext context, LivingEntity caster, LivingEntity target, String text, boolean toInt) {
        // Grab values
        int i = text.indexOf('{');
        if (i < 0) { return text; }

        int j = text.indexOf('}', i);
        if (j < 0) { return text; }

        StringBuilder builder = new StringBuilder();

        int k = 0;
        while (i >= 0 && j > i) {
            String key = text.substring(i + 1, j);
            if (context.containsKey(caster, key, useGlobalContext)) {
                Object obj = context.get(caster, key, useGlobalContext);
                if (obj instanceof Player) {
                    obj = ((Player) obj).getName();
                } else if (obj instanceof LivingEntity) {
                    obj = MobManager.getName((LivingEntity) obj);
                } else if (obj instanceof Number && toInt) {
                    obj = ((Number) obj).intValue();
                }
                builder.append(text, k, i);
                builder.append(obj);

                k = j + 1;
            } else if (key.equalsIgnoreCase("player") || key.equalsIgnoreCase("caster") ) {
                builder.append(text, k, i);
                builder.append(caster.getName());

                k = j + 1;
            } else if (key.equalsIgnoreCase("target")) {
                builder.append(text, k, i);
                builder.append(target.getName());

                k = j + 1;
            } else if (key.equalsIgnoreCase("targetUUID")) {
                builder.append(text, k, i);
                builder.append(target.getUniqueId().toString());

                k = j + 1;
            }
            i = text.indexOf('{', j);
            j = text.indexOf('}', i);
        }
        builder.append(text.substring(k));
        return builder.toString();
    }

    /**
     * Loads component data from the configuration
     *
     * @param skill  owning skill of the component
     * @param config config data to load from
     */
    public void load(DynamicSkill skill, DataSection config) {
        this.skill = skill;
        if (config == null) {
            return;
        }
        settings.load(config.getSection("data"));
        if (settings.has(ICON_KEY)) {
            String key = settings.getString(ICON_KEY);
            if (!key.equals("")) {
                skill.setAttribKey(key, this);
            }
        }
        //indicatorType = IndicatorType.getByKey(settings.getString(INDICATOR, "2D"));
        useGlobalContext = settings.getBool(USE_GLOBAL_CONTEXT, false);
        DataSection children = config.getSection("children");
        if (children != null) {
            for (String key : children.keys()) {
                final String typeName = children.getSection(key).getString(TYPE, "missing").toUpperCase();
                final ComponentType type = ComponentType.valueOf(typeName);
                final String mkey = key.replaceAll("-.+", "");
                try {
                    final EffectComponent child = ComponentRegistry.getComponent(type, mkey);
                    if (child != null) {
                        child.instanceKey = key;
                        child.load(skill, children.getSection(key));
                        this.children.add(child);
                    } else {
                        Logger.invalid("Invalid " + type + " component: " + mkey);
                    }
                } catch (Exception ex) {
                    // Failed to create the component, just don't add it
                    Logger.bug("Failed to create " + type + " component: " + key + " by "+skill.getName());
                }
            }
        }
    }

    private double getNum(SkillContext context, LivingEntity caster, String key, double fallback) {
        String val = settings.getString(key);
        if (val == null) { return fallback; }

        try {
            return Double.parseDouble(val);
        } catch (Exception ex) { /* Not a number */ }


        if (context.containsKey(caster, val, useGlobalContext)) {
            final String mapVal = String.valueOf(context.get(caster, val, useGlobalContext));
            try {
                return Double.parseDouble(mapVal);
            } catch (Exception ex) { /* Not a number */ }
        }

        try {
            final int mid = val.indexOf('-', 1);
            final double min = Double.parseDouble(val.substring(0, mid));
            final double max = Double.parseDouble(val.substring(mid + 1));
            return Math.random() * (max - min) + min;
        } catch (Exception ex) { /* Not a range */ }

        return 0;
    }
}
