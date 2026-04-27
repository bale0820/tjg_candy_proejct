package com.tjg_project.candy.domain.product.repository;

import com.tjg_project.candy.domain.product.entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    @Query(value = """
            select r.id, r.content, r.date, r.images, r.is_best, r.likes, r.ppk, r.product_name, r.tags, r.title, r.upk, u.name from reviews r, users u where r.upk = u.id
            """, nativeQuery = true)
    List<ProductReview> findAllReviewWithUserName();

    List<ProductReview> findByPpk(Long ppk);
}
