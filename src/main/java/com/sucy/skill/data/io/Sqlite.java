package com.sucy.skill.data.io;

import com.sucy.skill.SkillAPI;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * GeekCollectLimit
 * me.geek.collect.sql.impl
 *
 * @author 老廖
 * @since 2023/10/3 6:40
 */
public class Sqlite implements SqlService {

    private static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS `skillapi_players` (" +
            " `id` integer PRIMARY KEY, " +
            " `Name` VARCHAR(36) NOT NULL , " +
            " `data` TEXT Not Null" +
            ");";
    private HikariDataSource dataSource;

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void startSql() {
        final String url = "jdbc:sqlite:"+ SkillAPI.singleton().getDataFolder() + File.separator + "data.db";
        dataSource = new HikariDataSource();
        dataSource.setDataSourceClassName("org.sqlite.SQLiteDataSource");
        dataSource.addDataSourceProperty("url", url);
        //附件参数
        dataSource.setMaximumPoolSize(20);
        dataSource.setMinimumIdle(5);
        dataSource.setMaxLifetime(1800000);
        dataSource.setKeepaliveTime(0);
        dataSource.setConnectionTimeout(5000);
        dataSource.setIdleTimeout(60000);
        dataSource.setPoolName("SkillAPI-Fix-Sqlite");
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(SQL_CREATE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopSql() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
