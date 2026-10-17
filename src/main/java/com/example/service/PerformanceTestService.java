package com.example.service;

import com.example.model.Auteur;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import java.util.List;

public class PerformanceTestService {

    private final EntityManagerFactory emf;

    public PerformanceTestService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    // N+1 : 1 requête pour les auteurs + 1 par auteur pour ses livres
    public void testPerformanceSansCache() {
        EntityManager em = emf.createEntityManager();
        long start = System.currentTimeMillis();

        List<Auteur> auteurs = em
                .createQuery("SELECT a FROM Auteur a", Auteur.class)
                .getResultList();
        for (Auteur a : auteurs) {
            a.getLivres().size();
        }

        System.out.println("Durée sans optimisation (N+1) : "
                + (System.currentTimeMillis() - start) + " ms");
        em.close();
    }

    public void testPerformanceAvecJoinFetch() {
        EntityManager em = emf.createEntityManager();
        long start = System.currentTimeMillis();

        List<Auteur> auteurs = em
                .createQuery("SELECT DISTINCT a FROM Auteur a JOIN FETCH a.livres", Auteur.class)
                .getResultList();
        for (Auteur a : auteurs) {
            a.getLivres().size();
        }

        System.out.println("Durée avec JOIN FETCH : "
                + (System.currentTimeMillis() - start) + " ms");
        em.close();
    }

    public void testPerformanceAvecEntityGraph() {
        EntityManager em = emf.createEntityManager();
        long start = System.currentTimeMillis();

        EntityGraph<?> graph = em.getEntityGraph("Auteur.livres");
        TypedQuery<Auteur> query = em.createQuery("SELECT a FROM Auteur a", Auteur.class);
        query.setHint("javax.persistence.fetchgraph", graph);
        List<Auteur> auteurs = query.getResultList();
        for (Auteur a : auteurs) {
            a.getLivres().size();
        }

        System.out.println("Durée avec EntityGraph : "
                + (System.currentTimeMillis() - start) + " ms");
        em.close();
    }

    // Deux lectures dans deux sessions différentes : la 2e doit venir du cache L2
    public void testPerformanceAvecCache() {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();

        for (int lecture = 1; lecture <= 2; lecture++) {
            EntityManager em = emf.createEntityManager();
            long start = System.currentTimeMillis();

            for (long id = 1; id <= 10; id++) {
                Auteur a = em.find(Auteur.class, id);
                a.getLivres().size();
            }

            System.out.println("Durée avec cache L2 (lecture " + lecture + ") : "
                    + (System.currentTimeMillis() - start) + " ms");
            em.close();
        }

        System.out.println("L2 hits : " + stats.getSecondLevelCacheHitCount()
                + " | misses : " + stats.getSecondLevelCacheMissCount());
        System.out.println(java.util.Arrays.toString(stats.getSecondLevelCacheRegionNames()));
    }
}