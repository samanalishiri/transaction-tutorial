package com.saman.transaction;

import io.vavr.control.Try;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
public final class BatchRepository extends AbstractRepository {

    public static final BatchRepository INSTANCE = new BatchRepository();

    private final ExecutorService executor = Executors.newWorkStealingPool();

    private BatchRepository() {
    }

    public void batch(Connection connection, Consumer<Connection>... consumer) {

        try {
            connection.setAutoCommit(false);
            Arrays.stream(consumer).forEach(c -> c.accept(connection));
            connection.commit();
            logger.info("commit transaction!");

        } catch (SQLException e) {
            logException("batch", e);
            rollback(connection, e);

        } finally {
            logger.info("connection close!");
            close(connection);
        }
    }

    public void parallelBatch(Connection connection, Consumer<Connection>... consumer) {

        try {
            connection.setAutoCommit(false);

            ArrayList<Callable<Void>> collect = Arrays.stream(consumer).map(c -> {
                Callable<Void> a = () -> {
                    c.accept(connection);
                    return Void.TYPE.newInstance();
                };
                return a;
            }).collect(Collectors.toCollection(ArrayList::new));

            executor.invokeAll(collect);

        } catch (Exception e) {
            logException("parallelBatch", e);
            rollback(connection, e);

        } finally {
            executor.shutdown();
            while (!executor.isTerminated())
                logger.info("waiting for termination");

            Try.run(() -> connection.commit());
            logger.info("commit transaction!");
            Try.run(() -> connection.close());
            logger.info("connection close!");
        }
    }
}
