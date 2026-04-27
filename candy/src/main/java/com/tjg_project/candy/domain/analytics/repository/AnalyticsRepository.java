package com.tjg_project.candy.domain.analytics.repository;

import com.tjg_project.candy.domain.analytics.dto.ConversionRateDto;
import com.tjg_project.candy.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnalyticsRepository extends JpaRepository<Product, Long> {

    @Query("""
        SELECT new com.tjg_project.candy.domain.analytics.dto.ConversionRateDto(
            p.id,
            p.productName,
            (SELECT SUM(v.qty) FROM UserViewLog v WHERE v.ppk = p.id),
            (SELECT COALESCE(SUM(od.qty),0)
               FROM OrderDetail od
               WHERE od.ppk = p.id)
        )
        FROM Product p
    """)
    List<ConversionRateDto> getConversionRates();
}
