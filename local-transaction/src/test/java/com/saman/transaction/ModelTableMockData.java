package com.saman.transaction;

import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Objects.isNull;

public class ModelTableMockData {

    public static final ModelTableMockData INSTANCE = new ModelTableMockData();

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final Repository repository = Repository.INSTANCE;

    private ModelTableMockData() {
    }

    public void init() {
        Connection connection = DataSource.INSTANCE.getConnection();
        try {
            connection.setAutoCommit(false);

            IntStream.rangeClosed(1, 3)
                    .forEach(index -> repository.save(connection, Model.transientBuilder()
                            .code("code_" + index + 1)
                            .name("name_" + index + 1)
                            .build()));
            connection.commit();

        } catch (SQLException e) {
            logger.error("could not initial table_model");
            rollback(connection);

        } finally {
            close(connection);
        }
    }

    public void trunk() {
        List<Integer> identities = repository.getIdentities();

        if(isNull(identities) || identities.isEmpty())
            return;

        String identitiesString = join(",", identities.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));

        Connection connection = DataSource.INSTANCE.getConnection();
        PreparedStatement statement = null;

        try {
            String query = format("DELETE FROM model_table WHERE id in (%s)", identitiesString);
            statement = connection.prepareStatement(query);
            statement.execute();
            logger.debug(query);

        } catch (SQLException e) {
            logger.error("could not execute trunk table");
            logger.error(e.getMessage());

        } finally {
            close(statement, connection);
        }
    }

    public void resetData() {
        trunk();
        init();
    }

    private void rollback(Connection connection) {
        Try.run(() -> connection.rollback());
    }

    private void close(AutoCloseable... items) {
        if (isNull(items) || items.length == 0)
            return;

        Stream.of(items).forEach(item -> Try.run(item::close));
    }
}
