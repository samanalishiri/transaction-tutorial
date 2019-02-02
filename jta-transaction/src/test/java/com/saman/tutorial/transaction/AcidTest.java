package com.saman.tutorial.transaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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

        jpaBatchRepository.batch(em,
                entityManager -> {
                    entityManager.persist(DataEntity.create(1, "code_1", "name_1"));
                    DataEntity entity = entityManager.find(DataEntity.class, 1);
                    logger.info(entity.toString());
                },
                entityManager -> {
                    entityManager.persist(DataEntity.create(2, "code_2", "name_2"));
                    DataEntity entity = entityManager.find(DataEntity.class, 2);
                    logger.info(entity.toString());
                }
        );

        DataEntity entity1 = em.find(DataEntity.class, 1);
        logger.info(entity1.toString());
        DataEntity entity2 = em.find(DataEntity.class, 2);
        logger.info(entity2.toString());
    }

}
