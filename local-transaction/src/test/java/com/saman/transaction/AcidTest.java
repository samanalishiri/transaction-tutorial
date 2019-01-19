package com.saman.transaction;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
@RunWith(JUnit4.class)
public class AcidTest {

    private final Logger logger = LoggerFactory.getLogger("AppTest");

    private Repository repository = Repository.INSTANCE;
    private SynchronizedRepository synchronizedRepository = SynchronizedRepository.INSTANCE;

    @Before
    public void setUp() throws Exception {
        Assert.assertNotNull(repository);
        repository.resetData();
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
                        logger.error("throw exception!!!");
                        throw new RuntimeException();
                    });
        } catch (RuntimeException e) {
            logger.error("there was an error in transaction");
        }

        DataModel updatedModel = repository.findById(model.getId());

        Assert.assertNotEquals(model, updatedModel);
    }

    @Test
    public void consistencyTest() throws SQLException {
        DataModel model1 = DataModel.create(1, "code_5", "name_5");
        DataModel model2 = DataModel.create(2, "code_2", "name_2");

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
    public void isolationSerializableTest() throws SQLException, InterruptedException {
        DataModel model1 = DataModel.create(1, "code_5", "name_5");
        DataModel model2 = DataModel.create(1, "code_6", "name_6");

        Connection connection = DataSourceHelper.INSTANCE.get();

        try {
            repository.parallelBatch(connection,
                    c -> synchronizedRepository.update(c, model1),
                    c -> synchronizedRepository.update(c, model2)
            );
        } catch (RuntimeException e) {
            logger.error("there was an error in transaction");
        }

        DataModel updatedModel = repository.findById(model1.getId());

        Assert.assertNotEquals(model1, updatedModel);
        Assert.assertEquals(model2, updatedModel);

    }

    @Test
    public void durationTest() throws SQLException {
    }


}