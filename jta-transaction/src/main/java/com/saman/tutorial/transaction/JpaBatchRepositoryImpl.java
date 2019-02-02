package com.saman.tutorial.transaction;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Stateless
@Transactional(Transactional.TxType.REQUIRES_NEW)
public class JpaBatchRepositoryImpl implements JpaBatchRepository {

    private final Logger logger = Logger.getLogger("JpaBatchRepositoryImpl");

    @Override
    public void batch(EntityManager em, Consumer<EntityManager>... operations) throws Exception {

        try {
            em.joinTransaction();
            Arrays.stream(operations).forEach(operation -> operation.accept(em));
            logger.info("commit transaction");

        } catch (Exception e) {
            logger.info("rollback transaction");
            throw e;
        }
    }
}
