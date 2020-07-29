package com.saman.transaction;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
@DisplayName("ACID on 'Model' Tests")
@TestMethodOrder(OrderAnnotation.class)
public class AcidModelTest {

    public static final DataSource DATA_SOURCE = DataSource.INSTANCE;
    private final Logger logger = LoggerFactory.getLogger(AcidModelTest.class.getSimpleName());

    private final Repository repository = Repository.INSTANCE;

    private final BatchRepository batchRepository = BatchRepository.INSTANCE;

    @BeforeAll
    public static void beforeAll() {
        ModelTableMockData.INSTANCE.trunk();
    }

    @BeforeEach
    public void setUp() {
        assertNotNull(repository);
    }

    @AfterEach
    void tearDown() {
        ModelTableMockData.INSTANCE.trunk();
    }

    @Test
    @DisplayName("Atomicity Commit Test")
    @Order(1)
    public void atomicityCommit_WhenInsertMultiModelInTheTransaction_ThenCommitThemAtTheEndOfTransaction() {
        Connection connection = DATA_SOURCE.getConnection();

        try {
            batchRepository.batch(connection,
                    c -> {
                        int id = repository.save(c, Model.transientBuilder().code("code1").name("name1").build());
                        Optional<Model> model = repository.findById(id);
                        assertFalse(model.isPresent());
                    },
                    c -> {
                        int id = repository.save(c, Model.transientBuilder().code("code2").name("name2").build());
                        Optional<Model> model = repository.findById(id);
                        assertFalse(model.isPresent());
                    }
            );
        } catch (RuntimeException e) {
            logger.error("there is an error in transaction");
        }

        int count = repository.countAll();
        assertNotNull(count);
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Atomicity Rollback Test")
    @Order(2)
    public void atomicityRollback_WhenExceptionIsThrown_THenAllOperationShouldBeRollback() {
        Connection connection = DATA_SOURCE.getConnection();

        try {
            batchRepository.batch(connection,
                    c -> repository.save(c, Model.transientBuilder().code("testCode").name("testName").build()),
                    c -> {
                        logger.error("throw exception!!!");
                        throw new RuntimeException();
                    });
        } catch (RuntimeException e) {
            logger.error("there is an error in transaction");
        }

        int count = repository.countAll();
        assertNotNull(count);
        assertEquals(0, count);
    }

    @Test
    @DisplayName("Consistency Test")
    @Order(3)
    public void consistency_WhenBreakTheConstraint_ThenStorageMustPreventTheInsertModel() {

        Connection connection = DATA_SOURCE.getConnection();

        int id = repository.save(connection, Model.transientBuilder().code("code1").name("name1").build());

        try {
            batchRepository.batch(connection,
                    c -> repository.save(c, Model.persistenceBuilder(id).code("testCode").name("testName").build()),
                    c -> repository.save(c, Model.transientBuilder().code("code2").name("name2").build())
            );
        } catch (RuntimeException e) {
            logger.error("there is an error in transaction");
        }

        boolean isPresentModel = repository.findByCode("testCode").isPresent();
        assertFalse(isPresentModel);

        Optional<Model> model = repository.findById(id);
        assertTrue(model.isPresent());

        Model value = model.get();
        assertNotNull(value);

        assertNotNull(value.getId());
        assertEquals(id, value.getId());

        assertNotNull(value.getCode());
        assertEquals("code1", value.getCode());

        assertNotNull(value.getName());
        assertEquals("name1", value.getName());
    }

    @Test
    @DisplayName("Isolation Serializable Test")
    @Order(4)
    public void isolationSerializable_WhenUpdateDataParallel_ThenUpdateDataOneByOne() {
        Connection connection = DATA_SOURCE.getConnection();
        Optional<Model> testModel = repository.saveAndCommit(connection, Model.transientBuilder()
                .code("testCode")
                .name("testName")
                .build());

        assertTrue(testModel.isPresent());
        int id = testModel.get().getId();
        assertNotNull(id);

        try {
            batchRepository.parallelBatch(connection,
                    c -> {

                        Optional<Model> model = repository.updateAndCommit(c, Model.persistenceBuilder(id)
                                .code("code1")
                                .name("name1")
                                .build());

                        assertTrue(model.isPresent());

                        Model value = model.get();

                        assertNotNull(value.getCode());
                        assertEquals("code1", value.getCode());

                        assertNotNull(value.getName());
                        assertEquals("name1", value.getCode());
                    },
                    c -> {
                        Optional<Model> model = repository.updateAndCommit(c, Model.persistenceBuilder(id)
                                .code("code2")
                                .name("name2")
                                .build());

                        assertTrue(model.isPresent());

                        Model value = model.get();

                        assertNotNull(value.getCode());
                        assertEquals("code2", value.getCode());

                        assertNotNull(value.getName());
                        assertEquals("name2", value.getCode());
                    }
            );
        } catch (RuntimeException e) {
            logger.error("there is an error in transaction");
        }
    }

    @Test
    @DisplayName("Duration Test")
    public void durationTest() {
        Connection connection = DATA_SOURCE.getConnection();
        Optional<Model> modelBeforeException = repository.saveAndCommit(connection, Model.transientBuilder()
                .code("testCode")
                .name("testName")
                .build());

        assertTrue(modelBeforeException.isPresent());
        assertNotNull(modelBeforeException.get().getId());

        try {
            throw new RuntimeException("system shutdown!!!");
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
        }

        Optional<Model> modelAfterException = repository.findById(modelBeforeException.get().getId());
        assertEquals(modelBeforeException, modelAfterException);
    }

}
