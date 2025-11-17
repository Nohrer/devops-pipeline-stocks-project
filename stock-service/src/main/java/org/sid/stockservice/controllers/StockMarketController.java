package org.sid.stockservice.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.stockservice.dtos.StockMarketRequestDTO;
import org.sid.stockservice.dtos.StockMarketResponseDTO;
import org.sid.stockservice.services.StockMarketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
@Validated
@Slf4j
public class StockMarketController {
    
    private final StockMarketService stockMarketService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StockMarketResponseDTO> addStock(@Valid @RequestBody StockMarketRequestDTO requestDTO) {
        log.info("Adding new stock for company ID: {}", requestDTO.getCompanyId());
        StockMarketResponseDTO response = stockMarketService.addStock(requestDTO);
        log.info("Stock added successfully with ID: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StockMarketResponseDTO> updateStock(
            @PathVariable @Min(value = 1, message = "ID must be positive") Long id,
            @Valid @RequestBody StockMarketRequestDTO requestDTO) {
        log.info("Updating stock with ID: {}", id);
        StockMarketResponseDTO response = stockMarketService.updateStock(id, requestDTO);
        log.info("Stock updated successfully with ID: {}", id);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteStock(@PathVariable @Min(value = 1, message = "ID must be positive") Long id) {
        log.info("Deleting stock with ID: {}", id);
        stockMarketService.deleteStock(id);
        log.info("Stock deleted successfully with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public ResponseEntity<StockMarketResponseDTO> getStockById(
            @PathVariable @Min(value = 1, message = "ID must be positive") Long id) {
        log.debug("Fetching stock with ID: {}", id);
        StockMarketResponseDTO response = stockMarketService.getStockById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public ResponseEntity<List<StockMarketResponseDTO>> getAllStocks() {
        log.debug("Fetching all stocks");
        List<StockMarketResponseDTO> stocks = stockMarketService.getAllStocks();
        log.debug("Fetched {} stocks", stocks.size());
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public ResponseEntity<List<StockMarketResponseDTO>> getStocksByCompanyId(
            @PathVariable @Min(value = 1, message = "Company ID must be positive") Long companyId) {
        log.debug("Fetching stocks for company ID: {}", companyId);
        List<StockMarketResponseDTO> stocks = stockMarketService.getStocksByCompanyId(companyId);
        log.debug("Fetched {} stocks for company ID: {}", stocks.size(), companyId);
        return ResponseEntity.ok(stocks);
    }
    
    @PostMapping("/calculate-price/{companyId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BigDecimal> calculateRandomPrice(
            @PathVariable @Min(value = 1, message = "Company ID must be positive") Long companyId) {
        log.info("Calculating random price for company ID: {}", companyId);
        BigDecimal randomPrice = stockMarketService.updateCompanyCurrentPrice(companyId);
        log.info("Random price calculated for company ID {}: {}", companyId, randomPrice);
        return ResponseEntity.ok(randomPrice);
    }
}
