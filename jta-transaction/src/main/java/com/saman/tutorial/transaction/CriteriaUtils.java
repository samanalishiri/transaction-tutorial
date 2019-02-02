package com.saman.tutorial.transaction;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

public final class CriteriaUtils {

    private CriteriaUtils() {
    }

    public static <T> CriteriaQuery<T> createCriteriaQuery(EntityManager em, Class<T> clazz) {
        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        return criteriaBuilder.createQuery(clazz);
    }
}