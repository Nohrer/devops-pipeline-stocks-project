package org.sid.stockservice.controllers;

import lombok.RequiredArgsConstructor;
import org.sid.stockservice.dtos.StockMarketRequestDTO;
import org.sid.stockservice.dtos.StockMarketResponseDTO;
import org.sid.stockservice.services.StockMarketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/stocks")
@RequiredArgsConstructor
public class StockMarketController {
    
    private final StockMarketService stockMarketService;
    
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StockMarketResponseDTO> addStock(@RequestBody StockMarketRequestDTO requestDTO) {
        StockMarketResponseDTO response = stockMarketService.addStock(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<StockMarketResponseDTO> updateStock(
            @PathVariable Long id,
            @RequestBody StockMarketRequestDTO requestDTO) {
        StockMarketResponseDTO response = stockMarketService.updateStock(id, requestDTO);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        stockMarketService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public ResponseEntity<StockMarketResponseDTO> getStockById(@PathVariable Long id) {
        StockMarketResponseDTO response = stockMarketService.getStockById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public ResponseEntity<List<StockMarketResponseDTO>> getAllStocks() {
        List<StockMarketResponseDTO> stocks = stockMarketService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }
    
    @GetMapping("/company/{companyId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public ResponseEntity<List<StockMarketResponseDTO>> getStocksByCompanyId(@PathVariable Long companyId) {
        List<StockMarketResponseDTO> stocks = stockMarketService.getStocksByCompanyId(companyId);
        return ResponseEntity.ok(stocks);
    }
    
    @PostMapping("/calculate-price/{companyId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<BigDecimal> calculateRandomPrice(@PathVariable Long companyId) {
        BigDecimal randomPrice = stockMarketService.updateCompanyCurrentPrice(companyId);
        return ResponseEntity.ok(randomPrice);
    }
}
