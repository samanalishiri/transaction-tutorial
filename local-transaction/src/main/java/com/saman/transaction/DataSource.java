package com.saman.transaction;


import io.vavr.control.Try;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
public class DataSource {

    public static final DataSource INSTANCE = new DataSource();

    private final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    private final String JDBC_DB_URL = "jdbc:mysql://localhost:3306/tutorial?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

    private final String JDBC_USER = "root";

    private final String JDBC_PASS = "Root1234";

    private final BasicDataSource dataSource;

    private DataSource() {
        dataSource = new BasicDataSource();
        dataSource.setUsername(JDBC_USER);
        dataSource.setPassword(JDBC_PASS);
        dataSource.setDriverClassName(JDBC_DRIVER);
        dataSource.setUrl(JDBC_DB_URL);
        dataSource.setInitialSize(1);
    }

    public Connection getConnection() {
       return Try.of(() -> dataSource.getConnection()).getOrNull();
    }
}
