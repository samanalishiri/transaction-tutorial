package com.saman.tutorial.transaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.logging.Logger;

@RunWith(Arquillian.class)
public class AcidTest {

    private final Logger logger = Logger.getLogger("RepositoryTest");

    @PersistenceContext(unitName = "transactiontutorial")
    private EntityManager em;

    @Inject
    private Repository repository;

    @Inject
    private BatchProcessRepository batchRepository;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, "com.saman.tutorial.transaction")
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Before
    public void truncate() {
        repository.truncate();
    }

    @Test
    public void atomicityCommitTest() {

        DataEntity entity1 = DataEntity.create(1, "code_1", "name_1");
        DataEntity entity2 = DataEntity.create(2, "code_2", "name_2");

        try {
            batchRepository.batch(em,
                    em -> em.persist(entity1),
                    em -> em.persist(entity2));
        } catch (RuntimeException e) {
            logger.info("there was an error in transaction");
        }

        DataEntity persistentEntity1 = em.find(DataEntity.class, 1);
        Assert.assertEquals(entity1, persistentEntity1);

        DataEntity persistentEntity2 = em.find(DataEntity.class, 2);
        Assert.assertEquals(entity2, persistentEntity2);
    }

    @Test
    public void atomicityRollbackTest() {

        try {
            batchRepository.batch(em,
                    em -> em.persist(DataEntity.create(1, "code_1", "name_1")),
                    em -> {
                        throw new RuntimeException("throw exception");
                    }
            );
        } catch (RuntimeException e) {
            logger.info("there was an error in transaction");
        }

        Long numberOfRecord = repository.countAll();
        Assert.assertEquals(new Long(0L), numberOfRecord);
    }

    @Test
    public void failedConsistencyTest() {

        DataEntity entity1 = DataEntity.create(1, "code_1", "name_1");
        DataEntity entity2 = DataEntity.create(2, "code_1", "name_1");

        try {
            batchRepository.batch(em,
                    em -> {
                        em.persist(entity1);
                        em.flush();
                    },
                    em -> {
                        em.persist(entity2);
                        em.flush();
                    }
            );
        } catch (RuntimeException e) {
            logger.info("there was an error in transaction");
        }

        Long numberOfRecord = repository.countAll();
        Assert.assertEquals(new Long(0L), numberOfRecord);
    }


    @Test
    public void isolationSerializableTest() {

        DataEntity entity = DataEntity.create(1, "code_1", "name_1");
        repository.save(entity);

        DataEntity update1 = DataEntity.create(1, "code_5", "name_5");
        DataEntity update2 = DataEntity.create(1, "code_6", "name_6");


        try {
            batchRepository.parallelBatch(
                    () -> repository.synchronizedUpdate(update1),
                    () -> repository.synchronizedUpdate(update2)
            );
        } catch (RuntimeException e) {
            logger.info("there was an error in transaction");
            logger.info(e.getMessage());
        }

        DataEntity result = repository.findById(update1.getId());
        logger.info(result.toString());
    }

    @Test
    public void durationTest() {

        DataEntity entity = DataEntity.create(1, "code_1", "name_1");

        try {
            repository.save(entity);

        } catch (RuntimeException e) {
            logger.info("there was an error in transaction");
        }

        try {
            throw new RuntimeException("system shutdown!!!");
        } catch (RuntimeException e) {
            logger.info(e.getMessage());
        }

        DataEntity persistentEntity = repository.findById(1);

        Assert.assertEquals(entity, persistentEntity);
    }
}
