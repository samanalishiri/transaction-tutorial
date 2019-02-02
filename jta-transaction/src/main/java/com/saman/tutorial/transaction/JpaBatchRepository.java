package com.saman.tutorial.transaction;

import javax.persistence.EntityManager;
import java.util.function.Consumer;

public interface JpaBatchRepository {
    void batch(EntityManager em, Consumer<EntityManager>... operations);
}
