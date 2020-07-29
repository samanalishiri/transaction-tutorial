package com.saman.transaction;

import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.isNull;

public abstract class AbstractRepository {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    protected void rollback(Connection connection, Exception e) {
        logger.error("rollback transaction!");
        Try.run(() -> connection.rollback());
        throw new RuntimeException(e);
    }

    protected void close(AutoCloseable... items) {
        if (isNull(items) || items.length == 0)
            return;

        Stream.of(items).forEach(item -> Try.run(item::close));
    }

    protected void logSql(String sql, Object param) {
        logger.info(format("Query:%s, Param:%s", sql, param));
    }

    protected void logException(String method, Exception e) {
        logger.error("could not execute " + method);
        logger.error(e.getMessage());
    }
}
