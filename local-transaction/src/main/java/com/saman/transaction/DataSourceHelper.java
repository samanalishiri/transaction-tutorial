package com.saman.transaction;


import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
public class DataSourceHelper {

    public static final DataSourceHelper INSTANCE = new DataSourceHelper();

    private final Logger logger = LoggerFactory.getLogger("DataSourceHelper");

    private final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private final String JDBC_DB_URL = "jdbc:mysql://localhost:3306/transactiontutorial";
    private final String JDBC_USER = "root";
    private final String JDBC_PASS = "root";

    private final BasicDataSource dataSource;

    private DataSourceHelper() {
        dataSource = new BasicDataSource();
        dataSource.setUsername(JDBC_USER);
        dataSource.setPassword(JDBC_PASS);
        dataSource.setDriverClassName(JDBC_DRIVER);
        dataSource.setUrl(JDBC_DB_URL);
        dataSource.setInitialSize(1);
    }

    public Connection get() {
        try {
            return dataSource.getConnection();

        } catch (SQLException e) {
            logger.error("can't connect");
        }

        return null;
    }
}
