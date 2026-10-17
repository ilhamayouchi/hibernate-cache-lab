package com.example;

import com.example.service.DataInitService;
import com.example.service.PerformanceTestService;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Main {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate-cache-pu");

        new DataInitService(emf).initData();
        PerformanceTestService perf = new PerformanceTestService(emf);

        System.out.println("--- Test sans optimisation (N+1) ---");
        perf.testPerformanceSansCache();

        System.out.println("--- Test avec JOIN FETCH ---");
        perf.testPerformanceAvecJoinFetch();

        System.out.println("--- Test avec EntityGraph ---");
        perf.testPerformanceAvecEntityGraph();

        System.out.println("--- Test avec cache de second niveau ---");
        perf.testPerformanceAvecCache();

        emf.close();
    }
}