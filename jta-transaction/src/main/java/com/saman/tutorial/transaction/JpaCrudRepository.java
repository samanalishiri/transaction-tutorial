package com.saman.tutorial.transaction;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

import static com.saman.tutorial.transaction.CriteriaUtils.createCriteriaQuery;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
@Stateless
public class JpaCrudRepository implements CrudRepository {

    @PersistenceContext(unitName = "transactiontutorial")
    private EntityManager em;

    public JpaCrudRepository() {
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public Integer save(DataEntity e) {
        em.persist(e);
        return e.getId();
    }

    public List<DataEntity> findAll() {
        CriteriaQuery<DataEntity> criteriaQuery = createCriteriaQuery(em, DataEntity.class);
        return em.createQuery(criteriaQuery.select(criteriaQuery.from(DataEntity.class))).getResultList();
    }

    @Override
    public DataEntity findById(Integer id) {
        return em.find(DataEntity.class, id);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void update(DataEntity model) {
        em.merge(model);
        em.flush();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Override
    public void delete(Integer id) {
        em.remove(em.find(DataEntity.class, id));
    }

}