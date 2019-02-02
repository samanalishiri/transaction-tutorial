package com.saman.tutorial.transaction;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.function.Consumer;

@Stateless
public class JpaBatchRepositoryImpl implements JpaBatchRepository {

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Override
    public void batch(EntityManager em, Consumer<EntityManager>... operations) {
        em.joinTransaction();
        Arrays.stream(operations).forEach(operation -> operation.accept(em));
    }
}
