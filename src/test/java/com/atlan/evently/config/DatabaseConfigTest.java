package com.atlan.evently.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseConfigTest {

    @Autowired
    private DatabaseConfig databaseConfig;

    @Autowired
    private DataSource dataSource;

    @Test
    void testEntityManagerFactoryBeanCreation() {
        LocalContainerEntityManagerFactoryBean emf = databaseConfig.entityManagerFactory(dataSource);
        assertNotNull(emf);
        assertNotNull(emf.getDataSource());
        assertTrue(emf.getJpaVendorAdapter() instanceof HibernateJpaVendorAdapter);
    }

    @Test
    void testTransactionManagerCreation() {
        LocalContainerEntityManagerFactoryBean emf = databaseConfig.entityManagerFactory(dataSource);
        // Initialize the EMF to avoid null object
        emf.afterPropertiesSet();
        
        PlatformTransactionManager txManager = databaseConfig.transactionManager(emf);
        assertNotNull(txManager);
        assertTrue(txManager instanceof JpaTransactionManager);
    }
}