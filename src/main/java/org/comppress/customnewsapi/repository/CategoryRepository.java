package org.comppress.customnewsapi.repository;

import org.comppress.customnewsapi.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category,Long>, JpaSpecificationExecutor<Category> {
    List<Category> findByName(String name);
    Category findByNameAndLang(String name, String lang);
    Page<Category> findByLang(String lang, Pageable pageable);
    List<Category> findByLang(String lang);
    @Query(value = "SELECT * FROM category c WHERE c.id IN (:ids) ORDER BY FIELD(id, :ids)", nativeQuery = true)     // 2. Spring JPA In cause using @Query
    List<Category> findByCategoryIds(@Param("ids")List<Long> ids);
}