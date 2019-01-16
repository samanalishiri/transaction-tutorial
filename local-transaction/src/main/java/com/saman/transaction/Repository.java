package com.saman.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
public class Repository {

    private final Logger logger = LoggerFactory.getLogger("Repository");

    public static final Repository INSTANCE = new Repository();

    private final Transformer<DataModel> transformer = new DataModelTransformer();

    private Repository() {
    }

    public void batch(Connection connection, Consumer<Connection>... consumer) throws SQLException {

        try {
            connection.setAutoCommit(false);
            Arrays.stream(consumer).forEach(c -> c.accept(connection));
            connection.commit();
            logger.info("commit transaction, " + Thread.currentThread().getName());

        } catch (SQLException e) {
            logger.info("rollback transaction, " + Thread.currentThread().getName());
            connection.rollback();
            throw e;

        } finally {
            connection.close();
        }
    }

    public DataModel findById(int id) {

        Connection connection = DataSourceHelper.INSTANCE.get();

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT \n" +
                            "    *\n" +
                            "FROM\n" +
                            "    transaction.local_transaction_test tbl\n" +
                            "WHERE\n" +
                            "    tbl.id = ?");

            statement.setInt(1, id);
            ResultSet data = statement.executeQuery();

            return transformer.transform(data);

        } catch (SQLException e) {
            logger.info(e.getMessage());
        }

        return new DataModel();
    }

    public DataModel findByCode(String code) {

        Connection connection = DataSourceHelper.INSTANCE.get();

        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT \n" +
                            "    *\n" +
                            "FROM\n" +
                            "    transaction.local_transaction_test tbl\n" +
                            "WHERE\n" +
                            "    tbl.code = ?");

            statement.setString(1, code);
            ResultSet data = statement.executeQuery();

            if(data.next()) {
                return transformer.transform(data);
            }

        } catch (SQLException e) {
            logger.info(e.getMessage());
        }

        return new DataModel();
    }

    public void update(Connection connection, DataModel model) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(
                    "UPDATE local_transaction_test \n" +
                            "SET \n" +
                            "    code = ?,\n" +
                            "    name = ?\n" +
                            "WHERE\n" +
                            "    id = ?;");

            statement.setString(1, model.getCode());
            statement.setString(2, model.getName());
            statement.setInt(3, model.getId());
            statement.executeUpdate();

        } catch (SQLException e) {
            logger.error("can't execute statement");
            throw new RuntimeException(e);

        } finally {
            closeStatement(statement);
        }
    }

    public void save(Connection connection, DataModel model) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("INSERT INTO local_transaction_test (code, name) values (?, ?)");

            statement.setString(1, model.getCode());
            statement.setString(2, model.getName());
            statement.executeUpdate();

        } catch (SQLException e) {
            logger.error("can't execute statement");
            throw new RuntimeException(e);

        } finally {
            closeStatement(statement);

        }
    }

    private void closeStatement(PreparedStatement statement) {
        try {
            if (Objects.nonNull(statement)) {
                statement.close();
            }

        } catch (SQLException e) {
            logger.error("can't close statement");
            throw new RuntimeException(e);
        }
    }
}
