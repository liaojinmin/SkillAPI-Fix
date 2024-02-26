/**
 * SkillAPI
 * com.sucy.io.data.skill.SQLIO
 <p>
 * The MIT License (MIT)
 <p>
 * Copyright (c) 2014 Steven Sucy
 <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.data.io;

import com.alibaba.fastjson2.JSONObject;
import com.rit.sucy.version.VersionManager;
import com.rit.sucy.version.VersionPlayer;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerAccounts;
import com.sucy.skill.data.Settings;

import com.sucy.skill.utils.Pair;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads player data from the SQL Database
 */
public class SQLImpl extends IOManager {


    private final SqlService sqlService;

    private final BukkitTask bukkitTask;

    /**
     * Initializes the SQL IO Manager
     *
     * @param api API reference
     */
    public SQLImpl(SkillAPI api) {
        super(api);
        // 设置数据源
        Settings settings = SkillAPI.getSettings();
        if (settings.isUseSql()) {
            sqlService = new Mysql(settings);
        } else {
            sqlService = new Sqlite();
        }
        sqlService.startSql();
        bukkitTask = Bukkit.getScheduler()
                .runTaskTimerAsynchronously(api, this::saveAll, (20*60) * 5, (20*60) * 5);
    }

    private Connection getConnection() throws SQLException {
        return this.sqlService.getConnection();
    }

    @Override
    public HashMap<String, PlayerAccounts> loadAll() {
        HashMap<String, PlayerAccounts> result = new HashMap<>();
        for (Player player : VersionManager.getOnlinePlayers()) {
            result.put(new VersionPlayer(player).getIdString(), load(player));
        }
        return result;
    }

    @Override
    public PlayerAccounts loadData(OfflinePlayer player) {
        if (player == null) return null;
        return load(player);
    }

    private PlayerAccounts load(OfflinePlayer player) {
        String playerKey = new VersionPlayer(player).getIdString();
        String data = select(playerKey, player);
        if (data == null || data.isEmpty()) {
            return new PlayerAccounts(player);
        }
        AccountAgent accountAgent = new AccountAgent(JSONObject.parseObject(data));
        return accountAgent.adpPlayerAccount(player);
    }

    @Override
    public void saveData(PlayerAccounts data) {
        if (data == null || data instanceof FakePlayer) {
            return;
        }
        if (data.getOfflinePlayer() == null || !data.getOfflinePlayer().isOnline()) {
            return;
        }
        String key = new VersionPlayer((data.getOfflinePlayer())).getIdString();
        update(key, data.toJson());
    }

    public void saveByGeek(PlayerAccounts data) {
        if (data == null || data instanceof FakePlayer) {
            String info;
            if (data != null) {
                info = "is FakePlayer";
            } else  {
                info = "is null";
            }
            System.out.println("这个数据存在异常 -> "+info);
            return;
        }
        String key = new VersionPlayer((data.getOfflinePlayer())).getIdString();
        update(key, data.toJson());
    }


    @Override
    public void saveAll() {
       // System.out.println("正在准备储存所有数据");
        ConcurrentHashMap<String, PlayerAccounts> data = SkillAPI.getPlayerAccountData();
        List<Pair<String, String>> list = new ArrayList<>();
        List<String> keys = new ArrayList<>(data.keySet());
        for (String key : keys) {
            PlayerAccounts accounts = data.get(key);
            if (accounts == null || accounts instanceof FakePlayer) {
                continue;
            }
            if (accounts.getOfflinePlayer() == null || !accounts.getOfflinePlayer().isOnline()) {
                continue;
            }
            list.add(new Pair<>(key, accounts.toJson()));
        }
        updateAll(list);
      //  System.out.println("    数据储存完毕....");
    }

    public void insertSingle(PlayerAccounts data) {
        if (data instanceof FakePlayer) {
            return;
        }
        String key = new VersionPlayer((data.getOfflinePlayer())).getIdString();
        insert(key, data.toJson());
    }


    private void insert(String key, String data) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("insert into  skillapi_players(`Name`, `data`) values(?,?)")) {
                preparedStatement.setString(1, key);
                preparedStatement.setString(2, data);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insert(String key) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("insert into  skillapi_players(`Name`, `data`) values(?,?)")) {
                preparedStatement.setString(1, key);
                preparedStatement.setString(2, "");
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String select(String key, OfflinePlayer player) {
        String data = null;
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("select `data` from `skillapi_players` where Name=?")) {
                preparedStatement.setString(1, key);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    data = resultSet.getString("data");
                } else {
                    // 先找文件
              //      configIO.loadData(player);
                    insert(key);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    private void update(String key, String data) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("update `skillapi_players` set `data`=? where Name=?")) {
                preparedStatement.setString(1, data);
                preparedStatement.setString(2, key);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void updateAll(List<Pair<String, String>> data) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("update `skillapi_players` set `data`=? where Name=?")) {
                for (Pair<String, String> d : data) {
                    preparedStatement.setString(1, d.value);
                    preparedStatement.setString(2, d.key);
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
        if (sqlService != null) {
            sqlService.stopSql();
        }
    }

}
