package com.saman.tutorial.transaction;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class BatchProcessRepositoryImpl implements BatchProcessRepository {

    private final Logger logger = Logger.getLogger("JpaBatchRepositoryImpl");

    @Resource(lookup = "java:comp/DefaultManagedExecutorService")
    private ManagedExecutorService executor;

    @Override
    public void batch(EntityManager em, Consumer<EntityManager>... operations) {

        try {
            em.joinTransaction();
            Arrays.stream(operations).forEach(operation -> operation.accept(em));
            logger.info("commit transaction");

        } catch (Exception e) {
            logger.info("rollback transaction");
            logger.info(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void parallelBatch(Callable<Void>... operations) {

        try {
            executor.invokeAll(Arrays.asList(operations));

        } catch (Exception e) {
            logger.info("rollback transaction");
            logger.info(e.getMessage());
            throw new RuntimeException(e);

        } finally {
            logger.info("commit transaction");
        }
    }
}
