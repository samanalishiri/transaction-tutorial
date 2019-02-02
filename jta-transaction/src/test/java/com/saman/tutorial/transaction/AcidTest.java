package com.saman.tutorial.transaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
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
    private JpaBatchRepository jpaBatchRepository;

    @Deployment
    public static WebArchive createTestArchive() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addPackages(true, "com.saman.tutorial.transaction")
                .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void atomicityCommitTest() {
        repository.truncate();

        DataEntity entity1 = DataEntity.create(1, "code_1", "name_1");
        DataEntity entity2 = DataEntity.create(2, "code_2", "name_2");

        try {
            jpaBatchRepository.batch(em,
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
        repository.truncate();

        try {
            jpaBatchRepository.batch(em,
                    em -> em.persist(DataEntity.create(1, "code_1", "name_1")),
                    em -> {
                        logger.info("throw exception");
                        throw new RuntimeException();
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
        repository.truncate();

        DataEntity entity1 = DataEntity.create(1, "code_1", "name_1");
        DataEntity entity2 = DataEntity.create(2, "code_1", "name_1");

        try {
            jpaBatchRepository.batch(em,
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
}
