package com.sucy.skill.manager;

import com.rit.sucy.commands.CommandManager;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.SenderType;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.cmd.*;
import com.sucy.skill.data.Permissions;

/**
 * Sets up commands for the plugin
 */
public class CmdManager {
    public static ConfigurableCommand PROFESS_COMMAND;

    private final SkillAPI api;

    /**
     * Initializes a new command manager. This is handled by the API and
     * shouldn't be used by other plugins.
     *
     * @param api SkillAPI reference
     */
    public CmdManager(SkillAPI api) {
        this.api = api;
        this.initialize();
    }

    /**
     * Initializes commands with MCCore's CommandManager
     */
    public void initialize()
    {
        ConfigurableCommand root = new ConfigurableCommand(api, "class", SenderType.ANYONE);
        root.addSubCommands(
                new ConfigurableCommand(api, "attAction", SenderType.ANYONE, new CmdAttributeAction(), "临时属性", "", Permissions.AttributeAction),
                new ConfigurableCommand(api, "cast", SenderType.PLAYER_ONLY, new CmdCast(), "Casts a skill", "<skill>", Permissions.BASIC),
            new ConfigurableCommand(api, "changeclass", SenderType.ANYONE, new CmdChangeClass(), "Swaps classes", "<player> <group> <class>", Permissions.FORCE),
            new ConfigurableCommand(api, "exp", SenderType.ANYONE, new CmdExp(), "Gives players exp", "[player] <amount> [group]", Permissions.LVL),
            new ConfigurableCommand(api, "info", SenderType.ANYONE, new CmdInfo(), "Shows class info", "[player]", Permissions.BASIC),
            new ConfigurableCommand(api, "level", SenderType.ANYONE, new CmdLevel(), "Gives players levels", "[player] <amount> [group]", Permissions.LVL),
            new ConfigurableCommand(api, "lore", SenderType.PLAYER_ONLY, new CmdLore(), "Adds lore to item", "<lore>", Permissions.LORE),
            new ConfigurableCommand(api, "mana", SenderType.ANYONE, new CmdMana(), "Gives player mana", "[player] <amount>", Permissions.MANA),
            new ConfigurableCommand(api, "options", SenderType.PLAYER_ONLY, new CmdOptions(), "Views profess options", "", Permissions.BASIC),
            new ConfigurableCommand(api, "points", SenderType.ANYONE, new CmdPoints(), "Gives player points", "[player] <amount>", Permissions.POINTS),
            PROFESS_COMMAND = new ConfigurableCommand(api, "profess", SenderType.PLAYER_ONLY, new CmdProfess(), "Professes classes", "<class>", Permissions.BASIC),
            new ConfigurableCommand(api, "reload", SenderType.ANYONE, new CmdReload(), "Reloads the plugin", "", Permissions.RELOAD),
                new ConfigurableCommand(api, "reloadConfig", SenderType.ANYONE, new CmdReloadConfig(), "重载新配置、UI", "", Permissions.RELOAD),
            new ConfigurableCommand(api, "reset", SenderType.PLAYER_ONLY, new CmdReset(), "Resets account data", "", Permissions.BASIC),
            new ConfigurableCommand(api, "world", SenderType.PLAYER_ONLY, new CmdWorld(), "Moves to world", "<world>", Permissions.WORLD)
        );
        root.addSubCommands(
                new ConfigurableCommand(api, "consolecast", SenderType.CONSOLE_ONLY, new CmdConsoleCast(), "Casts a skill", "<skill>", Permissions.FORCE),
            new ConfigurableCommand(api, "forceattr", SenderType.CONSOLE_ONLY, new CmdForceAttr(), "Refunds/gives attributes", "<player> [attr] [amount]", Permissions.FORCE),
            new ConfigurableCommand(api, "forcecast", SenderType.CONSOLE_ONLY, new CmdForceCast(), "Player casts the skill", "<player> <skill> [level]", Permissions.FORCE),
            new ConfigurableCommand(api, "forceprofess", SenderType.CONSOLE_ONLY, new CmdForceProfess(), "Professes a player", "<player> <class>", Permissions.FORCE),
            new ConfigurableCommand(api, "forceskill", SenderType.CONSOLE_ONLY, new CmdForceSkill(), "Modifies skill levels", "<player> <up|down|reset> <skill>", Permissions.FORCE)
        );
        if (SkillAPI.getSettings().isUseSql()) {
            root.addSubCommand(new ConfigurableCommand(api, "save", SenderType.ANYONE, new CmdSave(), "安全的保存数据", "", "save"));
            root.addSubCommand(new ConfigurableCommand(api, "backup", SenderType.ANYONE, new CmdBackup(), "Backs up SQL data", "", Permissions.BACKUP));
        }

        root.addSubCommand(new ConfigurableCommand(api, "ap", SenderType.ANYONE, new CmdAP(), "Gives attrib points", "[player] <amount>", Permissions.ATTRIB));
        root.addSubCommand(new ConfigurableCommand(api, "attr", SenderType.PLAYER_ONLY, new CmdAttribute(), "Opens attribute menu", "", Permissions.BASIC));
        CommandManager.registerCommand(root);
    }

    public static String join(String[] args, int start) {
        return join(args, start, args.length - 1);
    }

    public static String join(String[] args, int start, int end) {
        final StringBuilder builder = new StringBuilder(args[start]);
        for (int i = start + 1; i <= end; i++) builder.append(' ').append(args[i]);
        return builder.toString();
    }

    /**
     * Unregisters all commands for SkillAPI from the server
     */
    public void clear()
    {
        CommandManager.unregisterCommands(api);
    }
}
