package com.saman.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
public class SynchronizedRepository extends Repository {

    public static final SynchronizedRepository INSTANCE = new SynchronizedRepository();

    @Override
    public synchronized void update(Connection connection, DataModel model) {
        super.update(connection, model);
    }

    @Override
    public synchronized void save(Connection connection, DataModel model) {
        super.save(connection, model);
    }

    public synchronized void updateWithCommit(Connection connection, DataModel model) throws SQLException {

        try {
            super.update(connection, model);
            connection.commit();
            logger.info("commit transaction");

        } catch (SQLException e) {
            logger.error("rollback transaction");
            logger.error(e.getMessage());
            connection.rollback();
            throw e;

        }
    }

    public synchronized void saveWithCommit(Connection connection, DataModel model) throws SQLException {

        try {
            super.save(connection, model);
            connection.commit();
            logger.info("commit transaction");

        } catch (SQLException e) {
            logger.error("rollback transaction");
            logger.error(e.getMessage());
            connection.rollback();
            throw e;

        }
    }
}
