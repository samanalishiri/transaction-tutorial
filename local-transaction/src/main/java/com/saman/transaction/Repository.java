package com.saman.transaction;

import io.vavr.control.Try;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.saman.transaction.CollectionTransformer.transform;
import static com.saman.transaction.Model.CODE_INDEX;
import static com.saman.transaction.Model.ID_INDEX;
import static com.saman.transaction.Model.NAME_INDEX;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
public final class Repository extends AbstractRepository {

    public static final Repository INSTANCE = new Repository();

    public static final String QUERY_INSERT = "INSERT INTO model_table (id, code, name) values (?, ?, ?)";

    public static final String QUERY_FIND_BY_ID = "SELECT * FROM model_table tbl WHERE tbl.id = ?";

    public static final String QUERY_UPDATE = "UPDATE model_table SET code = ?, name = ? WHERE id = ?";

    public static final String QUERY_DELETE_BY_ID = "DELETE FROM model_table WHERE id = ?";

    public static final String QUERY_COUNT_ID = "SELECT COUNT(id) AS count_id FROM model_table tbl";

    public static final String QUERY_FIND_ALL_ID = "SELECT id FROM model_table tbl";

    public static final String QUERY_FIND_BY_CODE = "SELECT * FROM model_table tbl WHERE tbl.code = ?";

    public static final String QUERY_FIND_BY_NAME = "SELECT * FROM model_table tbl WHERE tbl.name = ?";

    private final Function<ResultSet, Model> transformer = new ModelTransformer();

    private Repository() {
    }

    public int save(Connection connection, Model model) {
        if (model.getId() == 0)
            model = Model.persistenceBuilder(Sequence.nextModelId()).of(model).build();

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(QUERY_INSERT);
            statement.setInt(ID_INDEX, model.getId());
            statement.setString(CODE_INDEX, model.getCode());
            statement.setString(NAME_INDEX, model.getName());

            statement.executeUpdate();
            logSql(QUERY_INSERT, model);

        } catch (SQLException e) {
            logException("save", e);
            rollback(connection, e);

        } finally {
            close(statement);
        }

        return model.getId();
    }

    public Optional<Model> findById(int id) {

        Connection connection = DataSource.INSTANCE.getConnection();
        PreparedStatement statement = null;
        ResultSet tuples = null;

        try {
            statement = connection.prepareStatement(QUERY_FIND_BY_ID);
            statement.setInt(1, id);

            tuples = statement.executeQuery();
            logSql(QUERY_FIND_BY_ID, id);

            if (tuples.next())
                return ofNullable(transformer.apply(tuples));

        } catch (SQLException e) {
            logException("findById", e);

        } finally {
            close(tuples, statement, connection);
        }

        return empty();
    }

    public void update(Connection connection, Model model) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(QUERY_UPDATE);
            statement.setString(1, model.getCode());
            statement.setString(2, model.getName());
            statement.setInt(3, model.getId());

            statement.executeUpdate();
            logSql(QUERY_UPDATE, model);

        } catch (SQLException e) {
            logException("update", e);
            rollback(connection, e);

        } finally {
            close(statement);
        }
    }

    public void deleteById(Connection connection, int id) {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(QUERY_DELETE_BY_ID);
            statement.setInt(1, id);
            statement.execute();
            logSql(QUERY_DELETE_BY_ID, id);

        } catch (SQLException e) {
            logException("deleteById", e);
            rollback(connection, e);

        } finally {
            close(statement);
        }
    }

    public int countAll() {
        Connection connection = DataSource.INSTANCE.getConnection();
        PreparedStatement statement = null;
        ResultSet tuples = null;
        try {
            statement = connection.prepareStatement(QUERY_COUNT_ID);
            tuples = statement.executeQuery();

            logSql(QUERY_COUNT_ID, "none");

            if (tuples.next())
                return tuples.getInt("count_id");

        } catch (SQLException e) {
            logException("countAll", e);

        } finally {
            close(tuples, statement, connection);
        }

        return 0;
    }

    public List<Integer> getIdentities() {
        Connection connection = DataSource.INSTANCE.getConnection();
        PreparedStatement statement = null;
        ResultSet tuples = null;

        try {
            statement = connection.prepareStatement(QUERY_FIND_ALL_ID);
            tuples = statement.executeQuery();
            logSql(QUERY_FIND_ALL_ID, "none");

            return transform(tuples, tuple -> Try.of(() -> tuple.getInt("id")).get());

        } catch (SQLException e) {
            logException("getIdentities", e);

        } finally {
            close(tuples, statement, connection);
        }

        return new ArrayList<>();
    }

    public Optional<Model> findByCode(String code) {

        Connection connection = DataSource.INSTANCE.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(QUERY_FIND_BY_CODE);
            statement.setString(1, code);

            ResultSet data = statement.executeQuery();
            logSql(QUERY_FIND_BY_CODE, code);

            if (data.next())
                return ofNullable(transformer.apply(data));

        } catch (SQLException e) {
            logException("findByCode", e);
        }

        return empty();
    }

    public Optional<Model> findByName(String name) {

        Connection connection = DataSource.INSTANCE.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(QUERY_FIND_BY_NAME);
            statement.setString(1, name);

            ResultSet data = statement.executeQuery();
            logSql(QUERY_FIND_BY_CODE, name);

            if (data.next())
                return ofNullable(transformer.apply(data));

        } catch (SQLException e) {
            logException("findByName", e);
        }

        return empty();
    }

    public Optional<Model> saveAndCommit(Connection connection, Model model) {

        try {
            int id = this.save(connection, model);
            if (!connection.getAutoCommit())
                connection.commit();
            logger.info("commit transaction");
            return Optional.ofNullable(Model.persistenceBuilder(id).of(model).build());

        } catch (SQLException e) {
            logException("synchronizedSave", e);
            rollback(connection, e);
        }

        return empty();
    }

    public Optional<Model> updateAndCommit(Connection connection, Model model) {

        try {
            this.update(connection, model);
            if (!connection.getAutoCommit())
                connection.commit();
            logger.info("commit transaction");
            return findById(model.getId());

        } catch (SQLException e) {
            logException("synchronizedUpdate", e);
            rollback(connection, e);
        }
        return empty();
    }

}
