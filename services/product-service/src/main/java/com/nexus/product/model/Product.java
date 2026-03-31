package com.nexus.product.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.envers.Audited;

@Entity
@Table(name = "products")
@Data
@Audited // Especialista: Gera histórico automático de todas as mudanças
@SQLDelete(sql = "UPDATE products SET deleted = true WHERE id=?") 
@Where(clause = "deleted = false")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private Double price;
    
    @Column(name = "tenant_id")
    private String tenantId;

    @Column(nullable = false)
    private boolean deleted = Boolean.FALSE; 
}
