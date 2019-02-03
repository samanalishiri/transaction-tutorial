package com.saman.tutorial.transaction;

import javax.persistence.EntityManager;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public interface BatchProcessRepository {

    void batch(EntityManager em, Consumer<EntityManager>... operations);

    void parallelBatch(Callable<Void>... operations);
}
