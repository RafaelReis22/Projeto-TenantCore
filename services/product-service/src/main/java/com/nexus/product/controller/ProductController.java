package com.nexus.product.controller;

import com.nexus.product.dto.ProductDTO;
import com.nexus.product.model.Product;
import com.nexus.product.service.ProductService;
import com.nexus.product.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produtos", description = "Gestão de produtos com isolamento Multi-tenant")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Listar produtos", description = "Retorna apenas os produtos do seu Tenant (Filtrado por RLS)")
    public List<Product> getAll() {
        return productService.listAll();
    }

    @PostMapping
    @Operation(summary = "Criar produto", description = "Cria um novo produto injetando o tenant_id automaticamente")
    public Product create(@RequestBody ProductDTO dto) {
        return productService.create(dto);
    }
}
