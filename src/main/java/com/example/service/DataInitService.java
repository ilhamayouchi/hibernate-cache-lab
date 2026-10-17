package com.example.service;

import com.example.model.Auteur;
import com.example.model.Livre;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class DataInitService {

    private final EntityManagerFactory emf;

    public DataInitService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void initData() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            for (int i = 1; i <= 10; i++) {
                Auteur auteur = new Auteur();
                auteur.setNom("Auteur " + i);
                for (int j = 1; j <= 5; j++) {
                    Livre livre = new Livre();
                    livre.setTitre("Livre " + j + " de l'auteur " + i);
                    auteur.ajouterLivre(livre);
                }
                em.persist(auteur);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }
}