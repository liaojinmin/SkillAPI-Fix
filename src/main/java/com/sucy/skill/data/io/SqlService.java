package com.sucy.skill.data.io;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 作者: 老廖
 * 时间: 2022/11/6
 **/
public interface SqlService {


    Connection getConnection() throws SQLException;

    void startSql();

    void stopSql();
}
