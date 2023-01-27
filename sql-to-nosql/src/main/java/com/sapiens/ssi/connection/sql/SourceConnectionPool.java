package com.sapiens.ssi.connection.sql;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class SourceConnectionPool {

    private static BasicDataSource dataSource;


    public static BasicDataSource getDataSource(String url, String user, String password)  {
        if (dataSource == null)
        {
            BasicDataSource ds = new BasicDataSource();
            ds.setUrl(url);
            ds.setUsername(user);
            ds.setPassword(password);
            ds.setMinIdle(5);
            ds.setMaxIdle(10);
            ds.setMaxOpenPreparedStatements(100);

            dataSource = ds;
        }
        return dataSource;
    }
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private SourceConnectionPool(){ }
}



