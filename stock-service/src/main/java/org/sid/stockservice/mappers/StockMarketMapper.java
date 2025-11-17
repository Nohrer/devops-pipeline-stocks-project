package org.sid.stockservice.mappers;

import org.sid.stockservice.dtos.StockMarketRequestDTO;
import org.sid.stockservice.dtos.StockMarketResponseDTO;
import org.sid.stockservice.entities.StockMarket;
import org.springframework.stereotype.Component;

@Component
public class StockMarketMapper {
    
    public StockMarket toEntity(StockMarketRequestDTO dto) {
        if (dto == null) return null;
        
        return StockMarket.builder()
                .date(dto.getDate())
                .openValue(dto.getOpenValue())
                .highValue(dto.getHighValue())
                .lowValue(dto.getLowValue())
                .closeValue(dto.getCloseValue())
                .volume(dto.getVolume())
                .companyId(dto.getCompanyId())
                .build();
    }
    
    public StockMarketResponseDTO toDTO(StockMarket entity) {
        if (entity == null) return null;
        
        return StockMarketResponseDTO.builder()
                .id(entity.getId())
                .date(entity.getDate())
                .openValue(entity.getOpenValue())
                .highValue(entity.getHighValue())
                .lowValue(entity.getLowValue())
                .closeValue(entity.getCloseValue())
                .volume(entity.getVolume())
                .companyId(entity.getCompanyId())
                .build();
    }
    
    public void updateEntityFromDTO(StockMarketRequestDTO dto, StockMarket entity) {
        if (dto == null || entity == null) return;
        
        entity.setDate(dto.getDate());
        entity.setOpenValue(dto.getOpenValue());
        entity.setHighValue(dto.getHighValue());
        entity.setLowValue(dto.getLowValue());
        entity.setCloseValue(dto.getCloseValue());
        entity.setVolume(dto.getVolume());
        entity.setCompanyId(dto.getCompanyId());
    }
}
