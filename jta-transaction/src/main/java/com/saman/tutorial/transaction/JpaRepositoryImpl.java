package com.saman.tutorial.transaction;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.Transactional;
import java.util.List;

import static com.saman.tutorial.transaction.CriteriaUtils.createCriteriaQuery;

/**
 * @author Saman Alishiri, samanalishiri@gmail.com
 */
@Stateless
@Transactional
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class JpaRepositoryImpl implements Repository {

    @PersistenceContext(unitName = "transactiontutorial")
    private EntityManager em;

    public JpaRepositoryImpl() {
    }

    @Override
    public Integer save(DataEntity e) {
        em.joinTransaction();
        em.persist(e);
        return e.getId();
    }

    public List<DataEntity> findAll() {
        CriteriaQuery<DataEntity> cq = createCriteriaQuery(em, DataEntity.class);
        return em.createQuery(cq.select(cq.from(DataEntity.class))).getResultList();
    }

    @Override
    public DataEntity findById(Integer id) {
        return em.find(DataEntity.class, id);
    }

    public void update(DataEntity e) {
        em.joinTransaction();
        DataEntity entity = em.find(DataEntity.class, e.getId());
        entity.setCode(e.getCode());
        entity.setName(e.getName());
    }

    @Override
    public synchronized Void synchronizedUpdate(DataEntity e) throws Exception {
        this.update(e);
        em.flush();
        return Void.TYPE.newInstance();
    }

    @Override
    public void delete(Integer id) {
        em.joinTransaction();
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
    public void truncate() {
        em.joinTransaction();
        List<DataEntity> entities = findAll();
        entities.stream().forEach(em::remove);
    }
}
