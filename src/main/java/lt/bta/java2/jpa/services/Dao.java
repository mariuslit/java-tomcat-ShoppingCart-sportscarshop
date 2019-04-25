package lt.bta.java2.jpa.services;

import lt.bta.java2.jpa.helpers.EntityManagerHelper;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;

/**
 * DAO klase atsakinga uz rysi su DB ir CRUD operacijas
 */
public class Dao<T> implements AutoCloseable {

    final private EntityManager em;
    final private Class<T> clazz;

    // constructor
    public Dao(Class<T> clazz) {
        this.em = EntityManagerHelper.getEntityManager();
        this.clazz = clazz;
    }

    // CRUD

    // CREATE
    public T create(T entity) {

        em.getTransaction().begin();
        em.persist(entity);
        em.getTransaction().commit();
        return entity;
    }

    // READ
    public T read(Object pk) {

        T entity = em.find(clazz, pk);
        return entity;
    }

    public T read(Object pk, String graph) {

        EntityGraph entityGraph = em.getEntityGraph(graph);
        return em.find(clazz, pk, Collections.singletonMap(
                EntityManagerHelper.FETCH_GRAPH, entityGraph));
    }

    // UPDATE
    public T update(T entity) {

        em.getTransaction().begin();
        em.merge(entity);
        em.getTransaction().commit();
        return entity;
    }

    // DELETE
    public void delete(T entity) {

        em.getTransaction().begin();
        em.remove(entity);
        em.getTransaction().commit();
    }

    // READ_LIST
    public List<T> list(int size, int skip) {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(clazz);
        Root<T> from = criteriaQuery.from(clazz);
        CriteriaQuery<T> select = criteriaQuery.select(from);
        TypedQuery<T> typedQuery = em.createQuery(select);
        typedQuery.setFirstResult(skip);
        typedQuery.setMaxResults(size);
        return typedQuery.getResultList();
    }

    // READ_LIST
    public List<T> findBy(String field, Object value) {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(clazz);
        Root<T> from = criteriaQuery.from(clazz);
        criteriaQuery.select(from);
        criteriaQuery.where(criteriaBuilder.equal(from.get(field), value));
        TypedQuery<T> typedQuery = em.createQuery(criteriaQuery);
        return typedQuery.getResultList();
    }

    // READ_LIST_ALL skirtas pagintion kurti [1][2][3]...
    public List<T> listAll() {

        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(clazz);
        Root<T> from = criteriaQuery.from(clazz);
        CriteriaQuery<T> select = criteriaQuery.select(from);
        TypedQuery<T> typedQuery = em.createQuery(select);
        return typedQuery.getResultList();
    }

    public void close() {
        em.close();
    }
}