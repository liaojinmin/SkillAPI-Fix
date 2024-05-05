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
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
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
    public PlayerData loadData(OfflinePlayer player) {
        if (player == null) return null;
        String data = select(player.getUniqueId());
        if (data == null || data.isEmpty()) {
            return new PlayerData(player);
        }
        return loadOfJson(player, JSONObject.parseObject(data));

    }


    @Override
    public void saveData(PlayerData data) {
        if (data == null || data.getPlayer() == null || !data.getPlayer().isOnline()) {
            return;
        }
        update(data.getUniqueId(), saveOfJson(data).toJSONString());
    }


    @Override
    public void saveAll() {
       // System.out.println("正在准备储存所有数据");
        ConcurrentHashMap<UUID, PlayerData> data = SkillAPI.getPlayerDataMap();
        List<Pair<UUID, String>> list = new ArrayList<>();
        List<UUID> keys = new ArrayList<>(data.keySet());
        for (UUID key : keys) {
            PlayerData playerData = data.get(key);
            if (playerData == null) {
                continue;
            }
            list.add(new Pair<>(key, saveOfJson(playerData).toJSONString()));
        }
        updateAll(list);
      //  System.out.println("    数据储存完毕....");
    }

    private void insert(UUID key) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("insert into  skillapi_players(`Name`, `data`) values(?,?)")) {
                preparedStatement.setString(1, key.toString());
                preparedStatement.setString(2, "");
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private String select(UUID key) {
        String data = null;
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("select `data` from `skillapi_players` where Name=?")) {
                preparedStatement.setString(1, key.toString());
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    data = resultSet.getString("data");
                } else {
                    insert(key);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    private void update(UUID key, String data) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("update `skillapi_players` set `data`=? where Name=?")) {
                preparedStatement.setString(1, data);
                preparedStatement.setString(2, key.toString());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateAll(List<Pair<UUID, String>> data) {
        try (Connection connection = getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("update `skillapi_players` set `data`=? where Name=?")) {
                for (Pair<UUID, String> d : data) {
                    preparedStatement.setString(1, d.value);
                    preparedStatement.setString(2, d.key.toString());
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
