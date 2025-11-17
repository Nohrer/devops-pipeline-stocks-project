package org.sid.stockservice.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StockMarketResponseDTO {
    private Long id;
    private LocalDate date;
    private BigDecimal openValue;
    private BigDecimal highValue;
    private BigDecimal lowValue;
    private BigDecimal closeValue;
    private Long volume;
    private Long companyId;
}
