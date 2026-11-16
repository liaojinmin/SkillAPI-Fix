package com.sucy.skill.manager;

import com.rit.sucy.config.CommentedConfig;
import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.classes.RPGClass;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.dynamic.ComponentRegistry;
import com.sucy.skill.dynamic.DynamicClass;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.log.LogType;
import com.sucy.skill.log.Logger;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * <p>Skill API Registration Manager.</p>
 * <p>This handles loading skill and class data from cofiguration files and fetching
 * them from other plugins while validating everything to make sure it should be
 * added.</p>
 */
public class RegistrationManager {

    private static final String SKILL_FOLDER = "dynamic" + File.separator + "skill";
    private static final String CLASS_FOLDER = "dynamic" + File.separator + "class";
    private static final String SKILL_DIR    = SKILL_FOLDER + File.separator;
    private static final String CLASS_DIR    = CLASS_FOLDER + File.separator;

    private final SkillAPI api;

    /**
     * <p>Creates a new Registration Manager for handling registering new
     * classes or skills.</p>
     *
     * @param api SkillAPI reference
     */
    public RegistrationManager(SkillAPI api) {
        this.api = api;
        new File(api.getDataFolder()
                .getAbsolutePath() + File.separator + "dynamic" + File.separator + "skill").mkdirs();
        new File(api.getDataFolder()
                .getAbsolutePath() + File.separator + "dynamic" + File.separator + "class").mkdirs();
    }

    /**
     * Initializes the registration manager, fetching skills and classes from
     * configuration files and other plugins.
     */
    public void initialize() {
        // Load individual dynamic skills
        Logger.log(LogType.REGISTRATION, 1, "Loading individual dynamic skill files...");
        File skillRoot = new File(api.getDataFolder().getPath() + File.separator + SKILL_FOLDER);
        if (skillRoot.exists()) {
            File[] files = skillRoot.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.getName().endsWith(".yml")) { continue; }
                    String name = file.getName().replace(".yml", "");
                    try {
                        CommentedConfig sConfig = new CommentedConfig(api, SKILL_DIR + name);
                        DynamicSkill skill = new DynamicSkill(name);
                        skill.load(sConfig.getConfig().getSection(name));

                        if (!SkillAPI.isSkillRegistered(skill.getName())) {
                            api.addDynamicSkill(skill);
                            skill.registerEvents(api);
                            Logger.log(LogType.REGISTRATION, 2, "Loaded the dynamic skill: " + name);
                        } else if (SkillAPI.getSkill(name) instanceof DynamicSkill) {
                            Logger.log(LogType.REGISTRATION, 3, name + " is already loaded, skipping it");
                        } else {
                            Logger.invalid("Duplicate skill detected: " + name);
                        }
                    } catch (Exception ex) {
                        Logger.invalid("Failed to load skill: " + name + " - " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }
        }

        // Load individual dynamic classes
        Logger.log(LogType.REGISTRATION, 1, "Loading individual dynamic class files...");
        File classRoot = new File(api.getDataFolder().getPath() + File.separator + CLASS_FOLDER);
        if (classRoot.exists()) {
            File[] files = classRoot.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.getName().endsWith(".yml")) { continue; }
                    try {
                        String name = file.getName().replace(".yml", "");
                        CommentedConfig cConfig = new CommentedConfig(api, CLASS_DIR + name);
                        DynamicClass tree = new DynamicClass(api, name);
                        tree.load(cConfig.getConfig().getSection(name));
                        if (!SkillAPI.isClassRegistered(tree.getName())) {
                            api.addDynamicClass(tree);
                            //cConfig.clear();
                            //tree.save(cConfig.getConfig().createSection(name));
                            //cConfig.save();
                            Logger.log(LogType.REGISTRATION, 2, "Loaded the dynamic class: " + name);
                        } else if (SkillAPI.getClass(name) instanceof DynamicClass) {
                            Logger.log(LogType.REGISTRATION, 3, name + " is already loaded, skipping it");
                        } else {
                            Logger.invalid("Duplicate class detected: " + name);
                        }
                    } catch (Exception ex) {
                        Logger.invalid("Failed to load class file: " + file.getName() + " - " + ex.getMessage());
                    }
                }
            }
        }
        Logger.log(LogType.REGISTRATION, 0, "Registration complete");
        Logger.log(LogType.REGISTRATION, 0, " - " + SkillAPI.getSkills().size() + " skills");
        Logger.log(LogType.REGISTRATION, 0, " - " + SkillAPI.getClasses().size() + " classes");
    }

}
