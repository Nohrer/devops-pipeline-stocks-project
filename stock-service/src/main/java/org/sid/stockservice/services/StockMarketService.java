package org.sid.stockservice.services;

import lombok.RequiredArgsConstructor;
import org.sid.stockservice.dtos.StockMarketRequestDTO;
import org.sid.stockservice.dtos.StockMarketResponseDTO;
import org.sid.stockservice.entities.StockMarket;
import org.sid.stockservice.mappers.StockMarketMapper;
import org.sid.stockservice.repositories.StockMarketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class StockMarketService {
    
    private final StockMarketRepository stockMarketRepository;
    private final StockMarketMapper stockMarketMapper;
    private final Random random = new Random();
    
    public StockMarketResponseDTO addStock(StockMarketRequestDTO requestDTO) {
        StockMarket stockMarket = stockMarketMapper.toEntity(requestDTO);
        
        // If companyId is not provided, generate a random one
        if (stockMarket.getCompanyId() == null) {
            stockMarket.setCompanyId(random.nextLong(1, 10000));
        }
        
        StockMarket savedStock = stockMarketRepository.save(stockMarket);
        
        return stockMarketMapper.toDTO(savedStock);
    }
    
    public StockMarketResponseDTO updateStock(Long id, StockMarketRequestDTO requestDTO) {
        StockMarket stockMarket = stockMarketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found with id: " + id));
        
        stockMarketMapper.updateEntityFromDTO(requestDTO, stockMarket);
        
        StockMarket updatedStock = stockMarketRepository.save(stockMarket);
        
        return stockMarketMapper.toDTO(updatedStock);
    }
    
    public void deleteStock(Long id) {
        if (!stockMarketRepository.existsById(id)) {
            throw new RuntimeException("Stock not found with id: " + id);
        }
        stockMarketRepository.deleteById(id);
    }
    
    public StockMarketResponseDTO getStockById(Long id) {
        StockMarket stockMarket = stockMarketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found with id: " + id));
        return stockMarketMapper.toDTO(stockMarket);
    }
    
    public List<StockMarketResponseDTO> getAllStocks() {
        return stockMarketRepository.findAll().stream()
                .map(stockMarketMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    public List<StockMarketResponseDTO> getStocksByCompanyId(Long companyId) {
        return stockMarketRepository.findByCompanyId(companyId).stream()
                .map(stockMarketMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Updates the company current price with a random calculation
     * This is just a placeholder method that generates and saves a random price
     */
    public BigDecimal updateCompanyCurrentPrice(Long companyId) {
        // Generate a random price between 50.00 and 500.00
        double randomPrice = 50.0 + (random.nextDouble() * 450.0);
        BigDecimal calculatedPrice = BigDecimal.valueOf(randomPrice)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        
        // In this simplified version, we just return the random price
        // You could save it somewhere or use it for other calculations
        System.out.println("Random calculated price for company " + companyId + ": $" + calculatedPrice);
        
        return calculatedPrice;
    }
}
