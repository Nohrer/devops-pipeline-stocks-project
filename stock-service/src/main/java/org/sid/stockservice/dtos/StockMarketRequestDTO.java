package org.sid.stockservice.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StockMarketRequestDTO {
    
    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate date;
    
    @NotNull(message = "Open value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Open value must be positive")
    @Digits(integer = 10, fraction = 2, message = "Open value must have at most 10 integer digits and 2 decimal places")
    private BigDecimal openValue;
    
    @NotNull(message = "High value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "High value must be positive")
    @Digits(integer = 10, fraction = 2, message = "High value must have at most 10 integer digits and 2 decimal places")
    private BigDecimal highValue;
    
    @NotNull(message = "Low value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Low value must be positive")
    @Digits(integer = 10, fraction = 2, message = "Low value must have at most 10 integer digits and 2 decimal places")
    private BigDecimal lowValue;
    
    @NotNull(message = "Close value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Close value must be positive")
    @Digits(integer = 10, fraction = 2, message = "Close value must have at most 10 integer digits and 2 decimal places")
    private BigDecimal closeValue;
    
    @NotNull(message = "Volume is required")
    @Min(value = 0, message = "Volume cannot be negative")
    @Max(value = 999999999999L, message = "Volume is too large")
    private Long volume;
    
    @NotNull(message = "Company ID is required")
    @Min(value = 1, message = "Company ID must be positive")
    @Max(value = 999999999L, message = "Company ID is too large")
    private Long companyId;
}
