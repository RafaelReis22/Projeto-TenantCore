package com.nexus.product.service;

import com.nexus.product.context.TenantContext;
import com.nexus.product.dto.ProductDTO;
import com.nexus.product.model.Product;
import com.nexus.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }


    public List<Product> listAll() {
        return repository.findAll();
    }

  
    @Transactional
    public Product create(ProductDTO dto) {
        validate(dto);

        Product product = new Product();
        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        
        product.setTenantId(TenantContext.getTenantId());

        return repository.save(product);
    }

    private void validate(ProductDTO dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }
        if (dto.getPrice() == null || dto.getPrice() < 0) {
            throw new IllegalArgumentException("O preço deve ser um valor positivo.");
        }
    }
}
