package com.alten.shop.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.alten.shop.product.Product.InventoryStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByCode(String code);

    Optional<Product> findByCode(String code);

    Page<Product> findByCategoryIgnoreCase(String category, Pageable pageable);

    Page<Product> findByInventoryStatus(InventoryStatus status, Pageable pageable);


  Page<Product> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name, String code, String description, Pageable pageable
    );
}

