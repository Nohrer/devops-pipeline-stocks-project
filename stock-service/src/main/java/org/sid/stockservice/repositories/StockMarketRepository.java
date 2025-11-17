package org.sid.stockservice.repositories;

import org.sid.stockservice.entities.StockMarket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockMarketRepository extends JpaRepository<StockMarket, Long> {
    
    List<StockMarket> findByCompanyId(Long companyId);
    
    @Query("SELECT s FROM StockMarket s WHERE s.companyId = :companyId ORDER BY s.date DESC")
    Optional<StockMarket> findLatestByCompanyId(@Param("companyId") Long companyId);
}
