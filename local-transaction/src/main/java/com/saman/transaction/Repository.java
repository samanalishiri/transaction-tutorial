package com.saman.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
public class Repository {

    public static final Repository INSTANCE = new Repository();

    protected final Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final Transformer<DataModel> transformer = new DataModelTransformer();

    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    protected Repository() {
    }

    public void initData() throws SQLException {
        Connection connection = DataSourceHelper.INSTANCE.get();
        connection.setAutoCommit(false);

        DataModel model1 = DataModel.create(1, "code_1", "name_1");
        DataModel model2 = DataModel.create(2, "code_2", "name_2");
        DataModel model3 = DataModel.create(3, "code_3", "name_3");

        save(connection, model1);
        save(connection, model2);
        save(connection, model3);

        connection.commit();
        connection.close();
    }

    public void resetData() throws SQLException {
        Connection connection = DataSourceHelper.INSTANCE.get();
        connection.setAutoCommit(false);

        DataModel model1 = DataModel.create(1, "code_1", "name_1");
        DataModel model2 = DataModel.create(2, "code_2", "name_2");
        DataModel model3 = DataModel.create(3, "code_3", "name_3");

        update(connection, model1);
        update(connection, model2);
        update(connection, model3);

        connection.commit();
        connection.close();
    }

    public void batch(Connection connection, Consumer<Connection>... consumer) throws SQLException {

        try {
            connection.setAutoCommit(false);
            Arrays.stream(consumer).forEach(c -> c.accept(connection));
            connection.commit();
            logger.info("commit transaction");

        } catch (SQLException e) {
            logger.error("rollback transaction");
            logger.error(e.getMessage());
            connection.rollback();
            throw e;

        } finally {
            logger.info("connection.close!");
            connection.close();
        }
    }

    public void parallelBatch(Connection connection, Consumer<Connection>... consumer) throws SQLException, InterruptedException {

        try {
            connection.setAutoCommit(false);
            Arrays.stream(consumer).forEach(c -> executor.execute(() -> c.accept(connection)));

        } catch (SQLException e) {
            logger.error("rollback transaction");
            logger.error(e.getMessage());
            connection.rollback();
            throw e;

        } finally {
            executor.shutdown();
            while (!executor.isTerminated()) {
                logger.info("waiting for termination");
            }
            connection.commit();
            logger.info("commit transaction");
            connection.close();
            logger.info("connection.close!");
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


            if (data.next()) {
                DataModel model = transformer.transform(data);
                logger.info(model.toString());
                return model;
            }

        } catch (SQLException e) {
            logger.error("can't find DataModel{id=" + id + "}");
            logger.error(e.getMessage());
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

            if (data.next()) {
                DataModel model = transformer.transform(data);
                logger.info(model.toString());
                return model;
            }

        } catch (SQLException e) {
            logger.error("can't find DataModel{code=" + code + "}");
            logger.error(e.getMessage());
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
            logger.info(model.toString());

        } catch (SQLException e) {
            logger.error("can't execute update statement");
            logger.error(e.getMessage());
            throw new RuntimeException(e);

        } finally {
            closeStatement(statement);
        }
    }

    public void save(Connection connection, DataModel model) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("INSERT INTO local_transaction_test (id, code, name) values (?, ?, ?)");

            statement.setInt(1, model.getId());
            statement.setString(2, model.getCode());
            statement.setString(3, model.getName());
            statement.executeUpdate();
            logger.info(model.toString());

        } catch (SQLException e) {
            logger.error("can't execute insert statement");
            logger.error(e.getMessage());
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
