
package com.alten.shop.product.mapper;

import com.alten.shop.product.Product;
import com.alten.shop.product.dto.ProductRequest;
import com.alten.shop.product.dto.ProductResponse;
import org.mapstruct.*;


/**
 * Mapper MapStruct entre entités et DTOs.
 * Note : le code métier est généré dans le service (pas ici).
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /**
     * Transforme un ProductRequest en Product.
     * Les champs techniques/audit/derivés sont ignorés.
     */
    /**
     * Transforme une requête en entité {@link Product}.
     * Les champs d'audit et l'identifiant sont ignorés (gérés par JPA).
     * Le code est généré côté service.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true) // généré côté service
    @Mapping(target = "version", ignore = true) // la version est gérée par JPA
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "inventoryStatus", ignore = true)     // dérivé de quantity
    Product toEntity(ProductRequest request);

    /**
     * Met à jour une entité existante à partir d'un {@link ProductRequest}.
     * Les valeurs nulles sont ignorées pour éviter d’écraser accidentellement des champs.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "version", ignore = true) //on ne doit pas écraser la version existante
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "lastModifiedDate", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "inventoryStatus", ignore = true)
    void updateEntity(@MappingTarget Product entity, ProductRequest request);

    /**
     * Transforme une entité en réponse API.
     */
    @Mapping(target = "createdAt", source = "createdDate")
    @Mapping(target = "updatedAt", source = "lastModifiedDate")
    @Mapping(target = "inventoryStatus", expression = "java(entity.getInventoryStatus() != null ? entity.getInventoryStatus().name() : null)")
    ProductResponse toResponse(Product entity);
}
