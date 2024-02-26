package com.sucy.skill.data.io;


import com.sucy.skill.data.Settings;
import com.zaxxer.hikari.HikariDataSource;

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
public final class Mysql implements SqlService {
    private static final String MYSQL_CREATE = "CREATE TABLE IF NOT EXISTS `skillapi_players` (" +
            " `id` integer NOT NULL AUTO_INCREMENT, " +
            " `Name` VARCHAR(36) NOT NULL," +
            " `data` TEXT NOT NULL," +
            "PRIMARY KEY (`id`)" +
            ");";
    private HikariDataSource dataSource;
    private final Settings settings;


    public Mysql(Settings settings) {
        this.settings = settings;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void startSql() {
        final String MysqlUrl = "jdbc:mysql://" + settings.getSQLHost() + ":" + settings.getSQLPort() + "/" + settings.getSQLDatabase() + "?autoReconnect=true&useSSL=false";
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(MysqlUrl);
        dataSource.setUsername(settings.getSQLUser());
        dataSource.setPassword(settings.getSQLPass());
        try {
            Class.forName("com.mysql.jdbc.Driver");
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        } catch (RuntimeException | NoClassDefFoundError | ClassNotFoundException e) {
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        }
        dataSource.setMaximumPoolSize(30);
        dataSource.setMinimumIdle(5);
        dataSource.setMaxLifetime(1800000);
        dataSource.setKeepaliveTime(0);
        dataSource.setConnectionTimeout(5000);
        dataSource.setPoolName("SkillAPI-Fix-MYSQL");
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(MYSQL_CREATE);
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
