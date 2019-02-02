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
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RunWith(Arquillian.class)
public class AcidTest {

    private final Logger logger = Logger.getLogger("RepositoryTest");

    @PersistenceContext(unitName = "transactiontutorial")
    private EntityManager em;

    @Inject
    private CrudRepository crudRepository;

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
        crudRepository.truncate();

        try {
            jpaBatchRepository.batch(em,
                    em -> {
                        em.persist(DataEntity.create(1, "code_1", "name_1"));
                        DataEntity entity = em.find(DataEntity.class, 1);
                        logger.info(entity.toString());
                    },
                    em -> {
                        em.persist(DataEntity.create(2, "code_2", "name_2"));
                        DataEntity entity = em.find(DataEntity.class, 2);
                        logger.info(entity.toString());
                    }
            );
        } catch (Exception e) {
            logger.info("there was an error in transaction");
        }

        DataEntity entity1 = em.find(DataEntity.class, 1);
        logger.info(entity1.toString());
        DataEntity entity2 = em.find(DataEntity.class, 2);
        logger.info(entity2.toString());
    }

    @Test
    public void atomicityRollbackTest() {
        crudRepository.truncate();

        try {
            jpaBatchRepository.batch(em,
                    em -> {
                        em.persist(DataEntity.create(1, "code_1", "name_1"));
                        DataEntity entity = em.find(DataEntity.class, 1);
                        logger.info(entity.toString());
                    },
                    em -> {
                        logger.info("throw exception");
                        throw new RuntimeException();
                    }
            );
        } catch (Exception e) {
            logger.info("there was an error in transaction");
        }

        List<DataEntity> entities = crudRepository.findAll();
        logger.info(String.join("\n", entities.stream().map(DataEntity::toString).collect(Collectors.toList())));
    }

}
