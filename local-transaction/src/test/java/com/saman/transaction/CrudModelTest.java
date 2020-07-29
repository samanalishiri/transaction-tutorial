package com.saman.transaction;

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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
@DisplayName("CRUD operations on 'Model' Test")
@TestMethodOrder(OrderAnnotation.class)
public class CrudModelTest {

    public static final DataSource DATA_SOURCE = DataSource.INSTANCE;

    private static final Map<String, Model> MODELS = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(CrudModelTest.class.getSimpleName());

    private final Repository repository = Repository.INSTANCE;

    @BeforeAll
    public static void beforeAll() {
        ModelTableMockData.INSTANCE.trunk();
    }

    @BeforeEach
    public void setUp() {
        assertNotNull(repository);
    }

    @Test
    @DisplayName("GIVEN 'Model' WHEN execute save operation THEN return an ID")
    @Order(1)
    void save_GivenModelAsParam_WhenInsertModelIntoStorage_ThenReturnId() throws SQLException {
        Connection connection = DataSource.INSTANCE.getConnection();
        connection.setAutoCommit(false);

        Model model = Model.transientBuilder().code("testCode").name("testName").build();
        int id = repository.save(connection, model);

        connection.commit();

        assertNotNull(id);
        assertEquals(1, id);

        MODELS.put("test", Model.persistenceBuilder(id).of(model).build());
    }

    @Test
    @DisplayName("GIVEN 'ID' WHEN execute findById operation THEN return a Model")
    @Order(2)
    public void findById_GivenIdAsParam_WhenFindModelById_ThenReturnModel() {
        int id = MODELS.get("test").getId();
        Optional<Model> model = repository.findById(id);
        assertTrue(model.isPresent());

        Model value = model.get();
        assertNotNull(value);

        assertNotNull(value.getId());
        assertEquals(1, value.getId());

        assertNotNull(value.getCode());
        assertEquals("testCode", value.getCode());

        assertNotNull(value.getName());
        assertEquals("testName", value.getName());
    }

    @Test
    @DisplayName("GIVEN 'Model' WHEN execute update operation THEN return a Model")
    @Order(3)
    public void update_GivenModelAsParam_WhenReplaceCurrentSateWithNewSate_ThenCommitShouldBeSuccess() throws SQLException {

        Connection connection = DATA_SOURCE.getConnection();
        connection.setAutoCommit(false);

        Model model = MODELS.get("test");
        Model newSate = Model.persistenceBuilder(model.getId()).of(model).name("testNameUpdated").build();
        repository.update(connection, newSate);

        connection.commit();
        connection.close();

        Optional<Model> updateModel = repository.findById(model.getId());
        assertTrue(updateModel.isPresent());

        Model value = updateModel.get();
        assertNotNull(value);

        assertNotNull(value.getId());
        assertEquals(1, value.getId());

        assertNotNull(value.getCode());
        assertEquals("testCode", value.getCode());

        assertNotNull(value.getName());
        assertEquals("testNameUpdated", value.getName());
    }

    @Test
    @DisplayName("GIVEN 'ID' WHEN execute deleteById operation THEN remove the Model")
    @Order(4)
    public void deleteById_GivenIdAsParam_WhenRemoveModelById_ThenCommitShouldBeSuccess() throws SQLException {
        int id = MODELS.get("test").getId();
        Connection connection = DATA_SOURCE.getConnection();

        connection.setAutoCommit(false);
        repository.deleteById(connection, id);
        connection.commit();
        connection.close();

        Optional<Model> model = repository.findById(id);
        assertFalse(model.isPresent());
    }
}
