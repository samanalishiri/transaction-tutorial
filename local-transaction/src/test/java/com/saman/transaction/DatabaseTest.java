package com.saman.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */

@DisplayName("Database Test")
public class DatabaseTest {

    private final Logger logger = LoggerFactory.getLogger("DatabaseTest");

    private final Repository repository = Repository.INSTANCE;

    private final ModelTableMockData mockData = ModelTableMockData.INSTANCE;

    @BeforeEach
    public void setUp() {
        assertNotNull(repository);
    }

    @Test
    @DisplayName("Initial 'MODEL_TABLE' with stub data")
    @Order(1)
    public void insertModelTable_GivenStubData_ThenInsertInTheTable() {
        mockData.init();
        int count = repository.countAll();
        assertNotNull(count);
        assertEquals(3, count);
        logger.info("count = " + count);
    }

    @Test
    @DisplayName("Trunk 'MODEL_TABLE'")
    @Order(2)
    public void trunkModelTable_WhenCallTrunkFunction_ThenRemoveAllTuples() {
        ModelTableMockData.INSTANCE.trunk();
        int count = repository.countAll();
        assertEquals(0, count);
        logger.info("count = " + count);
    }
}
