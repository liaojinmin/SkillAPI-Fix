package com.sucy.skill.cmd;

import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.rit.sucy.config.parse.DataSection;
import com.rit.sucy.config.parse.YAMLParser;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerAccounts;
import com.sucy.skill.data.io.IOManager;
import com.sucy.skill.data.io.SQLImpl;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

/**
 * SkillAPI-Fix
 * com.sucy.skill.cmd
 *
 * @author 老廖
 * @since 2023/10/7 2:45
 */
public class CmdMigrate implements IFunction {
    private SkillAPI skillAPI;
    private final IOManager ioManager = SkillAPI.getIoManager();
    @Override
    public void execute(ConfigurableCommand configurableCommand, Plugin plugin, CommandSender commandSender, String[] strings) {
        this.skillAPI = (SkillAPI) plugin;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            HashMap<String, PlayerAccounts> map = loadAllOld();
            IOManager ioManager = SkillAPI.getIoManager();
            // 上库
            for (Map.Entry<String, PlayerAccounts> a : map.entrySet()) {
                ((SQLImpl)ioManager).insertSingle(a.getValue());
            }
            commandSender.sendMessage("转移完成，共计 " + map.size() + " 条数据");
        });
    }
    public HashMap<String, PlayerAccounts> loadAllOld() {
        HashMap<String, PlayerAccounts> result = new HashMap<>();
        File file = new File(skillAPI.getDataFolder(), "players");
        for (File a :  foreFile(file)) {
            String playerKey = a.getName().replace(".yml", "");
            PlayerAccounts accounts = loadDataOld(playerKey, a);
            result.put(playerKey, accounts);
        }
        return result;
    }

    public PlayerAccounts loadDataOld(String key, File file) {
        System.out.println("key "+key);
        System.out.println(" File "+file);
        DataSection files = YAMLParser.parseFile(file);
        return ioManager.load(Bukkit.getOfflinePlayer(UUID.fromString(key)), files);
    }


    private List<File> foreFile(File file) {
        List<File> fileList = new ArrayList<>();
        if (file.isDirectory()) {
            File[] f = file.listFiles();
            if (f != null) {
                for (File f2 : f) {
                    fileList.addAll(foreFile(f2));
                }
            }
        } else if (file.exists() && file.getAbsolutePath().endsWith(".yml")) {
            fileList.add(file);
        }
        return fileList;
    }
}
