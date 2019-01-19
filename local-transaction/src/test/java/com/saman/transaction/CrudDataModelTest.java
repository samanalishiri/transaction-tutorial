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
public class CrudDataModelTest {
    private final Logger logger = LoggerFactory.getLogger("AppTest");

    private Repository repository = Repository.INSTANCE;

    @Before
    public void setUp() throws Exception {
        Assert.assertNotNull(repository);
    }

    @Test
    public void updateTest() throws SQLException {
        repository.resetData();

        Connection connection = DataSourceHelper.INSTANCE.get();
        connection.setAutoCommit(false);

        DataModel model = DataModel.create(1, "code_4", "name_4");
        repository.update(connection, model);
        connection.commit();

        DataModel updatedModel = repository.findById(model.getId());
        Assert.assertEquals(model, updatedModel);

        connection.close();
    }
}
