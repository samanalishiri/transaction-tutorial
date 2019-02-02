package com.saman.tutorial.transaction;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import java.util.List;

import static com.saman.tutorial.transaction.CriteriaUtils.createCriteriaQuery;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
@Stateless
public class JpaRepositoryImpl implements Repository {

    @PersistenceContext(unitName = "transactiontutorial")
    private EntityManager em;

    public JpaRepositoryImpl() {
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
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
    public void update(DataEntity e) {
        DataEntity entity = em.find(DataEntity.class, e.getId());
        entity.setCode(e.getCode());
        entity.setName(e.getName());
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(Integer id) {
        em.remove(em.find(DataEntity.class, id));
    }

    @Override
    public Long countAll() {
        CriteriaBuilder qb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = qb.createQuery(Long.class);
        cq.select(qb.count(cq.from(DataEntity.class)));
        return em.createQuery(cq).getSingleResult();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void truncate() {
        List<DataEntity> entities = findAll();
        entities.stream().forEach(em::remove);
    }
}
