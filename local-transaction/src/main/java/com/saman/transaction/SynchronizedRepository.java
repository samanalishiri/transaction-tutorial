package com.saman.transaction;

import java.sql.Connection;

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
}
