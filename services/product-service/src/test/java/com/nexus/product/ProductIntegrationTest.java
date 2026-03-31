package com.nexus.product;

import com.nexus.product.model.Product;
import com.nexus.product.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

@SpringBootTest
@Testcontainers
class ProductIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("multi_tenant_db")
            .withUsername("admin")
            .withPassword("password")
            .withInitScript("01-init-rls.sql"); // Usa o seu script de RLS real!

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldEnforceTenantIsolation() {
        // 1. Simula login 
        jdbcTemplate.execute("SET app.current_tenant = 'TENANT_A'");
        
        List<Product> productsA = repository.findAll();
        
        // Deve ver apenas o produto 
        Assertions.assertEquals(1, productsA.size());
        Assertions.assertEquals("Iphone 15 (Tenant A)", productsA.get(0).getName());

        // 2. Simula login no banco
        jdbcTemplate.execute("SET app.current_tenant = 'TENANT_B'");
        
        List<Product> productsB = repository.findAll();
        
        // Deve ver apenas o produto 
        Assertions.assertEquals(1, productsB.size());
        Assertions.assertEquals("Macbook Pro (Tenant B)", productsB.get(0).getName());
    }
}
