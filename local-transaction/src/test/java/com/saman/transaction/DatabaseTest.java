package com.saman.transaction;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
@RunWith(JUnit4.class)
public class DatabaseTest {
    private final Logger logger = LoggerFactory.getLogger("AppTest");

    private Repository repository = Repository.INSTANCE;

    @Before
    public void setUp() throws Exception {
        Assert.assertNotNull(repository);
    }

    @After
    public void finish() throws Exception {
        repository.initData();
    }

    @Test
    public void trunkTableTest() throws SQLException {
        repository.trunk();

        int count = repository.countAll();
        Assert.assertEquals(0, count);
        logger.info("count = " + count);
    }
}
