package com.atlan.evently.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
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
        assertEquals("com.atlan.evently.model", emf.getPackagesToScan()[0]);
        assertTrue(emf.getJpaVendorAdapter() instanceof HibernateJpaVendorAdapter);
    }

    @Test
    void testTransactionManagerCreation() {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        PlatformTransactionManager txManager = databaseConfig.transactionManager(emf);
        assertNotNull(txManager);
        assertTrue(txManager instanceof JpaTransactionManager);
    }
}