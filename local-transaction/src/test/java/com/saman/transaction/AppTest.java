package com.saman.transaction;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
public class AppTest {

    private final Logger logger = LoggerFactory.getLogger("AppTest");

    private Repository repository = Repository.INSTANCE;

    @Before
    public void setUp() throws Exception {
        Assert.assertNotNull(repository);
    }

    @Test
    public void updateTest() throws SQLException {
        DataModel beforeUpdate = repository.findById(1);

        Connection connection = DataSourceHelper.INSTANCE.get();
        connection.setAutoCommit(false);

        DataModel model = DataModel.create(1, "code_4", "name_4");
        repository.update(connection, model);
        connection.commit();

        DataModel updatedModel = repository.findById(model.getId());

        Assert.assertEquals(model, updatedModel);

        repository.update(connection, beforeUpdate);
        connection.commit();

        connection.close();
    }

    @Test
    public void atomicityCommitTest() throws SQLException {
        DataModel model1 = DataModel.create(1, "code_5", "name_5");
        DataModel model2 = DataModel.create(1, "code_6", "name_6");

        Connection connection = DataSourceHelper.INSTANCE.get();

        try {
            repository.batch(connection,
                    c -> repository.update(c, model1),
                    c -> repository.update(c, model2)
            );
        } catch (RuntimeException e) {
            logger.error("there was an error in transaction");
        }

        DataModel updatedModel = repository.findById(model1.getId());

        logger.info(model1.toString());
        logger.info(model2.toString());
        logger.info(updatedModel.toString());

        Assert.assertNotEquals(model1, updatedModel);
        Assert.assertEquals(model2, updatedModel);
    }

    @Test
    public void atomicityRollbackTest() throws SQLException {
        DataModel model = DataModel.create(1, "code_5", "name_5");
        Connection connection = DataSourceHelper.INSTANCE.get();

        try {
            repository.batch(connection,
                    c -> repository.update(c, model),
                    c -> {
                        throw new RuntimeException();
                    });
        } catch (RuntimeException e) {
            logger.error("there was an error in transaction");
        }

        DataModel updatedModel = repository.findById(model.getId());

        logger.info(model.toString());
        logger.info(updatedModel.toString());

        Assert.assertNotEquals(model, updatedModel);
    }

    @Test
    public void consistencyTest() throws SQLException {
        DataModel model1 = DataModel.create("code_5", "name_5");
        DataModel model2 = DataModel.create("code_2", "name_2");

        Connection connection = DataSourceHelper.INSTANCE.get();

        try {
            repository.batch(connection,
                    c -> repository.save(c, model1),
                    c -> repository.save(c, model2)
            );
        } catch (RuntimeException e) {
            logger.error("there was an error in transaction");
        }

        DataModel model1AfterPersist = repository.findByCode("code_5");

        Assert.assertEquals(model1AfterPersist.getId(), 0);
    }

    @Test
    public void isolationTest() throws SQLException {
    }

    @Test
    public void durationTest() throws SQLException {
    }


}
